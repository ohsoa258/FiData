package com.fisk.chartvisual.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.ChartQueryDTO;
import com.fisk.chartvisual.entity.ChartPO;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import com.fisk.common.mybatis.FKBaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author gy
 */
public interface ChartMapper extends FKBaseMapper<ChartPO> {

    /**
     * 查询用户权限下所有报表
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<ChartPropertyVO> listChartDataByUserId(Page<ChartPropertyVO> page, @Param("query") ChartQueryDTO query);

    /**
     * 获取报表可视化数量
     * @return
     */
    @Select(" SELECT COUNT(1) FROM ( " +
            "select id, fid, name, content, details, create_time, 0 as chartType, image,background_image from tb_chart where del_flag = 1 " +
            "union all " +
            "select id, fid, name, content, details, create_time, 1 as chartType, image,background_image from tb_draft_chart where del_flag = 1 ) TOTAL ")
    Long getAmount();
}
