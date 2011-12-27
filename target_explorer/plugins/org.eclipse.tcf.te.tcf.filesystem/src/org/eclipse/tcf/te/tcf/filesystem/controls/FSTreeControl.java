/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River) - [345384] Provide property pages for remote file system nodes
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.controls;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tcf.te.tcf.filesystem.interfaces.IUIConstants;
import org.eclipse.tcf.te.tcf.filesystem.internal.celleditor.FSViewerCellEditorFactory;
import org.eclipse.tcf.te.tcf.filesystem.internal.dnd.FSDragSourceListener;
import org.eclipse.tcf.te.tcf.filesystem.internal.dnd.FSDropTargetListener;
import org.eclipse.tcf.te.ui.trees.AbstractTreeControl;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.part.MultiPageSelectionProvider;


/**
 * File system browser control.
 */
public class FSTreeControl extends AbstractTreeControl implements ISelectionChangedListener, FocusListener {

	/**
	 * Constructor.
	 */
	public FSTreeControl() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param parentPart The parent workbench part this control is embedded in or <code>null</code>.
	 */
	public FSTreeControl(IWorkbenchPart parentPart) {
		super(parentPart);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#doCreateTreeViewer(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    protected TreeViewer doCreateTreeViewer(Composite parent) {
		Assert.isNotNull(parent);
		// Override the parent method to create a multiple-selection tree.
		return new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		super.configureTreeViewer(viewer);

		Tree tree = viewer.getTree();
		tree.addFocusListener(this);
		//Add DnD support.
	    int operations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transferTypes = {LocalSelectionTransfer.getTransfer()};
		viewer.addDragSupport(operations, transferTypes, new FSDragSourceListener(viewer));
		viewer.addDropSupport(operations, transferTypes, new FSDropTargetListener(viewer));
		// Add editing support to rename files/folders.
		new FSViewerCellEditorFactory().addEditingSupport(viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#doCreateTreeViewerContentProvider(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ITreeContentProvider doCreateTreeViewerContentProvider(TreeViewer viewer) {
		return new FSTreeContentProvider(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#doCreateTreeViewerSelectionChangedListener(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ISelectionChangedListener doCreateTreeViewerSelectionChangedListener(TreeViewer viewer) {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#getAutoExpandLevel()
	 */
	@Override
	protected int getAutoExpandLevel() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#getContextMenuId()
	 */
	@Override
	protected String getContextMenuId() {
		return IUIConstants.ID_TREE_VIEWER_FS_CONTEXT_MENU;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		propagateSelection();
	}

	/**
	 * Propagate the current selection to the editor's selection provider.
	 */
	private void propagateSelection() {
	    IWorkbenchPart parent = getParentPart();
		if (parent != null) {
			IWorkbenchPartSite site = parent.getSite();
			if (site != null) {
				ISelection selection = getViewer().getSelection();
				ISelectionProvider selectionProvider = site.getSelectionProvider();
				selectionProvider.setSelection(selection);
				if (selectionProvider instanceof MultiPageSelectionProvider) {
					SelectionChangedEvent changedEvent = new SelectionChangedEvent(selectionProvider, selection);
					((MultiPageSelectionProvider) selectionProvider).firePostSelectionChanged(changedEvent);
				}
			}
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(final DoubleClickEvent event) {
		// If an handled and enabled command is registered for the ICommonActionConstants.OPEN
		// retargetable action id, redirect the double click handling to the command handler.
		//
		// Note: The default tree node expansion must be re-implemented in the active handler!
		ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command command = service != null ? service.getCommand(ICommonActionConstants.OPEN) : null;
		if (command != null && command.isDefined() && command.isEnabled()) {
			SafeRunner.run(new SafeRunnable(){
				@Override
                public void run() throws Exception {
					ISelection selection = event.getSelection();
					EvaluationContext ctx = new EvaluationContext(null, selection);
					ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
					ctx.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
					ctx.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					IWorkbenchPart part = getParentPart();
					if (part != null) {
						IWorkbenchPartSite site = part.getSite();
						ctx.addVariable(ISources.ACTIVE_PART_ID_NAME, site.getId());
						ctx.addVariable(ISources.ACTIVE_PART_NAME, part);
						ctx.addVariable(ISources.ACTIVE_SITE_NAME, site);
						ctx.addVariable(ISources.ACTIVE_SHELL_NAME, site.getShell());
					}
					ExecutionEvent executionEvent = new ExecutionEvent(command, Collections.EMPTY_MAP, part, ctx);
					command.executeWithChecks(executionEvent);
                }});
		} else {
			super.doubleClick(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
    public void focusGained(FocusEvent e) {
		propagateSelection();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
    public void focusLost(FocusEvent e) {
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getHelpId()
	 */
	@Override
    protected String getHelpId() {
		return IUIConstants.ID_TREE_VIEWER_FS_HELP;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getViewerId()
	 */
	@Override
    protected String getViewerId() {
	    return IUIConstants.ID_TREE_VIEWER_FS;
    }
}
