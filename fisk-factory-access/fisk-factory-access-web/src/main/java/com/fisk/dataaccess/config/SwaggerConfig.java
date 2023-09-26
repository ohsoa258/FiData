package com.fisk.dataaccess.config;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.dataaccess.FiskFactoryAccessApplication;
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
//
//    public static final String TAG_1 = "app-registration-controller";
//    public static final String TAG_2 = "physical-table-controller";
//    public static final String TAG_3 = "data-access-controller";
//    public static final String TAG_4 = "data-review-controller";
//    public static final String TAG_5 = "table-fields-controller";
//    public static final String TAG_6 = "table-access-controller";
//    public static final String TAG_7 = "datasource-controller";
//    public static final String TAG_8 = "systemWebIndex-controller";
//    public static final String FTP = "ftp-controller";
//    public static final String TABLE_HISTORY = "table-history-controller";
//    public static final String API_CONFIG = "api-config-controller";
//    public static final String API_PARAMETER = "api-parameter-controller";
//    public static final String DATA_TARGET = "data-target-controller";
//    public static final String DATA_TARGET_APP = "data-target-app-controller";
//    public static final String SAVEPOINT_HISTORY = "savepoint-history-controller";
//    public static final String DATA_OPS = "data-ops-controller";
//    public static final String SFTP = "sftp-controller";
//
//    public static final String STATEMENT = "statement-controller";


    public static final String TAG_1 = "应用注册";
    public static final String TAG_2 = "物理表";
    public static final String TAG_3 = "应用注册树";
    public static final String TAG_4 = "数据查看";
    public static final String TAG_5 = "表字段";
    public static final String TAG_6 = "物理表单表CRUD";
    public static final String TAG_7 = "数据源";
    public static final String TAG_8 = "首页API";
    public static final String FTP = "FTP数据源";
    public static final String TABLE_HISTORY = "表发布历史";
    public static final String API_CONFIG = "api配置";
    public static final String API_PARAMETER = "非实时api请求参数";
    public static final String DATA_TARGET = "数据目标";
    public static final String DATA_TARGET_APP = "数据目标应用";
    public static final String SAVEPOINT_HISTORY = "检查点历史记录";
    public static final String DATA_OPS = "数据运维";
    public static final String SFTP = "sftp";

    public static final String STATEMENT = "资产报表";
    public static final String API_STATE = "实时API接口状态控制器";
    public static final String MAIN_PAGE = "数据接入首页展示";
    public static final String test = "测试";



    @Bean
    public Docket createRestApi() {
        String basePck = FiskFactoryAccessApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .tags(new Tag(TAG_1, "应用注册"))
                .tags(new Tag(TAG_2, "物理表"))
                .tags(new Tag(TAG_3, "应用注册tree"))
                .tags(new Tag(TAG_4, "数据查看"))
                .tags(new Tag(TAG_5, "表字段"))
                .tags(new Tag(TAG_6, "物理表单表CRUD"))
                .tags(new Tag(TAG_7, "数据源"))
                .tags(new Tag(TAG_8, "首页API"))
                .tags(new Tag(FTP, "FTP数据源"))
                .tags(new Tag(TABLE_HISTORY, "表发布历史"))
                .tags(new Tag(API_CONFIG, "api配置"))
                .tags(new Tag(API_PARAMETER, "非实时api请求参数"))
                .tags(new Tag(DATA_TARGET, "数据目标"))
                .tags(new Tag(DATA_TARGET_APP, "数据目标应用"))
                .tags(new Tag(SAVEPOINT_HISTORY, "检查点历史记录"))
                .tags(new Tag(DATA_OPS, "数据运维"))
                .tags(new Tag(SFTP, "sftp"))
                .tags(new Tag(STATEMENT,"资产报表"))
                .tags(new Tag(API_STATE,"实时API接口状态控制器"))
                .tags(new Tag(MAIN_PAGE,"数据接入首页展示"))
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
