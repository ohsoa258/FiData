package com.fisk.datafactory.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowPageDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Lock
 */
@Mapper
public interface NifiCustomWorkflowMapper extends FKBaseMapper<NifiCustomWorkflowPO> {
    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<NifiCustomWorkflowVO> filter(Page<NifiCustomWorkflowVO> page, @Param("query") NifiCustomWorkflowPageDTO query);
}
