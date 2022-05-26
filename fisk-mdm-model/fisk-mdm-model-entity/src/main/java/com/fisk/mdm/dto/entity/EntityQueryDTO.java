package com.fisk.mdm.dto.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/25 17:37
 * @Version 1.0
 */
@Data
@NoArgsConstructor
public class EntityQueryDTO {

    private Integer id;
    private String name;
    private String type;
    private String aliasName;
    private List<EntityQueryDTO> children;

    public EntityQueryDTO(Integer id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}
