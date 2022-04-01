package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author chenYa
 * @version v1.0
 * @description model实体类
 * @date 2022/3/31 11:14
 */
@Data
@TableName("tb_model")
public class ModelPO extends BasePO {

    /**
     * modelID
     */
    public long id;

    /**
     * model名称
     */
    public String name;

    /**
     * model展示名称
     */
    public String displayName;

    /**
     * model描述
     */
    @TableField(value = "`desc`")
    public String desc;

    /**
     * model日志保留天数
     */
    public int logRetentionDays;

    /**
     * model图标路径
     */
    public String logoPath;

    /**
     * model当前版本id
     */
    public int currentVersionId;

    /**
     * model版本生成规则id
     */
    public int autobuildRuleId;

    /**
     * 属性日志表名
     */
    public String attributeLogName;
}
