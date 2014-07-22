/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.services;

import org.eclipse.tcf.te.launch.ui.handler.EditorHandlerDelegate;
import org.eclipse.tcf.te.runtime.services.AbstractService;
import org.eclipse.tcf.te.runtime.services.interfaces.IUIService;
import org.eclipse.tcf.te.ui.interfaces.handler.IEditorHandlerDelegate;

/**
 * UI service implementation.
 */
public class UIService extends AbstractService implements IUIService {
	private final IEditorHandlerDelegate editorHandlerDelegate = new EditorHandlerDelegate();

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.services.interfaces.IUIService#getDelegate(java.lang.Object, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <V> V getDelegate(Object context, Class<? extends V> clazz) {

		if (IEditorHandlerDelegate.class.isAssignableFrom(clazz)) {
			return (V) editorHandlerDelegate;
		}

		return null;
	}

}
