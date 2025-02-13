/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer.LanguageTranslateSerializer;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * ジョブ連携送信ジョブに関する情報を保持するクラス
 *
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkRcvInfo implements Serializable, RequestDto {

	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 7216413607782857671L;

	@JsonIgnore
	private static Log m_log = LogFactory.getLog( JobLinkRcvInfo.class );

	/** スコープ処理 */
	@JsonIgnore
	private Integer processingMethod = 0;

	/** ファシリティID */
	@RestItemName(value = MessageConstant.SOURCE_SCOPE)
	@RestValidateString(notNull = true)
	private String facilityID;

	/** スコープ */
	@JsonSerialize(using=LanguageTranslateSerializer.class)
	private String scope;

	/** 終了値 - 「情報」 */
	@RestItemName(value = MessageConstant.END_VALUE_INFO)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorInfoEndValue;

	/** 終了値 - 「警告」 */
	@RestItemName(value = MessageConstant.END_VALUE_WARNING)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorWarnEndValue;

	/** 終了値 - 「危険」 */
	@RestItemName(value = MessageConstant.END_VALUE_CRITICAL)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorCriticalEndValue;

	/** 終了値 - 「不明」 */
	@RestItemName(value = MessageConstant.END_VALUE_UNKNOWN)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorUnknownEndValue;

	/** メッセージが得られなかった場合に終了する */
	private Boolean failureEndFlg;

	/** メッセージが得られなかった場合に終了する - タイムアウト */
	@RestItemName(value = MessageConstant.TIME_OUT)
	@RestValidateInteger(minVal = 0, maxVal = 32767)
	private Integer monitorWaitTime;

	/** メッセージが得られなかった場合に終了する - 終了値 */
	@RestItemName(value = MessageConstant.END_VALUE_JOBLINKRCV_FAILURE)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorWaitEndValue;

	/** ジョブ連携メッセージID */
	@RestItemName(value = MessageConstant.JOBLINK_MESSAGE_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 512)
	private String joblinkMessageId;

	/** メッセージ */
	@RestItemName(value=MessageConstant.MESSAGE)
	@RestValidateString(maxLen = 4096)
	private String message;

	/** 確認期間フラグ */
	private Boolean pastFlg;

	/** 確認期間（分） */
	@RestItemName(value = MessageConstant.TARGET_PERIOD)
	@RestValidateInteger(minVal = 0, maxVal = 32767)
	private Integer pastMin;

	/** 重要度（情報）有効/無効 */
	private Boolean infoValidFlg;

	/** 重要度（警告）有効/無効 */
	private Boolean warnValidFlg;

	/** 重要度（危険）有効/無効 */
	private Boolean criticalValidFlg;

	/** 重要度（不明）有効/無効 */
	private Boolean unknownValidFlg;

	/** アプリケーションフラグ */
	private Boolean applicationFlg;

	/** アプリケーション */
	@RestItemName(value=MessageConstant.APPLICATION)
	@RestValidateString(maxLen = 64)
	private String application;

	/** 監視詳細フラグ */
	private Boolean monitorDetailIdFlg;

	/** 監視詳細 */
	@RestItemName(value=MessageConstant.MONITOR_DETAIL_ID)
	@RestValidateString(maxLen = 1024)
	private String monitorDetailId;

	/** メッセージフラグ */
	@RestItemName(value=MessageConstant.MESSAGE)
	@RestValidateString(maxLen = 4096)
	private Boolean messageFlg;

	/** 拡張情報フラグ */
	private Boolean expFlg;

	/** 終了値 - 「常に」フラグ */
	private Boolean monitorAllEndValueFlg;

	/** 終了値 - 「常に」 */
	@RestItemName(value = MessageConstant.END_VALUE_JOBLINKRCV_ALL)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorAllEndValue;

	/** ジョブ連携メッセージの拡張情報設定 */
	@RestItemName(value = MessageConstant.EXTENDED_INFO)
	private ArrayList<JobLinkExpInfo> jobLinkExpList;

	/** メッセージの引継ぎ情報設定 */
	private ArrayList<JobLinkInheritInfo> jobLinkInheritList;

	/**
	 * ファシリティIDを返す。<BR>
	 * @return ファシリティID
	 */
	public String getFacilityID() {
		return facilityID;
	}

	/**
	 * ファシリティIDを設定する。<BR>
	 * @param facilityID ファシリティID
	 */
	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}
	/**
	 * スコープ処理を返す。<BR>
	 * @return スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public Integer getProcessingMethod() {
		return processingMethod;
	}

	/**
	 * スコープ処理を設定する。<BR>
	 * @param processingMethod スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public void setProcessingMethod(Integer processingMethod) {
		this.processingMethod = processingMethod;
	}

	/**
	 * スコープを返す。<BR>
	 * @return スコープ
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * スコープを設定する。<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 *  終了値 - 「情報」を返す。<BR>
	 * @return  終了値 - 「情報」
	 */
	public Integer getMonitorInfoEndValue() {
		return monitorInfoEndValue;
	}

	/**
	 *  終了値 - 「情報」を設定する。<BR>
	 * @param monitorInfoEndValue  終了値 - 「情報」
	 */
	public void setMonitorInfoEndValue(Integer monitorInfoEndValue) {
		this.monitorInfoEndValue = monitorInfoEndValue;
	}

	/**
	 *  終了値 - 「警告」を返す。<BR>
	 * @return 終了値 - 「警告」
	 */
	public Integer getMonitorWarnEndValue() {
		return monitorWarnEndValue;
	}

	/**
	 * 終了値 - 「警告」を設定する。<BR>
	 * @param monitorWarnEndValue 終了値 - 「警告」
	 */
	public void setMonitorWarnEndValue(Integer monitorWarnEndValue) {
		this.monitorWarnEndValue = monitorWarnEndValue;
	}

	/**
	 * 終了値 - 「危険」を返す。<BR>
	 * @return 終了値 - 「危険」
	 */
	public Integer getMonitorCriticalEndValue() {
		return monitorCriticalEndValue;
	}

	/**
	 * 終了値 - 「危険」を設定する。<BR>
	 * @param monitorCriticalEndValue 終了値 - 「危険」
	 */
	public void setMonitorCriticalEndValue(Integer monitorCriticalEndValue) {
		this.monitorCriticalEndValue = monitorCriticalEndValue;
	}

	/**
	 * 終了値 - 「不明」を返す。<BR>
	 * @return 終了値 - 「不明」
	 */
	public Integer getMonitorUnknownEndValue() {
		return monitorUnknownEndValue;
	}

	/**
	 * 終了値 - 「不明」を設定する。<BR>
	 * @param monitorUnknownEndValue 終了値 - 「不明」
	 */
	public void setMonitorUnknownEndValue(Integer monitorUnknownEndValue) {
		this.monitorUnknownEndValue = monitorUnknownEndValue;
	}

	/**
	 * メッセージが得られなかった場合に終了する<BR>
	 * @return true:メッセージが得られなかった場合に終了する
	 */
	public Boolean getFailureEndFlg() {
		return failureEndFlg;
	}

	/**
	 * メッセージが得られなかった場合に終了する<BR>
	 * @param failureEndFlg true:メッセージが得られなかった場合に終了する
	 */
	public void setFailureEndFlg(Boolean failureEndFlg) {
		this.failureEndFlg = failureEndFlg;
	}

	/**
	 * メッセージが得られなかった場合に終了する - タイムアウトを返す。<BR>
	 * @return メッセージが得られなかった場合に終了する - タイムアウト
	 */
	public Integer getMonitorWaitTime() {
		return monitorWaitTime;
	}

	/**
	 * メッセージが得られなかった場合に終了する - タイムアウトを設定する。<BR>
	 * @param monitorWaitTime メッセージが得られなかった場合に終了する - タイムアウト
	 */
	public void setMonitorWaitTime(Integer monitorWaitTime) {
		this.monitorWaitTime = monitorWaitTime;
	}

	/**
	 * メッセージが得られなかった場合に終了する - 終了値を返す。<BR>
	 * @return メッセージが得られなかった場合に終了する - 終了値
	 */
	public Integer getMonitorWaitEndValue() {
		return monitorWaitEndValue;
	}

	/**
	 * メッセージが得られなかった場合に終了する - 終了値を設定する。<BR>
	 * @param monitorWaitEndValue メッセージが得られなかった場合に終了する - 終了値
	 */
	public void setMonitorWaitEndValue(Integer monitorWaitEndValue) {
		this.monitorWaitEndValue = monitorWaitEndValue;
	}

	/**
	 * ジョブ連携メッセージIDを返す。<BR>
	 * @return ジョブ連携メッセージID
	 */
	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	/**
	 * ジョブ連携メッセージIDを設定する。<BR>
	 * @param joblinkMessageId ジョブ連携メッセージID
	 */
	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	/**
	 * メッセージを返す。<BR>
	 * @return メッセージ
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * メッセージを設定する。<BR>
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 確認期間フラグを返す。<BR>
	 * @return 確認期間フラグ
	 */
	public Boolean getPastFlg() {
		return pastFlg;
	}

	/**
	 * 確認期間フラグを設定する。<BR>
	 * @param pastFlg 確認期間フラグ
	 */
	public void setPastFlg(Boolean pastFlg) {
		this.pastFlg = pastFlg;
	}

	/**
	 * 確認期間（分）を返す。<BR>
	 * @return 確認期間（分）
	 */
	public Integer getPastMin() {
		return pastMin;
	}

	/**
	 * 確認期間（分）を設定する。<BR>
	 * @param pastMin 確認期間（分）
	 */
	public void setPastMin(Integer pastMin) {
		this.pastMin = pastMin;
	}

	/**
	 * 重要度（情報）有効/無効を返す。<BR>
	 * @return 重要度（情報）有効/無効
	 */
	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	/**
	 * 重要度（情報）有効/無効を設定する。<BR>
	 * @param infoValidFlg 重要度（情報）有効/無効
	 */
	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	/**
	 * 重要度（警告）有効/無効を返す。<BR>
	 * @return 重要度（警告）有効/無効
	 */
	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	/**
	 * 重要度（警告）有効/無効を設定する。<BR>
	 * @param warnValidFlg 重要度（警告）有効/無効
	 */
	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	/**
	 * 重要度（危険）有効/無効を返す。<BR>
	 * @return 重要度（危険）有効/無効
	 */
	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	/**
	 * 重要度（危険）有効/無効を設定する。<BR>
	 * @param criticalValidFlg 重要度（危険）有効/無効
	 */
	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	/**
	 * 重要度（不明）有効/無効を返す。<BR>
	 * @return 重要度（不明）有効/無効
	 */
	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	/**
	 * 重要度（不明）有効/無効を設定する。<BR>
	 * @param unknownValidFlg 重要度（不明）有効/無効
	 */
	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	/**
	 * アプリケーションフラグを返す。<BR>
	 * @return アプリケーションフラグ
	 */
	public Boolean getApplicationFlg() {
		return applicationFlg;
	}

	/**
	 * アプリケーションフラグを設定する。<BR>
	 * @param applicationFlg アプリケーションフラグ
	 */
	public void setApplicationFlg(Boolean applicationFlg) {
		this.applicationFlg = applicationFlg;
	}

	/**
	 * アプリケーションを返す。<BR>
	 * @return アプリケーション
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * アプリケーションを設定する。<BR>
	 * @param application アプリケーション
	 */
	public void setApplication(String application) {
		this.application = application;
	}

	/**
	 * 監視詳細フラグを返す。<BR>
	 * @return 監視詳細フラグ
	 */
	public Boolean getMonitorDetailIdFlg() {
		return monitorDetailIdFlg;
	}

	/**
	 * 監視詳細フラグを設定する。<BR>
	 * @param monitorDetailIdFlg 監視詳細フラグ
	 */
	public void setMonitorDetailIdFlg(Boolean monitorDetailIdFlg) {
		this.monitorDetailIdFlg = monitorDetailIdFlg;
	}

	/**
	 * 監視詳細を返す。<BR>
	 * @return 監視詳細
	 */
	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	/**
	 * 監視詳細を設定する。<BR>
	 * @param monitorDetailId 監視詳細
	 */
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	/**
	 * メッセージフラグを返す。<BR>
	 * @return メッセージフラグ
	 */
	public Boolean getMessageFlg() {
		return messageFlg;
	}

	/**
	 * メッセージフラグを設定する。<BR>
	 * @param messageFlg メッセージフラグ
	 */
	public void setMessageFlg(Boolean messageFlg) {
		this.messageFlg = messageFlg;
	}

	/**
	 * 拡張情報フラグを返す。<BR>
	 * @return 拡張情報フラグ
	 */
	public Boolean getExpFlg() {
		return expFlg;
	}

	/**
	 * 拡張情報フラグを設定する。<BR>
	 * @param expFlg 拡張情報フラグ
	 */
	public void setExpFlg(Boolean expFlg) {
		this.expFlg = expFlg;
	}

	/**
	 * 終了値 - 「常に」フラグを返す。<BR>
	 * @return 終了値 - 「常に」フラグ
	 */
	public Boolean getMonitorAllEndValueFlg() {
		return monitorAllEndValueFlg;
	}

	/**
	 * 終了値 - 「常に」フラグを設定する。<BR>
	 * @param monitorAllEndValueFlg 終了値 - 「常に」フラグ
	 */
	public void setMonitorAllEndValueFlg(Boolean monitorAllEndValueFlg) {
		this.monitorAllEndValueFlg = monitorAllEndValueFlg;
	}

	/**
	 * 終了値 - 「常に」 を返す。<BR>
	 * @return  終了値 - 「常に」
	 */
	public Integer getMonitorAllEndValue() {
		return monitorAllEndValue;
	}

	/**
	 *  終了値 - 「常に」を設定する。<BR>
	 * @param monitorAllEndValue  終了値 - 「常に」
	 */
	public void setMonitorAllEndValue(Integer monitorAllEndValue) {
		this.monitorAllEndValue = monitorAllEndValue;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を返す。<BR>
	 * @return ジョブ連携メッセージの拡張情報設定
	 */
	public ArrayList<JobLinkInheritInfo> getJobLinkInheritList() {
		return jobLinkInheritList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を設定する。<BR>
	 * @param jobLinkInheritList ジョブ連携メッセージの拡張情報設定
	 */
	public void setJobLinkInheritList(ArrayList<JobLinkInheritInfo> jobLinkInheritList) {
		this.jobLinkInheritList = jobLinkInheritList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を返す。<BR>
	 * @return
	 */
	public ArrayList<JobLinkExpInfo> getJobLinkExpList() {
		return jobLinkExpList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を設定する。<BR>
	 * @param jobLinkJobExpList
	 */
	public void setJobLinkExpList(ArrayList<JobLinkExpInfo> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((applicationFlg == null) ? 0 : applicationFlg.hashCode());
		result = prime * result + ((criticalValidFlg == null) ? 0 : criticalValidFlg.hashCode());
		result = prime * result + ((expFlg == null) ? 0 : expFlg.hashCode());
		result = prime * result + ((facilityID == null) ? 0 : facilityID.hashCode());
		result = prime * result + ((infoValidFlg == null) ? 0 : infoValidFlg.hashCode());
		result = prime * result + ((jobLinkExpList == null) ? 0 : jobLinkExpList.hashCode());
		result = prime * result + ((jobLinkInheritList == null) ? 0 : jobLinkInheritList.hashCode());
		result = prime * result + ((joblinkMessageId == null) ? 0 : joblinkMessageId.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((messageFlg == null) ? 0 : messageFlg.hashCode());
		result = prime * result + ((monitorAllEndValue == null) ? 0 : monitorAllEndValue.hashCode());
		result = prime * result + ((monitorAllEndValueFlg == null) ? 0 : monitorAllEndValueFlg.hashCode());
		result = prime * result + ((monitorCriticalEndValue == null) ? 0 : monitorCriticalEndValue.hashCode());
		result = prime * result + ((monitorDetailId == null) ? 0 : monitorDetailId.hashCode());
		result = prime * result + ((monitorDetailIdFlg == null) ? 0 : monitorDetailIdFlg.hashCode());
		result = prime * result + ((monitorInfoEndValue == null) ? 0 : monitorInfoEndValue.hashCode());
		result = prime * result + ((monitorUnknownEndValue == null) ? 0 : monitorUnknownEndValue.hashCode());
		result = prime * result + ((failureEndFlg == null) ? 0 : failureEndFlg.hashCode());
		result = prime * result + ((monitorWaitEndValue == null) ? 0 : monitorWaitEndValue.hashCode());
		result = prime * result + ((monitorWaitTime == null) ? 0 : monitorWaitTime.hashCode());
		result = prime * result + ((monitorWarnEndValue == null) ? 0 : monitorWarnEndValue.hashCode());
		result = prime * result + ((pastFlg == null) ? 0 : pastFlg.hashCode());
		result = prime * result + ((pastMin == null) ? 0 : pastMin.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((unknownValidFlg == null) ? 0 : unknownValidFlg.hashCode());
		result = prime * result + ((warnValidFlg == null) ? 0 : warnValidFlg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JobLinkRcvInfo)) {
			return false;
		}
		JobLinkRcvInfo o1 = this;
		JobLinkRcvInfo o2 = (JobLinkRcvInfo) obj;

		boolean ret = false;
		// スコープ(階層)は比較しない
		ret = equalsSub(o1.getFacilityID(), o2.getFacilityID()) &&
				equalsSub(o1.getMonitorInfoEndValue(), o2.getMonitorInfoEndValue()) &&
				equalsSub(o1.getMonitorWarnEndValue(), o2.getMonitorWarnEndValue()) &&
				equalsSub(o1.getMonitorCriticalEndValue(), o2.getMonitorCriticalEndValue()) &&
				equalsSub(o1.getMonitorUnknownEndValue(), o2.getMonitorUnknownEndValue()) &&
				equalsSub(o1.getFailureEndFlg(), o2.getFailureEndFlg()) &&
				equalsSub(o1.getMonitorWaitTime(), o2.getMonitorWaitTime()) &&
				equalsSub(o1.getMonitorWaitEndValue(), o2.getMonitorWaitEndValue()) &&
				equalsSub(o1.getJoblinkMessageId(), o2.getJoblinkMessageId()) &&
				equalsSub(o1.getMessage(), o2.getMessage()) &&
				equalsSub(o1.getPastFlg(), o2.getPastFlg()) &&
				equalsSub(o1.getPastMin(), o2.getPastMin()) &&
				equalsSub(o1.getInfoValidFlg(), o2.getInfoValidFlg()) &&
				equalsSub(o1.getWarnValidFlg(), o2.getWarnValidFlg()) &&
				equalsSub(o1.getCriticalValidFlg(), o2.getCriticalValidFlg()) &&
				equalsSub(o1.getUnknownValidFlg(), o2.getUnknownValidFlg()) &&
				equalsSub(o1.getApplicationFlg(), o2.getApplicationFlg()) &&
				equalsSub(o1.getApplication(), o2.getApplication()) &&
				equalsSub(o1.getMonitorDetailIdFlg(), o2.getMonitorDetailIdFlg()) &&
				equalsSub(o1.getMonitorDetailId(), o2.getMonitorDetailId()) &&
				equalsSub(o1.getMessageFlg(), o2.getMessageFlg()) &&
				equalsSub(o1.getExpFlg(), o2.getExpFlg()) &&
				equalsSub(o1.getMonitorAllEndValueFlg(), o2.getMonitorAllEndValueFlg()) &&
				equalsSub(o1.getMonitorAllEndValue(), o2.getMonitorAllEndValue()) &&
				equalsArray(o1.getJobLinkInheritList(), o2.getJobLinkInheritList()) &&
				equalsArray(o1.getJobLinkExpList(), o2.getJobLinkExpList());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			return false;
		}
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}

	private boolean equalsArray(ArrayList<?> list1, ArrayList<?> list2) {
		if (list1 != null && !list1.isEmpty()) {
			if (list2 != null && list1.size() == list2.size()) {
				Object[] ary1 = list1.toArray();
				Object[] ary2 = list2.toArray();
				Arrays.sort(ary1);
				Arrays.sort(ary2);

				for (int i = 0; i < ary1.length; i++) {
					if (!ary1[i].equals(ary2[i])) {
						if (m_log.isTraceEnabled()) {
							m_log.trace("equalsArray : " + ary1[i] + "!=" + ary2[i]);
						}
						return false;
					}
				}
				return true;
			}
		} else if (list2 == null || list2.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// [確認期間フラグ]がtrueの場合、[確認期間（分）]必須
		if (pastFlg && pastMin == null) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "pastMin");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// 重要度のいずれかが選択されていなければエラー
		if (!infoValidFlg && !warnValidFlg && !criticalValidFlg && !unknownValidFlg) {
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_ONE_OR_MORE.getMessage(
					MessageConstant.PRIORITY.getMessage()));
		}

		// [アプリケーションフラグ]がtrueの場合、[アプリケーション]必須
		if (applicationFlg
				&& (application == null || application.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "application");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [監視詳細フラグ]がtrueの場合、[監視詳細]必須
		if (monitorDetailIdFlg
				&& (monitorDetailId == null || monitorDetailId.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorDetailId");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [メッセージフラグ]がtrueの場合、[メッセージ]必須
		if (messageFlg
				&& (message == null || message.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "message");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [拡張情報フラグ]がtrueの場合、[拡張情報]必須
		if (expFlg
				&& (jobLinkExpList == null || jobLinkExpList.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkExpList");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		if (jobLinkExpList != null && !jobLinkExpList.isEmpty()) {
			HashMap<String, String> expMap = new HashMap<>();
			for (JobLinkExpInfo exp : jobLinkExpList) {
				// [拡張情報]チェック
				exp.correlationCheck();

				// [拡張情報]でキーと値が重複して存在する場合エラー
				if (expMap.containsKey(exp.getKey())
						&& expMap.containsValue(exp.getValue())) {
					String[] r1 = {RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkExpList"),
							String.format("%s,%s", exp.getKey(), exp.getValue())};
					throw new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(r1));
				}
				expMap.put(exp.getKey(), exp.getValue());
			}
		}

		if (monitorAllEndValueFlg) {
			// [メッセージが得られたら常に]がtrueの場合
			if (monitorAllEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorAllEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
		} else {
			// [メッセージが得られたら常に]がfalseの場合
			if (monitorInfoEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorInfoEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
			if (monitorWarnEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorWarnEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
			if (monitorCriticalEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorCriticalEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
			if (monitorUnknownEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorUnknownEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
		}

		// [メッセージが得られなかった場合に終了する]がtrueの場合
		if (failureEndFlg) {
			if (monitorWaitTime == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorWaitTime");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
			if (monitorWaitEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorWaitEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
		}

		// [引継ぎ情報]
		if (jobLinkInheritList != null && !jobLinkInheritList.isEmpty()) {
			HashSet<String> inherits = new HashSet<>();
			for (JobLinkInheritInfo inherit : jobLinkInheritList) {
				// [引継ぎ情報]チェック
				inherit.correlationCheck();

				// [引継ぎ情報]で[ジョブ変数名]が重複して存在する場合エラー
				if (inherits.contains(inherit.getParamId())) {
					String[] r1 = {RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkInheritList"),
							inherit.getParamId()};
					throw new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(r1));
				}
				inherits.add(inherit.getParamId());
			}
		}
	}
}