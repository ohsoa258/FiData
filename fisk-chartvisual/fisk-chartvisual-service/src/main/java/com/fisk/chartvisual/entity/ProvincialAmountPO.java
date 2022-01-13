package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import com.fisk.common.entity.BaseSqlServerPO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/10/29 11:20
 */
@TableName("tb_provincial_amount")
@Data
public class ProvincialAmountPO extends BaseSqlServerPO {

    private Integer provincialId;
    private String salesAmount;
}
