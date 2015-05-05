/*******************************************************************************
 * Copyright (c) 2014, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.interfaces.runtime;

import java.io.File;
import java.util.List;

import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tcf.te.tcf.core.model.interfaces.IModel;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IResultOperation;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProvider;


/**
 * A model dealing with the file system at runtime.
 */
public interface IRuntimeModel extends IModel, IPeerNodeProvider {

	static class Delegate {
		public boolean filterRoot(DirEntry entry) {
			return true;
		}
	}

	/**
	 * Returns the channel of this runtime model
	 */
	public IChannel getChannel();

	/**
	 * Returns the root node of this model.
	 */
    public IFSTreeNode getRoot();

    /**
     * Returns the delegate that is used for customized behavior.
     */
    public Delegate getDelegate();

    /**
     * Returns an operation for restoring nodes from a path
     */
	public IResultOperation<IFSTreeNode> operationRestoreFromPath(String path);

    /**
     * Returns an operation for downloading multiple nodes to a destination
     */
	public IOperation operationDownload(List<IFSTreeNode> nodes, File destination, IConfirmCallback confirmCallback);

    /**
     * Returns an operation for restoring nodes from a path
     */
	public IResultOperation<IFSTreeNode[]> operationRestoreFavorites();
}
