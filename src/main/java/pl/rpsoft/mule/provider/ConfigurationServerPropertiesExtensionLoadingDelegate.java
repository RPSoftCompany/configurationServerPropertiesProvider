/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package pl.rpsoft.mule.provider;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.runtime.api.meta.model.declaration.fluent.ConfigurationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclarer;
import org.mule.runtime.api.meta.model.display.DisplayModel;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.api.loader.ExtensionLoadingDelegate;

import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import static org.mule.runtime.api.meta.Category.COMMUNITY;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

/**
 * Declares extension for Secure Properties Configuration module
 *
 * @since 1.0
 */
public class ConfigurationServerPropertiesExtensionLoadingDelegate implements ExtensionLoadingDelegate {

    public static final String EXTENSION_NAME = "Configuration Server Properties Provider";
    public static final String CONFIG_ELEMENT = "config";

    @Override
    public void accept(ExtensionDeclarer extensionDeclarer, ExtensionLoadingContext context) {
        ConfigurationDeclarer configurationDeclarer = extensionDeclarer.named(EXTENSION_NAME)
                .describedAs(String.format("Crafted %s Extension", EXTENSION_NAME))
                .withCategory(COMMUNITY)
                .onVersion("1.0.0")
                .fromVendor("RPSoft")
                .withConfig(CONFIG_ELEMENT);

        ParameterGroupDeclarer defaultParameterGroup = configurationDeclarer.onDefaultParameterGroup();
        defaultParameterGroup
                .withRequiredParameter("Configuration_Server").withDisplayModel(DisplayModel.builder().displayName("Configuration Server").build()).ofType(BaseTypeBuilder.create(JAVA).stringType().enumOf("Tower", "Spring Cloud Config Server", "Consul").defaultValue("Tower").build())
                .withExpressionSupport(NOT_SUPPORTED)
                .describedAs("Configuration server you use. You can choose from \"Tower Configuration Server\" (https://github.com/RPSoftCompany/tower), \"Spring Cloud Config Server\" (https://github.com/spring-cloud/spring-cloud-config) or \"Consul\" (https://github.com/hashicorp/consul)");

        defaultParameterGroup
                .withRequiredParameter("Base_URL").withDisplayModel(DisplayModel.builder().displayName("Base URL").build()).ofType(BaseTypeBuilder.create(JAVA).stringType().build())
                .withExpressionSupport(NOT_SUPPORTED)
                .describedAs("Configuration server base URL, e.g. http://127.0.0.1:8080/v1");

        defaultParameterGroup
                .withRequiredParameter("Configuration_Path").withDisplayModel(DisplayModel.builder().displayName("Configuration path").build()).ofType(BaseTypeBuilder.create(JAVA).stringType().build())
                .withExpressionSupport(NOT_SUPPORTED)
                .describedAs("Configuration path, e.g. DEV/App1");

        ParameterGroupDeclarer authParameterGroup = configurationDeclarer.onParameterGroup("Authentication");

        authParameterGroup
                .withOptionalParameter("Authorization_header_name").withDisplayModel(DisplayModel.builder().displayName("Authorization header name").build()).ofType(BaseTypeBuilder.create(JAVA).stringType().build())
                .withExpressionSupport(NOT_SUPPORTED)
                .describedAs("Configuration server authorization header name, e.g. \"X-Config-Token\"");

        authParameterGroup
                .withOptionalParameter("Authorization_header_value").withDisplayModel(DisplayModel.builder().displayName("Authorization header value").build()).ofType(BaseTypeBuilder.create(JAVA).stringType().build())
                .withExpressionSupport(NOT_SUPPORTED)
                .describedAs("Configuration server authorization header value");
    }

}
