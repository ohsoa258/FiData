package com.fisk.datagovernance.mapper.datasecurity;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.QualityReportLogPO;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_LogsPO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportLogVO;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.IntelligentDiscovery_LogsVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IntelligentDiscovery_LogsMapper extends FKBaseMapper<IntelligentDiscovery_LogsPO> {

    Page<IntelligentDiscovery_LogsVO> filter(Page<IntelligentDiscovery_LogsVO> page, @Param("ruleId") int ruleId, @Param("keyWord") String keyWord);
}
