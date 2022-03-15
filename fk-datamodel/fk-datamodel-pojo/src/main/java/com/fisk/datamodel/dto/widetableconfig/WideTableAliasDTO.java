package com.fisk.datamodel.dto.widetableconfig;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableAliasDTO {

    public String sql;

    public List<WideTableSourceTableConfigDTO> entity;


}
