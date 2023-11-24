package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.tableservice.TableServicePageDataDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.entity.TableServicePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper
public interface TableServiceMapper extends FKBaseMapper<TableServicePO> {

    /**
     * 分页查询表服务数据
     *
     * @param page
     * @param query
     * @return
     */
    Page<TableServicePageDataDTO> getTableServiceListData(Page<TableServicePageDataDTO> page, @Param("query") TableServicePageQueryDTO query);

    /**
     * 获取应用下的表服务
     * @param appId
     * @return
     */
    @Select(" SELECT\n" +
            " a.*\n" +
            " FROM\n" +
            " tb_app_service_config s\n" +
            " LEFT JOIN tb_table_service a ON a.id = s.service_id\n" +
            " WHERE\n" +
            " a.del_flag = 1\n" +
            " AND s.del_flag = 1\n" +
            " AND s.api_state = 1\n" +
            " AND s.type = 2\n" +
            " AND s.app_id = #{appId}")
    List<TableServicePO> getTableServiceInTheApp(@Param("appId") Integer appId);


    /**
     * 获取表下的应用
     * @param service_id
     * @return
     */
    @Select(" SELECT\n" +
            " a.*\n" +
            " FROM\n" +
            " tb_app_service_config s\n" +
            " LEFT JOIN tb_table_service a ON a.id = s.app_id\n" +
            " WHERE\n" +
            " a.del_flag = 1\n" +
            " AND s.del_flag = 1\n" +
            " AND s.api_state = 1\n" +
            " AND s.type = 2\n" +
            " AND s.service_id = #{service_id}")
    List<TableAppPO> getAppByTableService(@Param("service_id") Integer service_id);
}
