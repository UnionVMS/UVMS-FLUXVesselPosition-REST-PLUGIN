package eu.europa.ec.fisheries.uvms.plugins;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PluginDataHolder {

    public static final String PLUGIN_PROPERTIES = "flux-vesselposition-rest.properties";
    public static final String PROPERTIES = "settings.properties";
    public static final String CAPABILITIES = "capabilities.properties";

    private Properties applicationProperties;
    private Properties pluginProperties;
    private Properties pluginCapabilities;

    private final ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> capabilities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SetReportMovementType> cachedMovement = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, String> getSettings() {
        return settings;
    }

    public ConcurrentHashMap<String, String> getCapabilities() {
        return capabilities;
    }

    public ConcurrentHashMap<String, SetReportMovementType> getCachedMovement() {
        return cachedMovement;
    }

    public Properties getPluginApplicationProperties() {
        return applicationProperties;
    }

    public void setPluginApplicationProperties(Properties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public Properties getPluginProperties() {
        return pluginProperties;
    }

    public void setPluginProperties(Properties properties) {
        this.pluginProperties = properties;
    }

    public Properties getPluginCapabilities() {
        return pluginCapabilities;
    }

    public void setPluginCapabilities(Properties pluginCapabilities) {
        this.pluginCapabilities = pluginCapabilities;
    }

}
