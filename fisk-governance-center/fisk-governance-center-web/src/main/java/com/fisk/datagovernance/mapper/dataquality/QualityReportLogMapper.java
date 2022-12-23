package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.QualityReportLogPO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportLogVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告日志
 * @date 2022/5/16 18:24
 */
@Mapper
public interface QualityReportLogMapper extends FKBaseMapper<QualityReportLogPO> {
    /**
     * 查询数据校验分页列表
     *
     * @param page     分页信息
     * @param reportId 报告id
     * @param keyword  关键字
     * @return 查询结果
     */
    Page<QualityReportLogVO> getAll(Page<QualityReportLogVO> page, @Param("reportId") int reportId, @Param("keyword") String keyword);

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_quality_report_log(`report_id`, `report_name`, `report_type`, `report_type_name`, `report_desc`, `report_principal`, `report_notice_type`, `email_server_id`,`subject`, `body`, `recipient`, `send_time` ,`send_result`, `create_time`, `create_user`, `del_flag`) VALUES (#{reportId}, #{reportName}, #{reportType}, #{reportTypeName}, #{reportDesc}, #{reportPrincipal}, #{reportNoticeType}, #{emailServerId}, #{subject}, #{body}, #{recipient}, #{sendTime}, #{sendResult}, #{createTime}, #{createUser},1);")
    int insertOne(QualityReportLogPO po);
}
