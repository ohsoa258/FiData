package com.fisk.datamanagement.dto.entity;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class EntityTreeDTO {
    public String id;
    public String label;
    public String type;
    public String parentId;
    public String displayName;
    public List<EntityTreeDTO> children;
}
