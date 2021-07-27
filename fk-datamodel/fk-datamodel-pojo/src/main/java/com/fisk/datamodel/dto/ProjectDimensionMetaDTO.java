package com.fisk.datamodel.dto;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ProjectDimensionMetaDTO {
    public String tableName;
    public List<String> field;
}
