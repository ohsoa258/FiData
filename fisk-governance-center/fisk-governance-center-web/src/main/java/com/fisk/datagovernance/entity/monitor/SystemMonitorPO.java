package com.fisk.datagovernance.entity.monitor;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

@TableName("tb_system_monitor")
@Data
public class SystemMonitorPO extends BasePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ip")
    private String ip;

    @ApiModelProperty(value = "运行时间")
    private String upTime;

    @ApiModelProperty(value = "cpu核心数")
    private Integer cpuCores;

    @ApiModelProperty(value = "内存大小")
    private Integer rawTotal;

    @ApiModelProperty(value = "缓存大小")
    private Integer swapTotal;

    @ApiModelProperty(value = "cpu使用率")
    private Double cpuBusy;

    @ApiModelProperty(value = "内存使用大小")
    private Double rawUsed;

    @ApiModelProperty(value = "缓存使用大小")
    private Double swapUsed;

}
