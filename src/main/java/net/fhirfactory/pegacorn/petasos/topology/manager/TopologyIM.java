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
package net.fhirfactory.pegacorn.petasos.topology.manager;

import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.fhirfactory.pegacorn.petasos.model.topology.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ConcurrencyModeEnum;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ResilienceModeEnum;
import net.fhirfactory.pegacorn.petasos.topology.cache.TopologyDM;

/**
 * This class WILL do more in the future, but it is for now just a proxy to the
 * TopologyDM.
 */
@ApplicationScoped
public class TopologyIM {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyIM.class);

    @Inject
    TopologyDM topologyDataManager;


    public void registerNode(NodeElement newNodeElement) {
        LOG.debug(".registerNode(): Entry, newElement --> {}", newNodeElement);
        topologyDataManager.addNode(newNodeElement);
        if (newNodeElement.getContainingElementID() != null) {
            addContainedNodeToNode(newNodeElement.getContainingElementID(), newNodeElement);
        }
    }

    public void addContainedNodeToNode(NodeElementIdentifier nodeID, NodeElement containedNode) {
        LOG.debug(".addContainedNodeToNode(), nodeID --> {}, containedNode --> {}", nodeID, containedNode);
        NodeElement containingElement = getNode(nodeID);
        if (containingElement != null) {
            LOG.trace(".addContainedNodeToNode(): Containing Node exists, so add contained node!");
            containingElement.addContainedElement(containedNode.getNodeInstanceID());
        } else {
            LOG.trace(".addContainedNodeToNode(): Containing Node doesn't exist, so the containedNode is actually the Top node!");
        }
    }

    public void unregisterNode(NodeElementIdentifier elementID) {
        LOG.debug(".unregisterNode(): Entry, elementID --> {}", elementID);
        topologyDataManager.removeNode(elementID);
    }

    public Set<NodeElement> getNodeSet() {
        LOG.debug(".getNodeSet(): Entry");
        return (topologyDataManager.getNodeSet());
    }

    public NodeElement getNode(NodeElementIdentifier nodeID) {
        LOG.debug(".getNode(): Entry, nodeID --> {}", nodeID);
        NodeElement retrievedNode = topologyDataManager.getNode(nodeID);
        LOG.debug(".getNode(): Exit, retrievedNode --> {}", retrievedNode);
        return (retrievedNode);
    }

    public NodeElement getNode(String nodeName, NodeElementTypeEnum nodeType, String nodeVersion){
        LOG.debug(".getNode(): Entry, nodeName (String) --> {}, nodeType (NodeElementTypeEnum) --> {}, nodeVersion (String) --> {}", nodeName, nodeType, nodeVersion);
        NodeElement retrievedNode = topologyDataManager.getNode(nodeName, nodeType, nodeVersion);
        LOG.debug(".getNode(): Exit, retrievedNode (NodeElement) --> {}", retrievedNode);
        return(retrievedNode);
    }
    
    public NodeElement getNodeByKey(String nodeKey) {
        LOG.debug(".getNodeByKey(): Entry, nodeKey --> {}", nodeKey);
        NodeElement retrievedNode = topologyDataManager.getNodeByKey(nodeKey);
        LOG.debug(".getNodeByKey(): Exit, retrievedNode --> {}", retrievedNode);
        return (retrievedNode);   	
    }

    public void registerLink(LinkElement newLink) {
        LOG.debug(".registerLink(): Entry, newLink --> {}", newLink);
        topologyDataManager.addLink(newLink);
    }

    public void unregisterLink(LinkElementIdentifier linkID) {
        LOG.debug(".unregisterLink(): Entry, linkID --> {}", linkID);
        topologyDataManager.removeLink(linkID);
    }

    public Set<LinkElement> getLinkSet() {
        LOG.debug(".getLinkSet(): Entry");
        return (topologyDataManager.getLinkSet());
    }

    public LinkElement getLink(LinkElementIdentifier linkID) {
        LOG.debug(".getLink(): Entry, linkID --> {}", linkID);
        return (topologyDataManager.getLink(linkID));
    }

    public void registerEndpoint(EndpointElement newEndpoint) {
        LOG.debug(".registerLink(): Entry, newEndpoint --> {}", newEndpoint);
        topologyDataManager.addEndpoint(newEndpoint);
    }

    public void unregisterEndpoint(EndpointElementIdentifier endpointID) {
        LOG.debug(".unregisterLink(): Entry, endpointID --> {}", endpointID);
        topologyDataManager.removeEndpoint(endpointID);
    }

    public Set<EndpointElement> getEndpointSet() {
        LOG.debug(".getEndpointSet(): Entry");
        return (topologyDataManager.getEndpointSet());
    }

    public EndpointElement getEndpoint(EndpointElementIdentifier endpointID) {
        LOG.debug(".getEndpoint(): Entry, endpointID --> {}", endpointID);
        EndpointElement element = topologyDataManager.getEndpoint(endpointID);
        LOG.info(".getEndpoint(): Exit, EndpointElement --> {}", element);
        return (topologyDataManager.getEndpoint(endpointID));
    }

    public void setInstanceInPlace(NodeElementIdentifier nodeID, boolean instantionState) {
        LOG.debug(".setInstanceInPlace(): Entry, nodeID --> {}, instantiationState --> {}", nodeID, instantionState);
        NodeElement retrievedNode = topologyDataManager.getNode(nodeID);
        retrievedNode.setInstanceInPlace(instantionState);
        LOG.debug(".setInstanceInPlace(): Exit");
    }

    // Business Methods
    public Map<Integer, NodeElementIdentifier> getNodesWithMatchinUnqualifiedInstanceName(String serviceModuleInstanceName) {
        LOG.debug(".getNodesWithMatchinUnqualifiedInstanceName(): Entry, serviceModuleInstanceName --> {} ", serviceModuleInstanceName);
        Map<Integer, NodeElementIdentifier> matchingIDs = topologyDataManager.findNodesWithMatchingUnqualifiedInstanceName(serviceModuleInstanceName);
        LOG.debug(".getNodesWithMatchinUnqualifiedInstanceName(): Exit, matchingIDs count --> {}", matchingIDs.size());
        return (matchingIDs);
    }

    public FDNToken getSolutionID() {
        FDNToken solutionID = topologyDataManager.getSolutionID();
        return (solutionID);
    }

    public ConcurrencyModeEnum getConcurrencyMode(NodeElementIdentifier nodeID) {
        LOG.debug(".getConcurrencyMode(): Entry, nodeID --> {}", nodeID);
        NodeElement node = topologyDataManager.getNode(nodeID);
        if(node == null){
            LOG.debug(".getConcurrencyMode(): Exit, couldn't find anything - so returning default");
            return(ConcurrencyModeEnum.CONCURRENCY_MODE_STANDALONE );
        }
        if(node.getConcurrencyMode()== null){
            return(ConcurrencyModeEnum.CONCURRENCY_MODE_STANDALONE );
        }
        LOG.debug(".getConcurrencyMode(): Found Node and it has a ConcurrenceMode parameter, returing");
        return (node.getConcurrencyMode());
    }

    public ResilienceModeEnum getDeploymentResilienceMode(NodeElementIdentifier nodeID) {
        LOG.debug(".getDeploymentResilienceMode(): Entry, nodeID --> {}", nodeID);
        NodeElement node = topologyDataManager.getNode(nodeID);
        if(node == null){
            LOG.debug(".getDeploymentResilienceMode(): Exit, couldn't find anything - so returning default");
            return(ResilienceModeEnum.RESILIENCE_MODE_STANDALONE);
        }
        if(node.getResilienceMode() == null){
            return(ResilienceModeEnum.RESILIENCE_MODE_STANDALONE);
        }
        LOG.debug(".getDeploymentResilienceMode(): Found Node and it has a ResilienceMode parameter, returing");
        return (node.getResilienceMode());
    }

    public EndpointElement getEndpoint(NodeElement node, String endpointName, String endpointVersion){
        LOG.debug(".getEndpoint(): Entry");
        EndpointElement extractedEndpoint = topologyDataManager.getEndpoint(node, endpointName, endpointVersion);
        LOG.debug(".getEndpoint(): Exit");
        return(extractedEndpoint);
    }

}
