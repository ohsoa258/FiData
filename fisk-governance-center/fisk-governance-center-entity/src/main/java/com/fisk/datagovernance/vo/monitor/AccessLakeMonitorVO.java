package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-12-21
 * @Description:
 */
@Data
public class AccessLakeMonitorVO {
    @ApiModelProperty(value = "数据来源总数")
    private Integer sourceTotal;
    @ApiModelProperty(value = "缓存总数")
    private Integer catchTotal;
    @ApiModelProperty(value = "目标数据总数")
    private Integer targetTotal;
    @ApiModelProperty(value = "详情")
    private List<AccessLakeMonitorDetailVO> detailVO;
}
