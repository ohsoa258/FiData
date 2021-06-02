package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.ChartPropertyDTO;
import com.fisk.chartvisual.dto.ReleaseChart;
import com.fisk.common.response.ResultEnum;

/**
 * 图表管理
 * @author gy
 */
public interface IChartManage {

    /**
     * 保存图表到草稿箱
     * @param dto dto
     * @return 保存结果
     */
    public ResultEnum saveChartToDraft(ChartPropertyDTO dto);

    /**
     * 保存图表
     * @param dto dto
     * @return 保存结果
     */
    public ResultEnum saveChart(ReleaseChart dto);
}
