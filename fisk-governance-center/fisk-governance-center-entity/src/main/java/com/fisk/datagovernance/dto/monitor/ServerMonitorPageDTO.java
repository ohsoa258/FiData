package com.fisk.datagovernance.dto.monitor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.monitor.ServerTableVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-11
 * @Description:
 */
@Data
public class ServerMonitorPageDTO {

    @ApiModelProperty(value = "ip")
    public String ip;

    @ApiModelProperty(value = "分钟数、小时数、天数")
    public Integer number;

    @ApiModelProperty(value = "单位：分、小时、天")
    public Integer type;

    @ApiModelProperty(value = "服务名称")
    public String name;

    @ApiModelProperty(value = "服务端口")
    public Integer port;

    @ApiModelProperty(value = "page")
    public Page<ServerTableVO> page;
}
