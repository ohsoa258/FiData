package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.app.AppApiSubQueryDTO;
import com.fisk.dataservice.entity.AppServiceConfigPO;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 应用api mapper
 *
 * @author dick
 */
@Mapper
public interface AppServiceConfigMapper extends FKBaseMapper<AppServiceConfigPO> {
    Page<AppApiSubVO> getSubscribeAll(Page<AppApiSubVO> page, @Param("query") AppApiSubQueryDTO query);
    /**
     * 根据应用ID获取该应用下表的数据源
     * @param apiId
     * @return
     */
    @Select("            SELECT\n" +
            "            t1.id,\n" +
            "            t1.app_id,\n" +
            "            t1.service_id,\n" +
            "            t1.api_state,\n" +
            "            t1.type,\n" +
            "            t2.api_code,\n" +
            "            t2.api_name as ServiceName,\n" +
            "            t2.api_desc as ServiceDesc,\n" +
            "            1 as displayName,\n" +
            "            t2.datasource_id as dataSourceId,\n" +
            "            t1.create_time AS create_time,\n" +
            "            t2.create_time AS t2create_time,\n" +
            "            t3.create_time AS t3create_time\n" +
            "            FROM tb_app_service_config t1\n" +
            "            LEFT JOIN tb_api_config t2 ON t1.service_id=t2.id\n" +
            "            LEFT JOIN tb_app_config t3 ON t1.app_id=t3.id\n" +
            "            WHERE t1.del_flag=1\n" +
            "            AND t2.del_flag=1\n" +
            "            AND t3.del_flag=1\n" +
            "            AND t1.type=1\n" +
            "            AND t3.id= #{apiId}")
    List<AppApiSubVO>  getAppByIdApiService( @Param("apiId")long apiId);
    /**
     * 根据应用id和apiId查询单条订阅记录
     *
     * @return 查询结果
     */
    @Select("SELECT id,service_id,app_id,api_state FROM tb_app_service_config WHERE app_id=#{appId} AND service_id=#{apiId} AND del_flag=1 and type=1;")
    AppServiceConfigPO getSubscribeBy(@Param("appId") int appId, @Param("apiId") int apiId);

    /**
     * 根据应用id查询此应用下所有的api
     *
     * @return 查询结果
     */
    @Select("SELECT id,service_id,app_id,api_state,type FROM tb_app_service_config WHERE app_id=#{appId} AND del_flag=1 and type=1;")
    List<AppServiceConfigPO> getSubscribeListByAppId(@Param("appId") int appId);

    /**
     * 根据应用id查询此应用下所有的已启用的&有效的&API为有效的api
     *
     * @return 查询结果
     */
    @Select("SELECT\n" +
            "\tt1.id,\n" +
            "\tt1.service_id,\n" +
            "\tt1.app_id,\n" +
            "\tt1.api_state \n" +
            "FROM\n" +
            "\ttb_app_service_config t1\n" +
            "\tLEFT JOIN tb_api_config t2 ON t1.service_id = t2.id \n" +
            "WHERE\n" +
            "\tt1.app_id = #{appId} \n" +
            "\tAND t1.type = 1 \n" +
            "\tAND t1.del_flag = 1 \n" +
            "\tAND t2.del_flag = 1 ")
    List<AppServiceConfigPO> getSubscribeListBy(@Param("appId") int appId);
}
