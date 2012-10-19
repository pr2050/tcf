/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.listeners;

import org.eclipse.tcf.te.ui.views.expressions.SelectionSourceProvider;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

/**
 * The window listener implementation. Takes care of the
 * management of the global listeners per workbench window.
 */
public class WorkbenchWindowListener implements IWindowListener {
	// The global part listener instance
	private final WorkbenchPartListener partListener = new WorkbenchPartListener();
	// The global perspective listener instance
	private final WorkbenchPerspectiveListener perspectiveListener = new WorkbenchPerspectiveListener();
	// The global selection service source provider
	private final SelectionSourceProvider sourceProvider = new SelectionSourceProvider();

	/**
     * Constructor
     */
    public WorkbenchWindowListener() {
    	// Register the source provider with the evaluation service
		IEvaluationService service = (IEvaluationService)PlatformUI.getWorkbench().getService(IEvaluationService.class);
    	if (service != null) service.addSourceProvider(sourceProvider);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowActivated(IWorkbenchWindow window) {
		sourceProvider.windowActivated(window);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
		sourceProvider.windowDeactivated(window);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowClosed(IWorkbenchWindow window) {
		sourceProvider.windowClosed(window);

		// On close, remove all global listeners from the window
		if (window != null) {
			if (window.getPartService() != null) {
				window.getPartService().removePartListener(partListener);
			}
			window.removePerspectiveListener(perspectiveListener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void windowOpened(IWorkbenchWindow window) {
		sourceProvider.windowOpened(window);

		// On open, register all global listener to the window
		if (window != null) {
			if (window.getPartService() != null) {
				// Get the part service
				IPartService service = window.getPartService();
				// Unregister the part listener, just in case
				service.removePartListener(partListener);
				// Register the part listener
				service.addPartListener(partListener);
				// Signal the active part to the part listener after registration
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IWorkbenchPartReference partRef = page.getActivePartReference();
					if (partRef != null) partListener.partActivated(partRef);
				}
			}

			// Register the perspective listener
			window.addPerspectiveListener(perspectiveListener);
			// Signal the active perspective to the perspective listener after registration
			if (window.getActivePage() != null) {
				perspectiveListener.perspectiveActivated(window.getActivePage(), window.getActivePage().getPerspective());
			}
		}

	}

}
