package com.fisk.dataservice.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.AppConfigPO;
import com.fisk.dataservice.entity.BuiltinParmPO;
import com.fisk.dataservice.entity.ParmConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * api内置参数 mapper
 *
 * @author dick
 */
@Mapper
public interface ApiBuiltinParmMapper extends FKBaseMapper<BuiltinParmPO>
{
    /**
     * 根据apiId和appId修改字段有效性标记
     * @param appId 应用id
     * @param apiId apiId
     * @return 操作结果
     */
    @Update("UPDATE tb_builtin_parm SET del_flag=0 WHERE app_id=#{appId} AND api_id=#{apiId} AND del_flag=1;")
    int updateBySearch(@Param("appId") int appId,@Param("apiId") int apiId);
}
