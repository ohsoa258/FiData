package com.fisk.datagovernance.vo.monitor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-07-10
 * @Description:
 */
@Data
public class ServerMonitorDetailVO {

    @ApiModelProperty(value = "状态更新时间")
    private String statusDate;

    @ApiModelProperty(value = "持续时间")
    private String persistentDate;

    @ApiModelProperty(value = "时移ping")
    private List<DelayPingVO> delayPingVOList;

    @ApiModelProperty(value = "服务信息")
    private Page<ServerTableVO> serverTableVOList;
}
