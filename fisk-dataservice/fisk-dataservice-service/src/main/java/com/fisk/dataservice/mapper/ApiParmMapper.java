package com.fisk.dataservice.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.ParmConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * api参数 mapper
 *
 * @author dick
 */
@Mapper
public interface ApiParmMapper extends FKBaseMapper<ParmConfigPO> {
    /**
     * 根据apiId查询参数信息
     *
     * @return 查询结果
     */
    @Select("SELECT id,api_id,parm_name,parm_value,parm_desc FROM tb_parm_config WHERE api_id=#{apiId} AND del_flag=1;")
    List<ParmConfigPO> getListByApiId(@Param("apiId") int apiId);

    /**
     * 根据apiId查询参数信息
     *
     * @return 查询结果
     */
    List<ParmConfigPO> getListByApiIds(@Param("apiIds") List<Integer> apiIds);
}
