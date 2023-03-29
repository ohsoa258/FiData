package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author JinXingWang
 */
@TableName("tb_matching_rules")
@Data
public class MatchingRulesPO extends BasePO {

    /**
     * 模型id
     */
    public Integer modelId;
    /**
     * 实体id
     */
    public Integer entityId;
    /**
     *最低阈值
     */
     public  Integer lowThreshold;
    /**
     * 最高阈值
     */
    public Integer highThreshold;

    /**
     * 匹配字段类型 1 全部字段 2固定字段
     */
    public Integer matchFieldType;
}
