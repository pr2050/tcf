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
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.services.interfaces.IUIService;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IPendingOperationNode;
import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;
import org.eclipse.tcf.te.tcf.processes.ui.interfaces.IProcessMonitorUIDelegate;
import org.eclipse.tcf.te.tcf.processes.ui.navigator.runtime.AbstractLabelProviderDelegate;

/**
 * The label provider for the tree column "PPID".
 */
public class PPIDLabelProvider extends AbstractLabelProviderDelegate {

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

			String id = ppid.get() >= 0 ? Long.toString(ppid.get()) : ""; //$NON-NLS-1$

			IPeerModel peerModel = (IPeerModel)node.getAdapter(IPeerModel.class);
			IUIService service = peerModel != null ? ServiceManager.getInstance().getService(peerModel, IUIService.class) : null;
			IProcessMonitorUIDelegate delegate = service != null ? service.getDelegate(peerModel, IProcessMonitorUIDelegate.class) : null;

			String newId = delegate != null ? delegate.getText(element, "PPID", id) : null; //$NON-NLS-1$
			return newId != null ? newId : id;
		}

		return super.getText(element);
	}
}
