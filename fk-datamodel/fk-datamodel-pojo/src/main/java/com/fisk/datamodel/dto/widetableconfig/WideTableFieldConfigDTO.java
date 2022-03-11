package com.fisk.datamodel.dto.widetableconfig;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableFieldConfigDTO {

    public List<WideTableSourceTableConfigDTO> entity;

    public List<WideTableSourceRelationsDTO> relations;
}
