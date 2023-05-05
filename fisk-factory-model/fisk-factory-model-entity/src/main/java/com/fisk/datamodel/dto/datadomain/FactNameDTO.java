package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/16 15:06
 * 事实表 三级
 */
@Data
public class FactNameDTO {
    @ApiModelProperty(value = "事实Id")
    public Long factId;
    @ApiModelProperty(value = "事实表名")
    public String factTabName;
    /**
     * 3.业务域 4.业务流程  5.事实表
     */
    @ApiModelProperty(value = "标记 3.业务域 4.业务流程  5.事实表")
    public Integer flag;
    /**
     * 业务过程id
     */
    @ApiModelProperty(value = "业务过程id")
    public Long pid;
}
