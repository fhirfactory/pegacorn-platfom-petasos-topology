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
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.topology.loader.model.ConfigMapEndpointElement;
import net.fhirfactory.pegacorn.petasos.topology.loader.model.ConfigMapNodeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter
 */
public class TopologyFileElementTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyFileElementTransformer.class);

    public NodeElement convertToNodeElement(ConfigMapNodeElement incomingNodeDetail, FDNToken parentNodeInstanceID, FDNToken parentNodeFunctionID) {
        LOG.debug(".convertToNodeElement(): Entry, incomingNodeDetail --> {}, parentNodeInstanceID --> {}, parentNodeFunctionID --> {}", incomingNodeDetail, parentNodeInstanceID, parentNodeFunctionID);

        NodeElement newNode = new NodeElement();
        LOG.trace("convertToNodeElement(): Adding the ConcurrencyMode to the new NodeElement, concurrency mode --> {}", incomingNodeDetail.getConcurrencyMode().getConcurrencyMode());
        newNode.setConcurrencyMode(incomingNodeDetail.getConcurrencyMode());
        LOG.trace("convertToNodeElement(): Adding the ResilienceMode to the new NodeElement, resilience mode --> {}", incomingNodeDetail.getResilienceMode().getResilienceMode());
        newNode.setResilienceMode(incomingNodeDetail.getResilienceMode());
        LOG.trace("convertToNodeElement(): Adding the ElementType to the new NodeElement, type name --> {}", incomingNodeDetail.getTopologyElementType().getMapElementType());
        newNode.setTopologyElementType(incomingNodeDetail.getTopologyElementType());
        LOG.trace("convertToNodeElement(): Adding the InstanceID to the new NodeElement, instance name --> {}", incomingNodeDetail.getInstanceName());
        FDN newNodeInstanceID;
        if (parentNodeInstanceID == null) {
            newNodeInstanceID = new FDN();
        } else {
            newNodeInstanceID = new FDN(parentNodeInstanceID);
        }
        newNodeInstanceID.appendRDN(new RDN(incomingNodeDetail.getTopologyElementType().getMapElementType(), incomingNodeDetail.getInstanceName()));
        newNode.setElementInstanceID(newNodeInstanceID.getToken());
        LOG.trace("convertToNodeElement(): Adding the FunctionID to the new NodeElement, function name --> {}", incomingNodeDetail.getFunctionName());
        FDN newNodeFunctionID;
        if (parentNodeFunctionID == null) {
            newNodeFunctionID = new FDN();
            newNodeFunctionID.appendRDN(new RDN(incomingNodeDetail.getTopologyElementType().getMapElementType(), incomingNodeDetail.getFunctionName()));
            newNode.setElementFunctionTypeID(newNodeFunctionID.getToken());
        } else {
            newNodeFunctionID = new FDN(parentNodeFunctionID);
            if (!newNodeFunctionID.getUnqualifiedRDN().getNameValue().contentEquals(incomingNodeDetail.getFunctionName())) {
                newNodeFunctionID.appendRDN(new RDN(incomingNodeDetail.getTopologyElementType().getMapElementType(), incomingNodeDetail.getFunctionName()));
                newNode.setElementFunctionTypeID(newNodeFunctionID.getToken());
            }
        }
        newNode.setElementVersion(incomingNodeDetail.getElementVersion());
        LOG.trace("convertToNodeElement(): Adding the contained Node IDs to the Topology Element");
        if (!incomingNodeDetail.getContainedElements().isEmpty()) {
            Iterator<ConfigMapNodeElement> nodeElementIterator = incomingNodeDetail.getContainedElements().iterator();
            while (nodeElementIterator.hasNext()) {
                ConfigMapNodeElement containedNode = nodeElementIterator.next();
                LOG.trace("convertToNodeElement(): Adding the contained Node ID --> {}", containedNode.getInstanceName());
                FDN containedNodeFDN = new FDN(newNodeInstanceID);
                containedNodeFDN.appendRDN(new RDN(containedNode.getTopologyElementType().getMapElementType(), containedNode.getInstanceName()));
                newNode.addContainedElement(containedNodeFDN.getToken());
            }
        }
        if (!incomingNodeDetail.getEndpoints().isEmpty()) {
            Iterator<ConfigMapEndpointElement> endPointIterator = incomingNodeDetail.getEndpoints().iterator();
            while (endPointIterator.hasNext()) {
                // do something
            }
        }

        return (newNode);
              
    }
    
    public EndpointElement convertToEndpointElement(ConfigMapEndpointElement incomingEndpointDetail, FDNToken containingNodeInstanceID, FDNToken containingNodeFunctionID){
        LOG.debug(".convertToEndpointElement(): Entry, incomingEndpointDetail --> {}, containingNodeInstanceID --> {}, containingNodeFunctionID --> {}",incomingEndpointDetail,containingNodeInstanceID,containingNodeFunctionID);
        EndpointElement newElement = new EndpointElement();
        newElement.setContainingNodeID(containingNodeFunctionID);
        return(newElement);
    }

}
