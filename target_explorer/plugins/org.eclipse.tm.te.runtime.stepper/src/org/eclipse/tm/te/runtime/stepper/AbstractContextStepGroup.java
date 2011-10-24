/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroupIterator;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroupable;

/**
 * Abstract context step group implementation.
 */
public abstract class AbstractContextStepGroup<Data extends Object> extends ExecutableExtension implements IContextStepGroup<Data> {

	private ExecutableExtensionProxy<IContextStepGroupIterator<Data>> iteratorProxy = null;

	/**
	 * Constant to be returned in case the step group contains no steps.
	 */
	protected final static IContextStepGroupable<?>[] NO_STEPS = new IContextStepGroupable<?>[0];

	/**
	 * Constructor.
	 */
	public AbstractContextStepGroup() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup#isLocked()
	 */
	@Override
    public boolean isLocked() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.ExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    super.setInitializationData(config, propertyName, data);

		if (iteratorProxy == null) {
			iteratorProxy = new ExecutableExtensionProxy<IContextStepGroupIterator<Data>>(config) {
				@Override
				protected String getExecutableExtensionAttributeName() {
					return "iterator"; //$NON-NLS-1$
				}
			};
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup#getStepGroupIterator()
	 */
	@Override
    public IContextStepGroupIterator<Data> getStepGroupIterator() {
		return iteratorProxy != null ? iteratorProxy.newInstance() : null;
	}
}
