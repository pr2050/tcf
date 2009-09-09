/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * A model proxy represents a model for a specific presentation context and
 * fires deltas to notify listeners of changes in the model.
 */
public class TCFModelProxy extends AbstractModelProxy implements IModelProxy, Runnable {

    private static final int CONTENT_FLAGS =
        IModelDelta.ADDED | IModelDelta.REMOVED |
        IModelDelta.REPLACED | IModelDelta.INSERTED |
        IModelDelta.CONTENT | IModelDelta.STATE;

    private static final TCFNode[] EMPTY_NODE_ARRAY = new TCFNode[0];

    private final TCFModel model;
    private final Map<TCFNode,Integer> node2flags = new HashMap<TCFNode,Integer>();
    private final Map<TCFNode,TCFNode[]> node2children = new HashMap<TCFNode,TCFNode[]>();
    private final Map<TCFNode,ModelDelta> node2delta = new HashMap<TCFNode,ModelDelta>();

    private TCFNode selection;
    private boolean posted;
    private boolean disposed;

    private class ViewerUpdate implements IViewerUpdate {

        IStatus status;

        public Object getElement() {
            return null;
        }

        public TreePath getElementPath() {
            return null;
        }

        public IPresentationContext getPresentationContext() {
            return TCFModelProxy.this.getPresentationContext();
        }

        public Object getViewerInput() {
            return TCFModelProxy.this.getProxyViewer().getInput();
        }

        public void cancel() {
        }

        public void done() {
        }

        public IStatus getStatus() {
            return status;
        }

        public boolean isCanceled() {
            return false;
        }

        public void setStatus(IStatus status) {
            this.status = status;
        }
    }

    private class ChildrenCountUpdate extends ViewerUpdate implements IChildrenCountUpdate {

        int count;

        public void setChildCount(int count) {
            this.count = count;

        }
    }

    private class ChildrenUpdate extends ViewerUpdate implements IChildrenUpdate {

        int length;
        TCFNode[] children;

        void setLength(int length) {
            this.length = length;
            this.children = new TCFNode[length];
        }

        public int getLength() {
            return length;
        }

        public int getOffset() {
            return 0;
        }

        public void setChild(Object child, int offset) {
            children[offset] = (TCFNode)child;
        }
    }

    private final ChildrenCountUpdate children_count_update = new ChildrenCountUpdate();
    private final ChildrenUpdate children_update = new ChildrenUpdate();

    private TCFNode pending_node;

    TCFModelProxy(TCFModel model) {
        this.model = model;
    }

    public void installed(Viewer viewer) {
        super.installed(viewer);
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                assert !disposed;
                IPresentationContext p = getPresentationContext();
                if (p != null) model.onProxyInstalled(p, TCFModelProxy.this);
            }
        });
    }

    public void dispose() {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                assert !disposed;
                IPresentationContext p = getPresentationContext();
                if (p != null) model.onProxyDisposed(p);
                disposed = true;
            }
        });
        super.dispose();
    }

    void addDelta(TCFNode node, int flags) {
        if (flags != 0) {
            Integer delta = node2flags.get(node);
            if (delta != null) {
                node2flags.put(node, delta.intValue() | flags);
            }
            else {
                node2flags.put(node, flags);
            }
            post();
        }
    }

    void setSelection(TCFNode node) {
        selection = node;
        addDelta(node, IModelDelta.REVEAL);
    }

    void post() {
        if (!posted) {
            Protocol.invokeLater(this);
            posted = true;
        }
    }

    Viewer getProxyViewer() {
        return getViewer();
    }

    private TCFNode[] getNodeChildren(TCFNode node) {
        TCFNode[] res = node2children.get(node);
        if (res == null) {
            if (node.disposed) {
                res = EMPTY_NODE_ARRAY;
            }
            else if (!node.getData(children_count_update, null)) {
                pending_node = node;
                res = EMPTY_NODE_ARRAY;
            }
            else {
                children_update.setLength(children_count_update.count);
                if (!node.getData(children_update, null)) {
                    assert false;
                    pending_node = node;
                    res = EMPTY_NODE_ARRAY;
                }
                else {
                    res = children_update.children;
                }
            }
            node2children.put(node, res);
        }
        return res;
    }

    private int getNodeIndex(TCFNode node) {
        TCFNode[] arr = getNodeChildren(node.parent);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == node) return i;
        }
        return -1;
    }

    private ModelDelta makeDelta(ModelDelta root, TCFNode node, int flags) {
        ModelDelta delta = node2delta.get(node);
        if (delta == null) {
            if (node.parent == null) {
                delta = root.addNode(model.getLaunch(), -1, flags, getNodeChildren(node).length);
            }
            else {
                int parent_flags = 0;
                Integer parent_flags_obj = node2flags.get(node.parent);
                if (parent_flags_obj != null) parent_flags = parent_flags_obj;
                if ((flags & ~CONTENT_FLAGS) == 0 && (parent_flags & IModelDelta.CONTENT) != 0) return null;
                ModelDelta parent = makeDelta(root, node.parent, parent_flags);
                if (parent == null) return null;
                delta = parent.addNode(node, getNodeIndex(node), flags, getNodeChildren(node).length);
            }
            node2delta.put(node, delta);
        }
        assert delta.getFlags() == flags;
        return delta;
    }

    public void run() {
        posted = false;
        assert Protocol.isDispatchThread();
        if (disposed) return;
        if (node2flags.isEmpty()) return;
        pending_node = null;
        node2children.clear();
        node2delta.clear();
        ModelDelta root = new ModelDelta(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NO_CHANGE);
        for (TCFNode node : node2flags.keySet()) makeDelta(root, node, node2flags.get(node));
        if (pending_node == null) {
            node2flags.clear();
            if (!node2delta.isEmpty()) {
                fireModelChanged(root);
                node2delta.clear();
            }
            if (selection != null) {
                root = new ModelDelta(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NO_CHANGE);
                makeDelta(root, selection, IModelDelta.SELECT);
                fireModelChanged(root);
                node2delta.clear();
                selection = null;
            }
        }
        else if (pending_node.getData(children_count_update, this)) {
            assert false;
            post();
        }
        else {
            posted = true;
        }
        node2children.clear();
    }
}
