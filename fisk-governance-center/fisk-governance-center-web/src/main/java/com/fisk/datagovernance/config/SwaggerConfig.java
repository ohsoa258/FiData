package com.fisk.datagovernance.config;

import com.fisk.common.constants.SystemConstants;
import com.fisk.datagovernance.FkDataGovernanceApplication;
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

    public static final String TEST = "test-controller";
    public static final String TAG_1 = "businessfilter-controller";
    public static final String TAG_2 = "datacheck-controller";
    public static final String TAG_3 = "datasource-controller";
    public static final String TAG_4 = "emailserver-controller";
    public static final String TAG_5 = "lifecycle-controller";
    public static final String TAG_6 = "notice-controller";
    public static final String TAG_7 = "template-controller";

    @Bean
    public Docket createRestApi() {
        String basePck = FkDataGovernanceApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .tags(new Tag(TEST, "测试"))
                .tags(new Tag(TAG_1, "业务清洗API"))
                .tags(new Tag(TAG_2, "数据校验API"))
                .tags(new Tag(TAG_3, "数据源API"))
                .tags(new Tag(TAG_4, "邮件服务器API"))
                .tags(new Tag(TAG_5, "生命周期API"))
                .tags(new Tag(TAG_6, "告警通知API"))
                .tags(new Tag(TAG_7, "模板配置API"))
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
                .title("菲斯科  白泽项目  数据治理接口文档")
                .description("Create by Lock")
                .version("1.0.0测试版")
                .build();
    }
}
