package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/10/29 11:20
 */
@TableName("tb_provincial_amount")
@Data
public class ProvincialAmountPO extends BasePO {

    private Integer provincialId;
    private String salesAmount;
}
