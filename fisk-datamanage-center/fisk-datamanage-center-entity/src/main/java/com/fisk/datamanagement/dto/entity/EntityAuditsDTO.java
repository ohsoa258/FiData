package com.fisk.datamanagement.dto.entity;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityAuditsDTO {
    private String entityId;
    private long timestamp;
    private String user;
    private String action;
    private String details;
    private String eventKey;
    private String entity;
    private String type;
}
