/*
 * Copyright (c) 2020 Mark A. Hunter (ACT Health)
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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ResilienceModeEnum;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ConcurrencyModeEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.*;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPIdentifier;
import net.fhirfactory.pegacorn.petasos.topology.loader.TopologySynchronisationServer;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a Proxy to the Topology IM with some key (business function) capabilities associated
 * with the management and support of the Service Modules and Work Unit Processors.
 *
 * @author Mark A. Hunter
 * @since 2020-07-01
 *
 */
@ApplicationScoped
public class ServiceModuleTopologyProxy {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceModuleTopologyProxy.class);

    @Inject
    TopologyIM topologyManager;
    
    @Inject
    TopologySynchronisationServer topologySyncServer;
    
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
     * @param serviceModuleInstanceName The unqualified (but unique) Service Module name
     * @return the FDNToken Instance ID of the Service Module.
     */
    public NodeElementIdentifier getServiceModuleInstanceID(String serviceModuleInstanceName, String version) {
        LOG.debug(".getServiceModuleInstanceID(): Entry, serviceModuleTypeName --> {}, version --> {}", serviceModuleInstanceName, version);
        topologySyncServer.initialise();
        if (serviceModuleInstanceName == null || version == null) {
            throw (new IllegalArgumentException("getServiceModuleInstanceID(): serviceModuleInstanceName is null"));
        }
        Map<Integer, NodeElementIdentifier> nodeSet = topologyManager.getNodesWithMatchinUnqualifiedInstanceName(serviceModuleInstanceName);
        if (nodeSet.isEmpty()) {
            throw (new IllegalArgumentException("getServiceModuleInstanceID(): Not such serviceModuleInstanceName in Topology Map"));
        }
        NodeElementIdentifier instanceID = null;
        int nodeSetSize = nodeSet.size();
        boolean instanceFound = false;
        for (int counter = 0; counter < nodeSetSize; counter++) {
            NodeElementIdentifier currentInstanceID = nodeSet.get(counter);
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

    /**
     * This function extracts the appropriate Work Unit Processor (WUP) Instance ID (FDNToken) from the Topology Cache -
     * base on the context of the invoking WUP (derived within the context of the wildfly instance), the unqualified
     * name of the WUP (which needs to be unique within the context of the Service Module / wildfly instance) and the
     * Version number.
     *
     * @param wupInstanceName A (Service Module) unique name for a the Work Unit Processor (WUP)
     * @param version The version number of the Work Unit Processor (WUP)
     * @return The (solution-wide) unique Identifier (FDNToken) for the Work Unit Processor (WUP)
     */
    public WUPIdentifier getWUPIdentifier(String wupInstanceName, String version) {
        LOG.debug(".getWUPInstanceID(): Entry, serviceModuleTypeName --> {}, version --> {}", wupInstanceName, version);
        topologySyncServer.initialise();
        if (wupInstanceName == null) {
            throw (new IllegalArgumentException("getWUPInstanceID(): serviceModuleInstanceName is null"));
        }
        Map<Integer, NodeElementIdentifier> nodeSet = topologyManager.getNodesWithMatchinUnqualifiedInstanceName(wupInstanceName);
        LOG.trace(".getWUPInstanceID(): Retrieved candidate nodes from Topology Manager, size --> {}", nodeSet.size());
        if (nodeSet.isEmpty()) {
            throw (new IllegalArgumentException("getWUPInstanceID(): Not such serviceModuleInstanceName in Topology Map"));
        }
        LOG.trace(".getWUPInstanceID(): Let's now go through candidate list and see if there is a version that matches our desired value");
        WUPIdentifier instanceID = null;
        int nodeSetSize = nodeSet.size();
        boolean instanceFound = false;
        for (int counter = 0; counter < nodeSetSize; counter++) {
            NodeElementIdentifier currentInstanceID = nodeSet.get(counter);
            FDN currentInstanceFDN = new FDN(currentInstanceID);
            LOG.trace(".getWUPInstanceID(): Current candidate --> {}", currentInstanceFDN.getUnqualifiedRDN().getNameValue());
            String currentInstanceFDNQualifier = currentInstanceFDN.getUnqualifiedRDN().getNameQualifier();
            boolean isOfTypeWUP = currentInstanceFDNQualifier.contentEquals(NodeElementTypeEnum.WUP.getNodeElementType());
            if (isOfTypeWUP) {
                NodeElementIdentifier nodeID = new NodeElementIdentifier(currentInstanceID);
                NodeElement matchingElement = topologyManager.getNode(nodeID);
                LOG.trace(".getWUPInstanceID(): the current candidate element is a WUP, its version is --> {}",matchingElement.getVersion() );
                boolean isRightVersion = matchingElement.getVersion().contentEquals(version);
                if (isRightVersion) {
                    LOG.trace(".getWUPInstanceID(): the versions match!");
                    instanceFound = true;
                    instanceID = new WUPIdentifier(currentInstanceID);
                    break;
                }
            }
        }
        if (!instanceFound) {
            throw (new IllegalArgumentException("getWUPInstanceID(): Not such serviceModuleInstanceName (" + wupInstanceName + "), version (" + version + ") in Topology Map"));
        }
        LOG.debug(".getWUPInstanceID(): Exit, instanceID --> {}", instanceID);
        return (instanceID);
    }

    /**
     * This function pulls the NodeElement from the Topology Cache (via the TopologyIM --> TopologyDM) for the specified
     * ServiceModule and then constructs a new NodeElementFunctionToken with the Function ID (FDNToken) and Version
     * from the NodeElement.
     *
     * @param serviceModuleID The unique (FDNToken) of the ServiceModule
     * @return An unambiguous descriptor of the functional processing capability of the ServiceModule (NodeElementFunctionToken).
     */
    public NodeElementFunctionToken getServiceModuleFunctionToken(NodeElementIdentifier serviceModuleID) {
        LOG.debug(".getServiceModuleFunctionID(): Entry, serviceModuleID --> {}", serviceModuleID);
        NodeElement node = topologyManager.getNode(serviceModuleID);
        NodeElementFunctionToken functionToken = new NodeElementFunctionToken();
        functionToken.setFunctionID(node.getNodeFunctionID());
        functionToken.setVersion(node.getVersion());
        LOG.debug(".getServiceModuleFunctionID(): Exit, functionToken --> {}", functionToken);
        return (functionToken);
    }

    /**
     * This function pulls the NodeElement from the Topology Cache (via the TopologyIM <--> TopologyDM) for the specified
     * Work Unit Processor (WUP) and then constructs a new NodeElementFunctionToken with the Function ID (FDNToken) and Version
     * from the NodeElement.
     *
     * @param wupID The unique (FDNToken) of the Work Unit Processor (WUP)
     * @return An unambiguous descriptor of the functional processing capability of the WUP (NodeElementFunctionToken).
     */
    public NodeElementFunctionToken getWUPFunctionToken(WUPIdentifier wupID) {
        LOG.debug(".getWUPFunctionID(): Entry, wupID --> {}", wupID);
        NodeElementIdentifier nodeID = new NodeElementIdentifier(wupID);
        NodeElement node = topologyManager.getNode(nodeID);
        NodeElementFunctionToken functionToken = new NodeElementFunctionToken();
        FDNToken functionID = node.getNodeFunctionID();
        LOG.trace(".getWUPFunctionToken(): functionID --> {}", functionID);
        String version = node.getVersion();
        LOG.trace(".getWUPFunctionToken(): version --> {}", version);
        functionToken.setVersion(version);
        functionToken.setFunctionID(functionID);
        LOG.debug(".getWUPFunctionID(): Exit, functionToken --> {}", functionToken);
        return (functionToken);
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

    //
    // Passthrough Calls straight into the Topology Server (TopologyIM)
    //

    public void registerNode(NodeElement newNodeElement) {
        LOG.debug(".registerNode(): Entry, newElement --> {}", newNodeElement);
        topologyManager.registerNode(newNodeElement);
        LOG.debug(".registerNode(): Exit, newElement --> {}", newNodeElement);
    }

    public void addContainedNodeToNode(NodeElementIdentifier nodeID, NodeElement containedNode) {
        LOG.debug(".addContainedNodeToNode(): Entry, nodeID --> {}, containedNode --> {}", nodeID, containedNode);
        topologyManager.addContainedNodeToNode(nodeID, containedNode);
        LOG.debug(".addContainedNodeToNode(): Exit");
    }

    public void unregisterNode(NodeElementIdentifier elementID) {
        LOG.debug(".unregisterNode(): Entry, elementID --> {}", elementID);
        topologyManager.unregisterNode(elementID);
        LOG.debug(".unregisterNode(): Exit");
    }

    public Set<NodeElement> getNodeSet() {
        LOG.debug(".getNodeSet(): Entry");
        Set<NodeElement> returnedNodeSet = topologyManager.getNodeSet();
        LOG.debug(".getNodeSet(): Entry, returnedNodeSet --> {}", returnedNodeSet);
        return (returnedNodeSet);
    }

    public NodeElement getNode(NodeElementIdentifier nodeID) {
        LOG.debug(".getNode(): Entry, nodeID --> {}", nodeID);
        NodeElement retrievedNode = topologyManager.getNode(nodeID);
        LOG.debug(".getNode(): Exit, retrievedNode --> {}", retrievedNode);
        return (retrievedNode);
    }
    
    public NodeElement getNodeByKey(String nodeKey) {
        LOG.debug(".getNodeByKey(): Entry, nodeKey --> {}", nodeKey);
        NodeElement retrievedNode = topologyManager.getNodeByKey(nodeKey);
        LOG.debug(".getNodeByKey(): Exit, retrievedNode --> {}", retrievedNode);
        return (retrievedNode);   	
    }

    public void registerLink(LinkElement newLink) {
        LOG.debug(".registerLink(): Entry, newLink --> {}", newLink);
        topologyManager.registerLink(newLink);
        LOG.debug(".unregisterLink(): Exit");
    }

    public void unregisterLink(LinkElementIdentifier linkID) {
        LOG.debug(".unregisterLink(): Entry, linkID --> {}", linkID);
        topologyManager.unregisterLink(linkID);
        LOG.debug(".unregisterLink(): Exit");
    }

    public Set<LinkElement> getLinkSet() {
        LOG.debug(".getLinkSet(): Entry");
        Set<LinkElement> linkSet = topologyManager.getLinkSet();
        LOG.debug(".getLinkSet(): Exit, linkSet --> {}", linkSet);
        return (linkSet);
    }

    public LinkElement getLink(LinkElementIdentifier linkID) {
        LOG.debug(".getLink(): Entry, linkID --> {}", linkID);
        LinkElement link = topologyManager.getLink(linkID);
        LOG.debug(".getLink(): Exit, link --> {}", link);
        return (link);
    }

    public void registerEndpoint(EndpointElement newEndpoint) {
        LOG.debug(".registerEndpoint(): Entry, newEndpoint --> {}", newEndpoint);
        topologyManager.registerEndpoint(newEndpoint);
        LOG.debug(".registerEndpoint(): Entry");
    }

    public void unregisterEndpoint(EndpointElementIdentifier endpointID) {
        LOG.debug(".unregisterEndpoint(): Entry, endpointID --> {}", endpointID);
        topologyManager.unregisterEndpoint(endpointID);
        LOG.debug(".unregisterEndpoint(): Exit");
    }

    public Set<EndpointElement> getEndpointSet() {
        LOG.debug(".getEndpointSet(): Entry");
        Set<EndpointElement> endpointSet = topologyManager.getEndpointSet();
        LOG.debug(".getEndpointSet(): Exit, endpointSet --> {}", endpointSet);
        return (endpointSet);
    }

    public EndpointElement getEndpoint(EndpointElementIdentifier endpointID) {
        LOG.debug(".getEndpoint(): Entry, endpointID --> {}", endpointID);
        if(LOG.isTraceEnabled()){
            endpointListPrint();
        }
        EndpointElement endpoint = topologyManager.getEndpoint(endpointID);
        LOG.debug(".getEndpoint(): Exit, endpoint --> {}", endpoint);
        return (endpoint);
    }

    private void endpointListPrint(){
        LOG.trace(".endpointListPrint(): Entry");
        Set<EndpointElement> endpointSet = topologyManager.getEndpointSet();
        LOG.trace(".endpointListPrint(): Set Size --> {}", endpointSet.size());
        Iterator<EndpointElement> endpointIterator = endpointSet.iterator();
        while(endpointIterator.hasNext()){
            EndpointElement endpoint = endpointIterator.next();
            LOG.trace(".endpointListPrint(): Entry --> {}", endpoint.getEndpointInstanceID());
        }
    }

    //
    // Passthrough Business Methods
    //

    public Map<Integer, NodeElementIdentifier> getNodesWithMatchinUnqualifiedInstanceName(String serviceModuleInstanceName) {
        LOG.debug(".getNodesWithMatchinUnqualifiedInstanceName(): Entry, serviceModuleInstanceName --> {} ", serviceModuleInstanceName);
        Map<Integer, NodeElementIdentifier> matchingIDs = topologyManager.getNodesWithMatchinUnqualifiedInstanceName(serviceModuleInstanceName);
        LOG.debug(".getNodesWithMatchinUnqualifiedInstanceName(): Exit, matchingIDs --> {}", matchingIDs);
        return (matchingIDs);
    }

    public FDNToken getSolutionID() {
        LOG.debug(".getSolutionID(): Entry");
        FDNToken solutionID = topologyManager.getSolutionID();
        LOG.debug(".getSolutionID(): Exit, solutionID --> {}", solutionID);
        return (solutionID);
    }

    public ConcurrencyModeEnum getConcurrencyMode(NodeElementIdentifier nodeID) {
        LOG.debug(".getConcurrencyMode(): Entry, nodeID --> {}", nodeID);
        ConcurrencyModeEnum concurrencyMode = topologyManager.getConcurrencyMode(nodeID);
        LOG.debug(".getConcurrencyMode(): Exit, concurrencyMode --> {}", concurrencyMode);
        return (concurrencyMode);
    }

    public ResilienceModeEnum getDeploymentResilienceMode(NodeElementIdentifier nodeID) {
        LOG.debug(".getDeploymentResilienceMode(): Entry, nodeID --> {}", nodeID);
        ResilienceModeEnum resilienceMode = topologyManager.getDeploymentResilienceMode(nodeID);
        LOG.debug(".getDeploymentResilienceMode(): Exit, resilienceMode --> {}", resilienceMode);
        return (resilienceMode);
    }

    public void setNodeInstantiated(NodeElementIdentifier nodeID, boolean instantionState) {
        LOG.debug(".setInstanceInPlace(): Entry, nodeID --> {}, instantiationState --> {}", nodeID, instantionState);
        NodeElement retrievedNode = topologyManager.getNode(nodeID);
        retrievedNode.setInstanceInPlace(instantionState);
        LOG.debug(".setInstanceInPlace(): Exit");
    }
}
