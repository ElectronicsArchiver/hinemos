/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingNotFound;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.validation.RestValidateLong;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rpa.scenario.model.RpaScenario.CulcType;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTag;
import com.clustercontrol.rpa.session.RpaControllerBean;
import com.clustercontrol.util.MessageConstant;
public class AddRpaScenarioRequest implements RequestDto {
	
	public AddRpaScenarioRequest(){
	}

	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;
	/** 説明 */
	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;
	/** RPAツールID */
	private String rpaToolId;
	/** 手動操作時間 */
	@RestItemName(value = MessageConstant.RPA_SCENARIO_MANUAL_TIME_SPECIFIED_VALUE)
	@RestValidateLong(minVal = 1, maxVal = Long.MAX_VALUE)
	private Long manualTime;
	/** 手動操作時間算出方式 */
	private CulcType manualTimeCulcType;
	/** 運用開始日時 */
	private Long opeStartDate;
	/** シナリオ識別文字列 */
	@RestItemName(value = MessageConstant.RPA_SCENARIO_SCENARIO_IDENTIFY_STRING)
	@RestValidateString(notNull = true, maxLen = 512)
	private String scenarioIdentifyString;
	/** 複数ノード共通のシナリオ(チェックボックス) */
	private Boolean commonNodeScenario;
	/** シナリオ名 */
	@RestItemName(value = MessageConstant.RPA_SCENARIO_NAME)
	@RestValidateString(notNull = true, maxLen = 512)
	private String scenarioName;
	
	/** 実行ノード */
	private List<String> execNodes = new ArrayList<>();
	
	/** シナリオタグ紐付け情報 */
	private List<String> tagRelationList = new ArrayList<>();

	/** シナリオ実績作成設定ID */
	public String getScenarioOperationResultCreateSettingId() {
		return scenarioOperationResultCreateSettingId;
	}
	public void setScenarioOperationResultCreateSettingId(String scenarioOperationResultCreateSettingId) {
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
	}

	/** 説明 */
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	/** RPAツールID */
	public String getRpaToolId() {
		return rpaToolId;
	}
	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	/** 手動操作時間 */
	public Long getManualTime() {
		return this.manualTime;
	}
	public void setManualTime(Long manualTime) {
		this.manualTime = manualTime;
	}

	/** 手動操作時間算出方式 */
	public CulcType getManualTimeCulcType() {
		return this.manualTimeCulcType;
	}
	public void setManualTimeCulcType(CulcType manualTimeCulcType) {
		this.manualTimeCulcType = manualTimeCulcType;
	}

	/** 運用開始日時 */
	public Long getOpeStartDate() {
		return this.opeStartDate;
	}
	public void setOpeStartDate(Long opeStartDate) {
		this.opeStartDate = opeStartDate;
	}

	/** シナリオ識別文字列 */
	public String getScenarioIdentifyString() {
		return this.scenarioIdentifyString;
	}
	public void setScenarioIdentifyString(String scenarioIdentifyString) {
		this.scenarioIdentifyString = scenarioIdentifyString;
	}

	/** シナリオ判定方式 */
	public Boolean getCommonNodeScenario() {
		return this.commonNodeScenario;
	}
	public void setCommonNodeScenario(Boolean commonNodeScenario) {
		this.commonNodeScenario = commonNodeScenario;
	}

	/** シナリオ名 */
	public String getScenarioName() {
		return this.scenarioName;
	}
	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	/** 実行ノード */
	public List<String> getExecNodes() {
		return execNodes;
	}
	public void setExecNodes(List<String> execNodes) {
		this.execNodes = execNodes;
	}

	/** シナリオタグ紐付け情報 */
	public List<String> getTagRelationList() {
		return tagRelationList;
	}
	public void setTagRelationList(List<String> tagRelationList) {
		this.tagRelationList = tagRelationList;
	}

	@Override
	public String toString() {
		return "AddRpaScenarioRequest [description=" + description + ", rpaToolId=" + rpaToolId + ", manualTime=" + manualTime + ", manualTimeCulcType="
				+ manualTimeCulcType + ", opeStartDate=" + opeStartDate + ", scenarioIdentifyString=" + scenarioIdentifyString + ", commonNodeScenario="
				+ commonNodeScenario + ", scenarioName=" + scenarioName + ", execNodes=" + execNodes + ", tagRelationList=" + tagRelationList + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
			try {
				RpaControllerBean rpaControllerBean = new RpaControllerBean();
				// シナリオ実績作成設定IDの存在チェック
				RpaScenarioOperationResultCreateSetting createSetting = rpaControllerBean.getRpaScenarioOperationResultCreateSetting(this.scenarioOperationResultCreateSettingId);

				String ownerRoleId = createSetting.getOwnerRoleId();
				for (String tagId: tagRelationList) {
					// 参照可能なタグIDをチェック
					List<String> referableTagIds = rpaControllerBean.getRpaScenarioTagListByOwnerRole(ownerRoleId)
							.stream().map(RpaScenarioTag::getTagId).collect(Collectors.toList());
					
					if (!referableTagIds.contains(tagId)) {
						throw new InvalidSetting(
								MessageConstant.MESSAGE_NOT_FOUND.getMessage(
										MessageConstant.RPA_SCENARIO_TAG_ID.getMessage(), 
										tagId));
					}
				}
			} catch (RpaScenarioOperationResultCreateSettingNotFound | InvalidRole | HinemosUnknown e) {
				throw new InvalidSetting(e.getMessage());
			}
	}

}
