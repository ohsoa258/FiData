package com.fisk.datamodel.entity.businesslimited;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_business_limited_attribute")
public class BusinessLimitedAttributePO extends BasePO {
    /**
     * 业务限定id
     */
    public int businessLimitedId;
    /**
     *事实字段表id
     */
    public int factAttributeId;
    /**
     *计算逻辑
     */
    public String calculationLogic;
    /**
     *计算值
     */
    public String calculationValue;
    /**
     * 连接条件 or或and
     */
    public String joinCondition;



}
