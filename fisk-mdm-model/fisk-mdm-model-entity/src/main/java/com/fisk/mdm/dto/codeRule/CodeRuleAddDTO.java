package com.fisk.mdm.dto.codeRule;

import com.fisk.common.service.mdmBEOperate.dto.CodeRuleDTO;
import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "组id")
    @NotNull
    private Integer groupId;
    @ApiModelProperty(value = "编码规则DTO列表")
    private List<CodeRuleDTO> codeRuleDtoList;
}
