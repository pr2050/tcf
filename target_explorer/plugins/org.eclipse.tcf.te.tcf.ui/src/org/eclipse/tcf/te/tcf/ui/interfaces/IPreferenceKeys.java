/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.interfaces;

/**
 * Preference key identifiers.
 */
public interface IPreferenceKeys {
	/**
	 * Common prefix for all ui preference keys
	 */
	public final String PREFIX = "te.tcf.ui."; //$NON-NLS-1$

	/**
	 * The maximum number of recent actions shown in the recent action dialog.
	 * Defaults to 20.
	 */
	public final String PREF_MAX_RECENT_ACTION_ENTRIES = PREFIX + "maxRecentActions"; //$NON-NLS-1$
}
