package com.fisk.chartvisual.config;

import com.fisk.chartvisual.FkChartVisualApplication;
import com.fisk.common.core.constants.SystemConstants;
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

    public static final String TAG_1 = "图表管理1.0";
    public static final String TAG_2 = "中国地图";
    public static final String TAG_3 = "组件";
    public static final String TAG_4 = "数据服务";

    public static final String TAG_5 = "数据源管理";

    public static final String TAG_6 = "可视化2.0图标管理";
    public static final String TAG_7 = "数据集表";
    public static final String TAG_8 = "文件夹管理";
    public static final String TAG_9 = "系统页索引";
    public static final String TAG_10 = "可视化";



    @Bean
    public Docket createRestApi() {
        String basePck = FkChartVisualApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .tags(new Tag(TAG_1,"图表管理1.0"))
                .tags(new Tag(TAG_2,"中国地图"))
                .tags(new Tag(TAG_3,"组件"))
                .tags(new Tag(TAG_4,"数据服务"))
                .tags(new Tag(TAG_5,"数据源管理"))
                .tags(new Tag(TAG_6,"可视化2.0图标管理"))
                .tags(new Tag(TAG_7,"数据集表"))
                .tags(new Tag(TAG_8,"文件夹管理"))
                .tags(new Tag(TAG_9,"系统页索引"))
                .tags(new Tag(TAG_10,"可视化"))
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
