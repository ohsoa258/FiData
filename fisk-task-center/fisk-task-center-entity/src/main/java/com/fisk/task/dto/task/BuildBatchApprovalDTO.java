package com.fisk.task.dto.task;

import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.model.ApprovalDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-04-11
 * @Description: 批量审批
 */
@Data
public class BuildBatchApprovalDTO extends MQBaseDTO {

    public List<ApprovalDTO> data;
}
