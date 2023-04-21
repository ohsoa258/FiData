package com.fisk.datafactory.dto.customworkflowdetail;

import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author cfk
 */
@Data
public class DispatchJobHierarchyDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "工作名称")
    public String jobName;

    @ApiModelProperty(value = "输入")
    public List<Long> inport;

    @ApiModelProperty(value = "输出")
    public List<Long> outport;
    /**
     * job运行状态,包括未运行,运行成功,运行失败
     */
    @ApiModelProperty(value = "job运行状态,包括未运行,运行成功,运行失败")
    public NifiStageTypeEnum jobStatus;
    /**
     * 是否处理,true已处理,false未处理
     */
    @ApiModelProperty(value = "是否处理,true已处理,false未处理")
    public boolean jobProcessed;
    /**
     * 是否是支线最后一个job
     */
    @ApiModelProperty(value = "是否是支线最后一个job")
    public boolean last;

    /**
     * job_trace_id 预先生成
     */
    @ApiModelProperty(value = "job_trace_id 预先生成")
    public String jobTraceId;

    /**
     * 是否禁用
     */
    @ApiModelProperty(value = "是否禁用")
    public Boolean forbidden;

}
