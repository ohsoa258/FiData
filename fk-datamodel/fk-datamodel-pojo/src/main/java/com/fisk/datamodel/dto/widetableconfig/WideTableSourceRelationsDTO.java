package com.fisk.datamodel.dto.widetableconfig;

import ch.qos.logback.classic.sift.AppenderFactoryUsingJoran;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class WideTableSourceRelationsDTO {

    public String sourceTable;

    public String sourceColumn;

    public String joinType;

    public String targetTable;

    public String targetColumn;

}
