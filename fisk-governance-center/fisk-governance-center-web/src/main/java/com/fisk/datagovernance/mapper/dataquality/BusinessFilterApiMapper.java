package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiConfigPO;
import org.apache.ibatis.annotations.*;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗
 * @date 2022/10/8 16:58
 */
@Mapper
public interface BusinessFilterApiMapper extends FKBaseMapper<BusinessFilterApiConfigPO> {
    /**
     * 修改数据有效性
     *
     * @return 执行结果
     */
    @Update("UPDATE tb_bizfilter_api_config SET del_flag=0 WHERE rule_id = #{ruleId};")
    int updateByRuleId(@Param("ruleId") int ruleId);

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_bizfilter_api_config(`rule_id`, `api_auth_address`, `api_auth_body_type`, `api_auth_expir_minute`,`api_address`, `api_body_type`, `api_request_type`, `api_param_range`, `create_time`, `create_user`,`del_flag`) VALUES (#{ruleId}, #{apiAuthAddress}, #{apiAuthBodyType}, #{apiAuthExpirMinute}, #{apiAddress}, #{apiBodyType}, #{apiRequestType}, #{apiParamRange}, #{createTime}, #{createUser},1);")
    int insertOne(BusinessFilterApiConfigPO po);
}
