package com.colak.springsecurityjwtauthtutorial.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
public class SwaggerConfiguration {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList("bearerToken"))
                .addSecurityItem(new SecurityRequirement().addList("cookie"))
                .components(
                        new Components()
                                // we can authenticate the APIs using both JWT token as bearerToken and JWT Token as in cookie
                                .addSecuritySchemes("bearerToken", createJwtBearerScheme())
                                .addSecuritySchemes("cookie", createCookieScheme())
                );
    }

    /**
     * This defines a security scheme of type "HTTP," using the "bearer" scheme, which is common for
     * JWT (JSON Web Token) authentication. It specifies the bearer format as "JWT."
     */
    private SecurityScheme createJwtBearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    /**
     * This defines a security scheme of type "APIKEY," where the API key is passed in the header with the name "cookie."
     */
    private SecurityScheme createCookieScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("cookie");

    }

    private Info apiInfo() {
        return new Info()
                .title("Authentication Service Api Doc")
                .version("1.0.0")
                .description("HTTP APIs to manage user registration and authentication.")
                .contact(new Contact().name("Orçun Çolak"));
    }
}
