package com.fisk.datagovernance.config;

import com.fisk.common.core.constants.SystemConstants;
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

//    public static final String TEST = "test-controller";
//    public static final String BUSINESS_FILTER_CONTROLLER = "business-filter-controller";
//    public static final String DATA_CHECK_CONTROLLER = "data-check-controller";
//    public static final String DATASOURCE_CONTROLLER = "datasource-controller";
//    public static final String LIFECYCLE_CONTROLLER = "lifecycle-controller";
//    public static final String QUALITY_REPORT_CONTROLLER = "quality_report-controller";
//    public static final String TEMPLATE_CONTROLLER = "template-controller";
//    public static final String DATA0PSLOG_CONTROLLER = "data_ops_log-controller";
//    public static final String DATA_QUALITY_CLIENT_CONTROLLER = "data-quality-client-controller";
//    public static final String TABLE_SECURITY_CONFIG_CONTROLLER = "table-security-config-controller";
//    public static final String DATA_MASKING_CONFIG_CONTROLLER = "data_masking_config_controller";
//    public static final String ROW_SECURITY_CONFIG_CONTROLLER = "row_security_config_controller";
//    public static final String USER_GROUP_INFO = "user-group-info-controller";
//    public static final String USER_GROUP_ASSIGNMENT = "user-group-assignment-controller";
//    public static final String COLUMN_SECURITY_CONFIG = "column-security-config-controller";
//    public static final String INTELLIGENT_DISCOVERY = "intelligent-discovery-controller";
    public static final String TEST = "测试";
    public static final String BUSINESS_FILTER_CONTROLLER = "清洗规则API";
    public static final String DATA_CHECK_CONTROLLER = "校验规则API";
    public static final String DATASOURCE_CONTROLLER = "数据源API";
    public static final String LIFECYCLE_CONTROLLER = "回收规则API";
    public static final String QUALITY_REPORT_CONTROLLER = "质量报告API";
    public static final String TEMPLATE_CONTROLLER = "模板配置API";
    public static final String DATA0PSLOG_CONTROLLER = "数据运维日志API";
    public static final String DATA_QUALITY_CLIENT_CONTROLLER = "数据质量服务接口API";
    public static final String TABLE_SECURITY_CONFIG_CONTROLLER = "表级安全API";
    public static final String DATA_MASKING_CONFIG_CONTROLLER = "数据脱敏API";
    public static final String ROW_SECURITY_CONFIG_CONTROLLER = "行级安全API";
    public static final String USER_GROUP_INFO = "用户组";
    public static final String USER_GROUP_ASSIGNMENT = "用户组用户";
    public static final String COLUMN_SECURITY_CONFIG = "列级安全";
    public static final String INTELLIGENT_DISCOVERY = "数据安全-智能发现API";

    public static final String COLUMN_User_Assignment = "列用户分配";
    public static final String Row_User_Assignment = "行用户分配";

    @Bean
    public Docket createRestApi() {
        String basePck = FkDataGovernanceApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .tags(new Tag(TEST, "测试"))
                .tags(new Tag(BUSINESS_FILTER_CONTROLLER, "清洗规则API"))
                .tags(new Tag(DATA_CHECK_CONTROLLER, "校验规则API"))
                .tags(new Tag(DATASOURCE_CONTROLLER, "数据源API"))
                .tags(new Tag(LIFECYCLE_CONTROLLER, "回收规则API"))
                .tags(new Tag(QUALITY_REPORT_CONTROLLER, "质量报告API"))
                .tags(new Tag(TEMPLATE_CONTROLLER, "模板配置API"))
                .tags(new Tag(DATA0PSLOG_CONTROLLER, "数据运维日志API"))
                .tags(new Tag(DATA_QUALITY_CLIENT_CONTROLLER, "数据质量服务接口API"))
                .tags(new Tag(INTELLIGENT_DISCOVERY, "数据安全-智能发现API"))
                .tags(new Tag(TABLE_SECURITY_CONFIG_CONTROLLER, "表级安全API"))
                .tags(new Tag(DATA_MASKING_CONFIG_CONTROLLER, "数据脱敏API"))
                .tags(new Tag(ROW_SECURITY_CONFIG_CONTROLLER, "行级安全API"))
                .tags(new Tag(USER_GROUP_INFO, "用户组"))
                .tags(new Tag(USER_GROUP_ASSIGNMENT, "用户组用户"))
                .tags(new Tag(COLUMN_SECURITY_CONFIG, "列级安全"))
                .tags(new Tag(COLUMN_User_Assignment, "列用户分配"))
                .tags(new Tag(Row_User_Assignment, "行用户分配"))
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
