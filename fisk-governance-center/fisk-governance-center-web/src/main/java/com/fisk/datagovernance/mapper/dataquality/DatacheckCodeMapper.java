package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datagovernance.entity.dataquality.DatacheckCodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-05-13 14:04:19
 */
@Mapper
public interface DatacheckCodeMapper extends BaseMapper<DatacheckCodePO> {

    @Select("SELECT DISTINCT code_name FROM tb_datacheck_code")
    List<String> getCheckCodeList();
}

