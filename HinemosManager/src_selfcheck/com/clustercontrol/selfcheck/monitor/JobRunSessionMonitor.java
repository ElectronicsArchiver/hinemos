/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryDivergence;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * ジョブセッション数を確認する処理の実装クラス
 */
public class JobRunSessionMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( JobRunSessionMonitor.class );

	public long threshold;

	public final String monitorId = "SYS_JOB_SESSION";
	public final String subKey = "";
	public final String application = "SELFCHECK (Running Job Session)";

	public JobRunSessionMonitor() {
	}

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		return "monitoring running job session (thresdhold = " + threshold + ")";
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * ジョブセッション数の確認
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if (!HinemosPropertyCommon.selfcheck_monitoring_job_runningsession.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}
		/** ローカル変数 */
		JpaTransactionManager tm = null;
		long count = -1;
		boolean warn = true;

		threshold = HinemosPropertyCommon.selfcheck_monitoring_job_runningsession_threshold.getNumericValue();

		/** メイン処理 */
		if (m_log.isDebugEnabled())
			m_log.debug("monitoring job session running. (threshold = " + threshold + ")");

		try {
			// データソースのオブジェクトをJNDI経由で取得し、取得したコネクションが正しく動作するかを確認する
			tm = new JpaTransactionManager();
			tm.begin();

			// 判定対象値を取得する
			count = getJobRunSessionCount();

			if (count == -1) {
				m_log.info("skipped monitoring job session running. (threshold=" + threshold);
				return;
			} else if (count <= threshold) {
				m_log.debug("job session running size is low. (count = " + count + ", threshold = " + threshold + ")");
				warn = false;
			}
		} catch (Exception e) {
			m_log.warn("monitoring job run session failure. (threshold=" + threshold, e);
		} finally {
			if (tm != null) {
				tm.close();
			}
		}

		if (warn) {
			m_log.info("job session running size is too high. (count= "  + count + ", threshold = " + threshold + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		String[] msgAttr1 = { Long.toString(count), Long.toString(threshold) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_007, msgAttr1,
				"job run session count is too large (" +
						count +
						" > threshold " +
						threshold +
				").");

		return;
	}


	/**
	 * 実行中のジョブセッションレコード数を返すメソッド
	 * @param conn 問い合わせに利用するコネクション
	 * @return レコード数
	 */
	public static long getJobRunSessionCount() {
		// ローカル変数
		JpaTransactionManager tm = null;
		HinemosEntityManager em =null;
		
		//log.cc_job_session_jobから実行中を表すステータスのセッションのみを取得する
		List<Integer> unendStatusList = StatusConstant.getUnendList();
		List<Object> queryParams = new ArrayList<>();
		queryParams.add(CreateJobSession.TOP_JOBUNIT_ID);
		queryParams.add(CreateJobSession.TOP_JOB_ID);
		queryParams.addAll(unendStatusList);

		String query = String.format(
				"SELECT count(*) " + "FROM log.cc_job_session_job j " + "WHERE j.parent_jobunit_id = '%s' "
						+ "AND j.parent_job_id = '%s' AND j.status IN "
						+ "(" + String.join(",", Collections.nCopies(unendStatusList.size(), "%d")) + ")",
				queryParams.toArray());

		long count = -1;

		// メイン処理
		try {
			tm = new JpaTransactionManager();
			tm.begin();
			em = tm.getEntityManager();

			Long row = QueryDivergence.countResult(em.createNativeQuery(query).getSingleResult());
			if (row != null) {
				count = row;
			}

			tm.commit();
		} catch (Exception e) {
			m_log.warn("database query execution failure. (" + query + ")", e);
		} finally {
			if ( tm != null) {
				tm.close();
			}
		}

		return count;
	}

}
