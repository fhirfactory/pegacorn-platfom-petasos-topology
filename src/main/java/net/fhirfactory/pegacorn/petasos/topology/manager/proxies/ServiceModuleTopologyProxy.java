/*
 * Copyright (c) 2020 MAHun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.petasos.topology.manager.proxies;

import java.util.Map;
import java.util.Set;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ResilienceModeEnum;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ConcurrencyModeEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.LinkElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ServiceModuleTopologyProxy {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceModuleTopologyProxy.class);

    @Inject
    TopologyIM topologyManager;

    private static final Integer WUA_RETRY_LIMIT = 3;
    private static final Integer WUA_TIMEOUT_LIMIT = 10000; // 10 Seconds
    private static final Integer WUA_SLEEP_INTERVAL = 250; // 250 milliseconds
    private static final Integer WUA_CLEAN_UP_AGE_LIMIT = 60 * 1000; // 60 Seconds

    /**
     * This function returns the WUP Instance ID (FDNToken) of the
     * ServiceModule. The assumption is that the Service Module Instance Name
     * (String) is unique within the deployment environment (that is, no other
     * Modules will have the same name - other than the replicated instances of
     * course).
     *
     * @param serviceModuleInstanceName The unqualified (but unique) Service
     * Module name
     * @return the FDNToken Instance ID of the Service Module.
     */
    public FDNToken getServiceModuleInstanceID(String serviceModuleInstanceName, String version) {
        LOG.debug(".getServiceModuleInstanceID(): Entry, serviceModuleTypeName --> {}, version --> {}", serviceModuleInstanceName, version);
        if (serviceModuleInstanceName == null || version == null) {
            throw (new IllegalArgumentException("getServiceModuleInstanceID(): serviceModuleInstanceName is null"));
        }
        Map<Integer, FDNToken> nodeSet = topologyManager.getNodesWithMatchinUnqualifiedInstanceName(serviceModuleInstanceName);
        if (nodeSet.isEmpty()) {
            throw (new IllegalArgumentException("getServiceModuleInstanceID(): Not such serviceModuleInstanceName in Topology Map"));
        }
        FDNToken instanceID = null;
        int nodeSetSize = nodeSet.size();
        boolean instanceFound = false;
        for (int counter = 0; counter < nodeSetSize; counter++) {
            FDNToken currentInstanceID = nodeSet.get(counter);
            FDN currentInstanceFDN = new FDN(currentInstanceID);
            String currentInstanceFDNQualifier = currentInstanceFDN.getUnqualifiedRDN().getNameValue();
            boolean isOfTypeServiceModule = currentInstanceFDNQualifier.contentEquals(NodeElementTypeEnum.SERVICE_MODULE.getNodeElementType());
            if (isOfTypeServiceModule) {
                NodeElement matchingElement = topologyManager.getNode(currentInstanceID);
                boolean isRightVersion = matchingElement.getVersion().contentEquals(version);
                if (isRightVersion) {
                    instanceFound = true;
                    instanceID = currentInstanceID;
                    break;
                }
            }
        }
        if (!instanceFound) {
            throw (new IllegalArgumentException("getServiceModuleInstanceID(): Not such serviceModuleInstanceName (" + serviceModuleInstanceName + "), version (" + version + ") in Topology Map"));
        }
        LOG.debug(".getServiceModuleInstanceID(): Entry, instanceID --> {}", instanceID);
        return (instanceID);
    }

    public FDNToken getWUPInstanceID(String wupInstanceName, String version) {
        LOG.debug(".getWUPInstanceID(): Entry, serviceModuleTypeName --> {}, version --> {}", wupInstanceName, version);
        if (wupInstanceName == null) {
            throw (new IllegalArgumentException("getWUPInstanceID(): serviceModuleInstanceName is null"));
        }
        Map<Integer, FDNToken> nodeSet = topologyManager.getNodesWithMatchinUnqualifiedInstanceName(wupInstanceName);
        if (nodeSet.isEmpty()) {
            throw (new IllegalArgumentException("getWUPInstanceID(): Not such serviceModuleInstanceName in Topology Map"));
        }
        FDNToken instanceID = null;
        int nodeSetSize = nodeSet.size();
        boolean instanceFound = false;
        for (int counter = 0; counter < nodeSetSize; counter++) {
            FDNToken currentInstanceID = nodeSet.get(counter);
            FDN currentInstanceFDN = new FDN(currentInstanceID);
            String currentInstanceFDNQualifier = currentInstanceFDN.getUnqualifiedRDN().getNameValue();
            boolean isOfTypeWUP = currentInstanceFDNQualifier.contentEquals(NodeElementTypeEnum.WUP.getNodeElementType());
            if (isOfTypeWUP) {
                NodeElement matchingElement = topologyManager.getNode(currentInstanceID);
                boolean isRightVersion = matchingElement.getVersion().contentEquals(version);
                if (isRightVersion) {
                    instanceFound = true;
                    instanceID = currentInstanceID;
                    break;
                }
            }
        }
        if (!instanceFound) {
            throw (new IllegalArgumentException("getWUPInstanceID(): Not such serviceModuleInstanceName (" + wupInstanceName + "), version (" + version + ") in Topology Map"));
        }
        LOG.debug(".getWUPInstanceID(): Entry, instanceID --> {}", instanceID);
        return (instanceID);
    }

    public FDNToken getServiceModuleFunctionID(FDNToken serviceModuleID) {
        LOG.debug(".getServiceModuleFunctionID(): Entry, serviceModuleID --> {}", serviceModuleID);
        NodeElement node = topologyManager.getNode(serviceModuleID);
        FDNToken functionID = node.getNodeFunctionID();
        LOG.debug(".getServiceModuleFunctionID(): Exit, functionID --> {}", functionID);
        return (functionID);
    }

    public FDNToken getWUPFunctionID(FDNToken wupID) {
        LOG.debug(".getWUPFunctionID(): Entry, wupID --> {}", wupID);
        NodeElement node = topologyManager.getNode(wupID);
        FDNToken functionID = node.getNodeFunctionID();
        LOG.debug(".getWUPFunctionID(): Exit, functionID --> {}", functionID);
        return (functionID);
    }

    public Integer getWorkUnitActivityRetryLimit() {
        return (WUA_RETRY_LIMIT);
    }

    public Integer getWorkUnitActivityTimeoutLimit() {
        return (WUA_TIMEOUT_LIMIT);
    }

    public Integer getWorkUnitActivitySleepInterval() {
        return (WUA_SLEEP_INTERVAL);
    }

    public Integer getWorkUnitActivityCleanUpAgeLimit() {
        return (WUA_CLEAN_UP_AGE_LIMIT);
    }

    // Passthrough Calls
    @Transactional
    public void registerNode(NodeElement newNodeElement) {
        LOG.debug(".registerNode(): Entry, newElement --> {}", newNodeElement);
        topologyManager.registerNode(newNodeElement);
    }

    @Transactional
    public void addContainedNodeToNode(FDNToken nodeID, NodeElement containedNode) {
        LOG.debug(".addContainedNodeToNode(), nodeID --> {}, containedNode --> {}", nodeID, containedNode);
        topologyManager.addContainedNodeToNode(nodeID, containedNode);
    }

    @Transactional
    public void unregisterNode(FDNToken elementID) {
        LOG.debug(".unregisterNode(): Entry, elementID --> {}", elementID);
        topologyManager.unregisterNode(elementID);
    }

    public Set<NodeElement> getNodeSet() {
        LOG.debug(".getNodeSet(): Entry");
        Set<NodeElement> returnedNodeSet = topologyManager.getNodeSet();
        return (returnedNodeSet);
    }

    public NodeElement getNode(FDNToken nodeID) {
        LOG.debug(".getNode(): Entry, nodeID --> {}", nodeID);
        NodeElement retrievedNode = topologyManager.getNode(nodeID);
        LOG.debug(".getNode(): Exit, retrievedNode --> {}", retrievedNode);
        return (retrievedNode);
    }

    @Transactional
    public void registerLink(LinkElement newLink) {
        LOG.debug(".registerLink(): Entry, newLink --> {}", newLink);
        topologyManager.registerLink(newLink);
        LOG.debug(".unregisterLink(): Exit");
    }

    @Transactional
    public void unregisterLink(FDNToken linkID) {
        LOG.debug(".unregisterLink(): Entry, linkID --> {}", linkID);
        topologyManager.unregisterLink(linkID);
        LOG.debug(".unregisterLink(): Exit");
    }

    public Set<LinkElement> getLinkSet() {
        LOG.debug(".getLinkSet(): Entry");
        Set<LinkElement> linkSet = topologyManager.getLinkSet();
        LOG.debug(".getLinkSet(): Exit");
        return (linkSet);
    }

    public LinkElement getLink(FDNToken linkID) {
        LOG.debug(".getLink(): Entry, linkID --> {}", linkID);
        LinkElement link = topologyManager.getLink(linkID);
        LOG.debug(".getLink(): Exit, link --> {}", link);
        return (link);
    }

    @Transactional
    public void registerEndpoint(EndpointElement newEndpoint) {
        LOG.debug(".registerEndpoint(): Entry, newEndpoint --> {}", newEndpoint);
        topologyManager.registerEndpoint(newEndpoint);
    }

    @Transactional
    public void unregisterEndpoint(FDNToken endpointID) {
        LOG.debug(".unregisterEndpoint(): Entry, endpointID --> {}", endpointID);
        topologyManager.unregisterEndpoint(endpointID);
        LOG.debug(".unregisterEndpoint(): Exit");
    }

    public Set<EndpointElement> getEndpointSet() {
        LOG.debug(".getEndpointSet(): Entry");
        Set<EndpointElement> endpointSet = topologyManager.getEndpointSet();
        LOG.debug(".getEndpointSet(): Exit");
        return (endpointSet);
    }

    public EndpointElement getEndpoint(FDNToken endpointID) {
        LOG.debug(".getEndpoint(): Entry, endpointID --> {}", endpointID);
        EndpointElement endpoint = topologyManager.getEndpoint(endpointID);
        LOG.debug(".getEndpoint(): Exit, endpoint --> {}", endpoint);
        return (endpoint);
    }

    // Business Methods
    public Map<Integer, FDNToken> getNodesWithMatchinUnqualifiedInstanceName(String serviceModuleInstanceName) {
        LOG.debug(".getNodesWithMatchinUnqualifiedInstanceName(): Entry, serviceModuleInstanceName --> {} ", serviceModuleInstanceName);
        Map<Integer, FDNToken> matchingIDs = topologyManager.getNodesWithMatchinUnqualifiedInstanceName(serviceModuleInstanceName);
        return (matchingIDs);
    }

    public FDNToken getSolutionID() {
        FDNToken solutionID = topologyManager.getSolutionID();
        return (solutionID);
    }

    public ConcurrencyModeEnum getConcurrencyMode(FDNToken nodeID) {
        LOG.debug(".getConcurrencyMode(): Entry, nodeID --> {}", nodeID);
        ConcurrencyModeEnum concurrencyMode = topologyManager.getConcurrencyMode(nodeID);
        LOG.debug(".getConcurrencyMode(): Exit, couldn't find anything - so returning default");
        return (concurrencyMode);
    }

    public ResilienceModeEnum getDeploymentResilienceMode(FDNToken nodeID) {
        LOG.debug(".getDeploymentResilienceMode(): Entry, nodeID --> {}", nodeID);
        ResilienceModeEnum resilienceMode = topologyManager.getDeploymentResilienceMode(nodeID);
        LOG.debug(".getDeploymentResilienceMode(): Exit, couldn't find anything - so returning default");
        return (resilienceMode);
    }

    @Transactional
    public void setInstanceInPlace(FDNToken nodeID, boolean instantionState) {
        LOG.debug(".setInstanceInPlace(): Entry, nodeID --> {}, instantiationState --> {}", nodeID, instantionState);
        NodeElement retrievedNode = topologyManager.getNode(nodeID);
        retrievedNode.setInstanceInPlace(instantionState);
        LOG.debug(".setInstanceInPlace(): Exit");
    }

}
