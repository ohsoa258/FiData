package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamanagement.dto.standards.StandardsDTO;
import com.fisk.datamanagement.dto.standards.StandardsMenuDTO;
import com.fisk.datamanagement.dto.standards.StandardsQueryDTO;
import com.fisk.datamanagement.dto.standards.StandardsSourceQueryDTO;
import com.fisk.datamanagement.entity.StandardsPO;
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
public interface StandardsMapper extends BaseMapper<StandardsPO> {

    Page<StandardsMenuDTO> standardsQuery(Page<StandardsMenuDTO> page,@Param("dto") StandardsQueryDTO dto);
    List<StandardsDTO> getStandardsBySource(@Param("dto") StandardsSourceQueryDTO dto);
}
