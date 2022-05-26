package com.fisk.mdm.dto.attributeGroup;

import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/26 10:02
 * @Version 1.0
 */
@Data
public class AttributeInfoDTO {

    private Integer id;
    private String name;
    private String displayName;
    private String desc;
    private String dataType;
    private Integer dataTypeLength;
    private Integer dataTypeDecimalLength;
    private Integer existsGroup;
}
