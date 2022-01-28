package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.api.ApiRegisterQueryDTO;
import com.fisk.dataservice.entity.ApiConfigPO;
import com.fisk.dataservice.vo.api.ApiConfigVO;
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
    @Insert("INSERT INTO `tb_api_config`(`api_name`, `api_code`, `api_desc`, `api_type`, `datasource_id`, `table_name`, `create_sql`, `create_time`, `create_user`,`del_flag`) VALUES (#{apiName}, #{apiCode}, #{apiDesc}, #{apiType}, #{datasourceId}, #{tableName}, #{createSql}, #{createTime}, #{createUser},1);")
    int insertOne(ApiConfigPO po);

    /**
     * 根据code标识查询API
     *
     * @return 查询结果
     */
    @Select("SELECT id,api_code,api_name,api_code,api_type,datasource_id,create_sql FROM tb_api_config WHERE api_code=#{apiCode} AND del_flag=1;")
    ApiConfigPO getByApiCode(@Param("apiCode") String apiCode);

    /**
     * 查询所有API信息
     *
     * @return 查询结果
     */
    List<ApiConfigPO> getList(@Param("keyword") String keyword);
}
