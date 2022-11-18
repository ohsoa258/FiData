package com.fisk.datamanagement.dto.entity;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityStagingDTO {
    public String guid;
    public String name;
    public String parent;
    public String type;
    public String displayName;
}
