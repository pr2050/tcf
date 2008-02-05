/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.core.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;

/**
 * Computes the default source lookup path for a TCF launch configuration. The
 * default source lookup path is the folder or project containing the TCF
 * program being launched. If the program is not specified, the workspace is
 * searched by default.
 */
public class TCFSourcePathComputerDelegate implements
        ISourcePathComputerDelegate {

    public ISourceContainer[] computeSourceContainers(
            ILaunchConfiguration configuration, IProgressMonitor monitor)
            throws CoreException {
        ISourceContainer sourceContainer = null;
        /*
         * String path =
         * configuration.getAttribute(IPDAConstants.ATTR_PDA_PROGRAM,
         * (String)null); if (path != null) { IResource resource =
         * ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
         * if (resource != null) { IContainer container = resource.getParent();
         * if (container.getType() == IResource.PROJECT) { sourceContainer = new
         * ProjectSourceContainer((IProject)container, false); } else if
         * (container.getType() == IResource.FOLDER) { sourceContainer = new
         * FolderSourceContainer(container, false); } } }
         */
        if (sourceContainer == null) {
            sourceContainer = new WorkspaceSourceContainer();
        }
        return new ISourceContainer[] { sourceContainer };
    }
}
