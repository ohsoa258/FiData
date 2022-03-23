package com.fisk.datamodel.dto.widetableconfig;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableAliasDTO {

    public String sql;

    /**
     * 返回带前缀表名sql
     */
    public String preSql;

    public List<WideTableSourceTableConfigDTO> entity;


}
