/*******************************************************************************
 * Copyright (c) 2011, 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.tcf.te.core.interfaces.IViewerInput;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.nls.Messages;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * The tree viewer action manager used to provide the following states:
 * 1. The viewers' action persistence.
 * 2. Access the viewers' action.
 */
public class ViewerStateManager {
	// The single instance to provide the management.
	private static volatile ViewerStateManager instance;

	/**
	 * Get the single instance of the manager.
	 *
	 * @return The single instance of the viewer action manager.
	 */
	public static ViewerStateManager getInstance() {
		if (instance == null) {
			instance = new ViewerStateManager();
		}
		return instance;
	}

	// The map to store the viewers' states.
	private Map<String, TreeViewerState> viewerStates;

	/**
	 * Get the viewer action for the specified input id.
	 *
	 * @param inputId
	 * @return
	 */
	public TreeViewerState getViewerState(String inputId) {
		return viewerStates.get(inputId);
	}

	/**
	 * Get the filter descriptor for the specified viewer and input.
	 *
	 * @param viewerId The viewer's id.
	 * @param input The input.
	 * @return The enabled filter descriptors.
	 */
	public FilterDescriptor[] getFilterDescriptors(String viewerId, Object input) {
		if (input != null) {
			TreeViewerExtension viewerExtension = new TreeViewerExtension(viewerId);
			FilterDescriptor[] filterDescriptors = viewerExtension.parseFilters(input);
			if (filterDescriptors != null) {
				IViewerInput viewerInput = getViewerInput(input);
				if(viewerInput != null) {
					String inputId =viewerInput.getInputId();
					inputId = viewerId + "." + inputId; //$NON-NLS-1$
					TreeViewerState viewerState = getViewerState(inputId);
					if (viewerState != null) {
						viewerState.updateFilterDescriptor(filterDescriptors);
					}
				}
				return filterDescriptors;
			}
		}
		return new FilterDescriptor[0];
	}

	/***
	 * Get the viewer input from the input of the tree viewer.
	 * If the input is an instance of IViewerInput, then return
	 * the input. If the input can be adapted to a IViewerInput,
	 * then return the adapted object.
	 *
	 * @param input The input of the tree viewer.
	 * @return A viewer input or null.
	 */
	static IViewerInput getViewerInput(Object input) {
		IViewerInput viewerInput = null;
		if (input != null) {
			if (input instanceof IViewerInput) {
				viewerInput = (IViewerInput) input;
			}
			else {
				if (input instanceof IAdaptable) {
					viewerInput = (IViewerInput) ((IAdaptable) input).getAdapter(IViewerInput.class);
				}
				if (viewerInput == null) {
					viewerInput = (IViewerInput) Platform.getAdapterManager().getAdapter(input, IViewerInput.class);
				}
			}
		}
		return viewerInput;
    }

	/**
	 * Put the viewer action with its input id into the map.
	 *
	 * @param inputId The id of the input.
	 * @param viewerState The viewer's action.
	 */
	public void putViewerState(String inputId, TreeViewerState viewerState) {
		viewerStates.put(inputId, viewerState);
	}

	/**
	 * Load all the viewer states from an external storage. Called by the plugin's
	 * activator before they are used to configure the tree viewers.
	 */
	public void loadViewerStates() {
		viewerStates = Collections.synchronizedMap(new HashMap<String, TreeViewerState>());
		final File stateFile = getViewerStateFile();
		if (stateFile.exists()) {
			SafeRunner.run(new SafeRunnable() {
				@Override
                public void handleException(Throwable e) {
					// Ignore exception
                }
				@Override
				public void run() throws Exception {
					Reader reader = null;
					try {
						reader = new BufferedReader(new InputStreamReader(new FileInputStream(stateFile), "UTF-8")); //$NON-NLS-1$
						XMLMemento root = XMLMemento.createReadRoot(reader);
						loadViewerState(root);
					}
					finally {
						if (reader != null) {
							try {
								reader.close();
							}
							catch (IOException e) {
							}
						}
					}
				}
			});
		}
	}

	/**
	 * Load the viewer states from the memento root.
	 *
	 * @param root The memento's root.
	 */
	void loadViewerState(IMemento root) {
		IMemento[] children = root.getChildren("viewerState"); //$NON-NLS-1$
		if (children != null && children.length > 0) {
			for (IMemento child : children) {
				createViewerState(child);
			}
		}
	}

	/**
	 * Create a viewer action instance using the specified memento element.
	 *
	 * @param mViewerState The memento element.
	 */
	void createViewerState(IMemento mViewerState) {
		String id = mViewerState.getString("id"); //$NON-NLS-1$
		Assert.isNotNull(id);
		TreeViewerState viewerState = new TreeViewerState();
		viewerState.restoreState(mViewerState);
		viewerStates.put(id, viewerState);
	}

	/**
	 * Get the viewer action files. The default location is a file named "viewerstates.xml"
	 * under the plugin's action cache. If it is not available, default it to the ".tcf"
	 * directory under the user's home.
	 *
	 * @return The viewer action file.
	 */
	private File getViewerStateFile() {
		File location;
		try {
			location = UIPlugin.getDefault().getStateLocation().toFile();
		}
		catch (IllegalStateException e) {
			// An RCP workspace-less environment (-data @none)
			location = new File(System.getProperty("user.home"), ".tcf"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// Create the location if it not exist
		if (!location.exists()) {
			final File dir = location;
			SafeRunner.run(new SafeRunnable(){
				@Override
                public void run() throws Exception {
					if (!dir.mkdir()) {
						throw new Exception(Messages.ViewerStateManager_MkdirFailed);
					}
                }});
		}
		location = new File(location, "viewerstates.xml"); //$NON-NLS-1$
		return location;
	}

	/**
	 * Store the the viewer states. Called by the plugin's activator to
	 * save the action data.
	 */
	public void storeViewerStates() {
		final File stateFile = getViewerStateFile();
		final XMLMemento root = XMLMemento.createWriteRoot("viewerStates"); //$NON-NLS-1$
		storeViewerStates(root);
		SafeRunner.run(new SafeRunnable() {
			@Override
            public void handleException(Throwable e) {
				// Ignore exception
            }
			@Override
			public void run() throws Exception {
				Writer writer = null;
				try {
					writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stateFile), "UTF-8")); //$NON-NLS-1$
					root.save(writer);
				}
				finally {
					if (writer != null) {
						try {
							writer.close();
						}
						catch (IOException e) {
						}
					}
				}
			}
		});
	}

	/**
	 * Store the viewer's action to a memento element.
	 *
	 * @param root The memento element.
	 */
	void storeViewerStates(IMemento root) {
		for (String id : viewerStates.keySet()) {
			IMemento mViewerState = root.createChild("viewerState"); //$NON-NLS-1$
			mViewerState.putString("id", id); //$NON-NLS-1$
			TreeViewerState viewerState = viewerStates.get(id);
			viewerState.saveState(mViewerState);
		}
	}

	/**
	 * Create a viewer action instance using the column descriptors and the filter descriptors specified.
	 *
	 * @param columns The column descriptors.
	 * @param filters The filter descriptors.
	 * @return The tree viewer action instance.
	 */
	public static TreeViewerState createViewerState(ColumnDescriptor[] columns, FilterDescriptor[] filters) {
		TreeViewerState viewerState = new TreeViewerState();
		if (columns != null) {
			for (ColumnDescriptor column : columns) {
				viewerState.addColumn(column);
			}
		}
		if (filters != null) {
			for(FilterDescriptor filter : filters) {
				viewerState.addFilter(filter);
			}
		}
		return viewerState;
	}
}
