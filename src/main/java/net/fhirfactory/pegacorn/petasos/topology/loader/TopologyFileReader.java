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
import javax.inject.Singleton;
import net.fhirfactory.pegacorn.petasos.topology.loader.model.ConfigMapFileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter
 */
@Singleton
public class TopologyFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyFileReader.class);

    private static String fileName = "/TopologyConfigFile.json";

    public void readFile() {
        ObjectMapper objectMapper = new ObjectMapper();

        URL fileURL = getClass().getResource(fileName);
        LOG.trace("About to read file --> {}", fileURL.toString());
        ConfigMapFileModel mapTemplate = null;
        try {
            LOG.trace("About to read file --> {}", fileURL.toString());
            InputStream configFileIS = fileURL.openStream();
            LOG.trace("File Openned");
            mapTemplate = objectMapper.readValue(configFileIS, ConfigMapFileModel.class);
            LOG.trace("Something has been read!");
        } catch (Exception Ex) {
            LOG.trace("Error!!! " + Ex.toString());
        }
        if (mapTemplate == null) {
            LOG.trace("Nothing loaded...");
            return;
        }
        LOG.info("File read! --> " + mapTemplate.getSolutionNode().getFunctionName());
    }
}
