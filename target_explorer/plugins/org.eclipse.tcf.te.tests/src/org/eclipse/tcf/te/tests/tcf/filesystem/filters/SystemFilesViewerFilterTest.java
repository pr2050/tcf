/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.filters;

import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.tcf.filesystem.ui.filters.SystemFilesViewerFilter;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;

public class SystemFilesViewerFilterTest extends FSPeerTestCase {
	public void testSelect() {
		if(Host.isWindowsHost()) {
			SystemFilesViewerFilter filter = new SystemFilesViewerFilter();
			assertTrue(filter.select(null, testFolder, testFile));
		}
	}
}
