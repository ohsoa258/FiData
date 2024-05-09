package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamanagement.entity.StandardsBeCitedPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 *
 * @author wangjian
 * @date 2023-11-20 13:56:24
 */
@Mapper
public interface StandardsBeCitedMapper extends BaseMapper<StandardsBeCitedPO> {

    List<Integer> checkStandardBeCited(@Param("dbId") Integer dbId,@Param("tableId") Integer tableId,@Param("fieldId") Integer fieldId);
}
