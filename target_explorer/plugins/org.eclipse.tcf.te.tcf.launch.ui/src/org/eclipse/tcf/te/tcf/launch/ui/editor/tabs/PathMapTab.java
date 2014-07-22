/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.ui.editor.tabs;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.internal.debug.ui.launch.TCFPathMapTab;
import org.eclipse.tcf.services.IPathMap;
import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.tcf.core.interfaces.IPathMapGeneratorService;
import org.eclipse.tcf.te.tcf.core.interfaces.IPathMapService;
import org.eclipse.tcf.te.tcf.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.launch.ui.editor.AbstractTcfLaunchTabContainerEditorPage;
import org.eclipse.tcf.te.tcf.launch.ui.nls.Messages;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;

/**
 * Customized TCF path map launch configuration tab implementation to work better
 * inside an configuration editor tab.
 */
public class PathMapTab extends TCFPathMapTab {
	// Reference to the parent editor page
	private final AbstractTcfLaunchTabContainerEditorPage parentEditorPage;

	/* default */ Button showAutoGeneratedRules;

	/**
     * Constructor
     *
     * @param parentEditorPage The parent editor page. Must not be <code>null</code>.
     */
    public PathMapTab(AbstractTcfLaunchTabContainerEditorPage parentEditorPage) {
    	super();
    	this.parentEditorPage = parentEditorPage;
    }

