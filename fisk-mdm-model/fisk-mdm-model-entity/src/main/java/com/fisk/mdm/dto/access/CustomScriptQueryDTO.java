package com.fisk.mdm.dto.access;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CustomScriptQueryDTO {

    public Integer tableId;

    /**
     * 执行类型:1stg 2 ods
     */
    public Integer execType;

}
