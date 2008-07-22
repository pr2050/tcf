/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IExpressions;

public class TCFChildrenLocalVariables extends TCFChildren {

    private final TCFNode node;

    TCFChildrenLocalVariables(TCFNode node) {
        super(node.model.getLaunch().getChannel(), 128);
        this.node = node;
    }

    void onSuspended() {
        for (TCFNode n : getNodes()) ((TCFNodeLocalVariable)n).onSuspended();
        reset();
    }

    @Override
    protected boolean startDataRetrieval() {
        IExpressions exps = node.model.getLaunch().getService(IExpressions.class);
        if (exps == null) {
            set(null, null, new HashMap<String,TCFNode>());
            return true;
        }
        assert command == null;
        command = exps.getChildren(node.id, new IExpressions.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                Map<String,TCFNode> data = null;
                if (command == token && error == null) {
                    data = new HashMap<String,TCFNode>();
                    for (String id : contexts) {
                        TCFNode n = node.model.getNode(id);
                        if (n == null) n = new TCFNodeLocalVariable(node, id);
                        assert n.parent == node;
                        data.put(id, n);
                    }
                }
                set(token, error, data);
            }
        });
        return false;
    }
}
