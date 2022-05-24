package com.fisk.task.dto.pipeline;

import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author cfk
 */
@Data
public class NifiStageDTO extends BasePO {
    @ApiModelProperty(value = "组件id")
    public int componentId;
    @ApiModelProperty(value = "查询阶段")
    public int queryPhase;
    @ApiModelProperty(value = "转换阶段")
    public int transitionPhase;
    @ApiModelProperty(value = "插入阶段")
    public int insertPhase;
    @ApiModelProperty(value = "日志")
    public String comment;
    @ApiModelProperty(value = "pipelineTableLogId")
    public Integer pipelineTableLogId;

}
