package com.fisk.dataaccess.dto.apicondition;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ApiConditionDTO {
    /**
     * 类型名称
     */
    public String typeName;

    /**
     * 父级类型
     */
    public String parent;

    /**
     * 项
     */
    public String item;

    /**
     * 说明
     */
    public String instructions;

    /**
     * 实例
     */
    public String instance;

}
