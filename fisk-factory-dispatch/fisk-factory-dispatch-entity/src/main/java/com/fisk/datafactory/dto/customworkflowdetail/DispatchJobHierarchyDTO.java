package com.fisk.datafactory.dto.customworkflowdetail;

import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author cfk
 */
@Data
public class DispatchJobHierarchyDTO {
    public long id;
    public String jobName;
    public List<Long> inport;
    public List<Long> outport;
    /**
     * job运行状态,包括未运行,运行成功,运行失败
     */
    public NifiStageTypeEnum jobStatus;
    /**
     * 是否处理,true已处理,false未处理
     */
    public boolean jobProcessed;
    /**
     * 是否是支线最后一个job
     */
    public boolean last;

}
