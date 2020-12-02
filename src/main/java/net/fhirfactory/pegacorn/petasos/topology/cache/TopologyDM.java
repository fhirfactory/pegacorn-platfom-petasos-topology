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
package net.fhirfactory.pegacorn.petasos.topology.cache;

import java.util.*;

import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElementIdentifier;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import net.fhirfactory.pegacorn.petasos.model.topology.LinkElement;
import net.fhirfactory.pegacorn.petasos.model.topology.LinkElementIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;


/**
 * @author Mark A. Hunter
 * @since 2020-07-01
 */
@ApplicationScoped
public class TopologyDM {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyDM.class);

    private NodeElementIdentifier deploymentSolutionName;
    private ConcurrentHashMap<NodeElementIdentifier, NodeElement> nodeSet;
    private ConcurrentHashMap<String, FDNToken> nodeKeySet;
    private ConcurrentHashMap<LinkElementIdentifier, LinkElement> linkSet;
    private ConcurrentHashMap<EndpointElementIdentifier, EndpointElement> endpointSet;

    public TopologyDM() {
        LOG.info(".TopologyDM(): Constructor initialisation");
        this.deploymentSolutionName = null;
        this.nodeSet = new ConcurrentHashMap<NodeElementIdentifier, NodeElement>();
        this.linkSet = new ConcurrentHashMap<LinkElementIdentifier, LinkElement>();
        this.nodeKeySet = new ConcurrentHashMap<String, FDNToken>();
        this.endpointSet = new ConcurrentHashMap<EndpointElementIdentifier, EndpointElement>();
    }

    public NodeElementIdentifier getDeploymentSolutionName() {
        return deploymentSolutionName;
    }

    public void setDeploymentSolutionName(NodeElementIdentifier deploymentSolutionName) {
        this.deploymentSolutionName = deploymentSolutionName;
    }

    /**
     * This function adds an entry to the Element Set.
     * <p>
     * Note that the default behaviour is to UPDATE the values with the set if
     * there already exists an instance for the specified FDNToken (identifier).
     *
     * Note, we have to do a deep inspection of the ConcurrentHashMap key (FDNToken) content,
     * as the default only only looks for equivalence with respect to the action Object instance.
     *
     * @param newElement The NodeElement to be added to the Set
     */
    public void addNode(NodeElement newElement) {
        LOG.debug(".addNode(): Entry, newElement --> {}", newElement);
        if (newElement == null) {
            throw (new IllegalArgumentException(".addNode(): newElement is null"));
        }
        if (newElement.getNodeInstanceID() == null) {
            throw (new IllegalArgumentException(".addNode(): bad elementID within newElement"));
        }
        boolean elementFound = false;
        Enumeration<NodeElementIdentifier> list = this.nodeSet.keys();
        NodeElementIdentifier currentNodeID = null;
        while (list.hasMoreElements()) {
            currentNodeID = list.nextElement();
            if (LOG.isTraceEnabled()) {
                LOG.trace(".getNode(): Cache Entry --> {}", currentNodeID.toFullString());
            }
            if (currentNodeID.equals(newElement)) {
                LOG.trace(".addNode(): Element already in Cache");
                elementFound = true;
                break;
            }
        }
        if (elementFound) {
        	String nodeKey = currentNodeID.toTag() +"." + newElement.getVersion();
            this.nodeSet.put(currentNodeID, newElement);
            this.nodeKeySet.replace(nodeKey, currentNodeID);
        } else {
        	String nodeKey = newElement.getNodeInstanceID().toTag() + "."+newElement.getVersion();
            this.nodeSet.put(newElement.getNodeInstanceID(), newElement);
            this.nodeKeySet.put(nodeKey, newElement.getNodeInstanceID());
        }
    }

    public NodeElement getNode(String nodeName, NodeElementTypeEnum nodeType, String nodeVersion){
        LOG.debug(".getNode(): Entry, nodeName (String) --> {}, nodeType (NodeElementTypeEnum) --> {}, nodeVersion (String) --> {}", nodeName, nodeType, nodeVersion);
        if(nodeName == null || nodeType == null){
            LOG.debug(".getNode(): Exit, either nodeName or nodeType are null");
            return(null);
        }
        Collection<NodeElement> workingNodeSet = this.nodeSet.values();
        for(NodeElement currentNode: workingNodeSet){
            boolean isSameType = currentNode.getNodeArchetype() == nodeType;
            boolean isSameVersion = false;
            if( currentNode.getVersion() != null){
                isSameVersion = currentNode.getVersion().contentEquals(nodeVersion);
            }
            if(isSameType && isSameVersion){
                NodeElementIdentifier currentNodeID = currentNode.getNodeInstanceID();
                FDN nodeFDN = new FDN(currentNodeID);
                String nodeElementUnqualifiedName = nodeFDN.getUnqualifiedRDN().getUnqualifiedValue();
                if(nodeElementUnqualifiedName.contentEquals(nodeName)){
                    LOG.debug(".getNode(): Exit, returning found node (NodeElement) --> {}", currentNode);
                    return(currentNode);
                }
            }
        }
        LOG.debug(".getNode(): Exit, could not find any node matching specify nodeName, nodeType");
        return(null);
    }

    public void removeNode(NodeElementIdentifier elementID) {
        LOG.debug(".removeNode(): Entry, elementID --> {}", elementID);
        if (elementID == null) {
            throw (new IllegalArgumentException(".removeNode(): elementID is null"));
        }
        boolean elementFound = false;
        Enumeration<NodeElementIdentifier> list = this.nodeSet.keys();
        while (list.hasMoreElements()) {
            NodeElementIdentifier currentNodeID = list.nextElement();
            if (LOG.isTraceEnabled()) {
                LOG.trace(".getNode(): Cache Entry --> {}", currentNodeID.toFullString());
            }
            if (currentNodeID.equals(elementID)) {
                LOG.trace(".removeNode(): Element found, now removing it...");
                NodeElement currentElement = this.getNode(currentNodeID);
                String nodeKey = currentNodeID.toString() + "."+currentElement.getVersion();
                this.nodeKeySet.remove(nodeKey);
                this.nodeSet.remove(elementID);
                elementFound = true;
            }
        }
        if(!elementFound){
            LOG.trace(".removeNode(): No element with that elementID is in the map");
        }
        LOG.debug(".removeNode(): Exit");
    }

    public Set<NodeElement> getNodeSet() {
        LOG.debug(".getElementSet(): Entry");
        LinkedHashSet<NodeElement> elementSet = new LinkedHashSet<NodeElement>();
        if (this.nodeSet.isEmpty()) {
            LOG.debug(".getElementSet(): Exit, The module map is empty, returning null");
            return (null);
        }
        elementSet.addAll(this.nodeSet.values());
        if (LOG.isDebugEnabled()) {
            LOG.debug(".getElementSet(): Exit, returning an element set, size --> {}", elementSet.size());
        }
        return (elementSet);
    }

    public NodeElement getNode(NodeElementIdentifier nodeID) {
        LOG.debug(".getNode(): Entry, nodeID --> {}", nodeID);
        if (nodeID == null) {
            LOG.debug(".getNode(): Exit, provided a null nodeID , so returning null");
            return (null);
        }
        Enumeration<NodeElementIdentifier> list = this.nodeSet.keys();
        while (list.hasMoreElements()) {
            FDNToken currentNodeID = list.nextElement();
            if (LOG.isTraceEnabled()) {
                LOG.trace(".getNode(): Cache Entry --> {}", currentNodeID.toFullString());
            }
            if (currentNodeID.equals(nodeID)) {
                LOG.trace(".getNode(): Node found!!! WooHoo!");
                NodeElement retrievedNode = this.nodeSet.get(currentNodeID);
                LOG.debug(".getNode(): Exit, returning Endpoint --> {}", retrievedNode);
                return (retrievedNode);
            }
        }
        LOG.debug(".getNode(): Exit, returning null as an element with the specified ID was not in the map");
        return (null);
    }
    
    public NodeElement getNodeByKey(String nodeKey) {
    	LOG.debug(".getNodeByKey(): Entry, nodeKey --> {}", nodeKey);
    	FDNToken nodeID = this.nodeKeySet.get(nodeKey);
    	NodeElementIdentifier nodeIdentifier = new NodeElementIdentifier(nodeID);
    	NodeElement nodeElement = getNode(nodeIdentifier);
    	return(nodeElement);
    }

    public void addLink(LinkElement newLink) {
        LOG.debug(".addLink(): Entry, newLink --> {}", newLink);
        if (newLink == null) {
            throw (new IllegalArgumentException(".addLink(): newElement is null"));
        }
        if (newLink.getLinkID() == null) {
            throw (new IllegalArgumentException(".addLink(): bad Route Token within newLink"));
        }
        boolean elementFound = false;
        Enumeration<LinkElementIdentifier> list = this.linkSet.keys();
        LinkElementIdentifier currentLinkID = null;
        while (list.hasMoreElements()) {
            currentLinkID = list.nextElement();
            if (LOG.isTraceEnabled()) {
                LOG.trace(".addLink(): Cache Entry --> {}", currentLinkID.toFullString());
            }
            if (currentLinkID.equals(newLink)) {
                LOG.trace(".addLink(): Link already in Cache");
                elementFound = true;
                break;
            }
        }
        if (elementFound) {
            this.linkSet.replace(currentLinkID, newLink);
        } else {
            this.linkSet.put(newLink.getLinkID(), newLink);
        }
    }

    public void removeLink(LinkElementIdentifier linkID) {
        LOG.debug(".removeLink(): Entry, linkID --> {}", linkID);
        if (linkID == null) {
            throw (new IllegalArgumentException(".removeLink(): linkID is null"));
        }
        boolean elementFound = false;
        Enumeration<LinkElementIdentifier> list = this.linkSet.keys();
        FDNToken currentLinkID = null;
        while (list.hasMoreElements()) {
            currentLinkID = list.nextElement();
            if (LOG.isTraceEnabled()) {
                LOG.trace(".addLink(): Cache Entry --> {}", currentLinkID.toFullString());
            }
            if (currentLinkID.equals(linkID)) {
                LOG.trace(".addLink(): Link already in Cache");
                elementFound = true;
                break;
            }
        }
        if (elementFound) {
            LOG.trace(".removeLink(): Route found, now removing it...");
            this.linkSet.remove(currentLinkID);
        } else {
            LOG.trace(".removeLink(): No route with that linkID is in the map");
        }
        LOG.debug(".removeLink(): Exit");
    }

    public Set<LinkElement> getLinkSet() {
        LOG.debug(".getLinkSet(): Entry");
        LinkedHashSet<LinkElement> linkSet = new LinkedHashSet<LinkElement>();
        if (this.linkSet.isEmpty()) {
            LOG.debug(".getLinkSet(): Exit, The Link set is empty, returning null");
            return (null);
        }
        linkSet.addAll(this.linkSet.values());
        if (LOG.isDebugEnabled()) {
            LOG.debug(".getLinkSet(): Exit, returning an Link set, size --> {}", linkSet.size());
        }
        return (linkSet);
    }

    public LinkElement getLink(LinkElementIdentifier linkID) {
        LOG.debug(".getLink(): Entry, linkID --> {}", linkID);
        if (linkID == null) {
            LOG.debug(".getLink(): Exit, provided a null linkID , so returning null");
            return (null);
        }
        boolean elementFound = false;
        Enumeration<LinkElementIdentifier> list = this.linkSet.keys();
        FDNToken currentLinkID = null;
        while (list.hasMoreElements()) {
            currentLinkID = list.nextElement();
            if (LOG.isTraceEnabled()) {
                LOG.trace(".addLink(): Cache Entry --> {}", currentLinkID.toFullString());
            }
            if (currentLinkID.equals(linkID)) {
                LOG.trace(".addLink(): Link already in Cache");
                elementFound = true;
                break;
            }
        }
        if (elementFound) {
            LOG.trace(".getLink(): Link found!!! WooHoo!");
            LinkElement retrievedLink = this.linkSet.get(currentLinkID);
            LOG.debug(".getLink(): Exit, returning Link --> {}", retrievedLink);
            return (retrievedLink);
        } else {
            LOG.trace(".getLink(): Couldn't find Link!");
            LOG.debug(".getLink(): Exit, returning null as an Link with the specified ID was not in the map");
            return (null);
        }
    }

    public void addEndpoint(EndpointElement newEndpoint) {
        LOG.debug(".addEndpoint(): Entry, newEndpoint --> {}", newEndpoint);
        if (newEndpoint == null) {
            throw (new IllegalArgumentException(".addEndpoint(): newElement is null"));
        }
        if (newEndpoint.getEndpointInstanceID() == null) {
            throw (new IllegalArgumentException(".addLink(): bad Route Token within newEndpoint"));
        }
        boolean elementFound = false;
        Enumeration<EndpointElementIdentifier> list = this.endpointSet.keys();
        EndpointElementIdentifier currentEndpointID = null;
        while (list.hasMoreElements()) {
            currentEndpointID = list.nextElement();
            if (LOG.isTraceEnabled()) {
                LOG.trace(".addEndpoint(): Endpoint Cache Entry --> {}", currentEndpointID.toFullString());
            }
            if (currentEndpointID.equals(newEndpoint)) {
                LOG.trace(".addEndpoint(): Endpoint already in Cache");
                elementFound = true;
                break;
            }
        }
        if (elementFound) {
            LOG.trace(".addEndpoint(): Replacing Existing Endpoint in Cache");
            this.endpointSet.replace(currentEndpointID, newEndpoint);
        } else {
            LOG.trace(".addEndpoint(): Adding Endpoint to Cache");
            this.endpointSet.put(newEndpoint.getEndpointInstanceID(), newEndpoint);
        }
    }

    public void removeEndpoint(EndpointElementIdentifier endpointID) {
        LOG.debug(".removeEndpoint(): Entry, endpointID --> {}", endpointID);
        if (endpointID == null) {
            throw (new IllegalArgumentException(".removeEndpoint(): endpointID is null"));
        }
        boolean elementFound = false;
        Enumeration<EndpointElementIdentifier> list = this.endpointSet.keys();
        EndpointElementIdentifier currentEndpointID = null;
        while (list.hasMoreElements()) {
            currentEndpointID = list.nextElement();
            if (LOG.isTraceEnabled()) {
                LOG.trace(".removeEndpoint(): Cache Entry --> {}", currentEndpointID.toFullString());
            }
            if (currentEndpointID.equals(endpointID)) {
                LOG.trace(".removeEndpoint(): Link already in Cache");
                elementFound = true;
                break;
            }
        }
        if (elementFound) {
            LOG.trace(".removeEndpoint(): Route found, now removing it...");
            this.endpointSet.remove(currentEndpointID);
        } else {
            LOG.trace(".removeEndpoint(): No route with that linkID is in the map");
        }
        LOG.debug(".removeEndpoint(): Exit");
    }

    public Set<EndpointElement> getEndpointSet() {
        LOG.debug(".getEndpointSet(): Entry");
        LinkedHashSet<EndpointElement> endpoints = new LinkedHashSet<EndpointElement>();
        if (this.endpointSet.isEmpty()) {
            LOG.debug(".getEndpointSet(): Exit, The Endpoint set is empty, returning null");
            return (null);
        }
        endpoints.addAll(this.endpointSet.values());
        if (LOG.isDebugEnabled()) {
            LOG.debug(".getEndpointSet(): Exit, returning an endpoint set, size --> {}", endpoints.size());
        }
        return (endpoints);
    }

    public EndpointElement getEndpoint(EndpointElementIdentifier endpointID) {
        LOG.debug(".getEndpoint(): Entry, endpointID --> {}", endpointID);
        if (endpointID == null) {
            LOG.debug(".getEndpoint(): Exit, provided a null endpointID , so returning null");
            return (null);
        }
        LOG.trace(".getEndpoint(): Searched For Endpoint ID --> {}", endpointID.toFullString());
        Enumeration<EndpointElementIdentifier> list = this.endpointSet.keys();
        while (list.hasMoreElements()) {
            FDNToken currentEndpointID = list.nextElement();
            if (LOG.isTraceEnabled()) {
                LOG.trace(".getEndpoint(): Cache Entry --> {}", currentEndpointID.toFullString());
            }
            if (currentEndpointID.equals(endpointID)) {
                LOG.trace(".getEndpoint(): Endpoint found!!! WooHoo!");
                EndpointElement retrievedEndpoint = this.endpointSet.get(currentEndpointID);
                LOG.debug(".getEndpoint(): Exit, returning Endpoint --> {}", retrievedEndpoint);
                return (retrievedEndpoint);
            }
        }
        LOG.debug(".getEndpoint(): Exit, returning null as an Endpoint with the specified ID was not in the map");
        return (null);
    }

    public Map<Integer, NodeElementIdentifier> findNodesWithMatchingUnqualifiedInstanceName(String unqualifiedRDNName) {
        LOG.debug(".findNodesWithMatchingUnqualifiedInstanceName(): Entry, unqualifiedRDNName --> {}", unqualifiedRDNName);
        HashMap<Integer, NodeElementIdentifier> matchingSet = new HashMap<Integer, NodeElementIdentifier>();
        Enumeration<NodeElementIdentifier> elementIDEnumerator = nodeSet.keys();
        LOG.trace(".findNodesWithMatchingUnqualifiedInstanceName(): nodeSet size --> {} ", nodeSet.size());
        int entryCount = 0;
        while (elementIDEnumerator.hasMoreElements()) {
        	NodeElementIdentifier currentElementId = elementIDEnumerator.nextElement();
            FDN currentElementFDN = new FDN(currentElementId);
            RDN currentElementUnqualifiedRDN = currentElementFDN.getUnqualifiedRDN();
            String currentElementRDNValue = currentElementUnqualifiedRDN.getValue();
            if (currentElementRDNValue.contentEquals(unqualifiedRDNName)) {
            	NodeElementIdentifier nodeInstance = new NodeElementIdentifier(currentElementId);
                matchingSet.put(entryCount, nodeInstance);
                entryCount++;
            }
        }
        LOG.debug(".findMatchingNode(): Exit, matchingSet --> {}", matchingSet);
        return (matchingSet);
    }

    public NodeElementIdentifier getSolutionID() {
        Enumeration<NodeElementIdentifier> elementIDEnumerator = nodeSet.keys();
        while (elementIDEnumerator.hasMoreElements()) {
            NodeElementIdentifier currentElementId = elementIDEnumerator.nextElement();
            FDN currentElementFDN = new FDN(currentElementId);
            RDN currentElementUnqualifiedRDN = currentElementFDN.getUnqualifiedRDN();
            if (currentElementUnqualifiedRDN.getQualifier().contentEquals(NodeElementTypeEnum.SOLUTION.getNodeElementType())) {
                return (currentElementId);
            }
        }
        return (null);
    }

    public Map<Integer, NodeElement> getNodeContainmentHierarchy(NodeElementIdentifier nodeID) {
        LOG.debug(".getNodeContainmentHierarchy(): Entry, nodeID --> {}", nodeID);
        HashMap<Integer, NodeElement> nodeHierarchy = new HashMap<Integer, NodeElement>();
        if (nodeID == null) {
            return (nodeHierarchy);
        }
        boolean hasContainer = true;
        int counter = 0;
        NodeElementIdentifier currentNode = nodeID;
        while (hasContainer) {
            NodeElement currentElement = nodeSet.get(currentNode);
            if (currentElement == null) {
                hasContainer = false;
            } else {
                nodeHierarchy.put(counter, currentElement);
                counter++;
                if (currentElement.getContainingElementID() == null) {
                    hasContainer = false;
                } else {
                    currentNode = currentElement.getContainingElementID();
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(".getNodeContainmentHierarchy(): Exit, retrieved Heirarchy, depth --> {}", nodeHierarchy.size());
        }
        return (nodeHierarchy);
    }

    public EndpointElement getEndpoint(NodeElement node, String endpointName, String endpointVersion){
        LOG.debug(".getEndpoint(): Entry, node --> {}, endpointName --> {}, endpointVersion --> {}", node,endpointName, endpointVersion );
        Set<EndpointElementIdentifier> endpoints = node.getEndpoints();
        LOG.trace(".getEndpoint(): Number of Endpoints on the Node --> {}", endpoints.size());
        for(EndpointElementIdentifier endpointId: endpoints){
            LOG.trace(".getEndpoint(): Retrieving the endpoint from the cache");
            EndpointElement endpointNode = this.endpointSet.get(endpointId);
            LOG.trace(".getEndpoint(): Retrieved endpoint --> {}", endpointNode);
            FDN endpointFDN = new FDN(endpointId);
            String name = endpointFDN.getUnqualifiedRDN().getValue();
            LOG.trace(".getEndpoint(): unqualified name of endpoint --> {}", name);
            boolean namesMatch = stringValuesMatch(name, endpointName);
            LOG.trace(".getEndpoint(): namesMatch status --> {}", namesMatch);
            boolean versionsMatch = stringValuesMatch(endpointNode.getVersion(), endpointVersion);
            LOG.trace(".getEndpoint(): versionsMatch status --> {}", versionsMatch);
            if( namesMatch && versionsMatch ){
                LOG.debug(".getEndpoint(): Endpoint Found --> {}", endpointNode);
                return(endpointNode);
            }
        }
        LOG.debug(".getEndpoint(): no endpoints found!");
        return(null);
    }

    private boolean stringValuesMatch(String stringA, String stringB){
        if(stringA == null && stringB == null){
            return(true);
        }
        if(stringA == null || stringB == null){
            return(false);
        }
        boolean stringsAreEqual = stringA.contentEquals(stringB);
        return(stringsAreEqual);
    }

}
