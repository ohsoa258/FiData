package com.fisk.datamanagement.dto.entity;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityAttributesDTO {
    public String qualifiedName;
    public String rdbms_type;
    public String name;
    public String platform;
    public String hostname;
    public String port;
    public String protocol;
    public String contact_info;
    public String comment;
    public String description;
    public String data_type;
    public String length;
    public String owner;
    public String createdBy;
    public String createTime;
    public String updatedBy;
    public String updateTime;
    public EntityIdAndTypeDTO instance;
    public EntityIdAndTypeDTO db;
    public EntityIdAndTypeDTO table;
}
