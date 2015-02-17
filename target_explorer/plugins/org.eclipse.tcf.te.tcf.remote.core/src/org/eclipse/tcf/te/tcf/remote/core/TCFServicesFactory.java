/*******************************************************************************
 * Copyright (c) 2014, 2015 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.remote.core;

import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.IRemoteServicesDescriptor;
import org.eclipse.remote.core.IRemoteServicesFactory;

public class TCFServicesFactory implements IRemoteServicesFactory {
	@Override
    public IRemoteServices getServices(IRemoteServicesDescriptor descriptor) {
		return new TCFRemoteServices(descriptor);
	}
}