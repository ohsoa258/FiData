package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.atvserviceanalyse.AtvServiceMonitoringQueryDTO;
import com.fisk.dataservice.entity.LogPO;
import com.fisk.dataservice.vo.atvserviceanalyse.*;
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
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.fisk.dataservice.vo.logs.ApiLogVO>
     * @description 筛选器分页功能
     * @author dick
     * @date 2023/6/19 10:29
     * @version v1.0
     * @params page
     * @params apiId
     * @params appId
     * @params keyword
     * @params callCycleStartDate
     * @params callCycleEndDate
     */
    Page<ApiLogVO> filter(Page<ApiLogVO> page, @Param("apiId") Integer apiId, @Param("appId") Integer appId,
                          @Param("keyword") String keyword, @Param("createApiType") Integer createApiType, @Param("callCycleStartDate") String callCycleStartDate,
                          @Param("callCycleEndDate") String callCycleEndDate);

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


    List<AtvApiTimeConsumingRankingVO> getAtvApiTimeConsumingRanking(@Param("createApiType") int createApiType,
                                                                     @Param("appId") int appId, @Param("apiId") int apiId);

    List<AtvApiSuccessFailureRankingVO> getAtvApiSuccessFailureRanking(@Param("createApiType") int createApiType,
                                                                       @Param("appId") int appId, @Param("apiId") int apiId);

    List<AtvApiPrincipalDetailAppBindApiVO> getAtvApiPrincipalDetailAppBindApi(@Param("createApiType") int createApiType,
                                                                               @Param("appId") int appId, @Param("apiId") int apiId);

    List<AtvApiSqCountApiBindAppRankingVO> getAtvApiSqCountApiBindAppRanking(@Param("createApiType") int createApiType,
                                                                             @Param("appId") int appId, @Param("apiId") int apiId);
}
