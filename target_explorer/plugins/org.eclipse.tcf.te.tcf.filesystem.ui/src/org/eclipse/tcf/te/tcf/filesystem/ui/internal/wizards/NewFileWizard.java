/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.wizards;

import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCreate;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCreateFile;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;

/**
 * The wizard to create a new file in the file system of Target Explorer.
 */
public class NewFileWizard extends NewNodeWizard {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.wizards.NewNodeWizard#createWizardPage()
	 */
	@Override
	protected NewNodeWizardPage createWizardPage() {
		return new NewFileWizardPage();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.wizards.NewNodeWizard#getCreateOp(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode, java.lang.String, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	protected OpCreate getCreateOp(FSTreeNode folder, String name) {
		return new OpCreateFile(folder, name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.wizards.NewNodeWizard#getTitle()
	 */
	@Override
	protected String getTitle() {
		return Messages.NewFileWizard_NewFileWizardTitle;
	}
}
