package com.fisk.dataaccess.dto.api.doc;

import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API返回参数
 * @date 2022/2/3 14:10
 */
@Data
public class ApiResponseDTO {
    /**
     * 表名
     */
    public String tableName;
    /**
     * 参数名称
     */
    public String  parmName;

    /**
     * 参数类型
     */
    public String  parmType;

    /**
     * 参数描述
     */
    public String  parmDesc;

    /**
     * 参数推送规则
     */
    public String parmPushRule;

    /**
     * 参数推送规则
     */
    public String parmPushExample;

    /**
     * 行样式
     */
    public String  trStyle;
}
