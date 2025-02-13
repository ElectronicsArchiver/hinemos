/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.openapitools.client.model.GetMonitorListRequest;
import org.openapitools.client.model.MonitorFilterInfoRequest;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.CloudServiceConv;
import com.clustercontrol.utility.settings.monitor.xml.CloudServiceMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CloudServiceMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.xcloud.plugin.monitor.CloudServiceMonitorPlugin;

/**
 * Cloud サービス 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された Cloud サービス 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている Cloud サービス 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 *
 */
public class CloudServiceAction extends AbstractMonitorAction<CloudServiceMonitors> {
	public CloudServiceAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<CloudServiceMonitors> getDataClass() {
		return CloudServiceMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(CloudServiceMonitors colodServiceMonitors) {
		int res = CloudServiceConv.checkSchemaVersion(colodServiceMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				CloudServiceConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(CloudServiceMonitors cloudServiceMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, RestConnectFailed, ParseException {
		return CloudServiceConv.createMonitorInfoList(cloudServiceMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, RestConnectFailed {
		
		MonitorFilterInfoRequest monitorFilterInfo = new MonitorFilterInfoRequest();
		monitorFilterInfo.setMonitorTypeId(CloudServiceMonitorPlugin.monitorPluginId);
		GetMonitorListRequest dtoReq = new GetMonitorListRequest();
		dtoReq.setMonitorFilterInfo(monitorFilterInfo);
		
		List<MonitorInfoResponse> list = MonitorsettingRestClientWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(dtoReq);
		
		return list;
	
	}

	@Override
	protected CloudServiceMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return CloudServiceConv.createCloudServiceMonitors(monitorInfoList);
	}

	@Override
	protected void sort(CloudServiceMonitors object) {
		CloudServiceMonitor[] ms = object.getCloudServiceMonitor();
		Arrays.sort(
			ms,
			new Comparator<CloudServiceMonitor>() {
				@Override
				public int compare(CloudServiceMonitor obj1, CloudServiceMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setCloudServiceMonitor(ms);
	}
}
