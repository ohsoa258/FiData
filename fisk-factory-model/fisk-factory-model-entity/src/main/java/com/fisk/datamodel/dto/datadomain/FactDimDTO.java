package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/9/10 14:42
 * 事实表 三级
 */
@Data
public class FactDimDTO {
    @ApiModelProperty(value = "事实Id")
    public Long factId;
    @ApiModelProperty(value = "事实表名")
    public String factTabName;
    /**
     * 7.业务域  8.维度  9.业务过程  10.事实表
     */
    @ApiModelProperty(value = "标记 7.业务域  8.维度  9.业务过程  10.事实表")
    public Integer flag;
    /**
     * 上一级id
     */
    @ApiModelProperty(value = "上一级id")
    public Long pid;
}
