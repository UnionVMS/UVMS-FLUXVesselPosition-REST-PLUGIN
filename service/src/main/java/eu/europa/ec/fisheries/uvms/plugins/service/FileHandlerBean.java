package eu.europa.ec.fisheries.uvms.plugins.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
public class FileHandlerBean {

    private static final Logger LOG = LoggerFactory.getLogger(FileHandlerBean.class);

    public Properties getPropertiesFromFile(String fileName) {
        Properties props = new Properties();
        try {
            InputStream inputStream = FileHandlerBean.class.getClassLoader().getResourceAsStream(fileName);
            props.load(inputStream);
        } catch (IOException e) {
            LOG.debug("Properties file failed to load");
        }
        return props;
    }

}
