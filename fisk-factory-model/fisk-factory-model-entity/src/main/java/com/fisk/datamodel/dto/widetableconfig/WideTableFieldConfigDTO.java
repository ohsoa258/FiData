package com.fisk.datamodel.dto.widetableconfig;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableFieldConfigDTO {

    public int pageSize;

    /**
     * 所有表信息: entity.get(0)一定是主表
     */
    public List<WideTableSourceTableConfigDTO> entity;

    /**
     * 连线关系
     */
    public List<WideTableSourceRelationsDTO> relations;
}
