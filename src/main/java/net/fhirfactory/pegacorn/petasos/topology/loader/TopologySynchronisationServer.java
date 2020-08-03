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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URL;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.petasos.topology.loader.model.ConfigMapFileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter
 */
@ApplicationScoped
public class TopologySynchronisationServer {

    private static final Logger LOG = LoggerFactory.getLogger(TopologySynchronisationServer.class);
    boolean fileHasBeenLoaded;
    Object fileHasBeenLoadedLock;

    TopologySynchronisationServer() {
        fileHasBeenLoaded = false;
        fileHasBeenLoadedLock = new Object();
    }

    @Inject
    TopologyFileElementTransformer transformer;

    @PostConstruct
    public void initialise() {
        LOG.debug(".initialise(): Entry");
        synchronized(fileHasBeenLoadedLock){
            if (!fileHasBeenLoaded) {
                synchroniseFromFile();
                fileHasBeenLoaded = true;
            }
        }
        LOG.debug(".initialise(): Exit");
    }

    public void synchroniseFromFile() {
        if (fileHasBeenLoaded) {
            return;
        }
        LOG.debug(".readFile(): Entry");
        String filePath = "/META-INF/TopologyConfig.json";
        LOG.trace(".readFile(): Instantiate our ObjectMapper for JSON parsing of the Topology Configuration File");
        ObjectMapper objectMapper = new ObjectMapper();
        LOG.trace(".readFile(): Create the URL for the Topology Configuration File (it should be within the WAR), filename --> {}", filePath);
        URL fileURL = getClass().getResource(filePath);
        LOG.trace(".readFile(): URL created, content --> {}", fileURL);
        ConfigMapFileModel mapFileContent = null;
        try {
            LOG.trace(".readFile(): Open the file as an InputStream");
            InputStream configFileIS = fileURL.openStream();
            LOG.trace(".readFile(): File Openned, now reading content into the ObjectMapper");
            mapFileContent = objectMapper.readValue(configFileIS, ConfigMapFileModel.class);
            LOG.trace(".readFile(): Content Read, mapFileContent --> {}", mapFileContent);
        } catch (Exception Ex) {
            LOG.trace(".readFile(): Error!!! - ObjectMapper read failed - error --> {} " + Ex);
        }
        if (mapFileContent == null) {
            LOG.debug(".readFile(): Unable to read file or file was empty, exiting");
            return;
        }
        LOG.trace(".readFile(): Now processing the map file for Solution --> {}", mapFileContent.getSolutionNode().getFunctionName());
        transformer.convertToNodeElement(mapFileContent.getSolutionNode(), null, null);
        LOG.debug(".readFile(): Exit, file processed.");
    }
}
