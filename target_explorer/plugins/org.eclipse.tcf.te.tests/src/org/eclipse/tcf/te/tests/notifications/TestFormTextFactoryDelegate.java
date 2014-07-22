/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.notifications;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.tests.activator.UIPlugin;
import org.eclipse.tcf.te.ui.notifications.delegates.DefaultFormTextFactoryDelegate;

/**
 * Test notification form text factory delegate implementation.
 */
public class TestFormTextFactoryDelegate extends DefaultFormTextFactoryDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.notifications.delegates.DefaultFormTextFactoryDelegate#getImage(java.lang.String)
	 */
	@Override
	protected Image getImage(String key) {
		Assert.isNotNull(key);
	    return UIPlugin.getImage(key);
	}
}
