package pl.rpsoft.mule.provider;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProviderFactory;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static org.mule.runtime.api.component.ComponentIdentifier.builder;
import static org.mule.runtime.extension.api.util.NameUtils.defaultNamespace;
import static pl.rpsoft.mule.provider.ConfigurationServerPropertiesExtensionLoadingDelegate.CONFIG_ELEMENT;
import static pl.rpsoft.mule.provider.ConfigurationServerPropertiesExtensionLoadingDelegate.EXTENSION_NAME;

/**
 * Builds the provider for a configuration-server-properties-provider:config element.
 *
 * @since 1.0
 */
public class ConfigurationServerPropertiesProviderFactory implements ConfigurationPropertiesProviderFactory {

    public static final String EXTENSION_NAMESPACE = defaultNamespace(EXTENSION_NAME);
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationServerPropertiesProviderFactory.class);
    private static final ComponentIdentifier CUSTOM_PROPERTIES_PROVIDER =
            builder().namespace(EXTENSION_NAMESPACE).name(CONFIG_ELEMENT).build();

    private final static String CUSTOM_PROPERTIES_PREFIX = "configuration::";

    private boolean connected = false;
    private Map<String, Object> configurationMap = null;

    @Override
    public ComponentIdentifier getSupportedComponentIdentifier() {
        return CUSTOM_PROPERTIES_PROVIDER;
    }

    @Override
    public ConfigurationPropertiesProvider createProvider(ConfigurationParameters parameters,
                                                          ResourceProvider externalResourceProvider) {
        return new ConfigurationPropertiesProvider() {
            @Override
            public Optional<ConfigurationProperty> getConfigurationProperty(String configurationAttributeKey) {
                if (configurationAttributeKey.startsWith(CUSTOM_PROPERTIES_PREFIX)) {
                    String effectiveKey = configurationAttributeKey.substring(CUSTOM_PROPERTIES_PREFIX.length());
                    return Optional.of(new ConfigurationProperty() {
                        @Override
                        public Object getSource() {
                            return "Configuration server properties provider";
                        }

                        @Override
                        public Object getRawValue() {
                            try {
                                if (!connected) {
                                    connect(parameters);
                                }

                                return getProperty(effectiveKey);
                            } catch (Exception e) {
                                // IGNORE ?

                                return "";
                            }
                        }

                        @Override
                        public String getKey() {
                            return effectiveKey;
                        }
                    });
                }

                return Optional.empty();
            }

            @Override
            public String getDescription() {
                return "Configuration server properties provider";
            }
        };
    }

    /**
     * Connects to the given configuration server and collects all the configuration properties
     *
     * @param parameters - parameters provided by configuration provider
     */
    public void connect(ConfigurationParameters parameters) {
        String stringUrl = parameters.getStringParameter("Base_URL");
        String configurationPath = parameters.getStringParameter("Configuration_Path");

        String headerName = parameters.getStringParameter("Authorization_header_name");
        String headerValue = parameters.getStringParameter("Authorization_header_value");

        String configServerType = parameters.getStringParameter("Configuration_Server");

        String urlBuilder = stringUrl + "/" + configurationPath;
        if (configServerType.equals("Consul")) {
            urlBuilder += "?recurse=true";
        }

        try {
            LOGGER.debug("Trying to connect to configuration server on " + urlBuilder);

            URL url = new URL(urlBuilder);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Accept", "*/*");
            if (headerName != null) {
                con.setRequestProperty(headerName, headerValue);
            }
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                LOGGER.debug("Connection successful");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                configurationMap = new HashMap<>();

                LOGGER.debug("Configuration server type: " + configServerType);

                switch (configServerType) {
                    case "Tower":
                        JSONObject configurationObject = new JSONObject(response.toString());
                        configurationMap = configurationObject.toMap();
                        break;
                    case "Spring Cloud Config Server": {
                        JSONObject obj = new JSONObject(response.toString());
                        JSONArray array = obj.getJSONArray("propertySources");

                        for (int i = 0; i < array.length(); i++) {
                            Map<String, Object> tempMap = array.getJSONObject(i).getJSONObject("source").toMap();
                            configurationMap.putAll(tempMap);
                        }
                        break;
                    }
                    case "Consul": {
                        JSONArray array = new JSONArray(response.toString());

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject consulObject = array.getJSONObject(i);

                            String mainKey = consulObject.get("Key").toString();
                            String encodedValue = consulObject.get("Value").toString();

                            if (!encodedValue.equals("null")) {
                                String json = new String(Base64.getDecoder().decode(encodedValue));
                                JSONObject valueObject = new JSONObject(json);
                                Iterator<String> keys = valueObject.keys();

                                while (keys.hasNext()) {
                                    String tempKey = keys.next();
                                    String localKey = mainKey + "/" + tempKey;

                                    configurationMap.put(localKey, valueObject.get(tempKey).toString());
                                }
                            }
                        }
                        break;
                    }
                }

                for (Map.Entry<String, Object> value : configurationMap.entrySet()) {
                    LOGGER.debug(value.toString());
                }

                connected = true;
            } else {
                LOGGER.error("Invalid response from configuration server: " + con.getResponseMessage());
            }
        } catch (Exception exception) {
            LOGGER.error("Error connecting to configuration server: " + exception.getMessage());
        }
    }

    /**
     * Retrieves configuration property value from configuration server
     *
     * @param paramName configuration parameter name
     * @return returns configuration parameter value
     */
    public Object getProperty(String paramName) {
        if (!connected) {
            return "";
        }

        return configurationMap.get(paramName);
    }
}
