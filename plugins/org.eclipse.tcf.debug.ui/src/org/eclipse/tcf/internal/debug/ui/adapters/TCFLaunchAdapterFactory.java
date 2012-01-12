/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.internal.debug.ui.Activator;
import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.util.TCFTask;


public class TCFLaunchAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] adapter_list = {
        IElementLabelProvider.class,
        IElementContentProvider.class,
        IModelProxyFactory.class,
        ISuspendTrigger.class,
    };

    private static final IElementLabelProvider launch_label_provider = new TCFLaunchLabelProvider();

    @SuppressWarnings("rawtypes")
    private static Object getModelAdapter(Object from, Class to) {
        TCFLaunch launch = (TCFLaunch)from;
        TCFModel model = Activator.getModelManager().getModel(launch);
        if (model != null && to.isInstance(model)) return model;
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Object getAdapter(final Object from, final Class to) {
        if (from instanceof TCFLaunch) {
            if (to == IElementLabelProvider.class) return launch_label_provider;
            if (Protocol.isDispatchThread()) return getModelAdapter(from, to);
            return new TCFTask<Object>() {
                public void run() {
                    done(getModelAdapter(from, to));
                }
            }.getE();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Class[] getAdapterList() {
        return adapter_list;
    }
}
