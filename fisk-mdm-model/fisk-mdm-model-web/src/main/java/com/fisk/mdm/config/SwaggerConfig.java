package com.fisk.mdm.config;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.mdm.FiskMdmModelApplication;
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

    public static final String TAG_1 = "Entity-Controller";
    public static final String TAG_2 = "Model-Controller";
    public static final String TAG_3 = "Attribute-Controller";
    public static final String TAG_4 = "ModelVersion-Controller";
    public static final String TAG_5 = "MasterData-Controller";
    public static final String TAG_6 = "AttributeGroup-Controller";
    public static final String TAG_7 = "ViwGroup-Controller";
    public static final String TAG_8 = "ComplexType-Controller";
    public static final String TAG_9 = "MasterDataLog-Controller";
    public static final String TAG_10 = "AttributeLog-Controller";
    public static final String TAG_11 = "CodeRule-Controller";
    public static final String TAG_12="MatchingRules-Controller";
    public static final String TAG_13="Process-Controller";

    @Bean
    public Docket createRestApi() {
        String basePck = FiskMdmModelApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .tags(new Tag(TAG_1, "实体管理API"))
                .tags(new Tag(TAG_2, "模型管理API"))
                .tags(new Tag(TAG_3, "属性管理API"))
                .tags(new Tag(TAG_4, "模型版本管理API"))
                .tags(new Tag(TAG_5, "主数据管理API"))
                .tags(new Tag(TAG_6, "属性组管理API"))
                .tags(new Tag(TAG_7, "自定义视图管理API"))
                .tags(new Tag(TAG_8, "复杂数据类型"))
                .tags(new Tag(TAG_9, "主数据维护日志"))
                .tags(new Tag(TAG_10, "属性日志管理API"))
                .tags(new Tag(TAG_11, "自动创建编码管理API"))
                .tags(new Tag(TAG_12,"匹配规则管理API"))
                .tags(new Tag(TAG_13,"流程定义API"))
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
                .description("Create by Lock")
                .version("1.0.0测试版")
                .build();
    }



}
