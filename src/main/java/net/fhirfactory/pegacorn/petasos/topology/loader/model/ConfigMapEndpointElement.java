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

import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElementTypeEnum;

/**
 *
 * @author Mark A. Hunter
 */
public class ConfigMapEndpointElement {

    private EndpointElementTypeEnum endpointType;
    private String endpointInstanceID;
    private String endpointFunctionID;
    private String friendlyName;

    private boolean isServer;
    private boolean requiresEncryption;
    
    private String internalDNSEntry;
    private String externalDNSEntry;
    private String internalPortNumber;
    private String externalPortNumber;

    public EndpointElementTypeEnum getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(EndpointElementTypeEnum endpointType) {
        this.endpointType = endpointType;
    }

    public String getEndpointInstanceID() {
        return endpointInstanceID;
    }

    public void setEndpointInstanceID(String endpointInstanceID) {
        this.endpointInstanceID = endpointInstanceID;
    }

    public String getEndpointFunctionID() {
        return endpointFunctionID;
    }

    public void setEndpointFunctionID(String endpointFunctionID) {
        this.endpointFunctionID = endpointFunctionID;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public boolean isIsServer() {
        return isServer;
    }

    public void setIsServer(boolean isServer) {
        this.isServer = isServer;
    }

    public boolean isRequiresEncryption() {
        return requiresEncryption;
    }

    public void setRequiresEncryption(boolean requiresEncryption) {
        this.requiresEncryption = requiresEncryption;
    }

    public String getInternalDNSEntry() {
        return internalDNSEntry;
    }

    public void setInternalDNSEntry(String internalDNSEntry) {
        this.internalDNSEntry = internalDNSEntry;
    }

    public String getExternalDNSEntry() {
        return externalDNSEntry;
    }

    public void setExternalDNSEntry(String externalDNSEntry) {
        this.externalDNSEntry = externalDNSEntry;
    }

    public String getInternalPortNumber() {
        return internalPortNumber;
    }

    public void setInternalPortNumber(String internalPortNumber) {
        this.internalPortNumber = internalPortNumber;
    }

    public String getExternalPortNumber() {
        return externalPortNumber;
    }

    public void setExternalPortNumber(String externalPortNumber) {
        this.externalPortNumber = externalPortNumber;
    }

}
