package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamanagement.dto.DataSet.CodeSetQueryDTO;
import com.fisk.datamanagement.entity.CodeSetPO;
import com.fisk.datamanagement.vo.CodeSetVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 
 *
 * @author wangjian
 * @date 2024-01-30 10:09:37
 */
@Mapper
public interface CodeSetMapper extends BaseMapper<CodeSetPO> {

    Page<CodeSetVO> getAll(Page<CodeSetVO> page, @Param("query") CodeSetQueryDTO query);
}
