package com.zklcsoftware;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Config {

	 @Value("${sys.swagger.enable-swgger}")
	 private Boolean enableSwgger;
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
        		.enable(enableSwgger)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.zklcsoftware"))  //swgger插件作用范围
                .paths(or(regex("/api/.*")))//过滤的接口
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("中科联诚数字校园产品-系统接口")
                .description("中科联诚软件")
                .termsOfServiceUrl("http://www.zklcsoftware.com/")
                .version("1.0")
                .build();
    }
    
}
