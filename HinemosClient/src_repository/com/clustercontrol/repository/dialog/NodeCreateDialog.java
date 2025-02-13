/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.dialog;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.openapitools.client.model.AddNodeRequest;
import org.openapitools.client.model.DeviceSearchMessageInfoResponse;
import org.openapitools.client.model.GetNodesBySNMPRequest;
import org.openapitools.client.model.ModifyNodeRequest;
import org.openapitools.client.model.NodeInfoDeviceSearchResponse;
import org.openapitools.client.model.NodeInfoResponse;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.PropertyFieldColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.repository.action.GetNodeProperty;
import com.clustercontrol.repository.bean.NodeConstant;
import com.clustercontrol.repository.util.NodePropertyUtil;
import com.clustercontrol.repository.util.NodeSearchUtil;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.PropertySheet;

/**
 * ノードの作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class NodeCreateDialog extends CommonDialog {
	// ログ
	private static Log m_log = LogFactory.getLog( NodeCreateDialog.class );

	private final int sizeY = 800;
	
	/** SNMPバージョンコンボボックスのインデックス */
	private final int INDEX_VERSION_BOX_0 = 0;
	private final int INDEX_VERSION_BOX_1 = 1;
	private final int INDEX_VERSION_BOX_2 = 2;
	

	/** プロパティツリーのインデックス */
	private final int TREE_INDEX_FACILITY_ID = 0;
	private final int TREE_INDEX_FACILITY_NAME = 1;

	private final int TREE_INDEX_SERVER_BASE = 5;
	private final int TREE_INDEX_SB_HARDWARE = 0;
	private final int TREE_INDEX_SB_H_PLATFORM = 0;
	private final int TREE_INDEX_SB_NETWORK = 1;
	private final int TREE_INDEX_SB_N_IP_VERSION = 0;
	private final int TREE_INDEX_SB_N_IP_V4 = 1;
	private final int TREE_INDEX_SB_N_IP_V6 = 2;
	private final int TREE_INDEX_SB_N_NODE_NAME = 3;
	
	// ----- instance フィールド ----- //

	/** 初期表示ノードのファシリティID */
	private String facilityId = "aa";

	/** 一括変更対象スコープのファシリティID */
	private String scopeId = "";

	/** 変更前のプロパティ */
	private Property propertyOld = null;

	/** 変更前のノード情報 */
	private NodeInfoResponse nodeInfoOld = null;

	/** 変更用ダイアログ判別フラグ */
	private boolean isModifyDialog = false;

	/** ノード属性プロパティシート */
	private PropertySheet propertySheet = null;

	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_ownerRoleId = null;

	//SNMPで検出する機能のための追加コンポーネント
	private Button buttonAuto = null;

	private Label    ipAddressText = null; //"IP Address";
	private Text     ipAddressBox  = null;
	private Label    communityText = null; //"community";
	private Text     communityBox  = null;
	private Label    portText      = null; //"port";
	private Text     portBox       = null;
	private Label    versionText      = null; //"version";
	private Combo    versionBox       = null;
	private Label    securityLevelText = null;
	private Combo    securityLevelBox       = null;
	private Label    userText = null;
	private Text     userBox  = null;
	private Label    authPassText = null;
	private Text     authPassBox  = null;
	private Label    privPassText = null;
	private Text     privPassBox  = null;
	private Label empty_label_version = null;
	private Label empty_label_user = null;
	private Label empty_label_auth = null;
	private Label empty_label_security = null;
	private Label    authProtocolText = null;
	private Combo    authProtocolBox       = null;
	private Label    privProtocolText = null;
	private Combo    privProtocolBox       = null;
	private Tree tree = null;
	private NodeInfoResponse nodeInfo = null;

	private Group groupAuto = null;
	private Composite comp = null;
	private Property propertyChild = null;
	private String oldVersion = null;
	private String managerName = null;

	// ----- コンストラクタ ----- //

	/**
	 * 指定した形式のダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param facilityId
	 *            初期表示するノードのファシリティID
	 * @param isModifyDialog
	 *            変更用ダイアログとして利用する場合は、true
	 */
	public NodeCreateDialog(Shell parent, String managerName, String facilityId,
			boolean isModifyDialog) {
		super(parent);

		this.managerName = managerName;
		this.facilityId = facilityId;
		this.isModifyDialog = isModifyDialog;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 600);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();
		// タイトル
		shell.setText(Messages
				.getString("dialog.repository.node.create.modify"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);
		this.comp = parent;

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		this.comp.setLayoutData(gridData);

		//SNMPによる検出のグループ(SNMP)
		groupAuto = new Group(parent, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "auto", groupAuto);
		groupAuto.setLayoutData(gridData);
		layout = new GridLayout(5, false);
		groupAuto.setLayout(layout);
		if (isModifyDialog) {
			groupAuto.setText(Messages.getString("device.search") + " " +
					Messages.getString("repository.find.by.snmp.modify"));
		} else {
			groupAuto.setText(Messages.getString("device.search"));
		}

		//IPアドレス
		this.ipAddressText = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "ipaddress", ipAddressText);
		this.ipAddressText.setText(Messages.getString("ip.address") + " : ");

		this.ipAddressBox = new Text(groupAuto, SWT.BORDER | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "ipaddress", ipAddressBox);
		GridData grid = new GridData(GridData.FILL_HORIZONTAL);
		grid.minimumWidth = 120;
		this.ipAddressBox.setLayoutData(grid);

		// プロパティ取得
		if (this.isModifyDialog) {
			GetNodeProperty getNodeProperty = new GetNodeProperty(this.managerName, this.facilityId,
					PropertyDefineConstant.MODE_MODIFY);
			propertyOld = getNodeProperty.getProperty(false);
			nodeInfoOld = getNodeProperty.getNodeInfo();
		} else {
			GetNodeProperty getNodeProperty = new GetNodeProperty(this.managerName, this.facilityId,
					PropertyDefineConstant.MODE_ADD);
			propertyOld = getNodeProperty.getProperty(false);
			nodeInfoOld = getNodeProperty.getNodeInfo();
		}
		propertyChild = PropertyUtil.getProperty(propertyOld, NodeConstant.IP_ADDRESS_VERSION).get(0);
		String ipAddressVersion = propertyChild.getValue().toString();
		String ipAddress = null;
		if ("6".equals(ipAddressVersion)) {
			propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.IP_ADDRESS_V6).get(0);
			ipAddress = propertyChild.getValue().toString();
		} else {
			propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.IP_ADDRESS_V4).get(0);
			ipAddress = propertyChild.getValue().toString();
		}
		if ("".equals(ipAddress)) {
			this.ipAddressBox.setText(NodeSearchUtil.generateDefaultIp( "192.168.0." ));
		} else {
			this.ipAddressBox.setText(ipAddress);
		}

		//ポート名
		this.portText = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "portnumber", portText);
		this.portText.setText(Messages.getString("port.number") + " : ");

		this.portBox = new Text(groupAuto, SWT.BORDER | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "port", portBox);
		grid = new GridData(GridData.FILL_HORIZONTAL);
		grid.minimumWidth = 50;
		this.portBox.setLayoutData(grid);
		propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.SNMP_PORT).get(0);
		String snmpPort = propertyChild.getValue().toString();
		if ("".equals(snmpPort)) {
			this.portBox.setText("161");
		} else {
			this.portBox.setText(snmpPort);
		}

		// 改行のため、ダミーのラベルを挿入。
		Label label = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "dummy", label);

		//コミュニティ名
		this.communityText = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "communityname", communityText);
		this.communityText.setText(Messages.getString("community.name") + " : ");

		this.communityBox = new Text(groupAuto, SWT.BORDER | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "community", communityBox);
		this.communityBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.SNMP_COMMUNITY).get(0);
		String snmpCommunity = propertyChild.getValue().toString();
		if ("".equals(snmpCommunity)) {
			this.communityBox.setText("public");
		} else {
			this.communityBox.setText(snmpCommunity);
		}

		//バージョン
		this.versionText = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "snmpversion", versionText);
		this.versionText.setText(Messages.getString("snmp.version") + " : ");

		this.versionBox = new Combo(groupAuto, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "version", versionBox);
		this.versionBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.versionBox.add("1",INDEX_VERSION_BOX_0);
		this.versionBox.add("2c",INDEX_VERSION_BOX_1);
		this.versionBox.add("3",INDEX_VERSION_BOX_2);
		// デフォルトをv2cとする
		propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.SNMP_VERSION).get(0);
		String snmpVersion = propertyChild.getValue().toString();
		if (SnmpVersionConstant.STRING_V1.equals(snmpVersion)) {
			this.versionBox.select(INDEX_VERSION_BOX_0);
		} else if (SnmpVersionConstant.STRING_V2.equals(snmpVersion)) {
			this.versionBox.select(INDEX_VERSION_BOX_1);
		} else {
			this.versionBox.select(INDEX_VERSION_BOX_2);
			setVersion3Item();
		}
		oldVersion = versionBox.getText();

		this.versionBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String version = versionBox.getText();
				if (oldVersion.equals(version)) {
					return;
				}
				if (version.equals("3")) {
					setVersion3Item();
				} else if (oldVersion.equals("3"))  {
					empty_label_version.dispose();
					empty_label_user.dispose();
					empty_label_auth.dispose();
					empty_label_security.dispose();
					securityLevelText.dispose();
					securityLevelBox.dispose();
					userText.dispose();
					userBox.dispose();
					authPassText.dispose();
					authPassBox.dispose();
					privPassText.dispose();
					privPassBox.dispose();
					authProtocolText.dispose();
					authProtocolBox.dispose();
					privProtocolText.dispose();
					privProtocolBox.dispose();
					groupAuto.layout();
					NodeCreateDialog.this.comp.layout();
				}
				oldVersion = version;
			}
		});

		if (SnmpVersionConstant.STRING_V3.equals(snmpVersion) == false) {
			setAutoButton();
		}

		/////////////////////////////////////////////////////////////

		// マネージャ
		Label labelManager = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", labelManager);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		if(!this.isModifyDialog()){
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		}
		WidgetTestUtil.setTestId(this, "managerComposite", this.m_managerComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.managerName != null) {
			this.m_managerComposite.setText(this.managerName);
		}
		if(isModifyDialog == false) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					m_ownerRoleId.createRoleIdList(managerName);
				}
			});
		}

		// オーナーロールID
		Label labelRoleId = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "roleid", labelRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if(!this.isModifyDialog()){
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, this.managerName, true, Mode.OWNER_ROLE);
		} else {
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, this.managerName, false, Mode.OWNER_ROLE);
		}
		WidgetTestUtil.setTestId(this, "roleidlist", m_ownerRoleId);
		if (nodeInfoOld != null && nodeInfoOld.getOwnerRoleId() != null) {
			m_ownerRoleId.setText(nodeInfoOld.getOwnerRoleId());
		}
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_ownerRoleId.setLayoutData(gridData);


		/*
		 * 属性プロパティシート
		 */

		// ラベル
		Label labelAttribute = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "attribute", labelAttribute);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		labelAttribute.setLayoutData(gridData);
		labelAttribute.setText(Messages.getString("attribute") + " : ");

		// プロパティシート
		tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, tree);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = SWT.MIN;
		gridData.horizontalSpan = 1;
		tree.setLayoutData(gridData);
		tree.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シートの展開と色表示
				update();
			}
		});

		this.propertySheet = new PropertySheet(tree);
		this.propertySheet.setSize(230, 300);

		this.propertySheet.addTreeListener(new ITreeViewerListener() {

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				// シートの展開と色表示
				update();
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				// シートの展開と色表示
				update();
			}
		});

		// プロパティ設定
		this.propertySheet.setInput(propertyOld);

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(shell.getSize().x, sizeY ));

		this.expand();
		this.update();
	}

	private void setAutoButton() {
		this.buttonAuto = new Button(groupAuto, SWT.PUSH | SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "auto", buttonAuto);
		this.buttonAuto.setText(" Search ");
		GridData gridData = new GridData();
		gridData.horizontalIndent= 30;
		this.buttonAuto.setLayoutData(gridData);
		this.buttonAuto.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try{
					String ipAddressString = ipAddressBox.getText();

					if (ipAddressString.contains("%")) {
						MessageDialog.openWarning(getShell(), "Warning", Messages.getString("message.repository.37"));
						return;
					}

					// IPアドレスチェック
					InetAddress address = InetAddress.getByName(ipAddressString);

					if (address instanceof Inet4Address){

						//IPv4の場合はさらにStringをチェック
						if (!ipAddressString.matches(".{1,3}?\\..{1,3}?\\..{1,3}?\\..{1,3}?")){
							MessageDialog.openWarning(getShell(), "Warning",  Messages.getString("message.repository.37"));
							return;
						}

					} else if (address instanceof Inet6Address){
						//IPv6の場合は特にStringチェックは無し
					} else {
						MessageDialog.openWarning(getShell(), "Warning",  Messages.getString("message.repository.37"));
						return;
					}

					String ipAddress = ipAddressBox.getText();
					Integer port = Integer.parseInt(portBox.getText());
					String community = communityBox.getText();
					Integer version = versionBox.getSelectionIndex() == INDEX_VERSION_BOX_2 ? SnmpVersionConstant.TYPE_V3 : versionBox.getSelectionIndex();
					String securityLevel = securityLevelBox == null || securityLevelBox.isDisposed() ? null : securityLevelBox.getText();
					String user = userBox == null || userBox.isDisposed() ? null : userBox.getText();
					String authPassword = authPassBox == null || authPassBox.isDisposed() ? null : authPassBox.getText();
					String privPassword = privPassBox == null  || privPassBox.isDisposed() ? null : privPassBox.getText();
					String authProtocol = authProtocolBox == null || authProtocolBox.isDisposed() ? null : authProtocolBox.getText();
					String privProtocol = privProtocolBox == null || privProtocolBox.isDisposed() ? null : privProtocolBox.getText();

					Property propertySNMP = null;
					NodeInfoResponse nodeInfo = null;

					if (isModifyDialog) {
						NodeInfoDeviceSearchResponse nodeSnmp = getNodeInfoBySNMP(
								NodeCreateDialog.this.m_managerComposite.getText(),
								ipAddress, port, community, version,
								PropertyDefineConstant.MODE_MODIFY, facilityId,
								securityLevel, user, authPassword,
								privPassword, authProtocol, privProtocol);
						if (nodeSnmp == null) {
							// エラーのため処理終了
							return;
						}

						List<DeviceSearchMessageInfoResponse> list = nodeSnmp.getDeviceSearchMessageInfo();
						if (list != null && list.size() > 0) {
							DeviceSearchDialog dialog = new DeviceSearchDialog(getShell(), list);
							dialog.open();
						}
						
						nodeInfo = nodeSnmp.getNodeInfo();
						
						//現在画面に表示されている文字を取得
						nodeInfoOld=NodePropertyUtil.property2node(getInputData());
						
						//ノード情報の更新
						propertyOld=createProperty(nodeInfo);
						propertySheet.setInput(propertyOld);
						
					} else {
						NodeInfoDeviceSearchResponse nodeSnmp  = getNodeInfoBySNMP(
								NodeCreateDialog.this.m_managerComposite.getText(),
								ipAddress, port, community, version,
								PropertyDefineConstant.MODE_ADD, null, securityLevel, user, authPassword, privPassword, authProtocol, privProtocol);
						if(nodeSnmp != null) {
							nodeInfo = nodeSnmp.getNodeInfo();

							propertySNMP = NodePropertyUtil.node2property(
									m_managerComposite.getText(), nodeInfo,
									PropertyDefineConstant.MODE_ADD,
									Locale.getDefault(), false);
							propertySheet.setInput(propertySNMP);
						}
					}

					// シートの展開と色表示
					expand();
					update();


				} catch (NumberFormatException e1){
					// port番号が不正な場合
					MessageDialog.openWarning(getShell(), "Warning", Messages.getString("message.repository.38"));

				} catch (UnknownHostException e2) {
					// IPアドレスが不正な場合
					MessageDialog.openWarning(getShell(), "Warning",  Messages.getString("message.repository.37"));
				}
			}
		});
	}
	
	/**
	 * Device Search時のノード情報の更新
	 *
	 * @param nodeInfo
	 * 			Device Searchで更新されたノード情報
	 * @return 
	 * 			既存のノード情報をDevice Searchで取得した情報で更新したProperty
	 */
	private Property createProperty(NodeInfoResponse nodeInfo){
		
		if (nodeInfo == null){
			return null;
		}
		
		/*ノード変更時のDevice Searchはサーバ基本情報とデバイスのみ修正する*/
		/*Basic Information入れ替え
		 * サブプラットフォームは変更しない
		 * ノードプロパティの即時反映用ポートはリセットしない
		 */
		//hardware
		nodeInfoOld.setPlatformFamily(nodeInfo.getPlatformFamily());
		nodeInfoOld.setHardwareType(nodeInfo.getHardwareType());
		nodeInfoOld.setIconImage(nodeInfo.getIconImage());
		//network
		nodeInfoOld.setIpAddressVersion(nodeInfo.getIpAddressVersion());
		nodeInfoOld.setIpAddressV4(nodeInfo.getIpAddressV4());
		nodeInfoOld.setIpAddressV6(nodeInfo.getIpAddressV6());
		
		//nodeConfigInfo
		//hostname
		nodeInfoOld.getNodeHostnameInfo().clear();
		for(int i=0;i<nodeInfo.getNodeHostnameInfo().size();i++){
			nodeInfoOld.getNodeHostnameInfo().add(nodeInfo.getNodeHostnameInfo().get(i));
		}
		//nodeName
		nodeInfoOld.setNodeName(nodeInfo.getNodeName());
		//osname
		nodeInfoOld.setNodeOsInfo(nodeInfo.getNodeOsInfo());
		//device
		//cpu
		nodeInfoOld.getNodeCpuInfo().clear();
		for(int i=0;i<nodeInfo.getNodeCpuInfo().size();i++){
			nodeInfoOld.getNodeCpuInfo().add(nodeInfo.getNodeCpuInfo().get(i));
		}
		//memory
		nodeInfoOld.getNodeMemoryInfo().clear();
		for(int i=0;i<nodeInfo.getNodeMemoryInfo().size();i++){
			nodeInfoOld.getNodeMemoryInfo().add(nodeInfo.getNodeMemoryInfo().get(i));
		}
		//NIC
		nodeInfoOld.getNodeNetworkInterfaceInfo().clear();
		for(int i=0;i<nodeInfo.getNodeNetworkInterfaceInfo().size();i++){
			nodeInfoOld.getNodeNetworkInterfaceInfo().add(nodeInfo.getNodeNetworkInterfaceInfo().get(i));
		}
		//disk
		nodeInfoOld.getNodeDiskInfo().clear();
		for(int i=0;i<nodeInfo.getNodeDiskInfo().size();i++){
			nodeInfoOld.getNodeDiskInfo().add(nodeInfo.getNodeDiskInfo().get(i));
		}
		//filesystem
		nodeInfoOld.getNodeFilesystemInfo().clear();
		for(int i=0;i<nodeInfo.getNodeFilesystemInfo().size();i++){
			nodeInfoOld.getNodeFilesystemInfo().add(nodeInfo.getNodeFilesystemInfo().get(i));
		}
		//general dev
		nodeInfoOld.getNodeDeviceInfo().clear();
		for(int i=0;i<nodeInfo.getNodeDeviceInfo().size();i++){
			nodeInfoOld.getNodeDeviceInfo().add(nodeInfo.getNodeDeviceInfo().get(i));
		}
		
		return NodePropertyUtil.node2property(m_managerComposite.getText(), nodeInfoOld, PropertyDefineConstant.MODE_MODIFY, Locale.getDefault(), false);
	
	}

	/**
	 * プロパティシートの展開
	 *
	 */
	private void expand(){

		m_log.debug("expand");
		/*プロパティシートの展開を指定します。*/
		//レベル1までの展開
		this.propertySheet.expandToLevel(1);
		//サーバ基本情報
		Object element = this.propertySheet.getTree().getItem(5).getData();
		this.propertySheet.expandToLevel(element, 2);
		//構成情報
		element = this.propertySheet.getTree().getItem(6).getData();
		this.propertySheet.expandToLevel(element, 1);
		//サービス
		element =  this.propertySheet.getTree().getItem(7).getData();
		this.propertySheet.expandToLevel(element, 1);
		//デバイス
		element =  this.propertySheet.getTree().getItem(8).getData();
		this.propertySheet.expandToLevel(element, 1);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){

		m_log.debug("update");

		/*必須項目を色別表示します。*/

		// ファシリティID
		setColor(tree.getItem(TREE_INDEX_FACILITY_ID));

		// ファシリティ名
		setColor(tree.getItem(TREE_INDEX_FACILITY_NAME));

		// ハードウェア-プラットフォーム
		TreeItem severBase = tree.getItem(TREE_INDEX_SERVER_BASE);
		if(severBase.getItemCount() > 0 ) {
			TreeItem sbHardware = severBase.getItem(TREE_INDEX_SB_HARDWARE);
			if(sbHardware.getItemCount() > 0){
				TreeItem sbHwPlatform = sbHardware.getItem(TREE_INDEX_SB_H_PLATFORM);
				setColor(sbHwPlatform);
			}
		}
		
		// ネットワーク.
		if (severBase.getItemCount() > 1 ) {
			// IPアドレス
			TreeItem sbNetwork = severBase.getItem(TREE_INDEX_SB_NETWORK);
			if(sbNetwork.getItemCount() > 2){
				TreeItem sbNwIpVersion = sbNetwork.getItem(TREE_INDEX_SB_N_IP_VERSION);
				TreeItem sbNwIpAddressV4 = sbNetwork.getItem(TREE_INDEX_SB_N_IP_V4);
				TreeItem sbNwIpAddressV6 = sbNetwork.getItem(TREE_INDEX_SB_N_IP_V6);
				if("4".equals(sbNwIpVersion.getText(1))){
					sbNwIpVersion.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
					setColor(sbNwIpAddressV4);
					sbNwIpAddressV6.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}else if("6".equals(sbNwIpVersion.getText(1))){
					sbNwIpVersion.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
					sbNwIpAddressV4.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
					setColor(sbNwIpAddressV6);
				}else {
					sbNwIpVersion.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
					sbNwIpAddressV4.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
					sbNwIpAddressV6.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}
			// ノード名
			if(sbNetwork.getItemCount() > 3){
				TreeItem sbNwNodeName = sbNetwork.getItem(TREE_INDEX_SB_N_NODE_NAME);
				setColor(sbNwNodeName);
			}
		}

		for (TreeItem item : tree.getItems()) {
			setForegroundColor(item);
		}
	}

	/**
	 * 空文字だったらピンク色にする。
	 * @param item
	 */
	private void setColor(TreeItem item) {
		if (item == null) {
			m_log.debug("setColor() : 'item' is null.");
			return ;
		}
		Property element = (Property)item.getData();
		if(element == null){
			m_log.error("setColor() : 'element' is null.");
			return;
		}
		
		if ("".equals(element.getValueText())) {
			item.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			item.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 入力項目です、の色を薄くする。
	 * @param item
	 */
	private void setForegroundColor(TreeItem item) {
		if (item == null) {
			return;
		}
		Property element = (Property)item.getData();
		if (element != null && "".equals(element.getValueText())) {
			item.setForeground(1, PropertyFieldColorConstant.COLOR_EMPTY);
		} else {
			item.setForeground(1, PropertyFieldColorConstant.COLOR_FILLED);
		}

		for (TreeItem child : item.getItems()) {
			setForegroundColor(child);
		}
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		return result;
	}

	/**
	 * 入力値をマネージャに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		Property property = this.getInputData();
		if(property != null){
			String errMessage = "";
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(this.m_managerComposite.getText());
			Object[] arg = {this.m_managerComposite.getText()};
			if(!this.isModifyDialog()){
				// 作成の場合
				Property copy = PropertyUtil.copy(property);
				PropertyUtil.deletePropertyDefine(copy);
				try {
					nodeInfo = NodePropertyUtil.property2node(copy);
					if (m_ownerRoleId.getText().length() > 0) {
						nodeInfo.setOwnerRoleId(m_ownerRoleId.getText());
					}
					AddNodeRequest requestDto = new AddNodeRequest();
					RestClientBeanUtil.convertBean(nodeInfo, requestDto);

					//Enum値の入れる
					if (nodeInfo.getIpAddressVersion() != null) {
						requestDto.setIpAddressVersion(AddNodeRequest.IpAddressVersionEnum.fromValue(nodeInfo.getIpAddressVersion().getValue()));
					}
					if (nodeInfo.getSnmpVersion() != null) {
						requestDto.setSnmpVersion(AddNodeRequest.SnmpVersionEnum.fromValue(nodeInfo.getSnmpVersion().getValue()));
					}
					if (nodeInfo.getSnmpSecurityLevel() != null) {
						requestDto.setSnmpSecurityLevel(AddNodeRequest.SnmpSecurityLevelEnum.fromValue(nodeInfo.getSnmpSecurityLevel().getValue()));
					}
					if (nodeInfo.getSnmpAuthProtocol() != null) {
						requestDto.setSnmpAuthProtocol(AddNodeRequest.SnmpAuthProtocolEnum.fromValue(nodeInfo.getSnmpAuthProtocol().getValue()));
					}
					if (nodeInfo.getSnmpPrivProtocol() != null) {
						requestDto.setSnmpPrivProtocol(AddNodeRequest.SnmpPrivProtocolEnum.fromValue(nodeInfo.getSnmpPrivProtocol().getValue()));
					}
					if (nodeInfo.getWbemProtocol() != null) {
						requestDto.setWbemProtocol(AddNodeRequest.WbemProtocolEnum.fromValue(nodeInfo.getWbemProtocol().getValue()));
					}
					if (nodeInfo.getWinrmProtocol() != null) {
						requestDto.setWinrmProtocol(AddNodeRequest.WinrmProtocolEnum.fromValue(nodeInfo.getWinrmProtocol().getValue()));
					}

					wrapper.addNode(requestDto);

					// リポジトリキャッシュの更新
					ClientSession.doCheck();

					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.repository.4", arg));

					result = true;

				} catch (FacilityDuplicate e) {
					// ファシリティIDが重複している場合、エラーダイアログを表示する
					//ファシリティID取得
					ArrayList<?> values = PropertyUtil.getPropertyValue(copy, NodeConstant.FACILITY_ID);
					String args[] = { (String)values.get(0) };

					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.repository.26", args));

				} catch (Exception e) {
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
						if (!(e instanceof InvalidSetting)) {
							m_log.warn("action()", e);
						} else {
							m_log.info("action()" + errMessage);
						}
					}
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.repository.5") + errMessage);
				}
			} else {
				// 変更の場合
				Property copy = PropertyUtil.copy(property);
				PropertyUtil.deletePropertyDefine(copy);
				try {
					nodeInfo = NodePropertyUtil.property2node(copy);
					if (m_ownerRoleId.getText().length() > 0) {
						nodeInfo.setOwnerRoleId(m_ownerRoleId.getText());
					}
					ModifyNodeRequest requestDto = new ModifyNodeRequest();
					RestClientBeanUtil.convertBean(nodeInfo, requestDto);

					//Enum値の入れる
					if (nodeInfo.getIpAddressVersion() != null) {
						requestDto.setIpAddressVersion(ModifyNodeRequest.IpAddressVersionEnum.fromValue(nodeInfo.getIpAddressVersion().getValue()));
					}
					if (nodeInfo.getSnmpVersion() != null) {
						requestDto.setSnmpVersion(ModifyNodeRequest.SnmpVersionEnum.fromValue(nodeInfo.getSnmpVersion().getValue()));
					}
					if (nodeInfo.getSnmpSecurityLevel() != null) {
						requestDto.setSnmpSecurityLevel(ModifyNodeRequest.SnmpSecurityLevelEnum.fromValue(nodeInfo.getSnmpSecurityLevel().getValue()));
					}
					if (nodeInfo.getSnmpAuthProtocol() != null) {
						requestDto.setSnmpAuthProtocol(ModifyNodeRequest.SnmpAuthProtocolEnum.fromValue(nodeInfo.getSnmpAuthProtocol().getValue()));
					}
					if (nodeInfo.getSnmpPrivProtocol() != null) {
						requestDto.setSnmpPrivProtocol(ModifyNodeRequest.SnmpPrivProtocolEnum.fromValue(nodeInfo.getSnmpPrivProtocol().getValue()));
					}
					if (nodeInfo.getWbemProtocol() != null) {
						requestDto.setWbemProtocol(ModifyNodeRequest.WbemProtocolEnum.fromValue(nodeInfo.getWbemProtocol().getValue()));
					}
					if (nodeInfo.getWinrmProtocol() != null) {
						requestDto.setWinrmProtocol(ModifyNodeRequest.WinrmProtocolEnum.fromValue(nodeInfo.getWinrmProtocol().getValue()));
					}

					wrapper.modifyNode(nodeInfo.getFacilityId(), requestDto);

					// リポジトリキャッシュの更新
					ClientSession.doCheck();

					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.repository.10", arg));

					result = true;

				} catch (Exception e) {
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}

					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.repository.11") + errMessage);
				}
			}
		}

		return result;
	}

	/**
	 * 変更用ダイアログなのかを返します。
	 *
	 * @return 変更用ダイアログの場合、true
	 */
	public boolean isModifyDialog() {
		return this.isModifyDialog;
	}

	/**
	 * 一括変更対象スコープのファシリティIDを返します。
	 *
	 * @return 一括変更対象スコープのファシリティID
	 */
	public String getScopeId() {
		return this.scopeId;
	}

	/**
	 * 入力値を保持したデータモデルを生成します。
	 *
	 * @return データモデル
	 */
	public Property getInputData() {
		return (Property) this.propertySheet.getInput();
	}

	/**
	 * 入力値を保持したデータモデルを設定します。
	 *
	 * @param property
	 */
	public void setInputData(Property property) {
		propertySheet.setInput(property);
		this.update();
	}

	/**
	 * @return Returns the facilityId.
	 */
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		if (isModifyDialog()) {
			return Messages.getString("modify");
		} else {
			return Messages.getString("register");
		}
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * NodeInfoを返します。
	 *
	 * @return NodeInfo
	 */
	public NodeInfoResponse getNodeInfo() {
		return this.nodeInfo;
	}

	/**
	 *SNMPを利用してノード情報を生成します。<BR>
	 *
	 * @param pollingData SNMPポーリングのためのIPアドレスなど
	 * @param mode 編集可否モード
	 * @return ノード情報のプロパティ
	 */
	private static NodeInfoDeviceSearchResponse getNodeInfoBySNMP(String managerName, String ipAddress,
			int port, String community, int version, int mode,
			String facilityID, String securityLevel, String user,
			String authPassword, String privPassword, String authProtocol,
			String privProtocol) {

		try {
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);

			GetNodesBySNMPRequest requestDto = new GetNodesBySNMPRequest();
			requestDto.setAuthPass(authPassword);
			requestDto.setAuthProtocol(authProtocol);
			requestDto.setCommunity(community);
			requestDto.setFacilityID(facilityID);
			requestDto.setIpAddress(ipAddress);
			requestDto.setPort(port);
			requestDto.setPrivPass(privPassword);
			requestDto.setPrivProtocol(privProtocol);
			requestDto.setSecurityLevel(securityLevel);
			requestDto.setUser(user);

			switch (version) {
			case SnmpVersionConstant.TYPE_V1:
				requestDto.setVersion(GetNodesBySNMPRequest.VersionEnum.V1);
				break;
			case SnmpVersionConstant.TYPE_V2:
				requestDto.setVersion(GetNodesBySNMPRequest.VersionEnum.V2);
				break;
			case SnmpVersionConstant.TYPE_V3:
				requestDto.setVersion(GetNodesBySNMPRequest.VersionEnum.V3);
				break;
			default:
				// findbugs対応 デフォルト追加
				break;
			}

			NodeInfoDeviceSearchResponse nodeSnmp = wrapper.getNodePropertyBySNMP(requestDto);
			NodeInfoResponse nodeInfo = nodeSnmp.getNodeInfo();
			m_log.info("snmp2 " + nodeInfo.getNodeFilesystemInfo().size());
			NodePropertyUtil.setDefaultNode(nodeInfo);
			return nodeSnmp;
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (SnmpResponseError e) {
			MessageDialog.openWarning(null, Messages.getString("message"),
					Messages.getString("message.snmp.12"));
		} catch (HinemosUnknown e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("GetNodePropertyBySNMP(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return null;
	}

	private void setVersion3Item() {
		if (buttonAuto != null) {
			buttonAuto.dispose();
		}
		empty_label_version = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "emptylabelversion", empty_label_version);

		//セキュリティレベル
		securityLevelText = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "securitylevel", securityLevelText);
		securityLevelText.setText(Messages.getString("snmp.security.level") + " : ");

		securityLevelBox = new Combo(groupAuto, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "securitylevelbox", securityLevelBox);
		securityLevelBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		securityLevelBox.add(SnmpSecurityLevelConstant.NOAUTH_NOPRIV, 0);
		securityLevelBox.add(SnmpSecurityLevelConstant.AUTH_NOPRIV, 1);
		securityLevelBox.add(SnmpSecurityLevelConstant.AUTH_PRIV, 2);
		// デフォルトセット
		propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.SNMP_SECURITY_LEVEL).get(0);
		String securityLevel = propertyChild.getValue().toString();
		if (SnmpSecurityLevelConstant.NOAUTH_NOPRIV.equals(securityLevel)) {
			securityLevelBox.select(0);
		} else if (SnmpSecurityLevelConstant.AUTH_NOPRIV.equals(securityLevel)) {
				securityLevelBox.select(1);
		} else {
			securityLevelBox.select(2);
		}

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		// 改行のため、ダミーのラベルを挿入。
		empty_label_security = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "emptylabelsecurity2", empty_label_security);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		empty_label_security.setLayoutData(gridData);

		//ユーザー名
		userText = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "username", userText);
		userText.setText(Messages.getString("user.name") + " : ");

		userBox = new Text(groupAuto, SWT.BORDER | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "userbox", userBox);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		userBox.setLayoutData(gridData);
		propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.SNMP_USER).get(0);
		String userName = propertyChild.getValue().toString();
		if ("".equals(userName)) {
			userBox.setText("");
		} else {
			userBox.setText(userName);
		}

		// 改行のため、ダミーのラベルを挿入。
		empty_label_user = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "emptylabeluser3", empty_label_user);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		empty_label_user.setLayoutData(gridData);

		//認証パスワード
		authPassText = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "authpassword", authPassText);
		authPassText.setText(Messages.getString("snmp.auth.password") + " : ");

		authPassBox = new Text(groupAuto, SWT.BORDER | SWT.SINGLE);
		authPassBox.setEchoChar('*');
		WidgetTestUtil.setTestId(this, "authpassbox", authPassBox);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		authPassBox.setLayoutData(gridData);
		propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.SNMP_AUTH_PASSWORD).get(0);
		String authPassword = propertyChild.getValue().toString();
		if ("".equals(authPassword)) {
			authPassBox.setText("");
		} else {
			authPassBox.setText(authPassword);
		}

		//認証プロトコル
		authProtocolText = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "authprotocol", authProtocolText);
		authProtocolText.setText(Messages.getString("snmp.auth.protocol") + " : ");

		authProtocolBox = new Combo(groupAuto, SWT.DROP_DOWN | SWT.READ_ONLY);
		authProtocolBox.setSize(10, 30);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.minimumWidth = 50;
		authProtocolBox.setLayoutData(gridData);
		WidgetTestUtil.setTestId(this, "authprotocolbox", authProtocolBox);
		authProtocolBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		authProtocolBox.add(SnmpProtocolConstant.MD5, 0);
		authProtocolBox.add(SnmpProtocolConstant.SHA, 1);
		authProtocolBox.add(SnmpProtocolConstant.SHA224, 2);
		authProtocolBox.add(SnmpProtocolConstant.SHA256, 3);
		authProtocolBox.add(SnmpProtocolConstant.SHA384, 4);
		authProtocolBox.add(SnmpProtocolConstant.SHA512, 5);

		// デフォルトセット
		propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.SNMP_AUTH_PROTOCOL).get(0);
		String authProtocol = "";
		if (propertyChild.getValue() != null) {
			authProtocol = propertyChild.getValue().toString();
		}
		switch (authProtocol) {
		case SnmpProtocolConstant.MD5:
			authProtocolBox.select(0);
			break;
		case SnmpProtocolConstant.SHA:
			authProtocolBox.select(1);
			break;
		case SnmpProtocolConstant.SHA224:
			authProtocolBox.select(2);
			break;
		case SnmpProtocolConstant.SHA256:
			authProtocolBox.select(3);
			break;
		case SnmpProtocolConstant.SHA384:
			authProtocolBox.select(4);
			break;
		case SnmpProtocolConstant.SHA512:
			authProtocolBox.select(5);
			break;
		default:
			authProtocolBox.select(0);
			break;
		}

		// 改行のため、ダミーのラベルを挿入。
		empty_label_auth = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "emptylabelauth3", empty_label_auth);

		//暗号化パスワード
		privPassText = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "privpass", privPassText);
		privPassText.setText(Messages.getString("snmp.priv.password") + " : ");

		privPassBox = new Text(groupAuto, SWT.BORDER | SWT.SINGLE);
		privPassBox.setEchoChar('*');
		WidgetTestUtil.setTestId(this, "privpassbox", privPassBox);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		privPassBox.setLayoutData(gridData);
		propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.SNMP_PRIV_PASSWORD).get(0);
		String privPassword = propertyChild.getValue().toString();
		if ("".equals(privPassword)) {
			privPassBox.setText("");
		} else {
			privPassBox.setText(privPassword);
		}

		//暗号化プロトコル
		privProtocolText = new Label(groupAuto, SWT.NONE);
		WidgetTestUtil.setTestId(this, "privprotocol", privProtocolText);
		privProtocolText.setText(Messages.getString("snmp.priv.protocol") + " : ");

		privProtocolBox = new Combo(groupAuto, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "privprotocolbox", privProtocolBox);
		privProtocolBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		privProtocolBox.add(SnmpProtocolConstant.DES, 0);
		privProtocolBox.add(SnmpProtocolConstant.AES, 1);
		privProtocolBox.add(SnmpProtocolConstant.AES192, 2);
		privProtocolBox.add(SnmpProtocolConstant.AES256, 3);
		// デフォルトセット
		propertyChild = (Property)PropertyUtil.getProperty(propertyOld, NodeConstant.SNMP_PRIV_PROTOCOL).get(0);
		String privProtocol = "";
		if (propertyChild.getValue() != null) {
			privProtocol = propertyChild.getValue().toString();
		}
		switch (privProtocol) {
		case SnmpProtocolConstant.DES:
			privProtocolBox.select(0);
			break;
		case SnmpProtocolConstant.AES:
			privProtocolBox.select(1);
			break;
		case SnmpProtocolConstant.AES192:
			privProtocolBox.select(2);
			break;
		case SnmpProtocolConstant.AES256:
			privProtocolBox.select(3);
			break;
		default:
			privProtocolBox.select(0);
			break;
		}

		setAutoButton();
		groupAuto.layout();
		comp.layout();
	}
}
