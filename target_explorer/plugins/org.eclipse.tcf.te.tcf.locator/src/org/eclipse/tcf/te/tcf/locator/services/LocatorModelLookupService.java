/*******************************************************************************
 * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelPeerNodeQueryService;


/**
 * Default locator model lookup service implementation.
 */
public class LocatorModelLookupService extends AbstractLocatorModelService implements ILocatorModelLookupService {

	/**
	 * Constructor.
	 *
	 * @param parentModel The parent locator model instance. Must not be <code>null</code>.
	 */
	public LocatorModelLookupService(ILocatorModel parentModel) {
		super(parentModel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.core.interfaces.services.ILocatorModelLookupService#lkupPeerModelById(java.lang.String)
	 */
	@Override
	public IPeerModel lkupPeerModelById(String id) {
		Assert.isNotNull(id);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		IPeerModel node = null;
		for (IPeerModel candidate : getLocatorModel().getPeers()) {
			IPeer peer = candidate.getPeer();
			if (id.equals(peer.getID())) {
				node = candidate;
				break;
			} else if (peer.getAttributes().get("remote.id.transient") != null //$NON-NLS-1$
							&& peer.getAttributes().get("remote.id.transient").equals(id)) { //$NON-NLS-1$
				node = candidate;
				break;
			}
		}

		return node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupPeerModelById(java.lang.String, java.lang.String)
	 */
	@Override
	public IPeerModel lkupPeerModelById(String parentId, String id) {
		Assert.isNotNull(parentId);
		Assert.isNotNull(id);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		IPeerModel node = null;
		for (IPeerModel candidate : getLocatorModel().getChildren(parentId)) {
			IPeer peer = candidate.getPeer();
			if (id.equals(peer.getID())) {
				node = candidate;
				break;
			} else if (peer.getAttributes().get("remote.id.transient") != null //$NON-NLS-1$
							&& peer.getAttributes().get("remote.id.transient").equals(id)) { //$NON-NLS-1$
				node = candidate;
				break;
			}
		}

		return node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupPeerModelByAgentId(java.lang.String)
	 */
	@Override
	public IPeerModel[] lkupPeerModelByAgentId(String agentId) {
		Assert.isNotNull(agentId);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		List<IPeerModel> nodes = new ArrayList<IPeerModel>();
		for (IPeerModel candidate : getLocatorModel().getPeers()) {
			IPeer peer = candidate.getPeer();
			if (agentId.equals(peer.getAgentID())) {
				nodes.add(candidate);
			}
		}

		return nodes.toArray(new IPeerModel[nodes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupPeerModelByAgentId(java.lang.String, java.lang.String)
	 */
	@Override
	public IPeerModel[] lkupPeerModelByAgentId(String parentId, String agentId) {
		Assert.isNotNull(parentId);
		Assert.isNotNull(agentId);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		List<IPeerModel> nodes = new ArrayList<IPeerModel>();
		for (IPeerModel candidate : getLocatorModel().getChildren(parentId)) {
			IPeer peer = candidate.getPeer();
			if (agentId.equals(peer.getAgentID())) {
				nodes.add(candidate);
			}
		}

		return nodes.toArray(new IPeerModel[nodes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupPeerModelByName(java.lang.String)
	 */
	@Override
	public IPeerModel[] lkupPeerModelByName(String name) {
		Assert.isNotNull(name);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		List<IPeerModel> nodes = new ArrayList<IPeerModel>();
		for (IPeerModel candidate : getLocatorModel().getPeers()) {
			IPeer peer = candidate.getPeer();
			if (name.equals(peer.getName())) {
				nodes.add(candidate);
			}
		}

		return nodes.toArray(new IPeerModel[nodes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupPeerModelBySupportedServices(java.lang.String[], java.lang.String[])
	 */
	@Override
	public IPeerModel[] lkupPeerModelBySupportedServices(String[] expectedLocalServices, String[] expectedRemoteServices) {
		Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		ILocatorModel model = getLocatorModel();
		ILocatorModelPeerNodeQueryService queryService = model.getService(ILocatorModelPeerNodeQueryService.class);

		List<IPeerModel> nodes = new ArrayList<IPeerModel>();
		for (IPeerModel candidate : model.getPeers()) {
			String services = queryService.queryLocalServices(candidate);

			boolean matchesExpectations = true;

			// Ignore the local services if no expectations are set
			if (expectedLocalServices != null && expectedLocalServices.length > 0) {
				if (services != null) {
					for (String service : expectedLocalServices) {
						if (!services.contains(service)) {
							matchesExpectations = false;
							break;
						}
					}
				} else {
					matchesExpectations = false;
				}
			}

			if (!matchesExpectations) continue;

			services = queryService.queryRemoteServices(candidate);

			// Ignore the remote services if no expectations are set
			if (expectedRemoteServices != null && expectedRemoteServices.length > 0) {
				if (services != null) {
					for (String service : expectedRemoteServices) {
						if (!services.contains(service)) {
							matchesExpectations = false;
							break;
						}
					}
				} else {
					matchesExpectations = false;
				}
			}

			if (matchesExpectations) nodes.add(candidate);
		}

		return nodes.toArray(new IPeerModel[nodes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupMatchingStaticPeerModels(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel)
	 */
	@Override
	public IPeerModel[] lkupMatchingStaticPeerModels(IPeerModel peerNode) {
		Assert.isNotNull(peerNode);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		return lkupMatchingStaticPeerModels(peerNode.getPeer());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService#lkupMatchingStaticPeerModels(org.eclipse.tcf.protocol.IPeer)
	 */
	@Override
	public IPeerModel[] lkupMatchingStaticPeerModels(IPeer peer) {
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		List<IPeerModel> nodes = new ArrayList<IPeerModel>();

		if (peer != null) {
			for (IPeerModel candidate : getLocatorModel().getPeers()) {
				// Look only at the static peers here
				if (!candidate.isStatic()) continue;

				// If the agent id is available, match up the agent id first.
				if (candidate.getPeer().getAgentID() != null && candidate.getPeer().getAgentID().equals(peer.getAgentID())) {
					nodes.add(candidate);
					continue;
				}

				// Get the transport types. Transport type must match and must be either "TCP" or "SSL".
				String t1 = peer.getTransportName();
				String t2 = candidate.getPeer().getTransportName();

				if (t1 != null && t1.equals(t2) && ("TCP".equals(t1) || "SSL".equals(t1))) { //$NON-NLS-1$ //$NON-NLS-2$
					// Compare IP and Port. If they match, add the candidate to the result list
					String i1 = peer.getAttributes().get(IPeer.ATTR_IP_HOST);
					String i2 = candidate.getPeer().getAttributes().get(IPeer.ATTR_IP_HOST);
					if (i1 != null && i1.equals(i2)) {
						String p1 = peer.getAttributes().get(IPeer.ATTR_IP_PORT);
						String p2 = candidate.getPeer().getAttributes().get(IPeer.ATTR_IP_PORT);
						if (p1 != null && p1.equals(p2)) {
							nodes.add(candidate);
						}
					}
				}
			}
		}

		return nodes.toArray(new IPeerModel[nodes.size()]);
	}
}
