package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datagovernance.entity.dataquality.DatacheckServerApiConfigPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.ApiSeverSubVO;
import com.fisk.datagovernance.dto.dataquality.datacheck.ApiSubQueryDTO;
import com.fisk.datagovernance.vo.dataquality.datacheck.AppServiceCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-10-23 15:33:27
 */
@Mapper
public interface DatacheckServerApiConfigMapper extends BaseMapper<DatacheckServerApiConfigPO> {

    /**
     * 根据code标识查询API
     *
     * @return 查询结果
     */
    @Select("SELECT * FROM tb_datacheck_server_api_config WHERE api_code=#{apiCode} AND del_flag=1;")
    DatacheckServerApiConfigPO getByApiCode(@Param("apiCode") String apiCode);

    List<ApiSeverSubVO> getApiSubAll(@Param("query") ApiSubQueryDTO query,@Param("pageNo")Integer pageNo,@Param("size")Integer size);

    Integer getApiSubAllCount(@Param("query") ApiSubQueryDTO query);

    /**
     * 根据应用id查询此应用下所有的已启用的&有效的&API为有效的api
     *
     * @return 查询结果
     */
    @Select("SELECT\n" +
            "\ta.id,\n" +
            "\ta.app_id,\n" +
            "\ta.check_rule_id,\n" +
            "\ta.api_code,\n" +
            "\ta.api_desc,\n" +
            "\ta.api_state \n" +
            "FROM\n" +
            "\ttb_datacheck_server_api_config a\n" +
            "\tLEFT JOIN tb_datacheck_rule b ON a.check_rule_id = b.id \n" +
            "WHERE\n" +
            "\ta.del_flag = 1 \n" +
            "\tAND b.del_flag = 1 \n" +
            "\tAND a.api_state = 1\n" +
            "\tAND a.app_id = #{appId}")
    List<DatacheckServerApiConfigPO> getSubscribeListBy(@Param("appId") int appId);

    @Select("SELECT\n" +
            "\tt1.id as appId,\n" +
            "\tcount( 1 ) AS count \n" +
            "FROM\n" +
            "\ttb_datacheck_server_app_config t1\n" +
            "\tLEFT JOIN tb_datacheck_server_api_config t2 ON t1.id = t2.app_id \n" +
            "WHERE\n" +
            "\tt1.del_flag = 1 \n" +
            "\tAND t2.del_flag = 1 \n" +
            "GROUP BY\n" +
            "\tt1.id")
    List<AppServiceCountVO> getApiAppServiceCount();
}
