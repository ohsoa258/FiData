package com.fisk.common.server.ocr.dto.businessmetadata;

import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-06-29 10:50
 */
@Data
public class TableRuleParameterDTO {

    /**
     * 表id
     */
    public Integer tableId;

    /**
     * 表类型：
     */
    public Integer type;

}
