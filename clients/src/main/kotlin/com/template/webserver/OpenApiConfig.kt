package com.template.webserver


import com.google.common.base.Predicates
import org.springframework.context.annotation.Configuration
import springfox.documentation.swagger2.annotations.EnableSwagger2
import springfox.documentation.builders.PathSelectors

import springfox.documentation.builders.RequestHandlerSelectors

import springfox.documentation.spi.DocumentationType

import springfox.documentation.spring.web.plugins.Docket

import org.springframework.context.annotation.Bean





/**
 * @author sourabh
 * @implNote Configuration class for Open API Specifications
 */
@EnableSwagger2
@Configuration
open class OpenApiConfig {
//    /**
//     * Open API Configuration Bean
//     *
//     * @param title
//     * @param version
//     * @param description
//     * @return
//     */
//    @Bean
//    fun openApiConfiguration(): OpenAPI {
//        return OpenAPI()
//            .info(
//                Info()
//                    .title("Corda Node")
//                    .version("v1")
//                    .description("Forex Trade Cordapp")
//                    .termsOfService("Terms of service")
//                    .license(license)
//                    .contact(contact)
//            )
//    }
//
//    /**
//     * Contact details for the developer(s)
//     *
//     * @return
//     */
//    private val contact: Contact
//        get() {
//            val contact = Contact()
//            contact.email = "sourab_parsekar@persistent.com"
//            contact.name = "Sourabh Parsekar"
//            contact.url = "https://medium.com/@sourabhanant"
//            contact.extensions = emptyMap()
//            return contact
//        }
//
//    /**
//     * License creation
//     *
//     * @return
//     */
//    private val license: License
//        get() {
//            val license = License()
//            license.name = "Apache License, Version 2.0"
//            license.url = "http://www.apache.org/licenses/LICENSE-2.0"
//            license.extensions = emptyMap()
//            return license
//        }

    @Bean
    open fun api(): Docket? {
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot")))
            .paths(PathSelectors.any())
            .build()
            .pathMapping("/")
    }
}