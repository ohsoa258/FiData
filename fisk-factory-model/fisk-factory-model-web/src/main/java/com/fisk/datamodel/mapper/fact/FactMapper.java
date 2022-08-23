package com.fisk.datamodel.mapper.fact;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.fact.FactTabDTO;
import com.fisk.datamodel.entity.fact.FactPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper
public interface FactMapper extends FKBaseMapper<FactPO> {

    /**
     * 获取维度id集合
     * @return
     */
    @Select("SELECT id,fact_table_cn_name FROM tb_fact where del_flag=1 ORDER BY id DESC;")
    List<FactTabDTO> getFactTabList();

}
