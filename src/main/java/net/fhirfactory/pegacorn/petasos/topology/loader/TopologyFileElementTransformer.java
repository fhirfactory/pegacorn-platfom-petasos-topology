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
package net.fhirfactory.pegacorn.petasos.topology.loader;

import java.util.Iterator;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.topology.loader.model.ConfigMapEndpointElement;
import net.fhirfactory.pegacorn.petasos.topology.loader.model.ConfigMapNodeElement;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter
 */
@ApplicationScoped
public class TopologyFileElementTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyFileElementTransformer.class);

    @Inject
    TopologyIM topologyInformationManager;

    public NodeElement convertToNodeElement(ConfigMapNodeElement incomingNodeDetail, FDNToken parentNodeInstanceID, FDNToken parentNodeFunctionID) {
        LOG.debug(".convertToNodeElement(): Entry, incomingNodeDetail --> {}, parentNodeInstanceID --> {}, parentNodeFunctionID --> {}", incomingNodeDetail, parentNodeInstanceID, parentNodeFunctionID);

        NodeElement newNode = new NodeElement();
        LOG.trace(".convertToNodeElement(): Adding the ContainingNode to the new NodeElement, containing node instance id --> {}", parentNodeInstanceID);
        newNode.setContainingElementID(parentNodeInstanceID);
        LOG.trace(".convertToNodeElement(): Adding the ConcurrencyMode to the new NodeElement, concurrency mode --> {}", incomingNodeDetail.getConcurrencyMode().getConcurrencyMode());
        newNode.setConcurrencyMode(incomingNodeDetail.getConcurrencyMode());
        LOG.trace(".convertToNodeElement(): Adding the ResilienceMode to the new NodeElement, resilience mode --> {}", incomingNodeDetail.getResilienceMode().getResilienceMode());
        newNode.setResilienceMode(incomingNodeDetail.getResilienceMode());
        LOG.trace(".convertToNodeElement(): Adding the ElementType to the new NodeElement, type name --> {}", incomingNodeDetail.getTopologyElementType().getMapElementType());
        newNode.setTopologyElementType(incomingNodeDetail.getTopologyElementType());
        LOG.trace(".convertToNodeElement(): Adding the InstanceID to the new NodeElement, instance name --> {}", incomingNodeDetail.getInstanceName());
        FDN newNodeInstanceID;
        if (parentNodeInstanceID == null) {
            newNodeInstanceID = new FDN();
        } else {
            newNodeInstanceID = new FDN(parentNodeInstanceID);
        }
        newNodeInstanceID.appendRDN(new RDN(incomingNodeDetail.getTopologyElementType().getMapElementType(), incomingNodeDetail.getInstanceName()));
        newNode.setElementInstanceID(newNodeInstanceID.getToken());
        LOG.trace(".convertToNodeElement(): Adding the FunctionID to the new NodeElement, function name --> {}", incomingNodeDetail.getFunctionName());
        FDN newNodeFunctionID;
        if (parentNodeFunctionID == null) {
            LOG.trace(".convertToNodeElement(): parentNodeFunctionID is NULL, so we make ourselves the top!");
            newNodeFunctionID = new FDN();
            newNodeFunctionID.appendRDN(new RDN(incomingNodeDetail.getTopologyElementType().getMapElementType(), incomingNodeDetail.getFunctionName()));
            newNode.setElementFunctionTypeID(newNodeFunctionID.getToken());
        } else {
            newNodeFunctionID = new FDN(parentNodeFunctionID);
            if (incomingNodeDetail.getFunctionName() == null) {
                newNode.setElementFunctionTypeID(newNodeFunctionID.getToken());
            } else {
                if (!newNodeFunctionID.getUnqualifiedRDN().getNameValue().contentEquals(incomingNodeDetail.getFunctionName())) {
                    newNodeFunctionID.appendRDN(new RDN(incomingNodeDetail.getTopologyElementType().getMapElementType(), incomingNodeDetail.getFunctionName()));
                    newNode.setElementFunctionTypeID(newNodeFunctionID.getToken());
                }
            }
        }
        newNode.setElementVersion(incomingNodeDetail.getElementVersion());
        LOG.trace(".convertToNodeElement(): Calling on Topology Manager to add Node to Topology Cache, parentNodeInstanceID --> {}, newNode --> {}", parentNodeInstanceID, newNode);
        topologyInformationManager.registerNode(newNode);
        LOG.trace(".convertToNodeElement(): Adding the contained Node IDs to the Topology Element");
        if (!incomingNodeDetail.getContainedElements().isEmpty()) {
            LOG.trace(".convertToNodeElement(): Adding the contained Node IDs, number to be addded --> {}", incomingNodeDetail.getContainedElements().size());
            Iterator<ConfigMapNodeElement> nodeElementIterator = incomingNodeDetail.getContainedElements().iterator();
            while (nodeElementIterator.hasNext()) {
                ConfigMapNodeElement containedNode = nodeElementIterator.next();
                LOG.trace("convertToNodeElement(): Adding the contained Node ID --> {}", containedNode.getInstanceName());
                FDN containedNodeFDN = new FDN(newNodeInstanceID);
                containedNodeFDN.appendRDN(new RDN(containedNode.getTopologyElementType().getMapElementType(), containedNode.getInstanceName()));
                newNode.addContainedElement(containedNodeFDN.getToken());
                if (newNode.getElementFunctionTypeID() == null) {
                    convertToNodeElement(containedNode, newNodeInstanceID.getToken(), parentNodeFunctionID);
                } else {
                    convertToNodeElement(containedNode, newNodeInstanceID.getToken(), newNodeFunctionID.getToken());
                }
            }
        }
        LOG.trace(".convertToNodeElement(): Adding the contained Endpoint IDs to the Topology Element");
        if (!incomingNodeDetail.getEndpoints().isEmpty()) {
            LOG.trace(".convertToNodeElement(): Adding the contained Endpoint IDs, number to be added --> {}", incomingNodeDetail.getEndpoints().size());
            Iterator<ConfigMapEndpointElement> endPointIterator = incomingNodeDetail.getEndpoints().iterator();
            while (endPointIterator.hasNext()) {
                ConfigMapEndpointElement currentElement = endPointIterator.next();
                LOG.trace(".convertToNodeElement(): Adding the contained Endpoint --> {}", currentElement.getEndpointInstanceID());
                EndpointElement endpoint = convertToEndpointElement(currentElement, newNodeInstanceID.getToken(), newNodeFunctionID.getToken());
                LOG.trace(".convertToNodeElement(): Converted ConfigMapEndpointElement to EndpointElement --> {}", endpoint);
                newNode.addEndpoint(endpoint.getEndpointInstanceID());
                LOG.trace(".convertToNodeElement(): Calling on Topology Manager to register Endpoint, endpoint --> {}", endpoint);
                topologyInformationManager.registerEndpoint(endpoint);
            }
        }

        return (newNode);

    }

    public EndpointElement convertToEndpointElement(ConfigMapEndpointElement incomingEndpointDetail, FDNToken containingNodeInstanceID, FDNToken containingNodeFunctionID) {
        LOG.debug(".convertToEndpointElement(): Entry, incomingEndpointDetail --> {}, containingNodeInstanceID --> {}, containingNodeFunctionID --> {}", incomingEndpointDetail, containingNodeInstanceID, containingNodeFunctionID);
        EndpointElement newElement = new EndpointElement();
        LOG.trace(".convertToEndpointElement(): Adding Containing Node (Function) --> {}", containingNodeFunctionID);
        newElement.setContainingNodeID(containingNodeFunctionID);
        LOG.trace(".convertToEndpointElement(): Adding Endpoint Function ID --> {}", incomingEndpointDetail.getEndpointFunctionID());
        FDN endpointFunctionFDN = new FDN(containingNodeFunctionID);
        endpointFunctionFDN.appendRDN(new RDN(incomingEndpointDetail.getEndpointType().getEndpointType(), incomingEndpointDetail.getEndpointFunctionID()));
        newElement.setEndpointFunctionID(endpointFunctionFDN.getToken());
        LOG.trace(".convertToEndpointElement(): Adding Endpoint Instance ID --> {}", incomingEndpointDetail.getEndpointInstanceID());
        FDN endpointInstanceID = new FDN(containingNodeInstanceID);
        endpointInstanceID.appendRDN(new RDN(incomingEndpointDetail.getEndpointType().getEndpointType(), incomingEndpointDetail.getEndpointInstanceID()));
        newElement.setEndpointInstanceID(endpointInstanceID.getToken());
        LOG.trace(".convertToEndpointElement(): Adding Friendly Name --> {}", incomingEndpointDetail.getFriendlyName());
        newElement.setFriendlyName(incomingEndpointDetail.getFriendlyName());
        LOG.trace(".convertToEndpointElement(): Adding Hostname --> {}", incomingEndpointDetail.getInternalDNSEntry());
        newElement.setHostname(incomingEndpointDetail.getInternalDNSEntry());
        LOG.trace(".convertToEndpointElement(): Adding Port Number --> {}", incomingEndpointDetail.getInternalPortNumber());
        newElement.setPort(Integer.getInteger(incomingEndpointDetail.getInternalPortNumber()));
        LOG.trace(".convertToEndpointElement(): Adding isServer --> {}", incomingEndpointDetail.isIsServer());
        newElement.setServer(incomingEndpointDetail.isIsServer());
        LOG.trace(".convertToEndpointElement(): Adding Endpoint Type --> {}", incomingEndpointDetail.getEndpointType());
        newElement.setEndpointType(incomingEndpointDetail.getEndpointType());
        LOG.debug(".convertToEndpointElement(): Exit, newElement --> {}", newElement);
        return (newElement);
    }

}
