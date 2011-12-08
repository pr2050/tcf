/**
 * INetworkWireType.java
 * Created on Nov 11, 2011
 *
 * Copyright (c) 2011 Wind River Systems, Inc.
 *
 * The right to copy, distribute, modify, or otherwise make use
 * of this software may be licensed only pursuant to the terms
 * of an applicable Wind River license agreement.
 */
package org.eclipse.tcf.te.core.nodes.interfaces.wire;

/**
 * The properties specific to the wire type &quot;serial&quot;.
 */
public interface IWireTypeSerial {

	/**
	 * The data container.
	 */
	public static String PROPERTY_CONTAINER_NAME = "serial"; //$NON-NLS-1$

	/**
	 * The serial device name.
	 */
	public static final String PROPERTY_SERIAL_DEVICE = "device"; //$NON-NLS-1$

	/**
	 * The baud rate.
	 */
	public static final String PROPERTY_SERIAL_BAUD_RATE = "baudrate"; //$NON-NLS-1$

	/**
	 * The data bits
	 */
	public static final String PROPERTY_SERIAL_DATA_BITS = "databits"; //$NON-NLS-1$

	/**
	 * The parity
	 */
	public static final String PROPERTY_SERIAL_PARITY = "parity"; //$NON-NLS-1$

	/**
	 * The stop bits
	 */
	public static final String PROPERTY_SERIAL_STOP_BITS = "stopbits"; //$NON-NLS-1$

	/**
	 * The flow control
	 */
	public static final String PROPERTY_SERIAL_FLOW_CONTROL = "flowcontrol"; //$NON-NLS-1$
}
