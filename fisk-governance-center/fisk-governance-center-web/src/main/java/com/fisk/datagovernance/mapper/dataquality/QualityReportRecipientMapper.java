package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.QualityReportRecipientPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告接收人
 * @date 2022/5/16 18:24
 */
@Mapper
public interface QualityReportRecipientMapper extends FKBaseMapper<QualityReportRecipientPO> {
    /**
     * 修改质量报表接收人
     *
     * @return 执行结果
     */
    @Update("UPDATE tb_quality_report_recipient SET del_flag=0 WHERE report_id = #{reportId};")
    int updateByReportId(@Param("reportId") int reportId);
}
