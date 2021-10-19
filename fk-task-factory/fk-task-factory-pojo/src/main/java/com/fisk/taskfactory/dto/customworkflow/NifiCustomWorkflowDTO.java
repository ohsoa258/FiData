package com.fisk.taskfactory.dto.customworkflow;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowDTO {

    public long id;
    public String workflowId;
    public String workflowName;
    public int status;
}
