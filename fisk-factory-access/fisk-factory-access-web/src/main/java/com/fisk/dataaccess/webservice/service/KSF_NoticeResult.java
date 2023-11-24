package com.fisk.dataaccess.webservice.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 前置机定制接口
 * ksf接口的统一返回类型
 */
@Data
public class KSF_NoticeResult {

    /**
     * 执行情况
     */
    @ApiModelProperty(value = "执行情况")
    private String STATUS;

    /**
     * 文本信息
     */
    @ApiModelProperty(value = "文本信息")
    private String INFOTEXT;

//    /**
//     * 物料凭证编号
//     */
//    @ApiModelProperty(value = "物料凭证编号")
//    private String MBLNR;

}
