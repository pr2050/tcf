/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.editor.tree.columns;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IPendingOperationNode;
import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;

/**
 * The label provider for the tree column "PPID".
 */
public class PPIDLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IRuntimeModel || element instanceof IPendingOperationNode) {
			return ""; //$NON-NLS-1$
		}

		if (element instanceof IProcessContextNode) {
			final IProcessContextNode node = (IProcessContextNode)element;

			final AtomicLong ppid = new AtomicLong();

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					ppid.set(node.getSysMonitorContext().getPPID());
				}
			};

			Assert.isTrue(!Protocol.isDispatchThread());
			Protocol.invokeAndWait(runnable);

			return ppid.get() >= 0 ? Long.toString(ppid.get()) : ""; //$NON-NLS-1$
		}

		return super.getText(element);
	}
}