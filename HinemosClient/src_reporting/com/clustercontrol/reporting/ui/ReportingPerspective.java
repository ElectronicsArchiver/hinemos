/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.clustercontrol.ClusterControlPerspectiveBase;
import com.clustercontrol.reporting.view.ReportingScheduleView;
import com.clustercontrol.reporting.view.ReportingTemplateSetView;

/**
 * レポーティングパースペクティブクラス<BR>
 * 
 * @version 5.0.a
 * @since 4.1.2
 */
public class ReportingPerspective extends ClusterControlPerspectiveBase {

	public static final String ID = "com.clustercontrol.enterprise.reporting.ui.ReportingPerspective";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui
	 * .IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);

		float percent = 0.6f;
		// エディタ領域のIDを取得
		String editorArea = layout.getEditorArea();
		// エディタ領域の上部percent%を占めるフォルダを作成
		IFolderLayout top = layout.createFolder("top", IPageLayout.TOP, percent, editorArea);
		// エディタ領域の下部(1-percent)%を占めるフォルダを作成
		IFolderLayout bottom = layout.createFolder( "bottom", IPageLayout.BOTTOM, (IPageLayout.RATIO_MAX - percent), editorArea );

		top.addView(ReportingScheduleView.ID);
		bottom.addView(ReportingTemplateSetView.ID);
	}
}