/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorStringValueInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.settings.monitor.xml.SyslogInfo;
import com.clustercontrol.utility.settings.monitor.xml.SyslogMonitor;
import com.clustercontrol.utility.settings.monitor.xml.SyslogMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * システムログ 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 */
public class SystemlogConv {
	private final static Log logger = LogFactory.getLog(SystemlogConv.class);

	private final static String SCHEMA_TYPE = "H";
	private final static String SCHEMA_VERSION = "1";
	private final static String SCHEMA_REVISION = "2";

	/**
	 * <BR>
	 *
	 * @return
	 */
	public static SchemaInfo getSchemaVersion(){
		SchemaInfo schema = new SchemaInfo();

		schema.setSchemaType(SCHEMA_TYPE);
		schema.setSchemaVersion(SCHEMA_VERSION);
		schema.setSchemaRevision(SCHEMA_REVISION);

		return schema;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	public static int checkSchemaVersion(SchemaInfo schemaInfo) {
		return BaseConv.checkSchemaVersion(
				SCHEMA_TYPE,
				SCHEMA_VERSION,
				SCHEMA_REVISION,
				schemaInfo.getSchemaType(),
				schemaInfo.getSchemaVersion(),
				schemaInfo.getSchemaRevision()
				);
	}

	/**
	 * Castor で作成した形式の リソース 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ParseException 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(SyslogMonitors syslogMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (SyslogMonitor syslogMonitor : syslogMonitors.getSyslogMonitor()) {
			logger.debug("Monitor Id : " + syslogMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(syslogMonitor.getMonitor());
			StringValue[] values = syslogMonitor.getStringValue();
			MonitorConv.sort(values);
			for (StringValue stringValue : values) {
				monitorInfo.getStringValueInfo().add(MonitorConv.createMonitorStringValueInfo(stringValue));
			}

			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の リソース 監視設定情報へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static SyslogMonitors createSyslogMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		SyslogMonitors syslogMonitors = new SyslogMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			SyslogMonitor syslogMonitor = new SyslogMonitor();
			syslogMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			int orderNo = 0;
			for (MonitorStringValueInfoResponse monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				syslogMonitor.addStringValue(MonitorConv.createStringValue(monitorInfo.getMonitorId(),monitorStringValueInfo, ++orderNo));
			}

			syslogMonitor.setSyslogInfo(createSyslogInfo(monitorInfo));
			syslogMonitors.addSyslogMonitor(syslogMonitor);
		}

		syslogMonitors.setCommon(MonitorConv.versionDto2Xml());
		syslogMonitors.setSchemaInfo(getSchemaVersion());

		return syslogMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static SyslogInfo createSyslogInfo(MonitorInfoResponse monitorInfo) {
		SyslogInfo syslogInfo = new SyslogInfo();
		syslogInfo.setMonitorTypeId("");

		syslogInfo.setMonitorId(monitorInfo.getMonitorId());

		return syslogInfo;
	}
}
