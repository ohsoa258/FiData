package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.GlossaryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Mapper
public interface GlossaryMapper extends FKBaseMapper<GlossaryPO> {

    @Select("select count(1) from tb_glossary_library where del_flag = 1")
    Integer getGlossaryTotal();
}
