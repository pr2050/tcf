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
package com.windriver.tcf.rse.ui;

import java.util.Map;

import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext;
import org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessImpl;

public class TCFRemoteProcess extends RemoteProcessImpl {

    public TCFRemoteProcess(IRemoteProcessContext context, IHostProcess process) {
        super(context, process);
        assert process != null;
    }
    
    public Object getObject() {
        return _underlyingProcess;
    }

    public Map<String,Object> getProperties() {
        return ((TCFProcessResource)_underlyingProcess).getProperties();
    }

    public String getUserTimePC() {
        return ((TCFProcessResource)_underlyingProcess).getUserTimePC();
    }

    public String getSysTimePC() {
        return ((TCFProcessResource)_underlyingProcess).getSysTimePC();
    }
}
