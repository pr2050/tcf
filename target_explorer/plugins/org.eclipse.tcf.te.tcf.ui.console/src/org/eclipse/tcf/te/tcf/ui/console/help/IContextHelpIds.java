/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.console.help;

import org.eclipse.tcf.te.tcf.ui.console.activator.UIPlugin;

/**
 * UI Context help id definitions.
 */
public interface IContextHelpIds {

	/**
	 * UI plug-in common context help id prefix.
	 */
	public final static String PREFIX = UIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$

	/**
	 * Communication monitor console preferences page.
	 */
	public final static String MONITOR_CONSOLE_PREFERENCES_PAGE = PREFIX + "MonitorConsolePreferencesPage"; //$NON-NLS-1$
}
