/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.ui.interfaces.preferences.IPreferenceKeys;


/**
 * The bundle's preference initializer implementation.
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

	/**
	 * Constructor.
	 */
	public PreferencesInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// Get the bundles preferences manager
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(UIPlugin.getUniqueIdentifier());
		if (prefs != null) {
			// [Hidden] Editor content contribution: default on
			prefs.putBoolean(IPreferenceKeys.PREF_FEATURE_ENABLE_EDITOR_CONTENT_CONTRIBUTION, true);
		}
		IPreferenceStore preferenceStore = UIPlugin.getDefault().getPreferenceStore();
		preferenceStore.setDefault(PreferencePage.PREF_AUTOSAVING, PreferencePage.DEFAULT_AUTOSAVING);
		preferenceStore.setDefault(PreferencePage.PREF_RENAMING_IN_PLACE_EDITOR, PreferencePage.DEFAULT_RENAMING_IN_PLACE_EDITOR);
		preferenceStore.setDefault(PreferencePage.PREF_COPY_PERMISSION, PreferencePage.DEFAULT_COPY_PERMISSION);
		preferenceStore.setDefault(PreferencePage.PREF_COPY_OWNERSHIP, PreferencePage.DEFAULT_COPY_OWNERSHIP);
	}
}