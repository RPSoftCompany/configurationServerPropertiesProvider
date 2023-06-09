# Configuration Server Properties Provider

This MuleSoft connector allows to collect configuration properties from different configuration servers.
Currently you can use it with Tower, Consul or Spring Cloud.

## How to use it?

To start using this connector you'll need to build it with maven first.

```shell
mvn clean install -DskipTests
```

After the installation you'll be able to add it as the dependency to your application pom.xml

```XML

<dependency>
    <groupId>pl.rpsoft.mule.provider</groupId>
    <artifactId>configuration-server-properties-provider-module</artifactId>
    <version>1.1.0</version>
    <classifier>mule-plugin</classifier>
</dependency>
```

This operation will add and additional connector you can choose from called **Configuration Server Properties Provider
Config**

![img.png](doc/globalTypeChoose.png)

![img.png](doc/globalElementProperties.png)

After you'll add it to your application, you'll be able to use any configuration property from the configuration server
you'll need. To do so, just add the **configuration::** prefix before the property name.

![img.png](doc/usage.png)

## Supported configuration servers

| Configuration server       | Home page                                                               |
|----------------------------|-------------------------------------------------------------------------|
| Tower                      | https://github.com/RPSoftCompany/tower                                  |
| Consul                     | https://www.consul.io/                                                  |
| Spring Cloud Config Server | https://docs.spring.io/spring-cloud-config/docs/current/reference/html/ |

## Troubleshooting

In case of any issues with this plugin, please add the debug information to your log4j.xml file and double-check
provided properties both in plugin configuration and your configuration server.

```XML
<!-- Configuration server troubleshooting -->
<AsyncLogger name="pl.rpsoft.mule.provider" level="DEBUG"/>
```