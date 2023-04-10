package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 *
 * @author JinXingWang
 */
@TableName("tb_matching_rules_filed")
@Data
public class MatchingRulesFiledPO extends BasePO {
    public Integer attributeId;
    public Integer weight;
    public long matchingRulesId;
}
