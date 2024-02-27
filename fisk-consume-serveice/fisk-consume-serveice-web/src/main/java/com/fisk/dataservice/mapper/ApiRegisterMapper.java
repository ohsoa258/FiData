package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.api.ApiRegisterQueryDTO;
import com.fisk.dataservice.entity.ApiConfigPO;
import com.fisk.dataservice.vo.api.ApiConfigVO;
import com.fisk.dataservice.vo.tableapi.TopFrequencyVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * api注册mapper
 *
 * @author dick
 */
@Mapper
public interface ApiRegisterMapper extends FKBaseMapper<ApiConfigPO> {
    /**
     * api列表分页功能
     *
     * @param page 分页对象
     * @return 查询结果
     */
    Page<ApiConfigVO> getAll(Page<ApiConfigVO> page, @Param("query") ApiRegisterQueryDTO query);

    /**
     * 新增一条api数据
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO `tb_api_config`(`api_name`, `api_code`, `api_desc`, `api_type`, `datasource_id`, `table_name`,`table_name_alias`, `table_type`, `table_business_type`, `table_path`, `create_sql`, `create_count_sql`,`create_api_type`,`api_proxy_url`,`expiration_time`,`expiration_type`,`menu_id`,`api_menu_id`,`enable_cache`,`cache_time`,`create_time`, `create_user`,`del_flag`) VALUES (#{apiName}, #{apiCode}, #{apiDesc}, #{apiType}, #{datasourceId}, #{tableName}, #{tableNameAlias}, #{tableType}, #{tableBusinessType}, #{tablePath}, #{createSql}, #{createCountSql},#{createApiType},#{apiProxyUrl},#{expirationTime},#{expirationType},#{menuId},#{apiMenuId},#{enableCache},#{cacheTime},#{createTime}, #{createUser},1);")
    int insertOne(ApiConfigPO po);

    /**
     * 根据code标识查询API
     *
     * @return 查询结果
     */
    @Select("SELECT * FROM tb_api_config WHERE api_code=#{apiCode} AND del_flag=1;")
    ApiConfigPO getByApiCode(@Param("apiCode") String apiCode);

    /**
     * 查询所有API信息
     *
     * @return 查询结果
     */
    List<ApiConfigPO> getList(@Param("keyword") String keyword, @Param("createApiType") Integer createApiType);


    /**
     * 根据id集合批量查询
     *
     * @return 查询结果
     */
    List<ApiConfigPO> getListByAppApiIds(@Param("apiIds") List<Integer> apiIds, @Param("appId") Integer appId);

    @Select("SELECT IFNULL(SUM(number), 0) FROM `tb_logs` WHERE create_time >= CURDATE() and del_flag = 1")
    int getTotalNumber();
    @Select("SELECT count(1) FROM `tb_logs` WHERE create_time >= CURDATE() and del_flag = 1")
    int getFrequency();
    @Select("select count(1) FROM (SELECT api_id FROM `tb_logs` WHERE create_time >= CURDATE() and del_flag = 1 GROUP BY api_id) as api_log")
    int getApiNumber();
    @Select("SELECT count(important_interface) FROM `tb_logs` WHERE create_time >= CURDATE() and important_interface = 1 and business_state = '成功' and del_flag = 1")
    int focusApiTotalNumber();
    @Select("SELECT count(1) FROM `tb_logs` WHERE create_time >= CURDATE() and business_state = '成功' and del_flag = 1")
    int successNumber();
    @Select("SELECT count(1) FROM `tb_logs` WHERE create_time >= CURDATE() and business_state = '失败' and del_flag = 1")
    int faildNumber();

    @Select("SELECT t1.frequency,t2.api_name as apiName from (SELECT api_id,count(1) as frequency FROM `tb_logs` WHERE create_time >= CURDATE() and business_state = '成功' and del_flag = 1 GROUP BY api_id ORDER BY frequency DESC limit 5) as t1\n" +
            "LEFT JOIN tb_api_config t2 on t1.api_id = t2.id")
    List<TopFrequencyVO> getTopFrequency();
}
