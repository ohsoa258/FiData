package com.fisk.datagovernance.entity.monitor;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@TableName("tb_monitor_drop_down_box")
@Data
public class MonitorDropDownBoxPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "下拉框名称")
    private String name;

    @ApiModelProperty(value = "值")
    private Integer value;

    @ApiModelProperty(value = "单位:1分 2小时 3天")
    private Integer unit;

}
