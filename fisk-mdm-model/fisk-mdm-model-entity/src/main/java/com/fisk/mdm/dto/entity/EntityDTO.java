package com.fisk.mdm.dto.entity;

import com.fisk.mdm.enums.WhetherTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/4/2 18:17
 */
@Data
public class EntityDTO {

    /**
     * 模型id
     */
    private Integer modelId;

    /**
     * 实体名称
     */
    private String name;

    /**
     * 展示名称
     */
    private String displayName;

    /**
     * 描述
     */
    private String desc;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 是否开启成员日志
     * 0：false 1:true
     */
    private WhetherTypeEnum enableMemberLog;

    /**
     * 审批规则表id
     */
    private Integer approvalRuleId;

    /**
     * 生成code规则表id
     */
    private Integer buildCodeRuleId;

    /**
     * 派生层级表id
     */
    private Integer hierarchyId;
}
