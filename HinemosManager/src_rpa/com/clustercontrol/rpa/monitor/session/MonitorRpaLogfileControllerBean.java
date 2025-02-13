/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.monitor.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.MonitorOnAgentUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rpa.monitor.factory.RunMonitorRpaLogfileString;
import com.clustercontrol.util.HinemosTime;

/**
 * RPAログファイル監視の管理を行う Session Bean <BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 * @see MonitorLogfileControllerBean
 * 
 */
public class MonitorRpaLogfileControllerBean {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( MonitorRpaLogfileControllerBean.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(MonitorRpaLogfileControllerBean.class.getName());
		
		try {
			_lock.writeLock();
			
			ArrayList<MonitorInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				refreshCache();
			}
		} finally {
			_lock.writeUnlock();
			m_log.info("Static Initialization [Thread : " + Thread.currentThread() + ", User : " + (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID) + "]");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static ArrayList<MonitorInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_RPA_LOGFILE);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_RPA_LOGFILE + " : " + cache);
		return cache == null ? null : (ArrayList<MonitorInfo>)cache;
	}
	
	private static void storeCache(ArrayList<MonitorInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_RPA_LOGFILE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_RPA_LOGFILE, newCache);
	}
	
	/**
	 * RPAログファイル監視一覧リストを返します。
	 * 
	 * 
	 * @return MonitorInfoの2次元配列
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getRpaLogfileList() throws MonitorNotFound, InvalidRole, HinemosUnknown{

		JpaTransactionManager jtm = null;
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectMonitor().getMonitorListObjectPrivilegeModeNONE(HinemosModuleConstant.MONITOR_RPA_LOGFILE);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getRpaLogfileList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	public static void refreshCache () {
		m_log.info("refreshCache()");
		
		long startTime = HinemosTime.currentTimeMillis();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_lock.writeLock();
			
			em.clear();
			ArrayList<MonitorInfo> rpaLogfileCache = new MonitorRpaLogfileControllerBean().getRpaLogfileList();
			storeCache(rpaLogfileCache);
			
			m_log.info("refresh rpaLogfileCache " + (HinemosTime.currentTimeMillis() - startTime) +
					"ms. size=" + rpaLogfileCache.size());
		} catch (Exception e) {
			m_log.warn("failed refreshing cache.", e);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * 
	 * <注意！> このメソッドはAgentユーザ以外で呼び出さないこと！
	 * <注意！> キャッシュの都合上、Agentユーザ以外から呼び出すと、正常に動作しません。
	 * 
	 * facilityIDごとのRPAログファイル監視一覧リストを返します。
	 * withCalendarをtrueにするとMonitorInfoのcalendarDTOに値が入ります。
	 * 
	 * 
	 * @return MonitorInfoの2次元配列
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * 
	 */
	public ArrayList<MonitorInfo> getRpaLogfileListForFacilityId(String facilityId, boolean withCalendar)
			throws MonitorNotFound, HinemosUnknown {
		ArrayList<MonitorInfo> ret = new ArrayList<MonitorInfo>();
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			ArrayList<MonitorInfo> monitorList = getCache();
			
			for (MonitorInfo monitorInfo : monitorList) {
				String scope = monitorInfo.getFacilityId();
				if (new RepositoryControllerBean().containsFacilityIdWithoutList(scope, facilityId, monitorInfo.getOwnerRoleId())) {
					if (withCalendar) {
						String calendarId = monitorInfo.getCalendarId();
						try {
							CalendarInfo calendar = new CalendarControllerBean().getCalendarFull(calendarId);
							monitorInfo.setCalendar(calendar);
						} catch (Exception e) {
							m_log.warn("getRpaLogfileListForFacilityId() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
							throw new HinemosUnknown(e.getMessage(), e);
						}
					}
					ret.add(monitorInfo);
				}
			}
			
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getRpaLogfileListForFacilityId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}
	
	/**
	 * RPAログファイル監視結果を通知する.
	 * エージェントではログファイル監視と同一の情報が送信されるため、引数はログファイル監視と同様
	 * @see MonitorLogfileControllerBean#run
	 * 
	 * @param results ログファイル監視結果のリスト
	 * @throws HinemosUnknown 
	 */
	public void run(String facilityId, List<LogfileResultDTO> results) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<OutputBasicInfo> notifyInfoList = new ArrayList<>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			if (results != null) {
				for (LogfileResultDTO result : results) {
					if (! MonitorOnAgentUtil.checkFacilityId(facilityId, result.runInstructionInfo, result.monitorInfo)) {
						m_log.debug("skip to run because facilityId is not contained.");
						continue;
					}
					RunMonitorRpaLogfileString runMonitorRpaLogfileString = new RunMonitorRpaLogfileString();
					notifyInfoList.addAll(runMonitorRpaLogfileString.run(facilityId, result));
				}
			}

			// 通知設定
			jtm.addCallback(new NotifyCallback(notifyInfoList));

			jtm.commit();
		} catch (HinemosUnknown e) {
			m_log.warn("failed storeing result.", e);
			jtm.rollback();
			
			throw e;
		} finally {
			if (jtm != null)
				jtm.close(this.getClass().getName());
		}
	}
}
