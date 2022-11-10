package com.fisk.mdm.dto.codeRule;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/6/23 14:14
 * @Version 1.0
 */
@Data
public class CodeRuleGroupUpdateDTO extends CodeRuleGroupDTO {

    @NotNull
    private Integer id;
}
