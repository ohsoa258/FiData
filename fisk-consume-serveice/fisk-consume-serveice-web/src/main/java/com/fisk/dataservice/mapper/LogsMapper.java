package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.LogPO;
import com.fisk.dataservice.vo.atvserviceanalyse.AtvCallApiFuSingAnalyseVO;
import com.fisk.dataservice.vo.atvserviceanalyse.AtvYasCallApiAnalyseVO;
import com.fisk.dataservice.vo.atvserviceanalyse.AtvTopCallApiAnalyseVO;
import com.fisk.dataservice.vo.logs.ApiLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 日志 mapper
 *
 * @author dick
 */
@Mapper
public interface LogsMapper extends FKBaseMapper<LogPO> {

    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param apiId apiIds
     * @param appId appId
     * @return 查询结果
     */
    Page<ApiLogVO> filter(Page<ApiLogVO> page, @Param("apiId") Integer apiId, @Param("appId") Integer appId, @Param("keyword") String keyword);

    /**
     * 统计昨天和今天API在各个时间节点的调用情况
     *
     * @return 查询结果
     */
    @Select("SELECT\n" +
            "\tDATE_FORMAT( create_time, '%Y-%m-%d' ) AS dateSlot,\n" +
            "\tDATE_FORMAT( create_time, '%H:00' ) AS timeSlot,\n" +
            "\tCOUNT(*) AS totalCount \n" +
            "FROM\n" +
            "\ttb_logs \n" +
            "WHERE\n" +
            "\tdel_flag = 1 \n" +
            "\tAND log_type = 100 \n" +
            "\tAND create_time >= CURDATE() - INTERVAL 1 DAY \n" +
            "\tAND create_time < CURDATE() + INTERVAL 1 DAY \n" +
            "GROUP BY\n" +
            "\tdateSlot,\n" +
            "\ttimeSlot \n" +
            "ORDER BY\n" +
            "\tdateSlot,timeSlot ASC;")
    List<AtvYasCallApiAnalyseVO> getAtvYasCallApiAnalyse();

    /**
     * 统计今天API调用情况，根据调用次数取前20条
     *
     * @return 查询结果
     */
    @Select("SELECT\n" +
            "\tapi.api_code AS apiCode,\n" +
            "\tapi.api_name AS apiName,\n" +
            "\tapi.api_desc AS apiDesc,\n" +
            "\tCOUNT(*) AS totalCount \n" +
            "FROM\n" +
            "\ttb_logs log\n" +
            "\tLEFT JOIN tb_api_config api ON log.api_id = api.id \n" +
            "WHERE\n" +
            "\tlog.del_flag = 1 \n" +
            "\tAND api.del_flag = 1 \n" +
            "\tAND log.log_type = 100 \n" +
            "\tAND log.create_time >= CURDATE() \n" +
            "\tAND log.create_time < CURDATE() + INTERVAL 1 DAY \n" +
            "GROUP BY\n" +
            "\tapi.api_code,\n" +
            "\tapi.api_name,\n" +
            "\tapi.api_desc \n" +
            "ORDER BY\n" +
            "\ttotalCount DESC \n" +
            "\tLIMIT 0,\n" +
            "\t20")
    List<AtvTopCallApiAnalyseVO> getAtvTopCallApiAnalyse();
}
