package com.fisk.datafactory.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowPageDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 查询数据调度图当天运行情况
     * @param status 已发布（1）、未发布（0）、发布失败（2）、正在发布（3）
     * @return 个数
     */
    @Select("select count(1) from tb_nifi_custom_workflow where to_days(create_time) = to_days(now()) and `status` = #{status};")
    int getNum(@Param("status") int status);
}
