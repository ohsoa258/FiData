package com.fisk.mdm.dto.codeRule;

import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/7/4 17:04
 * @Version 1.0
 */
@Data
public class CodeRuleDeleteDTO {

    /**
     * 规则组id
     */
    private Integer groupId;

    /**
     * 规则id
     */
    private Integer id;
}
