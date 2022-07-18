package com.fisk.mdm.dto.viwGroup;

import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/31 15:07
 * @Version 1.0
 */
@Data
public class ViwGroupCheckDTO {

    private Integer id;
    private String aliasName;
    private String name;
    private String displayName;
    private String desc;
    private String dataType;
    private Integer dataTypeLength;
    private Integer dataTypeDecimalLength;
    private Integer domainEntityId;
    private String domainName;
    private String mapType;
    /**
     * 实体id,实体名称
     */
    private Integer entityId;
    private String entityName;
    private String entityDisplayName;
}
