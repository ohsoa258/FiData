package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xgf
 * @date 2023年11月20日 9:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_business_category")
public class BusinessCategoryPO extends BasePO {
    public Integer pid;

    public String name;

    public String description;
}
