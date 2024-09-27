package com.fisk.datafactory.dto.customworkflow;

import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-09-20
 * @Description:
 */
@Data
public class WorkFlowQueryDTO {

    public List<Integer> tableIds;
    public Integer tableType;
}
