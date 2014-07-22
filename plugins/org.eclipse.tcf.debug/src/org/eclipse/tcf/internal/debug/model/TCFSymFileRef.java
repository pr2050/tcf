/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.model;

import java.math.BigInteger;
import java.util.Map;

public class TCFSymFileRef {
    public String context_id;
    public int address_size;
    public BigInteger address;
    public Map<String,Object> props;
    public Throwable error;

    public String toString() {
        StringBuffer bf = new StringBuffer();
        bf.append('[');
        bf.append(context_id);
        bf.append(',');
        bf.append(address_size);
        bf.append(',');
        bf.append(address);
        bf.append(',');
        bf.append(props.toString());
        bf.append(',');
        bf.append(error);
        bf.append(']');
        return bf.toString();
    }
}
