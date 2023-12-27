package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.BusinessCategoryPO;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author xgf
 * @date 2023年11月20日 9:49
 */
@Mapper
public interface BusinessExtendedfieldsMapper extends FKBaseMapper<BusinessExtendedfieldsPO> {

    @Select("select * from tb_business_extendedfields where indexid = #{indexid} and del_flag = 1")
    List<BusinessExtendedfieldsPO> selectParentpId(@Param("indexid") String indexid);

    @Update("update tb_business_extendedfields set del_flag = 0 where indexid = #{indexid} and del_flag = 1")
    int updateByName(@Param("indexid") String indexid);



}
