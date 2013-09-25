/*******************************************************************************
 * Copyright (c) 2012, 2013 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
import org.eclipse.tcf.te.tcf.processes.core.model.steps.AttachStep;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Attach to process command handler implementation.
 */
public class AttachHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					execute(event, (IStructuredSelection)selection);
				}
			};

			Protocol.invokeLater(runnable);
		}

		return null;
	}

	/**
	 * Executes the attach for the given selection.
	 * <p>
	 * <b>Note:</b> This method must be called from within the TCF dispatch thread.
	 *
	 * @param event The execution event.
	 * @param selection The selection. Must not be <code>null</code>.
	 */
	protected void execute(final ExecutionEvent event, final IStructuredSelection selection) {
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(selection);

		// Analyze the selection and collect all nodes per parent peer model node.
		final Map<IPeerModel, List<IProcessContextNode>> contexts = new HashMap<IPeerModel, List<IProcessContextNode>>();
		Iterator<?> iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object candidate = iterator.next();
			if (candidate instanceof IProcessContextNode) {
				IProcessContextNode node = (IProcessContextNode)candidate;
				IPeerModel peerModel = (IPeerModel)node.getAdapter(IPeerModel.class);
				if (peerModel != null) {
					List<IProcessContextNode> nodes = contexts.get(peerModel);
					if (nodes == null) {
						nodes = new ArrayList<IProcessContextNode>();
						contexts.put(peerModel, nodes);
					}
					if (!nodes.contains(node)) nodes.add(node);
				}
			}
		}

		// If not empty, attach to all nodes of the original selection per parent peer model node.
		if (!contexts.isEmpty()) {
			for (Entry<IPeerModel, List<IProcessContextNode>> entry : contexts.entrySet()) {
				IPeerModel peerModel = entry.getKey();
				List<IProcessContextNode> nodes = entry.getValue();
				doAttach(event, peerModel, nodes.toArray(new IProcessContextNode[nodes.size()]));
			}
		}
	}

	/**
	 * Executes the attach to the given process context nodes.
	 * <p>
	 * <b>Note:</b> This method must be called from within the TCF dispatch thread.
	 *
	 * @param event The execution event.
	 * @param nodes The list of process context nodes. Must not be <code>null</code>.
	 */
	protected void doAttach(final ExecutionEvent event, final IPeerModel peerModel, final IProcessContextNode[] nodes) {
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(peerModel);
		Assert.isNotNull(nodes);

		if (nodes.length > 0) {
			AttachStep step = new AttachStep();
			step.executeAttach(peerModel, nodes, new Callback());
		}
	}
}
