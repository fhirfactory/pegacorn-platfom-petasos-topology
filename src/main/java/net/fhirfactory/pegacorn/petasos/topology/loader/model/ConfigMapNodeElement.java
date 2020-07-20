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
package net.fhirfactory.pegacorn.petasos.topology.loader.model;

import java.util.ArrayList;
import java.util.List;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ConcurrencyMode;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.resilienceMode;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementInstanceTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter
 */
public class ConfigMapNodeElement {
        private static final Logger LOG = LoggerFactory.getLogger(NodeElement.class);

    private NodeElementInstanceTypeEnum topologyElementType;
    private String instanceName;
    private String functionName;
    private List<ConfigMapLinkElement> links;
    private List<ConfigMapEndpointElement> endpoints;
    private List<ConfigMapNodeElement> containedElements;
    private String elementVersion;
    private resilienceMode resilienceMode;
    private ConcurrencyMode concurrencyMode;
    
    public ConfigMapNodeElement(){
        endpoints = new ArrayList<ConfigMapEndpointElement>();
        containedElements = new ArrayList<ConfigMapNodeElement>();
        links = new ArrayList<ConfigMapLinkElement>();
    }

    public NodeElementInstanceTypeEnum getTopologyElementType() {
        return topologyElementType;
    }

    public void setTopologyElementType(NodeElementInstanceTypeEnum topologyElementType) {
        this.topologyElementType = topologyElementType;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public List<ConfigMapEndpointElement> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<ConfigMapEndpointElement> endpoints) {
        this.endpoints = endpoints;
    }

    public List<ConfigMapNodeElement> getContainedElements() {
        return containedElements;
    }

    public void setContainedElements(List<ConfigMapNodeElement> containedElements) {
        this.containedElements = containedElements;
    }

    public String getElementVersion() {
        return elementVersion;
    }

    public void setElementVersion(String elementVersion) {
        this.elementVersion = elementVersion;
    }

    public resilienceMode getResilienceMode() {
        return resilienceMode;
    }

    public void setResilienceMode(resilienceMode resilienceMode) {
        this.resilienceMode = resilienceMode;
    }

    public ConcurrencyMode getConcurrencyMode() {
        return concurrencyMode;
    }

    public void setConcurrencyMode(ConcurrencyMode concurrencyMode) {
        this.concurrencyMode = concurrencyMode;
    }

    public List<ConfigMapLinkElement> getLinks() {
        return links;
    }

    public void setLinks(List<ConfigMapLinkElement> links) {
        this.links = links;
    }

 
}
