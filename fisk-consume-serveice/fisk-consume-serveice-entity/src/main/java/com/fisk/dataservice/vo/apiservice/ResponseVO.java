package com.fisk.dataservice.vo.apiservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据集
 * @date 2022/1/18 11:15
 */
@Data
public class ResponseVO {
    /**
     * 当前页，起始页为第一页
     */
    @ApiModelProperty(value = "当前页")
    public Integer current;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "每页大小")
    public Integer size;

    /**
     * 总条数
     */
    @ApiModelProperty(value = "total")
    public Integer total;

    /**
     * 总页数
     */
    @ApiModelProperty(value = "page")
    public Integer page;
//    /**
//     * 密钥
//     */
//    @ApiModelProperty(value = "密钥")
//    public String encryptKey;

    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "查询数据集")
    public List<Object> dataArray;
}
