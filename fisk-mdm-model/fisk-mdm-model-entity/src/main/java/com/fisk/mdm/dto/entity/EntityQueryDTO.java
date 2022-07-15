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
    private String dataType;
    private String displayName;
    private String aliasName;
    private String desc;
    private Integer dataTypeLength;
    private Integer dataTypeDecimalLength;
    private Integer domainEntityId;
    private String domainName;
    private String mapType;
    /**
     * 是否选中 0:未选中 1:选中
     */
    private Integer isCheck;
    /**
     * 是否选中 0:是 1:不是
     */
    private Integer isMainEntity;
    private List<EntityQueryDTO> children;

    public EntityQueryDTO(Integer id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}
