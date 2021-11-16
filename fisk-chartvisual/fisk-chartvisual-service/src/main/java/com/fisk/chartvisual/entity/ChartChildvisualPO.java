package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/11/12 10:40
 */
@TableName("tb_chart_childvisual")
@Data
public class ChartChildvisualPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer chartId;
    private String content;
    public byte[] componentBackground;

    @TableLogic
    private Integer delFlag;
}
