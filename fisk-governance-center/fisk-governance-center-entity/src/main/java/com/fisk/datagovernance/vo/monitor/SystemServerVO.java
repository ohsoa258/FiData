package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.joda.time.DateTime;

/**
 * @Author: wangjian
 * @Date: 2023-09-21
 * @Description:
 */
@Data
public class SystemServerVO {
    @ApiModelProperty(value = "ip")
    private String ip;
    @ApiModelProperty(value = "服务名称")
    private String name;
    @ApiModelProperty(value = "服务端口")
    private Integer port;
    @ApiModelProperty(value = "状态")
    private String status;
    @ApiModelProperty(value = "最后执行时间")
    private String lastDate;
}
