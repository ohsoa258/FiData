package com.fisk.datagovernance.entity.monitor;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author wangjian
 * @date 2024-10-11 15:24:49
 */
@TableName("tb_server_monitor_type")
@Data
public class ServerMonitorTypePO extends BasePO {

    @ApiModelProperty(value = "自定义类型")
    private String serverType;
}
