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

}
