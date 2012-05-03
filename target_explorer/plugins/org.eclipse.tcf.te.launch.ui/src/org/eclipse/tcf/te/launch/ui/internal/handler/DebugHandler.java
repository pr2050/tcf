/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.handler;

import org.eclipse.debug.core.ILaunchManager;

/**
 * Run launch handler implementation.
 */
public class DebugHandler extends LaunchHandler {

	/**
	 * Constructor.
	 */
	public DebugHandler() {
		super(ILaunchManager.DEBUG_MODE);
	}
}
