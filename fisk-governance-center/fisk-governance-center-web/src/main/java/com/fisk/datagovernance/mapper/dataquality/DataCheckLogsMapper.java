package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckLogsQueryDTO;
import com.fisk.datagovernance.entity.dataquality.DataCheckLogsPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckLogsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验日志
 * @date 2022/3/23 12:42
 */
@Mapper
public interface DataCheckLogsMapper extends FKBaseMapper<DataCheckLogsPO>
{
    Page<DataCheckLogsVO> getAll(Page<DataCheckLogsVO> page, @Param("query") DataCheckLogsQueryDTO query);
}