package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/1/13 19:00
 */
@TableName("tb_chart_image")
@Data
public class ChartImagePO extends BasePO {

    private String imagePath;
}
