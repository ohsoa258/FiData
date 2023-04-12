package com.fisk.datamanagement.dto.metadataentityoperationLog;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-08 10:43
 * @description
 */
@Data
public class MetaDataEntityOperationLogDTO {
    private long id;
    private String metadataEntityId;
    private String operationType;   //操作类型
    private String beforeChange;    //操作前
    private String afterChange;     //操作后
    private String createUser;
    private LocalDateTime createTime;
    private int delFlag;
}
