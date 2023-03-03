package com.fisk.dataservice.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.TableAppDatasourcePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TableAppDatasourceMapper extends FKBaseMapper<TableAppDatasourcePO> {
    @Update("UPDATE tb_table_app_datasource SET del_flag=0 WHERE table_app_id = #{tableAppId};")
    int updateByTableAppId(@Param("tableAppId") int tableAppId);
}
