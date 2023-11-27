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
@TableName("tb_business_synchronous")
public class BusinessSynchronousPO extends BasePO {

    public String typeName;

    public String typeData;
}
