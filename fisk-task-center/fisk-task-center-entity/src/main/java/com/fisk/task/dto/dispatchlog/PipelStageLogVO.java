package com.fisk.task.dto.dispatchlog;

import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class PipelStageLogVO extends BasePO {
    @ApiModelProperty(value = "阶段批次号")
    public String stateTraceId;
    @ApiModelProperty(value = "日志内容")
    public String msg;
    @ApiModelProperty(value = "日志类别")
    public Integer type;
    @ApiModelProperty(value = "日志类别名称")
    public String typeName;
}
