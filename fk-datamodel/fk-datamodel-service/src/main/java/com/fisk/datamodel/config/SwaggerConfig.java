package com.fisk.datamodel.config;

import com.fisk.common.constants.SystemConstants;
import com.fisk.datamodel.FkDataModelApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    public static final String factAttribute = "fact-attribute-controller";
    public static final String fact="fact-controller";
    public static final String dimension="dimension-controller";
    public static final String dimensionAttribute="dimension-attribute-controller";
    public static final String businessProcess="business-process-controller";
    public static final String businessArea="business-area-controller";
    public static final String dataSourceArea="data-source-area-controller";

    @Bean
    public Docket createRestApi() {
        String basePck = FkDataModelApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePck))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(apiKey())
                .tags(new Tag(factAttribute, "数仓建模-事实字段"))
                .tags(new Tag(fact,"数仓建模-事实表"))
                .tags(new Tag(dimension,"数仓建模--维度"))
                .tags(new Tag(dimensionAttribute,"数仓建模–维度字段"))
                .tags(new Tag(businessProcess,"业务过程"))
                .tags(new Tag(businessArea,"业务域"))
                .tags(new Tag(dataSourceArea,"计算数据源"))
                .securityContexts(securityContexts());
    }

    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("^(?!auth).*$")).build());
        return securityContexts;
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference(SystemConstants.HTTP_HEADER_AUTH, authorizationScopes));
        return securityReferences;
    }

    private List<ApiKey> apiKey() {
        List<ApiKey> list = new ArrayList<>();
        list.add(new ApiKey(SystemConstants.HTTP_HEADER_AUTH, SystemConstants.HTTP_HEADER_AUTH, "header"));
        //配置输入token的备注 TOKEN_HEADER_STRING = "Authorization"
        return list;
    }

    private ApiInfo apiInfo() {

        return new ApiInfoBuilder()
                .title("数据建模  接口文档")
                .description("Create by Lock")
                .version("1.0.0测试版")
                .build();
    }
}
