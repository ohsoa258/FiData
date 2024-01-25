package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.sun.org.apache.bcel.internal.generic.FCMPG;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface FactTreeListMapper extends FKBaseMapper<FactTreePOs> {
    @Select("select * from tb_facttreelist where pid = #{pid} and del_flag = 1")
    List<FactTreePOs> selectParentpIds(@Param("pid") String pid);
    @Update("update tb_facttreelist set del_flag=0 where pid=#{pid} and del_flag= 1")
    int updateByName(@Param("pid") String pid);
    @Select("select pid from tb_facttreelist where fact_tab_name=#{factTabName} and fact_field_en_name=#{factFieldEnName}")
    List<String> selectsParentpIds(@Param("factTabName") String factTabName ,@Param("factFieldEnName")String factFieldEnName );


}

