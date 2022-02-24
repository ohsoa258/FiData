package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.ChartPropertyEditDTO;
import com.fisk.chartvisual.dto.ReleaseChart;
import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;

/**
 * @author WangYan
 * @date 2022/2/21 11:42
 */
public interface DiagramManageService {

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
}
