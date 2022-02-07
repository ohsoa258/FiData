package com.fisk.dataservice.dto.api.doc;

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
    public String title;

    /**
     * 文档版本
     */
    public String docVersion;

    /**
     * 发行公司
     */
    public  String isuCompany;

    /**
     * 发行日期
     */
    public  String isuDate;

    /**
     * 文档目的
     */
    public  String docPurpose;

    /**
     * 读者对象
     */
    public  String readers;

    /**
     * 接口对接规范
     */
    public  String standard;

    /**
     * 登录授权规范
     */
    public  String authStandard;

    /**
     * 测试环境
     */
    public  String uatAddress;

    /**
     * 正式环境
     */
    public  String prdAddress;

    /**
     * 文档目录
     */
    public List<ApiCatalogueDTO> apiCatalogueDTOS;

    /**
     * API基础信息
     */
    public  List<ApiBasicInfoDTO> apiBasicInfoDTOS;

    /**
     * 联系人
     */
    public  List<ApiContactsDTO> apiContactsDTOS;

    /**
     * API版本
     */
    public List<ApiVersionDTO> apiVersionDTOS;

    /**
     * API返回代码示例
     */
    public  List<ApiResponseCodeDTO> apiResponseCodeDTOS;
}
