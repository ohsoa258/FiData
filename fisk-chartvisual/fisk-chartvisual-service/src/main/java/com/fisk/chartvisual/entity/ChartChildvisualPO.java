package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fisk.common.entity.BaseChartPO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/11/12 10:40
 */
@TableName("tb_chart_childvisual")
@Data
public class ChartChildvisualPO extends BaseChartPO {

    private Integer chartId;
    private String content;
    public byte[] componentBackground;
    public byte[] layComponentBackground;
}
