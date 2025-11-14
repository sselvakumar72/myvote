package com.lvt.apps.myvote.ms.configs;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhg.cpm.customers.zetasdk.clients.BeneficiaryOpsClient;
import com.uhg.cpm.customers.zetasdk.clients.ZetaAuthClient;
import com.uhg.cpm.customers.zetasdk.clients.impl.ZetaAuthClientImpl;
import com.uhg.cpm.customers.zetasdk.clients.impl.beneficiaryops.BeneficiaryOpsAuthenticationService;
import com.uhg.cpm.customers.zetasdk.clients.impl.beneficiaryops.BeneficiaryOpsClientImpl;
import com.uhg.cpm.customers.zetasdk.config.ZetaSdkClientConfigurationProperties;
import com.uhg.cpm.customers.zetasdk.mappers.beneficiaryops.BeneficiaryOpsErrorMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
@EnableConfigurationProperties(FeatureFlagProperties.class)
public class ServiceConfiguration {

    @Value("${springdoc.swagger-ui.server}")
    private String swaggerHost;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl(swaggerHost);
        return new OpenAPI().servers(List.of(server));
    }

    @Bean
    public BeneficiaryOpsClient beneficiaryOpsClient(
            ZetaSdkClientConfigurationProperties props,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper) {

        // TODO:  we should refactor the SDK so client is easier to build.
        final ZetaSdkClientConfigurationProperties.ZetaClientConfiguration clientConfiguration = props.getZetaClientConfiguration();
        final ZetaAuthClient authClient = new ZetaAuthClientImpl(clientConfiguration.getAuthorization(), webClientBuilder);
        final BeneficiaryOpsAuthenticationService authenticationService = new BeneficiaryOpsAuthenticationService(authClient);
        final BeneficiaryOpsErrorMapper errorMapper = new BeneficiaryOpsErrorMapper(objectMapper);

        return new BeneficiaryOpsClientImpl(clientConfiguration, webClientBuilder, authenticationService, errorMapper);
    }
}
