package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/3/1 16:28
 */
@TableName("tb_chart_option")
@Data
public class ChartOptionPO extends BasePO {

    private Integer chartId;
    private String content;
}
