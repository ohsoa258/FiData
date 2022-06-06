package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.mdm.enums.MdmStatusTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/4/2 16:31
 */
@TableName("tb_entity")
@Data
public class EntityPO extends BasePO {

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
     * 状态：0：未创建  1：创建成功 2：创建失败
     */
    private MdmStatusTypeEnum status;

    /**
     * 描述
     */
    @TableField(value = "`desc`")
    private String desc;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 是否开启成员日志 0：false 1:true
     */
    private Integer enableMemberLog;

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
