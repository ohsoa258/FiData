package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/10/28 17:12
 */
@TableName("tb_provincial")
@Data
public class ProvincialPO extends BasePO {

    private String provincialName;
}
