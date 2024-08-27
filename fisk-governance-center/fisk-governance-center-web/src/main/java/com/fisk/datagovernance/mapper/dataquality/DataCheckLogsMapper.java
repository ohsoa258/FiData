package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckLogsQueryDTO;
import com.fisk.datagovernance.entity.dataquality.DataCheckLogsPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckLogsVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DeleteCheckResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验日志
 * @date 2022/3/23 12:42
 */
@Mapper
public interface DataCheckLogsMapper extends FKBaseMapper<DataCheckLogsPO>
{
    Page<DataCheckLogsVO> getAll(Page<DataCheckLogsVO> page, @Param("query") DataCheckLogsQueryDTO query);

    @Select("SELECT\n" +
            "\tid,\n" +
            "\trule_id AS ruleId,\n" +
            "\tcheck_batch_number AS checkBatchNumber,\n" +
            "\tuser_comment AS userComment \n" +
            "FROM\n" +
            "\ttb_datacheck_rule_logs \n" +
            "WHERE\n" +
            "\tdel_flag = 1 \n" +
            "\tAND IFNULL( user_comment, '' )!= '' \n" +
            "\tAND check_result!='通过' \n"+
            "\tAND check_batch_number = (\n" +
            "\tSELECT\n" +
            "\t\treport_batch_number \n" +
            "\tFROM\n" +
            "\t\ttb_quality_report_log \n" +
            "\tWHERE\n" +
            "\t\tdel_flag = 1 \n" +
            "\t\tAND report_id = #{reportId} \n" +
            "\tORDER BY\n" +
            "\t\tcreate_time DESC \n" +
            "\tLIMIT 0,\n" +
            "\t1)")
    List<DataCheckLogsVO> getDataCheckLogUserComment(@Param("reportId") int reportId);
}