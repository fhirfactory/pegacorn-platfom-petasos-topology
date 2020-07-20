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
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ConcurrencyMode;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.resilienceMode;
import net.fhirfactory.pegacorn.petasos.model.topology.*;
import net.fhirfactory.pegacorn.petasos.topology.cache.TopologyDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

/**
 * This class WILL do more in the future, but it is for now just a proxy to the
 * TopologyDM.
 */
@ApplicationScoped
public class TopologyIM {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyIM.class);

    @Inject
    TopologyDM topologyDataManager;

//    @Inject
//    ElementNameExtensions nameExtensions;

    public void registerNode(NodeElement newElement) {
        LOG.debug(".registerNode(): Entry, newElement --> {}", newElement);
        topologyDataManager.addNode(newElement);
        if (newElement.getContainingElementID() != null) {
            addContainedNodeToNode(newElement.getContainingElementID(), newElement);
        }
    }

    public void addContainedNodeToNode(FDNToken nodeID, NodeElement containedNodeID) {
        NodeElement containingElement = getNode(nodeID);
        if (containingElement != null) {
            containingElement.addContainedElement(containedNodeID.getElementInstanceID());
        } else {
            NodeElement newContainingElement = new NodeElement();
            FDN containedElementTypeFDN = new FDN(containedNodeID.getElementFunctionTypeID());
            FDN newContainingElementTypeFDN = containedElementTypeFDN.getParentFDN();
            newContainingElement.setElementFunctionTypeID(newContainingElementTypeFDN.getToken());
            newContainingElement.setElementInstanceID(nodeID);
            newContainingElement.setElementVersion(containedNodeID.getElementVersion());
            newContainingElement.addContainedElement(containedNodeID.getElementInstanceID());
            FDN newContainingElementFDN = new FDN(nodeID);
            if (newContainingElementFDN.getRDNCount() > 1) {
                FDN newContainingContainingElementFDN = new FDN(nodeID).getParentFDN();
                newContainingElement.setContainingElementID(newContainingContainingElementFDN.getToken());
            }
            switch (containedNodeID.getTopologyElementType()) {
                case WUP_INSTANCE:
                case WUP_FUNCTION:
                case WUP_INTERCHANGE_PAYLOAD_TRANSFORMER:
                case WUP_INTERCHANGE_ROUTER:
                case WUP_CONTAINER_INGRES_PROCESSOR:
                case WUP_CONTAINER_INGRES_GATEKEEPER:
                case WUP_CONTAINER_INGRES_CONDUIT:
                case WUP_CONTAINER_EGRESS_CONDUIT:
                case WUP_CONTAINER_EGRESS_PROCESSOR:
                case WUP_CONTAINER_EGRESS_GATEKEEPER:
                    containedNodeID.setTopologyElementType(NodeElementInstanceTypeEnum.PROCESSING_PLANT);
                    break;
                case PROCESSING_PLANT:
                    containedNodeID.setTopologyElementType(NodeElementInstanceTypeEnum.SERVICE_MODULE);
                    break;
                case SERVICE_MODULE:
                    containedNodeID.setTopologyElementType(NodeElementInstanceTypeEnum.APPLICATION_SERVER);
                    break;
                case APPLICATION_SERVER:
                    containedNodeID.setTopologyElementType(NodeElementInstanceTypeEnum.SITE);
                    break;
                case SITE:
                    containedNodeID.setTopologyElementType(NodeElementInstanceTypeEnum.SERVICE);
                    break;
                case SERVICE:
                    containedNodeID.setTopologyElementType(NodeElementInstanceTypeEnum.SUBSYSTEM);
                    break;
                case SUBSYSTEM:
                default:
                    containedNodeID.setTopologyElementType(NodeElementInstanceTypeEnum.SOLUTION);
            }
            registerNode(newContainingElement);
        }
    }

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
        return (topologyDataManager.getNode(nodeID));
    }

    public void registerLink(LinkElement newLink) {
        LOG.debug(".registerLink(): Entry, newLink --> {}", newLink);
        topologyDataManager.addLink(newLink);
    }

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

    public void registerEndpoint(EndpointElement newEndpoint) {
        LOG.debug(".registerLink(): Entry, newEndpoint --> {}", newEndpoint);
        topologyDataManager.addEndpoint(newEndpoint);
    }

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


    // Business Methods
    public FDNToken getServiceModuleContext(String serviceModuleTypeName) {
        Map<Integer, FDNToken> matchingIDs = topologyDataManager.findMatchingNode(serviceModuleTypeName);
        if (matchingIDs.isEmpty()) {
            return (null);
        }
        if (matchingIDs.size() > 1) {
            /* return(null) */ // Being Brave....
        }
        FDNToken moduleContext = matchingIDs.get(0);
        return (moduleContext);
    }

    public FDNToken getSolutionID() {
        FDNToken solutionID = topologyDataManager.getSolutionID();
        return (solutionID);
    }

    public ConcurrencyMode getConcurrencyMode(FDNToken nodeID){
        LOG.debug(".getConcurrencyMode(): Entry, nodeID --> {}", nodeID);
        Map<Integer, NodeElement> nodeHierarchy = topologyDataManager.getNodeContainmentHierarchy(nodeID);
        if(nodeHierarchy.isEmpty()){
            LOG.debug(".getConcurrencyMode(): Exit, node hierarchy is empty - returning default mode");
            return(ConcurrencyMode.CONCURRENCY_MODE_STANDALONE);
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
        return(ConcurrencyMode.CONCURRENCY_MODE_STANDALONE);
    }

    public resilienceMode getDeploymentResilienceMode(FDNToken nodeID){
        LOG.debug(".getDeploymentResilienceMode(): Entry, nodeID --> {}", nodeID);
        Map<Integer, NodeElement> nodeHierarchy = topologyDataManager.getNodeContainmentHierarchy(nodeID);
        if(nodeHierarchy.isEmpty()){
            LOG.debug(".getDeploymentResilienceMode(): Exit, node hierarchy is empty - returning default mode");
            return(resilienceMode.RESILIENCE_MODE_STANDALONE);
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
        return(resilienceMode.RESILIENCE_MODE_STANDALONE);
    }

}
