package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表管理
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_chart")
public class ChartPO extends BasePO {
    public String content;
}
