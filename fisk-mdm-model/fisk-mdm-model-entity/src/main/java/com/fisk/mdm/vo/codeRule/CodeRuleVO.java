package com.fisk.mdm.vo.codeRule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import com.fisk.mdm.dto.codeRule.CodeRuleDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/7/1 14:27
 * @Version 1.0
 */
@Data
public class CodeRuleVO extends BaseUserInfoVO {

    /**
     * 实体id
     */
    private Integer entityId;

    /**
     * 属性id
     */
    private Integer attributeId;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String desc;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间",required = true)
    public LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间",required = true)
    public LocalDateTime updateTime;

    /**
     * 规则组详细数据
     */
    private List<CodeRuleDTO> groupDetailsList;
}