    /**
     * Returns the parent editor page.
     *
     * @return The parent editor page.
     */
    public final AbstractTcfLaunchTabContainerEditorPage getParentEditorPage() {
    	return parentEditorPage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.internal.debug.ui.launch.TCFPathMapTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
     */
    @Override
    public void initializeFrom(ILaunchConfiguration config) {
        super.initializeFrom(config);

        // Restore the state of the "Show auto-generated path map rules" button
        IDialogSettings settings = getDialogSettings(config);
        if (settings != null) {
        	SWTControlUtil.setSelection(showAutoGeneratedRules, settings.getBoolean("showAutoGeneratedRules")); //$NON-NLS-1$
            // Refresh the viewer
            getViewer().refresh();
        }
    }

    /**
     * Returns the dialog settings to use to store configuration specific dialog settings.
     *
     * @param config The active launch configuration. Must not be <code>null</code>.
     * @return The dialog settings store or <code>null</code>.
     */
    protected IDialogSettings getDialogSettings(ILaunchConfiguration config) {
    	Assert.isNotNull(config);

    	IDialogSettings settings = null;

		// Determine the current context name
	    IModelNode context = LaunchContextsPersistenceDelegate.getFirstLaunchContext(config);
	    if (context instanceof IPeerNode) {
   		    String name = ((IPeerNode)context).getPeer().getName();
   		    if (name != null) {
   		    	// Get the plug-in dialog settings
   		    	settings = UIPlugin.getDefault().getDialogSettings();
   		    	if (settings != null) {
   		    		// Get the page dialog settings
   		    		settings = DialogSettings.getOrCreateSection(settings, PathMapTab.class.getSimpleName());
   		    		if (settings != null) {
   		   	   		    // Get the context specific dialog settings
   		   	   		    settings = DialogSettings.getOrCreateSection(settings, name);
   		    		}
   		    	}
   		    }
	    }

    	return settings;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.debug.ui.launch.TCFPathMapTab#getName()
	 */
	@Override
	public String getName() {
	    return Messages.PathMapEditorPage_name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.debug.ui.launch.TCFPathMapTab#getColumnText(int)
	 */
	@Override
	protected String getColumnText(int column) {
		String text = super.getColumnText(column);
		if (text != null && text.trim().length() > 0) {
			String key = "PathMapEditorPage_column_" + text; //$NON-NLS-1$
			if (Messages.hasString(key))
				text = Messages.getString(key);
			else {
    			key = "PathMapEditorPage_column_" + column; //$NON-NLS-1$
    			if (Messages.hasString(key))
    				text = Messages.getString(key);
			}
		}
	    return text != null ? text : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.debug.ui.launch.TCFPathMapTab#showContextQuery()
	 */
	@Override
	protected boolean showContextQuery() {
	    return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.debug.ui.launch.TCFPathMapTab#createCustomControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createCustomControls(Composite parent) {
		Assert.isNotNull(parent);
	    super.createCustomControls(parent);

	    // Add a "[ ] Show all" to the bottom of the page
	    showAutoGeneratedRules = new Button(parent, SWT.CHECK);
	    showAutoGeneratedRules.setText(Messages.PathMapEditorPage_showAll_label);
	    showAutoGeneratedRules.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    showAutoGeneratedRules.addSelectionListener(new SelectionAdapter() {
	    	@SuppressWarnings("synthetic-access")
            @Override
	    	public void widgetSelected(SelectionEvent e) {
	    		if (getViewer() != null) {
		        	// Save the state of the "Show auto-generated path map rules" button
		    		if (getViewer().getInput() instanceof ILaunchConfiguration) {
		    			IDialogSettings settings = getDialogSettings((ILaunchConfiguration)getViewer().getInput());
		    			if (settings != null) {
		    				settings.put("showAutoGeneratedRules", SWTControlUtil.getSelection(showAutoGeneratedRules)); //$NON-NLS-1$
		    			}
		    		}
		        	// Refresh the viewer
		    		getViewer().refresh();
	    		}
	    	}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.debug.ui.launch.TCFPathMapTab#configureTableViewer(org.eclipse.jface.viewers.CheckboxTableViewer)
	 */
	@Override
	protected void configureTableViewer(CheckboxTableViewer viewer) {
		Assert.isNotNull(viewer);
	    super.configureTableViewer(viewer);

	    // Add a filter filtering out the generated mappings if "Show all" is not checked
	    viewer.addFilter(new ViewerFilter() {
	    	@Override
	    	public boolean select(Viewer viewer, Object parentElement, Object element) {
	    		if (showAutoGeneratedRules != null && !showAutoGeneratedRules.isDisposed() && !showAutoGeneratedRules.getSelection() && element instanceof IPathMap.PathMapRule) {
	    			IPathMap.PathMapRule rule = (IPathMap.PathMapRule) element;
	    			return rule.getProperties().get(PROP_GENERATED) == null || !((Boolean)rule.getProperties().get(PROP_GENERATED)).booleanValue();
	    		}
	    	    return true;
	    	}
	    });

	    // Add a filter hiding the host to target path mappings
	    viewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IPathMap.PathMapRule) {
	    			IPathMap.PathMapRule rule = (IPathMap.PathMapRule) element;
	    			return !IPathMapService.PATHMAP_PROTOCOL_HOST_TO_TARGET.equals(rule.getProtocol());
				}
				return true;
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.debug.ui.launch.TCFPathMapTab#initializePathMap(java.util.List, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	protected void initializePathMap(List<IPathMap.PathMapRule> map, ILaunchConfiguration config) {
	    super.initializePathMap(map, config);

	    IModelNode context = LaunchContextsPersistenceDelegate.getFirstLaunchContext(config);
	    if (context instanceof IPeerNode) {
	    	IPeerNode peerNode = (IPeerNode)context;
	    	IPathMapGeneratorService service = ServiceManager.getInstance().getService(peerNode, IPathMapGeneratorService.class);
	    	if (service != null) {
	    		IPathMap.PathMapRule[] rules = service.getPathMap(peerNode);
	    		if (rules != null && rules.length > 0) {
	    			for (IPathMap.PathMapRule rule : rules) {
	    				rule.getProperties().put(PROP_GENERATED, Boolean.TRUE);
	    				map.add(rule);
	    			}
	    		}
	    	}
	    }
	}

    /* (non-Javadoc)
     * @see org.eclipse.tcf.internal.debug.ui.launch.TCFPathMapTab#updateLaunchConfigurationDialog()
     */
	@Override
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
		if (parentEditorPage != null) {
			performApply(AbstractTcfLaunchTabContainerEditorPage.getLaunchConfig(parentEditorPage.getPeerModel(parentEditorPage.getEditorInput())));
			parentEditorPage.checkLaunchConfigDirty();
		}
	}
}
