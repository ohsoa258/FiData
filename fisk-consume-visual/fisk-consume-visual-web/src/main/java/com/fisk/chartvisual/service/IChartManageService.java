package com.fisk.chartvisual.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.chartVisual.ChartPropertyDTO;
import com.fisk.chartvisual.dto.chartVisual.ChartPropertyEditDTO;
import com.fisk.chartvisual.dto.chartVisual.ChartQueryDTO;
import com.fisk.chartvisual.dto.chartVisual.ReleaseChart;
import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;

/**
 * 图表管理
 *
 * @author gy
 */
public interface IChartManageService {

    /**
     * 保存图表到草稿箱
     *
     * @param dto dto
     * @return 保存结果
     */
    ResultEnum saveChartToDraft(ChartPropertyDTO dto);

    /**
     * 保存图表
     *
     * @param dto dto
     * @return 保存结果
     */
    ResultEntity<Long> saveChart(ReleaseChart dto);

    /**
     * 根据id获取图表详情
     *
     * @param id   id
     * @param type 类型
     * @return 结果
     */
    ChartPropertyVO getDataById(int id, ChartQueryTypeEnum type);

    /**
     * 修改图表
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateChart(ChartPropertyEditDTO dto);

    /**
     * 删除图表
     *
     * @param id id
     * @param type 查询类型（草稿，发布）
     * @return 执行结果
     */
    ResultEnum deleteDataById(int id, ChartQueryTypeEnum type);

    /**
     * 查询数据列表
     * @param page 分页信息
     * @param query where条件
     * @return 数据
     */
    Page<ChartPropertyVO> listData(Page<ChartPropertyVO> page, ChartQueryDTO query);

    /**
     * 获取报表数据可视化数量
     * @return
     */
    ResultEntity<Long> amount();
}