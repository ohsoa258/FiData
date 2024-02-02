package com.fisk.datamanagement.config;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.datamanagement.FKDataManagementApplication;
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
 * @author JianWenYang
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
//
//    public static final String LABEL_CATEGORY = "label-category-controller";
//    public static final String LABEL = "label-controller";
//    public static final String META_DATA_ENTITY = "metadata-entity-controller";
//    public static final String CLASSIFICATION = "classification-controller";
//    public static final String GLOSSARY = "glossary-controller";
//    public static final String BUSINESS_META_DATA = "业务元数据接口";
//    public static final String DATA_ASSETS = "data_assets-controller";
//    public static final String DATA_QUALITY = "data-quality-controller";
//    public static final String DATA_MASKING = "data-masking-controller";
//    public static final String SYNCHRONIZATION_DATA = "血缘补偿同步接口";
//    public static final String PROCESS = "process-controller";
//    public static final String GLOBAL_SEARCH = "globalSearch-controller";
//    public static final String DATA_LOGGING = "dataLogging-controller";


    public static final String LABEL_CATEGORY = "标签类目";
    public static final String LABEL = "标签管理";
    public static final String META_DATA_ENTITY = "元数据对象";
    public static final String CLASSIFICATION = "业务分类";
    public static final String GLOSSARY = "术语库";
    public static final String BUSINESS_META_DATA = "业务元数据接口";
    public static final String DATA_ASSETS = "数据资产";
    public static final String DATA_QUALITY = "数据质量";
    public static final String DATA_MASKING = "数据脱敏";
    public static final String SYNCHRONIZATION_DATA = "血缘补偿同步接口";
    public static final String PROCESS = "元数据血缘连线处理";
    public static final String GLOBAL_SEARCH = "全局搜索";
    public static final String DATA_LOGGING = "数据报表记录数";
    public static final String OperateLog = "操作日志";
    public static final String MetaData = "元数据";
    public static final String MetadataAttribute = "元数据属性";
    public static final String AZURE_SERVER = "Azure服务 OpenAI";
    public static final String EXPORT_TEMPLATE = "元数据导出模板";
    public static final String BUSINESS_Category = "指标数据对象";

    public static final String STANDARDS = "数据标准";

    public static final String CODESET = "代码集";


    @Bean
    public Docket createRestApi() {
        String basePck = FKDataManagementApplication.class.getPackage().getName();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePck))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(apiKey())
                .tags(new Tag(LABEL_CATEGORY, "标签类目"))
                .tags(new Tag(LABEL, "标签管理"))
                .tags(new Tag(META_DATA_ENTITY, "元数据对象"))
                .tags(new Tag(CLASSIFICATION, "业务分类"))
                .tags(new Tag(GLOSSARY, "术语库"))
                .tags(new Tag(BUSINESS_META_DATA, "业务元数据"))
                .tags(new Tag(DATA_ASSETS, "数据资产"))
                .tags(new Tag(DATA_QUALITY, "数据质量"))
                .tags(new Tag(DATA_MASKING, "数据脱敏"))
                .tags(new Tag(SYNCHRONIZATION_DATA, "手动同步元数据"))
                .tags(new Tag(PROCESS, "元数据血缘连线处理"))
                .tags(new Tag(GLOBAL_SEARCH, "全局搜索"))
                .tags(new Tag(DATA_LOGGING, "数据报表记录数"))
                .tags(new Tag(OperateLog, "操作日志"))
                .tags(new Tag(MetaData, "元数据"))
                .tags(new Tag(MetadataAttribute, "元数据属性"))
                .tags(new Tag(AZURE_SERVER, "Azure服务 OpenAI"))
                .tags(new Tag(EXPORT_TEMPLATE, "元数据导出模板"))
                .tags(new Tag(STANDARDS,"数据标准"))
                .tags(new Tag(BUSINESS_Category, "指标数据对象"))
                .tags(new Tag(CODESET,"代码集"))
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
                .title("数据治理接口文档")
                .description("Create by Lock")
                .version("1.0.0测试版")
                .build();
    }

}
