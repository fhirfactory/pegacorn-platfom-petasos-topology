package net.fhirfactory.pegacorn.petasos.topology.loader;

import org.jboss.arquillian.container.test.api.Deployment;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import net.fhirfactory.pegacorn.petasos.topology.manager.proxies.ServiceModuleTopologyProxy;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;

@RunWith(Arquillian.class)
public class TopologyServicesTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyServicesTest.class);

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive testWAR;

        File[] fileSet = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();
        LOG.debug(".createDeployment(): ShrinkWrap Library Set for run-time equivalent, length --> {}", fileSet.length);
        for (int counter = 0; counter < fileSet.length; counter++) {
            File currentFile = fileSet[counter];
            LOG.trace(".createDeployment(): Shrinkwrap Entry --> {}", currentFile.getName());
        }

        File topologyFile = new File("/TopologyConfig.json");
        testWAR = ShrinkWrap.create(WebArchive.class, "topology-file-reader-test.war")
                .addAsLibraries(fileSet)
                .addPackages(true, "net.fhirfactory.pegacorn.petasos.topology")
                .addAsManifestResource(topologyFile, "/TopologyConfig.json")
                .addAsManifestResource("META-INF/beans.xml", "beans.xml");
        Map<ArchivePath, Node> content = testWAR.getContent();
        Set<ArchivePath> contentPathSet = content.keySet();
        Iterator<ArchivePath> contentPathSetIterator = contentPathSet.iterator();
        while (contentPathSetIterator.hasNext()) {
            ArchivePath currentPath = contentPathSetIterator.next();
            LOG.trace(".createDeployment(): testWare Entry Path --> {}", currentPath.get());
        }
        return (testWAR);
    }

    @javax.inject.Inject
    TopologyIM topologyServer;

    @javax.inject.Inject
    TopologySynchronisationServer topologyLoader;

    @javax.inject.Inject
    ServiceModuleTopologyProxy serviceModuleProxy;

/*    @Test
    public void testLoadingOfFile() {
        LOG.info(".testLoadingOfFile(): Info Test");

        LOG.info(".testLoadingOfFile(): Executing FileReader Load");
        topologyLoader.readFile();
        LOG.info(".testLoadingOfFile(): Now showing content of the TopologyCache");
        Set<NodeElement> nodeSet = topologyServer.getNodeSet();
        LOG.info(".testLoadingOfFile(): nodeSet Size --> {}", nodeSet.size());
    }
*/
    @Test
    public void testTopologyServer() {
        LOG.info(".testTopologyServer(): Info Test");
        topologyLoader.initialise();
        LOG.info(".testTopologyServer(): Now showing content of the TopologyCache");
        Set<NodeElement> nodeSet = topologyServer.getNodeSet();
        LOG.info(".testTopologyServer(): nodeSet --> {}", nodeSet);
        Iterator<NodeElement> nodeSetIterator = nodeSet.iterator();
        while (nodeSetIterator.hasNext()) {
            NodeElement currentNode = nodeSetIterator.next();
            FDN currentNodeFDN = new FDN(currentNode.getNodeInstanceID());
            LOG.info(".testTopologyServer(): Node Instance ID--> {}", currentNodeFDN.getUnqualifiedToken());
        }
    }

    @Test
    public void testServiceModuleTopologyProxy() {
        LOG.info(".testServiceModuleTopologyProxy(): Info Test");
        topologyLoader.initialise();
        LOG.info(".testServiceModuleTopologyProxy(): Now showing content of the TopologyCache via the ServiceModuleTopologyProxy");
        Set<NodeElement> nodeSet = serviceModuleProxy.getNodeSet();
        LOG.info(".testServiceModuleTopologyProxy(): nodeSet --> {}", nodeSet);
        Iterator<NodeElement> nodeSetIterator = nodeSet.iterator();
        while (nodeSetIterator.hasNext()) {
            NodeElement currentNode = nodeSetIterator.next();
            LOG.info(".testServiceModuleTopologyProxy(): Node Instance ID--> {}", currentNode.getNodeInstanceID());
        }
    }
}
