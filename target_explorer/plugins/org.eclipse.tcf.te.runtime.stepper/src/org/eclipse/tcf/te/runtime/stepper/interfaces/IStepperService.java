/*******************************************************************************
 * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.stepper.interfaces;

import org.eclipse.tcf.te.runtime.services.interfaces.IService;

/**
 * Stepper service.
 */
public interface IStepperService extends IService {

	public static final String OPERATION_CONNECT = "connect";  //$NON-NLS-1$
	public static final String OPERATION_DISCONNECT = "disconnect";  //$NON-NLS-1$

	/**
	 * Get the step group id for the given context and operation
	 * or <code>null</code> if this operation is not available.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @param operation The operation. Must not be <code>null</code>.
	 * @return The step group id or <code>null</code>.
	 */
	public String getStepGroupId(Object context, String operation);


	/**
	 * Get the step group name for the given context and operation
	 * or <code>null</code> if this operation is not available.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @param operation The operation. Must not be <code>null</code>.
	 * @return The step group name or <code>null</code>.
	 */
	public String getStepGroupName(Object context, String operation);

	/**
	 * Get the step context for the given context and operation
	 * or <code>null</code> if this operation is not available.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @param operation The operation. Must not be <code>null</code>.
	 * @return The step context or <code>null</code>.
	 */
	public IStepContext getStepContext(Object context, String operation);

	/**
	 * Get the enabled state for the given operation.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @param operation The operation. Must not be <code>null</code>.
	 * @return <code>true</code> if the operation is enabled.
	 */
	public boolean isEnabled(Object context, String operation);
}