package com.fisk.datamanagement.dto.entity;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class EntityTreeDTO {
    public String id;
    public String name;
    public String type;
    public List<EntityTreeDTO> list;
}
