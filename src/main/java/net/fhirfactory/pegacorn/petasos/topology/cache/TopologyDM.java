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
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.LinkElement;
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

    private FDNToken deploymentSolutionName;
    private ConcurrentHashMap<FDNToken, NodeElement> nodeSet;
    private ConcurrentHashMap<FDNToken, LinkElement> linkSet;
    private ConcurrentHashMap<FDNToken, EndpointElement> endpointSet;

    public TopologyDM() {
        LOG.info(".TopologyDM(): Constructor initialisation");
        this.deploymentSolutionName = null;
        this.nodeSet = new ConcurrentHashMap<FDNToken, NodeElement>();
        this.linkSet = new ConcurrentHashMap<FDNToken, LinkElement>();
        this.endpointSet = new ConcurrentHashMap<FDNToken, EndpointElement>();
    }

    public FDNToken getDeploymentSolutionName() {
        return deploymentSolutionName;
    }

    public void setDeploymentSolutionName(FDNToken deploymentSolutionName) {
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
        Enumeration<FDNToken> list = this.nodeSet.keys();
        FDNToken currentNodeID = null;
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
            this.nodeSet.replace(currentNodeID, newElement);
        } else {
            this.nodeSet.put(newElement.getNodeInstanceID(), newElement);
        }
    }

    public void removeNode(FDNToken elementID) {
        LOG.debug(".removeNode(): Entry, elementID --> {}", elementID);
        if (elementID == null) {
            throw (new IllegalArgumentException(".removeNode(): elementID is null"));
        }
        boolean elementFound = false;
        Enumeration<FDNToken> list = this.nodeSet.keys();
        while (list.hasMoreElements()) {
            FDNToken currentNodeID = list.nextElement();
            if (LOG.isTraceEnabled()) {
                LOG.trace(".getNode(): Cache Entry --> {}", currentNodeID.toFullString());
            }
            if (currentNodeID.equals(elementID)) {
                LOG.trace(".removeNode(): Element found, now removing it...");
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

    public NodeElement getNode(FDNToken nodeID) {
        LOG.debug(".getNode(): Entry, nodeID --> {}", nodeID);
        if (nodeID == null) {
            LOG.debug(".getNode(): Exit, provided a null nodeID , so returning null");
            return (null);
        }
        Enumeration<FDNToken> list = this.nodeSet.keys();
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

    public void addLink(LinkElement newLink) {
        LOG.debug(".addLink(): Entry, newLink --> {}", newLink);
        if (newLink == null) {
            throw (new IllegalArgumentException(".addLink(): newElement is null"));
        }
        if (newLink.getLinkID() == null) {
            throw (new IllegalArgumentException(".addLink(): bad Route Token within newLink"));
        }
        boolean elementFound = false;
        Enumeration<FDNToken> list = this.linkSet.keys();
        FDNToken currentLinkID = null;
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

    public void removeLink(FDNToken linkID) {
        LOG.debug(".removeLink(): Entry, linkID --> {}", linkID);
        if (linkID == null) {
            throw (new IllegalArgumentException(".removeLink(): linkID is null"));
        }
        boolean elementFound = false;
        Enumeration<FDNToken> list = this.linkSet.keys();
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

    public LinkElement getLink(FDNToken linkID) {
        LOG.debug(".getLink(): Entry, linkID --> {}", linkID);
        if (linkID == null) {
            LOG.debug(".getLink(): Exit, provided a null linkID , so returning null");
            return (null);
        }
        boolean elementFound = false;
        Enumeration<FDNToken> list = this.linkSet.keys();
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
        Enumeration<FDNToken> list = this.endpointSet.keys();
        FDNToken currentEndpointID = null;
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

    public void removeEndpoint(FDNToken endpointID) {
        LOG.debug(".removeEndpoint(): Entry, endpointID --> {}", endpointID);
        if (endpointID == null) {
            throw (new IllegalArgumentException(".removeEndpoint(): endpointID is null"));
        }
        boolean elementFound = false;
        Enumeration<FDNToken> list = this.endpointSet.keys();
        FDNToken currentEndpointID = null;
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

    public EndpointElement getEndpoint(FDNToken endpointID) {
        LOG.debug(".getEndpoint(): Entry, endpointID --> {}", endpointID);
        if (endpointID == null) {
            LOG.debug(".getEndpoint(): Exit, provided a null endpointID , so returning null");
            return (null);
        }
        LOG.trace(".getEndpoint(): Searched For Endpoint ID --> {}", endpointID.toFullString());
        Enumeration<FDNToken> list = this.endpointSet.keys();
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

    public Map<Integer, FDNToken> findNodesWithMatchingUnqualifiedInstanceName(String unqualifiedRDNName) {
        LOG.debug(".findNodesWithMatchingUnqualifiedInstanceName(): Entry, unqualifiedRDNName --> {}", unqualifiedRDNName);
        HashMap<Integer, FDNToken> matchingSet = new HashMap<Integer, FDNToken>();
        Enumeration<FDNToken> elementIDEnumerator = nodeSet.keys();
        LOG.trace(".findNodesWithMatchingUnqualifiedInstanceName(): nodeSet size --> {} ", nodeSet.size());
        int entryCount = 0;
        while (elementIDEnumerator.hasMoreElements()) {
            FDNToken currentElementId = elementIDEnumerator.nextElement();
            FDN currentElementFDN = new FDN(currentElementId);
            RDN currentElementUnqualifiedRDN = currentElementFDN.getUnqualifiedRDN();
            String currentElementRDNValue = currentElementUnqualifiedRDN.getNameValue();
            if (currentElementRDNValue.contentEquals(unqualifiedRDNName)) {
                matchingSet.put(entryCount, currentElementId);
                entryCount++;
            }
        }
        LOG.debug(".findMatchingNode(): Exit, matchingSet --> {}", matchingSet);
        return (matchingSet);
    }

    public FDNToken getSolutionID() {
        Enumeration<FDNToken> elementIDEnumerator = nodeSet.keys();
        while (elementIDEnumerator.hasMoreElements()) {
            FDNToken currentElementId = elementIDEnumerator.nextElement();
            FDN currentElementFDN = new FDN(currentElementId);
            RDN currentElementUnqualifiedRDN = currentElementFDN.getUnqualifiedRDN();
            if (currentElementUnqualifiedRDN.getNameQualifier().contentEquals(NodeElementTypeEnum.SOLUTION.getNodeElementType())) {
                return (currentElementId);
            }
        }
        return (null);
    }

    public Map<Integer, NodeElement> getNodeContainmentHierarchy(FDNToken nodeID) {
        LOG.debug(".getNodeContainmentHierarchy(): Entry, nodeID --> {}", nodeID);
        HashMap<Integer, NodeElement> nodeHierarchy = new HashMap<Integer, NodeElement>();
        if (nodeID == null) {
            return (nodeHierarchy);
        }
        boolean hasContainer = true;
        int counter = 0;
        FDNToken currentNode = nodeID;
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
}
