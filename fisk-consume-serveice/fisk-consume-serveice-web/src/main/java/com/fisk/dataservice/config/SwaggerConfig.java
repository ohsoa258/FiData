package com.fisk.dataservice.config;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.dataservice.FiskConsumeServeiceApplication;
import com.google.common.base.Predicates;
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

//    public static final String TAG_1 = "datasource-controller";
//    public static final String TAG_2 = "appregister-controller";
//    public static final String TAG_3 = "apiregister-controller";
//    public static final String TAG_4 = "apiservice-controller";
//    public static final String TAG_5 = "logs-controller";
//    public static final String TAG_6 = "dataAnalysisView-controller";
//    public static final String TAG_7 = "tableservice-controller";
//    public static final String TAG_8 = "serviceAnalyse-controller";
//    public static final String TAG_9 = "apiTableViewService-controller";
public static final String TAG_1 = "数据源接口";
    public static final String TAG_2 = "应用接口";
    public static final String TAG_3 = "API接口";
    public static final String TAG_4 = "本地服务接口";
    public static final String TAG_5 = "数据服务日志";
    public static final String TAG_6 = "视图服务接口";
    public static final String TAG_7 = "表服务接口";
    public static final String TAG_8 = "数据分析服务";
    public static final String TAG_9 = "数据服务元数据调用";
    public static final String TAG_10 = "代理服务接口";
    public static final String TAG_11 = "表服务API接口";
    public static final String TAG_12 = "主数据版本sql";
    public static final String TQ_TAG_13 = "塘桥数据质量管理";
    public static final String TQ_TAG_14 = "塘桥审计数据安全管理";

    public static final String TQ_TAG_15 = "塘桥审计数据安全接口管理";
    public static final String TQ_TAG_16 = "塘桥数据订阅服务";
    public static final String TQ_TAG_17 = "塘桥数据订阅Api服务";
    public static final String TQ_TAG_18 = "塘桥中心库实施";
    public static final String TQ_TAG_19 = "塘桥中心库实施Api";






    public static final String TQ_TAG_33 = "浦东塘桥-数据集成管理";
    public static final String TQ_TAG_34 = "浦东塘桥-数据质量任务管理";
    public static final String TQ_TAG_35 = "浦东塘桥-标签数据管理";
    public static final String TQ_TAG_36 = "浦东塘桥-问题库";
    public static final String TQ_TAG_37 = "浦东塘桥-数据流程调度";
    public static final String TQ_TAG_38 = "浦东塘桥-数据质量核查";
    public static final String TQ_TAG_39 = "浦东塘桥-日志存储";
    public static final String TQ_TAG_40 = "浦东塘桥-日志收集";
    public static final String TQ_TAG_41 = "浦东塘桥-日志分析";
    public static final String TQ_TAG_42 = "浦东塘桥-数据访问视图和权限管理";
    public static final String TQ_TAG_43 = "浦东塘桥-数据脱敏加密管理";

    @Bean
    public Docket createRestApi() {
        String basePck = FiskConsumeServeiceApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .tags(new Tag(TAG_1,"数据源接口"))
                .tags(new Tag(TAG_2,"应用接口"))
                .tags(new Tag(TAG_3,"API接口"))
                .tags(new Tag(TAG_4,"本地服务接口"))
                .tags(new Tag(TAG_5,"数据服务日志"))
                .tags(new Tag(TAG_8,"服务数据分析"))
                .tags(new Tag(TAG_6,"视图服务接口"))
                .tags(new Tag(TAG_7,"表服务接口"))
                .tags(new Tag(TAG_9,"数据服务元数据调用"))
                .tags(new Tag(TAG_10,"代理服务接口"))
                .tags(new Tag(TAG_11,"表服务API接口"))
                .tags(new Tag(TAG_12,"主数据版本sql"))
                .tags(new Tag(TQ_TAG_13,"塘桥数据质量管理"))
                .tags(new Tag(TQ_TAG_14,"塘桥应用管理"))
                .tags(new Tag(TQ_TAG_15,"塘桥审计数据安全接口管理"))
                .tags(new Tag(TQ_TAG_16,"塘桥数据订阅服务"))
                .tags(new Tag(TQ_TAG_17,"塘桥数据订阅Api服务"))
                .tags(new Tag(TQ_TAG_18,"塘桥中心库实施"))
                .tags(new Tag(TQ_TAG_19,"塘桥中心库实施Api"))
                .tags(new Tag(TQ_TAG_33,"浦东塘桥-数据集成管理"))
                .tags(new Tag(TQ_TAG_34,"浦东塘桥-数据质量任务管理"))
                .tags(new Tag(TQ_TAG_35,"浦东塘桥-标签数据管理"))
                .tags(new Tag(TQ_TAG_36,"浦东塘桥-问题库"))
                .tags(new Tag(TQ_TAG_37,"浦东塘桥-数据流程调度"))
                .tags(new Tag(TQ_TAG_38,"浦东塘桥-数据质量核查"))
                .tags(new Tag(TQ_TAG_39,"浦东塘桥-日志存储"))
                .tags(new Tag(TQ_TAG_40,"浦东塘桥-日志收集"))
                .tags(new Tag(TQ_TAG_41,"浦东塘桥-日志分析"))
                .tags(new Tag(TQ_TAG_42,"浦东塘桥-数据访问视图和权限管理"))
                .tags(new Tag(TQ_TAG_43,"浦东塘桥-数据脱敏加密管理"))
                .select()
                .apis(Predicates.or(
                                RequestHandlerSelectors.basePackage(basePck),
                                RequestHandlerSelectors.basePackage("pd.tangqiao")
                        ))
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
