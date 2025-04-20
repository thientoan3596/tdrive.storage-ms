package org.thluon.tdrive.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BasePathConf {
  // private final SwaggerUiConfigProperties swaggerUiConfigProperties;

  // @Value("${application.api.endpoint}/${application.api.version}")
  // private String service_endpoint;

  /**
   * Expect to be /api/service-name/v1/ However, some misconfig might causing duplicate //
   *
   * <p>Using String.replaceAll("/+","/") to sanitize Actuator need to be started with this path,
   * there for API Gateway can redirect request correctly NB! Surely the last character will not be
   * "/"
   */
  // @Bean
  // String thisServiceEndpoint() {
  //  String basePath = "/" + service_endpoint;
  //  return basePath.replaceAll("/+", "/").replaceAll("$/", "");
  // }

  /**
   * Remapping actuator path. Because using Spring Cloud Gateway, therefor API Gateway can redirect
   * request correctly
   */
  // @Bean
  // @Primary
  // WebEndpointProperties webEndpointProperties(String thisServiceEndpoint) {
  //  WebEndpointProperties properties = new WebEndpointProperties();
  //  properties.setBasePath(thisServiceEndpoint + "/actuator");
  //  return properties;
  // }

  // @Value("#{'${SECURE_ACTUATORS:}'.split(',')}")
  // private String[] SECURE_ACTUATORS;

  /**
   * Construct FULL path of secured Actuators. For example, - If SECURE_ACTUATORS item is /env, then
   * transform it to [this service endpoint]/env.
   *
   * <p>Otherwise, retains it. This is making assumption that if the environment contains the words
   * [/api], User already know the correct service path. See {@link
   * BasePathConf#webEndpointProperties(String)} for references.
   */
  // @Bean
  // List<String> securedActuatorPaths() {
  //  List<String> paths = new ArrayList<>();
  //  for (String str : SECURE_ACTUATORS) {
  //    if (str.contains("/api/")) {
  //      paths.add(str);
  //    } else {
  //      paths.add((thisServiceEndpoint() + "/" + str).replaceAll("/+", "/"));
  //    }
  //  }
  //  return paths;
  // }
  //
  // @PostConstruct
  // void springDocConfigProperties() {
  //  swaggerUiConfigProperties.setPath(thisServiceEndpoint() + "/swagger-ui.html");
  //  swaggerUiConfigProperties.setUrl(thisServiceEndpoint() + "/v3/api-doc");
  // }
  //
  // @Bean
  // RouterFunction<ServerResponse> openApiRouter(OpenApiResource openApiResource) {
  //  return RouterFunctions.route()
  //      .GET(thisServiceEndpoint() + "/v3/api-docs", openApiResource::openapiJson)
  //      .build();
  // }
}
