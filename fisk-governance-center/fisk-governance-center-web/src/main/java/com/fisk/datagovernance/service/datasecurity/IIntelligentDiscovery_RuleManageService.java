package com.fisk.datagovernance.service.datasecurity;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_LogsQueryDTO;
import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_RuleDTO;
import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_RuleQueryDTO;
import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_WhiteListDTO;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_RulePO;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.IntelligentDiscovery_LogsVO;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.IntelligentDiscovery_RuleExtInfoVO;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.IntelligentDiscovery_RuleVO;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.IntelligentDiscovery_ScanResultVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface IIntelligentDiscovery_RuleManageService extends IService<IntelligentDiscovery_RulePO> {

    List<FilterFieldDTO> getSearchColumn();

    Page<IntelligentDiscovery_RuleVO> getRulePageList(IntelligentDiscovery_RuleQueryDTO dto);

    ResultEnum addRule(IntelligentDiscovery_RuleDTO dto);

    ResultEnum editRule(IntelligentDiscovery_RuleDTO dto);

    ResultEnum editRuleState(int id);

    ResultEnum deleteRule(int id);

    Page<IntelligentDiscovery_LogsVO> getRuleScanLogPageList(IntelligentDiscovery_LogsQueryDTO dto);

    void downloadRuleScanResult(String uniqueId, HttpServletResponse response);

    ResultEntity<List<IntelligentDiscovery_ScanResultVO>> previewScanResult(String absolutePath);

    IntelligentDiscovery_RuleExtInfoVO getRuleExtInfo();

    List<String> getNextCronExeTime(String cron);

    ResultEntity<Object> createScanReport(int id);

    ResultEnum saveWhiteList(IntelligentDiscovery_WhiteListDTO dto);
}
