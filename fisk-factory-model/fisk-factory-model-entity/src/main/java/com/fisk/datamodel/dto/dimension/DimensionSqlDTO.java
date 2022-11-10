package com.fisk.datamodel.dto.dimension;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionSqlDTO {
    public long id;
    public Integer appId;
    public String sqlScript;
}
