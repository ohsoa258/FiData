package com.fisk.mdm.dto.access;

import lombok.Data;

/**
 * @author jianwenyang
 */
@Data
public class CustomScriptDTO {

    public Integer id;

    /**
     * 表id
     */
    public Integer tableId;

    /**
     * 执行顺序
     */
    public Integer sequence;

    /**
     * 名称
     */
    public String name;

    /**
     * 脚本
     */
    public String script;

    /**
     * 执行类型:1stg 2 ods
     */
    public Integer execType;

}
