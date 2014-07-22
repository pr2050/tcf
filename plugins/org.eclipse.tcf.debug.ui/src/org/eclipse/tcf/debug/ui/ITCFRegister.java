/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.ui;

import org.eclipse.tcf.services.IRegisters;
import org.eclipse.tcf.util.TCFDataCache;

/**
 * ITCFRegister is an interface that is implemented by TCF debug model elements
 * that represent a register on a remote target.
 * A visual element in a debugger view can be adapted to this interface -
 * if the element represents a register.
 *
 * @noimplement
 */
public interface ITCFRegister extends ITCFObject {

    /**
     * Get register properties cache.
     * @return The register properties cache.
     */
    TCFDataCache<IRegisters.RegistersContext> getContext();

    /**
     * Get register children cache.
     * @return The register children cache.
     */
    ITCFChildren getChildren();

    /**
     * Get register value cache.
     * @return The register value cache.
     */
    TCFDataCache<byte[]> getValue();
}
