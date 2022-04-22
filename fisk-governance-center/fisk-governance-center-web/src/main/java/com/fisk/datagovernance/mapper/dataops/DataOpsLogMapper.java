package com.fisk.datagovernance.mapper.dataops;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataops.DataOpsLogPO;
import com.fisk.datagovernance.vo.dataops.DataOpsLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维日志
 * @date 2022/4/22 11:38
 */
@Mapper
public interface DataOpsLogMapper extends FKBaseMapper<DataOpsLogPO> {

    /**
     * 查询数据运维日志分页列表
     *
     * @param page    分页信息
     * @param keyword where条件
     * @return 查询结果
     */
    Page<DataOpsLogVO> getAll(Page<DataOpsLogVO> page, @Param("keyword") String keyword);
}
