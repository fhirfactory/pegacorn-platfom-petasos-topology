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
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ConcurrencyModeEnum;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ResilienceModeEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.*;
import net.fhirfactory.pegacorn.petasos.topology.cache.TopologyDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

/**
 * This class WILL do more in the future, but it is for now just a proxy to the
 * TopologyDM.
 */

@ApplicationScoped
public class TopologyIM {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyIM.class);

    @Inject
    TopologyDM topologyDataManager;

    @Transactional
    public void registerNode(NodeElement newNodeElement) {
        LOG.debug(".registerNode(): Entry, newElement --> {}", newNodeElement);
        topologyDataManager.addNode(newNodeElement);
        if (newNodeElement.getContainingElementID() != null) {
            addContainedNodeToNode(newNodeElement.getContainingElementID(), newNodeElement);
        }
    }

    @Transactional
    public void addContainedNodeToNode(FDNToken nodeID, NodeElement containedNode) {
        LOG.debug(".addContainedNodeToNode(), nodeID --> {}, containedNode --> {}", nodeID, containedNode);
        NodeElement containingElement = getNode(nodeID);
        if (containingElement != null) {
            LOG.trace(".addContainedNodeToNode(): Containing Node exists, so add contained node!");
            containingElement.addContainedElement(containedNode.getNodeInstanceID());
        } else {
            LOG.trace(".addContainedNodeToNode(): Containing Node doesn't exist, so the containedNode is actually the Top node!");
        }
    }

    @Transactional
    public void unregisterNode(FDNToken elementID) {
        LOG.debug(".unregisterNode(): Entry, elementID --> {}", elementID);
        topologyDataManager.removeNode(elementID);
    }

    public Set<NodeElement> getNodeSet() {
        LOG.debug(".getNodeSet(): Entry");
        return (topologyDataManager.getNodeSet());
    }

    public NodeElement getNode(FDNToken nodeID) {
        LOG.debug(".getNode(): Entry, nodeID --> {}", nodeID);
        NodeElement retrievedNode = topologyDataManager.getNode(nodeID);
        LOG.debug(".getNode(): Exit, retrievedNode --> {}", retrievedNode);
        return (retrievedNode);
    }

    @Transactional
    public void registerLink(LinkElement newLink) {
        LOG.debug(".registerLink(): Entry, newLink --> {}", newLink);
        topologyDataManager.addLink(newLink);
    }

    @Transactional
    public void unregisterLink(FDNToken linkID) {
        LOG.debug(".unregisterLink(): Entry, linkID --> {}", linkID);
        topologyDataManager.removeLink(linkID);
    }

    public Set<LinkElement> getLinkSet() {
        LOG.debug(".getLinkSet(): Entry");
        return (topologyDataManager.getLinkSet());
    }

    public LinkElement getLink(FDNToken linkID) {
        LOG.debug(".getLink(): Entry, linkID --> {}", linkID);
        return (topologyDataManager.getLink(linkID));
    }

    @Transactional
    public void registerEndpoint(EndpointElement newEndpoint) {
        LOG.debug(".registerLink(): Entry, newEndpoint --> {}", newEndpoint);
        topologyDataManager.addEndpoint(newEndpoint);
    }

    @Transactional
    public void unregisterEndpoint(FDNToken endpointID) {
        LOG.debug(".unregisterLink(): Entry, endpointID --> {}", endpointID);
        topologyDataManager.removeEndpoint(endpointID);
    }

    public Set<EndpointElement> getEndpointSet() {
        LOG.debug(".getEndpointSet(): Entry");
        return (topologyDataManager.getEndpointSet());
    }

    public EndpointElement getEndpoint(FDNToken endpointID) {
        LOG.debug(".getEndpoint(): Entry, endpointID --> {}", endpointID);
        return (topologyDataManager.getEndpoint(endpointID));
    }
    
    @Transactional
    public void setInstanceInPlace(FDNToken nodeID, boolean instantionState){
        LOG.debug(".setInstanceInPlace(): Entry, nodeID --> {}, instantiationState --> {}", nodeID, instantionState);
        NodeElement retrievedNode = topologyDataManager.getNode(nodeID);
        retrievedNode.setInstanceInPlace(instantionState);
        LOG.debug(".setInstanceInPlace(): Exit");
    }


    // Business Methods
    public Map<Integer, FDNToken> getNodesWithMatchinUnqualifiedInstanceName(String serviceModuleInstanceName) {
        LOG.debug(".getNodesWithMatchinUnqualifiedInstanceName(): Entry, serviceModuleInstanceName --> {} ", serviceModuleInstanceName);
        Map<Integer, FDNToken> matchingIDs = topologyDataManager.findNodesWithMatchingUnqualifiedInstanceName(serviceModuleInstanceName);
        return (matchingIDs);
    }
    
    public FDNToken getSolutionID() {
        FDNToken solutionID = topologyDataManager.getSolutionID();
        return (solutionID);
    }

    public ConcurrencyModeEnum getConcurrencyMode(FDNToken nodeID){
        LOG.debug(".getConcurrencyMode(): Entry, nodeID --> {}", nodeID);
        Map<Integer, NodeElement> nodeHierarchy = topologyDataManager.getNodeContainmentHierarchy(nodeID);
        if(nodeHierarchy.isEmpty()){
            LOG.debug(".getConcurrencyMode(): Exit, node hierarchy is empty - returning default mode");
            return(ConcurrencyModeEnum.CONCURRENCY_MODE_STANDALONE);
        }
        int hierarchyHeight = nodeHierarchy.size();
        for(int counter = 0; counter < hierarchyHeight; counter++){
            NodeElement currentElement = nodeHierarchy.get(counter);
            if(currentElement.getConcurrencyMode() != null){
                LOG.debug(".getConcurrencyMode(): Exit, Found mode in hierarchy --> {}", currentElement.getConcurrencyMode() );
                return(currentElement.getConcurrencyMode());
            }
        }
        LOG.debug(".getConcurrencyMode(): Exit, couldn't find anything - so returning default");
        return(ConcurrencyModeEnum.CONCURRENCY_MODE_STANDALONE);
    }

    public ResilienceModeEnum getDeploymentResilienceMode(FDNToken nodeID){
        LOG.debug(".getDeploymentResilienceMode(): Entry, nodeID --> {}", nodeID);
        Map<Integer, NodeElement> nodeHierarchy = topologyDataManager.getNodeContainmentHierarchy(nodeID);
        if(nodeHierarchy.isEmpty()){
            LOG.debug(".getDeploymentResilienceMode(): Exit, node hierarchy is empty - returning default mode");
            return(ResilienceModeEnum.RESILIENCE_MODE_STANDALONE);
        }
        int hierarchyHeight = nodeHierarchy.size();
        for(int counter = 0; counter < hierarchyHeight; counter++){
            NodeElement currentElement = nodeHierarchy.get(counter);
            if(currentElement.getResilienceMode() != null){
                LOG.debug(".getDeploymentResilienceMode(): Exit, Found mode in hierarchy --> {}", currentElement.getResilienceMode() );
                return(currentElement.getResilienceMode());
            }
        }
        LOG.debug(".getConcurrencyMode(): Exit, couldn't find anything - so returning default");
        return(ResilienceModeEnum.RESILIENCE_MODE_STANDALONE);
    }

}
