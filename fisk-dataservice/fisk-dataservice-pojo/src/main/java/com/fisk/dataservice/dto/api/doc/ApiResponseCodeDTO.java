package com.fisk.dataservice.dto.api.doc;

import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API代码示例
 * @date 2022/2/3 14:10
 */
@Data
public class ApiResponseCodeDTO {
    /**
     * 代码code
     */
    public String code;

    /**
     * 类型
     */
    public String type;

    /**
     * 描述
     */
    public String desc;

    /**
     * 行样式
     */
    public String trStyle;
}
