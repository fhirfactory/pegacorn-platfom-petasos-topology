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
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.topology.manager.TopologyIM;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;

@RunWith(Arquillian.class)
public class TopologyFileReaderTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyFileReaderTest.class);

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive testWAR;

        File[] fileSet = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();
        LOG.debug(".createDeployment(): ShrinkWrap Library Set for Pegacorn-Platform-CommonCode, length --> {}", fileSet.length);
        for (int counter = 0; counter < fileSet.length; counter++) {
            File currentFile = fileSet[counter];
            LOG.trace(".createDeployment(): Shrinkwrap Entry --> {}", currentFile.getName());
        }

        File topologyFile = new File("/TopologyConfigFile.json");
        testWAR = ShrinkWrap.create(WebArchive.class, "topology-file-reader-test.war")
                .addAsLibraries(fileSet)
                .addPackages(true, "net.fhirfactory.pegacorn.petasos.topology")
                .addAsManifestResource(topologyFile, "/TopologyConfigFile.json")
                .addAsManifestResource("META-INF/beans.xml", "beans.xml");
        Map<ArchivePath, Node> content = testWAR.getContent();
        Set<ArchivePath> contentPathSet = content.keySet();
        Iterator<ArchivePath> contentPathSetIterator = contentPathSet.iterator();
        while(contentPathSetIterator.hasNext()){
            ArchivePath currentPath = contentPathSetIterator.next();
            LOG.trace(".createDeployment(): testWare Entry Path --> {}", currentPath.get());
        }
        return (testWAR);
    }

    @javax.inject.Inject
    TopologyIM topologyServer;

    @javax.inject.Inject
    TopologyFileReader topologyLoader;
    
    @Test
    public void testLoadingOfFile() {
        LOG.info(".readFile(): Info Test");

        LOG.info(".readFile(): Executing FileReader Load");
       topologyLoader.readFile("/META-INF/TopologyConfigFile.json");
        LOG.info(".readFile(): Now showing content of the TopologyCache");
        Set<NodeElement> nodeSet = topologyServer.getNodeSet();
        LOG.info(".readFile(): nodeSet --> {}", nodeSet);
        Iterator<NodeElement> nodeSetIterator = nodeSet.iterator();
        while (nodeSetIterator.hasNext()) {
            NodeElement currentNode = nodeSetIterator.next();
            LOG.trace(".readFile(): Node Instance ID--> {}", currentNode.getNodeInstanceID());
        }
    }
}
