package com.fisk.dataaccess.dto.api;

import lombok.Data;

/**
 * @author: cfk
 * CreateTime: 2022/06/09 11:05
 * Description:
 */
@Data
public class PipelApiDispatchDTO {
    public String workflowId;
    public long appId;
    public long apiId;

}