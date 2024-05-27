package com.fisk.datamanagement.mapper;

import com.fisk.datamanagement.entity.MetaAnalysisEmailConfigPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 56263
* @description 针对表【tb_meta_analysis_email_config】的数据库操作Mapper
* @createDate 2024-05-22 11:01:15
* @Entity com.fisk.datamanagement.entity.MetaAnalysisEmailConfigDTO
*/
public interface MetaAnalysisEmailConfigMapper extends BaseMapper<MetaAnalysisEmailConfigPO> {

    @Select("select cron_exp from tb_meta_analysis_email_config")
    List<String> getCron();
}




