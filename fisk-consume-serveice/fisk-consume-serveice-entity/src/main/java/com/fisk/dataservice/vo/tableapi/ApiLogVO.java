package com.fisk.dataservice.vo.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2023-12-15
 * @Description:
 */
@Data
public class ApiLogVO {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "apiId")
    private Integer apiId;
    @ApiModelProperty(value = "消费返回信息")
    private String msg;
    @ApiModelProperty(value = "消费状态")
    private Integer status;
    @ApiModelProperty(value = "批次号")
    private String fidataBatchCode;
    @ApiModelProperty(value = "重发次数")
    private int retryumber;
    @ApiModelProperty(value = "历史状态")
    private Integer state;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
