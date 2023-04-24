package com.fisk.task.config;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.task.FkTaskApplication;
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

    public static final String DispatchLog = "调度日志";
    public static final String MessageLog = "消息日志";
    public static final String Nifi = "Nifi";
    public static final String OlapTask = "Olap任务";
    public static final String PipelineTask = "管道任务";
    public static final String PublishTask = "发布任务";
    public static final String TableTopic = "表主题";
    public static final String TaskLog = "任务日志";
    public static final String TBETLIncremental = "TBETL增量";

    @Bean
    public Docket createRestApi() {
        String basePck = FkTaskApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePck))
                .paths(PathSelectors.any())
                .build()
                .tags(new Tag(DispatchLog,"调度日志"))
                .tags(new Tag(MessageLog,"消息日志"))
                .tags(new Tag(Nifi,"Nifi"))
                .tags(new Tag(OlapTask,"Olap任务"))
                .tags(new Tag(PipelineTask,"管道任务"))
                .tags(new Tag(PublishTask,"发布任务"))
                .tags(new Tag(TableTopic,"表主题"))
                .tags(new Tag(TaskLog,"任务日志"))
                .tags(new Tag(TBETLIncremental,"TBETL增量"))
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
