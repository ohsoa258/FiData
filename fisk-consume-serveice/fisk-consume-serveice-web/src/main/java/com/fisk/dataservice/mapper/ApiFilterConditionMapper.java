package com.fisk.dataservice.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.FilterConditionConfigPO;
import com.fisk.dataservice.entity.ParmConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 过滤条件 mapper
 *
 * @author dick
 */
@Mapper
public interface ApiFilterConditionMapper extends FKBaseMapper<FilterConditionConfigPO>
{
    /**
     * 根据apiId修改字段有效性标记
     * @param apiId apiId
     * @return 操作结果
     */
    @Update("UPDATE tb_filtercondition_config SET del_flag=0 WHERE api_id=#{apiId} AND del_flag=1;")
    int updateByApiId(@Param("apiId") int apiId);

    /**
     * 根据apiId查询过滤条件信息
     *
     * @return 查询结果
     */
    @Select("SELECT id,api_id,field_name,operator,field_value FROM tb_filtercondition_config WHERE api_id=#{apiId} AND del_flag=1;")
    List<FilterConditionConfigPO> getListByApiId(@Param("apiId") int apiId);
}
