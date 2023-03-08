package com.fisk.datamanagement.dto.metadataattribute;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetadataAttributeDTO {

    public Integer metadataEntityId;

    public String name;

    public String value;

    /**
     * 0 技术属性 1 元数据属性
     */
    public Integer groupType;

}
