/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.core.model;

import java.beans.PropertyChangeEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.core.interfaces.IViewerInput;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.processes.core.callbacks.QueryDoneOpenChannel;
import org.eclipse.tcf.te.tcf.processes.core.callbacks.RefreshChildrenDoneOpenChannel;
import org.eclipse.tcf.te.tcf.processes.core.callbacks.RefreshDoneOpenChannel;

/**
 * The process tree model implementation.
 */
public class ProcessModel {
	/* default */static final String PROCESS_ROOT_KEY = CoreBundleActivator.getUniqueIdentifier() + ".process.root"; //$NON-NLS-1$

	/**
	 * Get the process model stored in the peer model.
	 * If there's no process model yet, create a new process model.
	 *
	 * @param peerModel The target's peer model.
	 * @return The process model representing the process.
	 */
	public static ProcessModel getProcessModel(final IPeerModel peerModel) {
		if (peerModel != null) {
			if (Protocol.isDispatchThread()) {
				ProcessModel model = (ProcessModel) peerModel.getProperty(PROCESS_ROOT_KEY);
				if (model == null) {
					model = new ProcessModel(peerModel);
					peerModel.setProperty(PROCESS_ROOT_KEY, model);
				}
				return model;
			}
			final AtomicReference<ProcessModel> reference = new AtomicReference<ProcessModel>();
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					reference.set(getProcessModel(peerModel));
				}
			});
			return reference.get();
		}
		return null;
	}

	// The root node of the peer model
	private ProcessTreeNode root;
	// The polling interval in seconds. If it is zero, then stop polling periodically.
	/* default */int interval;
	// The timer to schedule polling task.
	/* default */Timer pollingTimer;
	// The flag to indicate if the polling has been stopped.
	/* default */boolean stopped;
	private IPeerModel peerModel;

	/**
	 * Create a File System Model.
	 */
	ProcessModel(IPeerModel peerModel) {
		this.peerModel = peerModel;
		this.stopped = true;
	}

	/**
	 * Get the root node of the peer model.
	 *
	 * @return The root node.
	 */
	public ProcessTreeNode getRoot() {
		return root;
	}

	/**
	 * Set the root node of the peer model.
	 *
	 * @param root The root node
	 */
	public void createRoot(IPeerModel peerModel ) {
		this.root = ProcessTreeNode.createRootNode(peerModel);
	}

	/**
	 * Start the periodical polling.
	 */
	void startPolling() {
	    setStopped(false);
	    pollingTimer = new Timer();
		schedulePolling();
    }

	/**
	 * Set the status of the polling and
	 * fire a property change event.
	 *
	 * @param stopped if the polling should be stopped.
	 */
	void setStopped(boolean stopped) {
		if(this.stopped != stopped) {
			boolean old = this.stopped;
			this.stopped = stopped;
			Boolean oldValue = Boolean.valueOf(old);
			Boolean newValue = Boolean.valueOf(stopped);
			PropertyChangeEvent event = new PropertyChangeEvent(peerModel, "stopped", oldValue, newValue); //$NON-NLS-1$
			IViewerInput viewerInput = (IViewerInput) peerModel.getAdapter(IViewerInput.class);
			viewerInput.firePropertyChange(event);
		}
    }

	/**
	 * Stop the periodical polling.
	 */
	void stopPolling() {
		setStopped(true);
	}

	/**
	 * Schedule the periodical polling.
	 */
	void schedulePolling() {
	    TimerTask pollingTask = new TimerTask(){
			@Override
	        public void run() {
				refresh(new Callback() {
					@Override
					protected void internalDone(Object caller, IStatus status) {
						if (!stopped) {
							schedulePolling();
						}
						else {
							pollingTimer.cancel();
							pollingTimer = null;
						}
					}
				});
	        }};
        pollingTimer.schedule(pollingTask, interval * 1000L);
    }

	/**
	 * Set new interval.
	 *
	 * @param interval The new interval.
	 */
	public void setInterval(int interval) {
		Assert.isTrue(interval >= 0);
		if (this.interval != interval) {
			if(this.interval == 0) {
				this.interval = interval;
				startPolling();
			} else {
				this.interval = interval;
				if(interval == 0) {
					stopPolling();
				}
			}
		}
	}

	/**
	 * Get the current interval.
	 *
	 * @return the current interval.
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * Query the children of the given process context.
	 *
	 * @param parentNode The process context node. Must not be <code>null</code>.
	 */
	public void queryChildren(ProcessTreeNode parentNode) {
		Assert.isNotNull(parentNode);
		parentNode.queryStarted();
		Tcf.getChannelManager().openChannel(parentNode.peerNode.getPeer(), null, new QueryDoneOpenChannel(parentNode));
	}

	/**
	 * Recursively refresh the children of the given process context with a callback, which is
	 * called when whole process is finished.
	 *
	 * @param parentNode The process context node. Must not be <code>null</code>.
	 * @param callback The callback object, or <code>null</code> when callback is not needed.
	 */
	public void refresh(final ProcessTreeNode parentNode, final ICallback callback) {
		Assert.isNotNull(parentNode);
		parentNode.queryStarted();
		Tcf.getChannelManager().openChannel(parentNode.peerNode.getPeer(), null, new RefreshDoneOpenChannel(callback, parentNode));
	}

	/**
	 * Recursively refresh the children of the given process context.
	 *
	 * @param parentNode The process context node. Must not be <code>null</code>.
	 */
	public void refresh(final ProcessTreeNode parentNode) {
		refresh(parentNode, null);
	}

	/**
	 * Recursively refresh the tree from the root node with a callback, which
	 * is called when the whole process is finished.
	 *
	 * @param callback The callback object or <code>null</code> when callback is not needed.
	 */
	public void refresh(ICallback callback) {
		if (this.root.childrenQueried && !this.root.childrenQueryRunning) {
			refresh(this.root, callback);
		}
		else {
			if (callback != null) {
				callback.done(this, Status.OK_STATUS);
			}
		}
	}

	/**
	 * Recursively refresh the tree from the root node.
	 */
	public void refresh() {
		if(this.root.childrenQueried && !this.root.childrenQueryRunning) {
			refresh(this.root, null);
		}
	}

	/**
	 * If the polling has been stopped.
	 *
	 * @return true if it is stopped.
	 */
	public boolean isRefreshStopped() {
	    return stopped;
    }

	/**
	 * Get the peer model associated with this model.
	 *
	 * @return The peer model.
	 */
	IPeerModel getPeerModel() {
		return peerModel;
	}

	/**
	 * Refresh the children without refreshing itself.
	 *
	 * @param parentNode The parent whose children are to be refreshed.
	 */
	public void refreshChildren(ProcessTreeNode parentNode) {
		Assert.isNotNull(parentNode);
		Tcf.getChannelManager().openChannel(parentNode.peerNode.getPeer(), null, new RefreshChildrenDoneOpenChannel(parentNode));
    }
}
