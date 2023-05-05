package com.fisk.datafactory.config;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.datafactory.FiskFactoryDispatchApplication;
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

//    public static final String TASK_SCHEDULE = "task-schedule-controller";
//    public static final String NIFI_CUSTOM_WORKFLOW = "nifi-custom-workflow-controller";
//    public static final String NIFI_COMPONENT = "nifi-component-controller";
//    public static final String NIFI_CUSTOM_WORKFLOW_DETAIL = "nifi-custom-workflow-detail-controller";
//    public static final String NIFI_PORT = "nifi-port-controller";
//    public static final String SYSTEM_WEB_INDEX = "SystemWebIndex-controller";
//    public static final String PIPELINE_PROCESS_MONITOR = "Pipeline-Process-Monitor-Controller";

    public static final String TASK_SCHEDULE = "调度中心";
    public static final String NIFI_CUSTOM_WORKFLOW = "NIFI数据管道";
    public static final String NIFI_COMPONENT = "可视化视图-组件列表";
    public static final String NIFI_CUSTOM_WORKFLOW_DETAIL = "NIFI数据管道详情";
    public static final String NIFI_PORT = "提供nifi参数";
    public static final String SYSTEM_WEB_INDEX = "首页API";
    public static final String PIPELINE_PROCESS_MONITOR = "管道流程监控";

    public static final String DataFactory = "数据工厂";
    public static final String DispatchEmail = "映射邮件";
    public static final String TaskDataSourceConfig = "任务数据源配置";
    public static final String TaskSetting = "任务设置";


    @Bean
    public Docket createRestApi() {
        String basePck = FiskFactoryDispatchApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .tags(new Tag(TASK_SCHEDULE, "调度中心"))
                .tags(new Tag(NIFI_CUSTOM_WORKFLOW, "NIFI数据管道"))
                .tags(new Tag(NIFI_COMPONENT, "可视化视图-组件列表"))
                .tags(new Tag(NIFI_CUSTOM_WORKFLOW_DETAIL, "NIFI数据管道详情"))
                .tags(new Tag(NIFI_PORT, "提供nifi参数"))
                .tags(new Tag(SYSTEM_WEB_INDEX, "首页API"))
                .tags(new Tag(PIPELINE_PROCESS_MONITOR, "管道流程监控"))
                .tags(new Tag(DataFactory, "数据工厂"))
                .tags(new Tag(DispatchEmail, "映射邮件"))
                .tags(new Tag(TaskDataSourceConfig, "任务数据源配置"))
                .tags(new Tag(TaskSetting, "任务设置"))
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
