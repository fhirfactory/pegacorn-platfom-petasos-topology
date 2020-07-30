/*
 * Copyright (c) 2020 MAHun
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
package net.fhirfactory.pegacorn.petasos.topology.manager.facades;

import java.util.Map;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ResilienceModeEnum;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ConcurrencyModeEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementInstanceTypeEnum;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ServiceModuleTopologyFacade{
    private static final Logger LOG = LoggerFactory.getLogger(ServiceModuleTopologyFacade.class);
    
    @Inject 
    TopologyIM topologyServer;

    private static final Integer WUA_RETRY_LIMIT = 3;
    private static final Integer WUA_TIMEOUT_LIMIT = 10000; // 10 Seconds
    private static final Integer WUA_SLEEP_INTERVAL = 250; // 250 milliseconds
    private static final Integer WUA_CLEAN_UP_AGE_LIMIT = 60 * 1000; // 60 Seconds

    public ResilienceModeEnum getDeploymentMode(FDNToken serviceModuleInstanceID) {
        return (ResilienceModeEnum.RESILIENCE_MODE_STANDALONE);
    }

    public ConcurrencyModeEnum getConcurrencyMode(FDNToken serviceModuleInstanceID) {
        return (ConcurrencyModeEnum.CONCURRENCY_MODE_STANDALONE);
    }

    /**
     * This function returns the WUP Instance ID (FDNToken) of the ServiceModule.
     * The assumption is that the Service Module Instance Name (String) is unique
     * within the deployment environment (that is, no other Modules will have 
     * the same name - other than the replicated instances of course). 
     * 
     * @param serviceModuleInstanceName The unqualified (but unique) Service Module name
     * @return the FDNToken Instance ID of the Service Module.
     */
    public FDNToken getServiceModuleInstanceID(String serviceModuleInstanceName) {
        LOG.debug(".getServiceModuleInstanceID(): Entry, serviceModuleTypeName --> {}", serviceModuleInstanceName);
        if(serviceModuleInstanceName==null){
            throw(new IllegalArgumentException("getServiceModuleInstanceID(): serviceModuleInstanceName is null"));
        }
        Map<Integer, FDNToken> nodeSet = topologyServer.getNodesWithMatchinUnqualifiedInstanceName(serviceModuleInstanceName);
        if(nodeSet.isEmpty()){
            throw(new IllegalArgumentException("getServiceModuleInstanceID(): Not such serviceModuleInstanceName in Topology Map"));
        }
        FDNToken instanceID = null;
        if(nodeSet.size() == 1){
            instanceID = nodeSet.get(0);
        } else {
            int nodeSetSize = nodeSet.size();
            for(int counter=0; counter < nodeSetSize; counter++){
                FDNToken currentInstanceID = nodeSet.get(counter);
                FDN currentInstanceFDN = new FDN(currentInstanceID);
                String currentInstanceFDNQualifier = currentInstanceFDN.getUnqualifiedRDN().getNameValue();
                if(currentInstanceFDNQualifier.contentEquals(NodeElementInstanceTypeEnum.SERVICE_MODULE.getMapElementType())){
                    instanceID = currentInstanceID;
                    break;
                }
            }
        }
        return (instanceID);
    }

/*    public FDNToken getServiceModuleTypeID(String serviceModuleTypeName) {
        FDNToken moduleTypeID = topologyServer.getServiceModuleContext(serviceModuleTypeName);
        return(moduleTypeID);*/
/*        
        FDN typeContextFDN = new FDN();
        typeContextFDN.appendRDN(new RDN("System", "PegacornTest(1.0.0-SNAPSHOT)"));
        typeContextFDN.appendRDN(new RDN("Subsystem", "Communicate(1.0.0-SNAPSHOT)"));
        typeContextFDN.appendRDN(new RDN("Service", "iris0-communicate(1.0.0-SNAPSHOT)"));
        return (typeContextFDN);
*/
/*    } */

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
}
