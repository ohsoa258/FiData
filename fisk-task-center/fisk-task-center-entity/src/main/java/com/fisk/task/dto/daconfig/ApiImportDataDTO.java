package com.fisk.task.dto.daconfig;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

/**
 * @author cfk
 * @version 1.3
 * @description
 * @date 2022/5/1 17:30
 */
@Data
public class ApiImportDataDTO extends MQBaseDTO {


    public Long appId;


    public Long apiId;


    public String workflowId;


    public String pipelApiDispatch;
}
