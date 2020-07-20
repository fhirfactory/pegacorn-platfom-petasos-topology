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
package net.fhirfactory.pegacorn.petasos.topology.properties;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.resilienceMode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.petasos.model.resilience.mode.ConcurrencyMode;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;

@ApplicationScoped
public class ServiceModuleProperties {

    private static final Integer WUA_RETRY_LIMIT = 3;
    private static final Integer WUA_TIMEOUT_LIMIT = 10000; // 10 Seconds
    private static final Integer WUA_SLEEP_INTERVAL = 250; // 250 milliseconds
    private static final Integer WUA_CLEAN_UP_AGE_LIMIT = 60 * 1000; // 60 Seconds

    @Inject
    TopologyIM deploymentTopology;

    public resilienceMode getDeploymentMode() {
        return (resilienceMode.RESILIENCE_MODE_STANDALONE);
    }

    public ConcurrencyMode getWUAConcurrencyMode() {
        return (ConcurrencyMode.CONCURRENCY_MODE_STANDALONE);
    }

    public FDN getServiceModuleInstanceContext(String serviceModuleTypeName) {
        FDN instanceContextFDN = new FDN();
        instanceContextFDN.appendRDN(new RDN("System", "PegacornTest(1.0.0-SNAPSHOT)"));
        instanceContextFDN.appendRDN(new RDN("Subsystem", "Communicate(1.0.0-SNAPSHOT)"));
        instanceContextFDN.appendRDN(new RDN("Service", "iris0-communicate(1.0.0-SNAPSHOT)"));
        instanceContextFDN.appendRDN(new RDN("Site", "TestHarness1"));
        instanceContextFDN.appendRDN(new RDN("Pod", "Unpodded"));
        return (instanceContextFDN);
    }

    public FDNToken getServiceModuleTypeID(String serviceModuleTypeName) {
        FDNToken moduleTypeID = deploymentTopology.getServiceModuleContext(serviceModuleTypeName);
        return(moduleTypeID);
/*        
        FDN typeContextFDN = new FDN();
        typeContextFDN.appendRDN(new RDN("System", "PegacornTest(1.0.0-SNAPSHOT)"));
        typeContextFDN.appendRDN(new RDN("Subsystem", "Communicate(1.0.0-SNAPSHOT)"));
        typeContextFDN.appendRDN(new RDN("Service", "iris0-communicate(1.0.0-SNAPSHOT)"));
        return (typeContextFDN);
*/
    }

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
