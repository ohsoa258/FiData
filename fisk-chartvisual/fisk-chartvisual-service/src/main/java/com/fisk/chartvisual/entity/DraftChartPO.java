package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 草稿报表管理
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_draft_chart")
public class DraftChartPO extends BasePO {
    public String content;
}
