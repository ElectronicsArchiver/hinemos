/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeFailureTypeEnum;
import com.clustercontrol.util.MessageConstant;

public class AddHttpStringMonitorRequest extends AbstractAddStringMonitorRequest {

	@RestItemName(value = MessageConstant.PRIORITY_CHANGE_FAILURE_TYPE)
	@RestValidateObject(notNull = true)
	@RestBeanConvertEnum
	private PriorityChangeFailureTypeEnum priorityChangeFailureType;

	public AddHttpStringMonitorRequest() {

	}

	private HttpCheckInfoRequest httpCheckInfo;

	public HttpCheckInfoRequest getHttpCheckInfo() {
		return httpCheckInfo;
	}

	public void setHttpCheckInfo(HttpCheckInfoRequest httpCheckInfo) {
		this.httpCheckInfo = httpCheckInfo;
	}


	public PriorityChangeFailureTypeEnum getPriorityChangeFailureType() {
		return priorityChangeFailureType;
	}

	public void setPriorityChangeFailureType(PriorityChangeFailureTypeEnum priorityChangeFailureType) {
		this.priorityChangeFailureType = priorityChangeFailureType;
	}

	@Override
	public String toString() {
		return "AddHttpStringMonitorRequest [HttpCheckInfo=" + httpCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", priorityChangeJudgmentType=" + priorityChangeJudgmentType + ", priorityChangeFailureType="
				+ priorityChangeFailureType + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId="
				+ ownerRoleId + "]";
	}

}
