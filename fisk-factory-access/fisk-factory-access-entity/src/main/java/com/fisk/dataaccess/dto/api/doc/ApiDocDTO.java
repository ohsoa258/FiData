package com.fisk.dataaccess.dto.api.doc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description API文档
 * @date 2022/2/3 14:08
 */
@Data
public class ApiDocDTO {
    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    public String title;

    /**
     * 文档版本
     */
    @ApiModelProperty(value = "文档版本")
    public String docVersion;

    /**
     * 发行公司
     */
    @ApiModelProperty(value = "发行公司")
    public String isuCompany;

    /**
     * 发行日期
     */
    @ApiModelProperty(value = "发行日期")
    public String isuDate;

    /**
     * 文档目的
     */
    @ApiModelProperty(value = "文档目的")
    public String docPurpose;

    /**
     * 读者对象
     */
    @ApiModelProperty(value = "读者对象")
    public String readers;

    /**
     * 接口对接规范
     */
    @ApiModelProperty(value = "接口对接规范")
    public String standard;

    /**
     * 接口对接规范_查询
     */
    @ApiModelProperty(value = "接口对接规范_查询")
    public String standard_query;

    /**
     * 登录授权规范
     */
    @ApiModelProperty(value = "登录授权规范")
    public String authStandard;

    /**
     * 测试环境
     */
    @ApiModelProperty(value = "测试环境")
    public String uatAddress;

    /**
     * 正式环境
     */
    @ApiModelProperty(value = "正式环境")
    public String prdAddress;

    /**
     * 文档目录
     */
    @ApiModelProperty(value = "文档目录")
    public List<ApiCatalogueDTO> apiCatalogueDTOS;

    /**
     * API基础信息
     */
    @ApiModelProperty(value = "API基础信息")
    public List<ApiBasicInfoDTO> apiBasicInfoDTOS;

    /**
     * 联系人
     */
    @ApiModelProperty(value = "联系人")
    public List<ApiContactsDTO> apiContactsDTOS;

    /**
     * API版本
     */
    @ApiModelProperty(value = "API版本")
    public List<ApiVersionDTO> apiVersionDTOS;

    /**
     * pushData Json格式
     */
    @ApiModelProperty(value = "pushData Json格式")
    public String pushDataJson;

    /**
     * pushData List
     */
    @ApiModelProperty(value = "pushData List")
    public List<ApiResponseDTO> pushDataDtos;

    /**
     * 调用API C#代码示例
     */
    @ApiModelProperty(value = "调用API C#代码示例")
    public String apiCodeExamplesNet;

    /**
     * 调用API JAVA代码示例
     */
    @ApiModelProperty(value = "调用API JAVA代码示例")
    public String apiCodeExamplesJava;

    /**
     * API返回代码示例
     */
    @ApiModelProperty(value = "API返回代码示例")
    public List<ApiResponseCodeDTO> apiResponseCodeDTOS;
}
