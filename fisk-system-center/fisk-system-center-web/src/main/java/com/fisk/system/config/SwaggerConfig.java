package com.fisk.system.config;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.system.FiskSystemCenterApplication;
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
 * @author gy
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

//    public static final String DATAVIEW = "dataview-controller";
//    public static final String PERMISSION = "permission-controller";
//    public static final String ROLE_INFO = "role-info-controller";
//    public static final String SERVICE_REGISTRY = "service-registry-controller";
//    public static final String USER = "user-controller";
//    public static final String SYSTEM = "system-controller";
//    public static final String KEYWORD = "keywords-controller";
//    public static final String UPLOAD = "upload-controller";
//    public static final String DATASOURCE = "datasource-controller";
//    public static final String EMAIL_SERVER_CONTROLLER = "email-server-controller";
//    public static final String LICENSE_CONTROLLER = "license-controller";
//    public static final String SQLFACTORY_CONTROLLER = "sqlFactory-controller";
//    public static final String SYSTEM_VERSION_CONTROLLER = "system-version-controller";
//    public static final String SYSTEM_LOG_CONTROLLER = "system-log-controller";

    public static final String DATAVIEW = "视图过滤";
    public static final String PERMISSION = "权限管理";
    public static final String ROLE_INFO = "角色管理";
    public static final String SERVICE_REGISTRY = "服务注册";
    public static final String USER = "用户中心服务";
    public static final String SYSTEM = "系统Logo信息服务";
    public static final String KEYWORD = "SQL关键字管理";
    public static final String UPLOAD = "上传管理";
    public static final String DATASOURCE = "数据源管理";
    public static final String EMAIL_SERVER_CONTROLLER = "邮件服务器API";
    public static final String LICENSE_CONTROLLER = "license许可证管理";
    public static final String SQLFACTORY_CONTROLLER = "SQL语句校验处理";
    public static final String SYSTEM_VERSION_CONTROLLER = "平台版本信息管理";
    public static final String SYSTEM_LOG_CONTROLLER = "平台服务日志管理";
    public static final String AUDIT_LOGS = "审计日志";
    public static final String DATA_SECURITY = "数据安全";
    public static final String Tset = "测试";

    @Bean
    public Docket createRestApi() {
        String basePck = FiskSystemCenterApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePck))
                .paths(PathSelectors.any())
                .build()
                .tags(new Tag(PERMISSION,"权限管理"))
                .tags(new Tag(ROLE_INFO,"角色管理"))
                .tags(new Tag(SERVICE_REGISTRY,"服务注册"))
                .tags(new Tag(USER,"用户中心服务"))
                .tags(new Tag(SYSTEM,"系统Logo信息服务"))
                .tags(new Tag(DATAVIEW,"视图过滤"))
                .tags(new Tag(KEYWORD,"SQL关键字管理"))
                .tags(new Tag(UPLOAD,"上传管理"))
                .tags(new Tag(DATASOURCE,"数据源管理"))
                .tags(new Tag(EMAIL_SERVER_CONTROLLER, "邮件服务器API"))
                .tags(new Tag(LICENSE_CONTROLLER, "license许可证管理"))
                .tags(new Tag(SQLFACTORY_CONTROLLER, "SQL语句校验处理"))
                .tags(new Tag(SYSTEM_VERSION_CONTROLLER,"平台版本信息管理"))
                .tags(new Tag(SYSTEM_LOG_CONTROLLER,"平台服务日志管理"))
                .tags(new Tag(AUDIT_LOGS,"审计日志"))
                .tags(new Tag(DATA_SECURITY,"数据安全"))
                .tags(new Tag(Tset,"测试"))
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
