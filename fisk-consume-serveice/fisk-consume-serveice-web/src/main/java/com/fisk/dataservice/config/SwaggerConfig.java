package com.fisk.dataservice.config;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.dataservice.FiskConsumeServeiceApplication;
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
 * @author dick
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    public static final String TAG_1 = "datasource-controller";
    public static final String TAG_2 = "appregister-controller";
    public static final String TAG_3 = "apiregister-controller";
    public static final String TAG_4 = "apiservice-controller";
    public static final String TAG_5 = "logs-controller";
    public static final String TAG_6 = "dataAnalysisView-controller";
    public static final String TAG_7 = "tableservice-controller";
    public static final String TAG_8 = "serviceAnalyse-controller";

    @Bean
    public Docket createRestApi() {
        String basePck = FiskConsumeServeiceApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .tags(new Tag(TAG_1,"数据源接口"))
                .tags(new Tag(TAG_2,"应用接口"))
                .tags(new Tag(TAG_3,"API接口"))
                .tags(new Tag(TAG_4,"API服务接口"))
                .tags(new Tag(TAG_5,"数据服务日志"))
                .tags(new Tag(TAG_8,"服务数据分析"))
                .tags(new Tag(TAG_6,"视图服务接口"))
                .tags(new Tag(TAG_7,"表服务接口"))
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePck))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(apiKey())
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
                .title("菲斯科  白泽项目  接口文档")
                .description("Create by Dick")
                .version("1.0.0测试版")
                .build();
    }
}
