package com.fisk.datamodel.dto.customscript;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CustomScriptQueryDTO {

    /**
     * 表类型:1维度 2事实
     */
    public Integer type;

    public Integer tableId;

    /**
     * 执行类型:1stg 2 ods
     */
    public Integer execType;

}
