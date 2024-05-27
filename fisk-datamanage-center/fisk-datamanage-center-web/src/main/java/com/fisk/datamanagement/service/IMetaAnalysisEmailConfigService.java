package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.metaanalysisemailconfig.MetaAnalysisEmailConfigDTO;
import com.fisk.datamanagement.entity.MetaAnalysisEmailConfigPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tb_meta_analysis_email_config】的数据库操作Service
* @createDate 2024-05-22 11:01:15
*/
public interface IMetaAnalysisEmailConfigService extends IService<MetaAnalysisEmailConfigPO> {

    /**
     * 变更分析发送邮件
     */
    void sendEmailOfMetaAudit();

    /**
     * 获取变更分析邮箱配置的详情
     */
    MetaAnalysisEmailConfigDTO getMetaAnalysisEmailConfig();

    /**
     * 编辑变更分析邮箱配置的详情
     * @param dto
     * @return
     */
    Object editMetaAnalysisEmailConfig(MetaAnalysisEmailConfigDTO dto);

}
