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

    public static final String FACT_ATTRIBUTE = "fact-attribute-controller";
    public static final String FACT="fact-controller";
    public static final String DIMENSION ="dimension-controller";
    public static final String DIMENSION_ATTRIBUTE ="dimension-attribute-controller";
    public static final String BUSINESS_PROCESS ="business-process-controller";
    public static final String BUSINESS_AREA ="business-area-controller";
    public static final String DATASOURCE_AREA ="data-source-area-controller";
    public static final String ATOMIC_INDICATOR ="atomic-indicators-controller";
    public static final String DATA_AREA ="data-area-controller";
    public static final String PROJECT_INFO ="project-info-controller";

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
                .tags(new Tag(FACT_ATTRIBUTE, "数仓建模-事实字段"))
                .tags(new Tag(FACT,"数仓建模-事实表"))
                .tags(new Tag(DIMENSION,"数仓建模--维度"))
                .tags(new Tag(DIMENSION_ATTRIBUTE,"数仓建模–维度字段"))
                .tags(new Tag(BUSINESS_PROCESS,"业务过程"))
                .tags(new Tag(BUSINESS_AREA,"业务域"))
                .tags(new Tag(DATASOURCE_AREA,"计算数据源"))
                .tags(new Tag(ATOMIC_INDICATOR,"数仓建模–原子指标"))
                .tags(new Tag(DATA_AREA,"数据域"))
                .tags(new Tag(PROJECT_INFO,"项目信息"))
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
