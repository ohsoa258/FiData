package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamodel.entity.BusinessAreaPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: Lock
 */
@Mapper
public interface BusinessAreaMapper extends BaseMapper<BusinessAreaPO> {

    @Select("select id,business_name from 表名 where del_flag=1")
    List<BusinessAreaPO> getName();

}
