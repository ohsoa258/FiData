package com.fisk.mdm.dto.codeRule;

import com.fisk.common.service.mdmBEOperate.dto.CodeRuleDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/7/1 11:21
 * @Version 1.0
 */
@Data
public class CodeRuleAddDTO {

    @NotNull
    private Integer groupId;
    private List<CodeRuleDTO> codeRuleDtoList;
}
