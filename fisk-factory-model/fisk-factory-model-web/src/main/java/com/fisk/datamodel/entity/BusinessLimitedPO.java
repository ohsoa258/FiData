package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author cfk
 */
@Data
@TableName("tb_business_limited")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessLimitedPO extends BasePO {
    /**
     *业务限定名称
     */
    public String limitedName;
    /**
     *业务限定描述
     */
    public String limitedDes;
    /**
     *事实表id
     */
    public int factId;
}
