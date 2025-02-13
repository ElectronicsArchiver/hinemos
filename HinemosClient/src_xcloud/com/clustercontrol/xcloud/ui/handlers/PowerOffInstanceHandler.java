/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.ui.views.InstanceMonitorService;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

public class PowerOffInstanceHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	@Override
	public Object internalExecute(ExecutionEvent event) throws CloudManagerException, InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		final IInstance instance = (IInstance)selection.getFirstElement();

		final List<String> instanceIds = new ArrayList<>();
		for (@SuppressWarnings("rawtypes") Iterator iter = selection.iterator(); iter.hasNext();) {
			instanceIds.add(((IInstance)iter.next()).getId());
		}

		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				instanceIds.size() > 1 ? MessageFormat.format(msgConfirmPowerOffComputeNodeMulti, instanceIds.size()):
					MessageFormat.format(msgConfirmPowerOffComputeNode, instance.getName(), instance.getId()))) {

			String managerName = instance.getCloudScope().getCloudScopes().getHinemosManager().getManagerName();
			CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
			endpoint.powerOffInstances(instance.getCloudScope().getId(), instance.getLocation().getId(), String.join(",", instanceIds));

			// 成功報告ダイアログを生成
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					msgFinishPowerOffComputeNode);

			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					instance.getLocation().updateLocation();

					for (String instanceId: instanceIds) {
						InstanceMonitorService.getInstanceMonitorService().startMonitor(
								instance.getHinemosManager().getManagerName(),
								instance.getCloudScope().getId(),
								instance.getLocation().getId(),
								instanceId,
								"stopped"
								);
					}
				}
			});
		}
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishPowerOffComputeNode;
	}
}
