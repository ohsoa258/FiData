package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjian
 * @date 2023-10-07 10:54:30
 */
@TableName("tb_table_api_log")
@Data
public class TableApiLogPO extends BasePO {

    @ApiModelProperty(value = "apiID")
    private Integer apiId;

    @ApiModelProperty(value = "消费数量")
    private Integer number;

    @ApiModelProperty(value = "消费状态")
    private Integer status;

    @ApiModelProperty(value = "是否是重点接口 0否，1是")
    private Integer importantInterface;

    @ApiModelProperty(value = "本次消费的批次号")
    private String fidataBatchCode;

    @ApiModelProperty(value = "消费信息")
    private String msg;

    @ApiModelProperty(value = "历史状态")
    private Integer state;

    @ApiModelProperty(value = "重发次数")
    private int retryNumber;
}
