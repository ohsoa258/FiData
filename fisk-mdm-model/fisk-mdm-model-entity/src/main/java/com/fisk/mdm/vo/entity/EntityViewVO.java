package com.fisk.mdm.vo.entity;

import com.fisk.mdm.dto.attributeGroup.AttributeInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/7/15 16:04
 * @Version 1.0
 */
@Data
public class EntityViewVO {

    private Integer id;
    private Integer modelId;
    private String name;
    private String displayName;
    private String desc;
    private String status;
    private String tableName;
    private Boolean enableMemberLog;
    private Integer approvalRuleId;
    private Integer buildCodeRuleId;
    private Integer hierarchyId;
    /**
     * 属性信息
     */
    private List<AttributeInfoDTO> attributeList;
}
