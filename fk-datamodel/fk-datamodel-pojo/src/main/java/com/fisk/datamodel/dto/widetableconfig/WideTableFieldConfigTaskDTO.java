package com.fisk.datamodel.dto.widetableconfig;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableFieldConfigTaskDTO {

    public int id;

    public int businessId;

    public String name;

    public String sql;

    public List<WideTableSourceTableConfigDTO> entity;

    public List<WideTableSourceRelationsDTO> relations;

}
