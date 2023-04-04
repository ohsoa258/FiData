package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterSaveDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiConfigPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterApiAuthVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterQueryApiVO;

import java.util.List;

public interface IBusinessFilterApiManageService extends IService<BusinessFilterApiConfigPO> {
    /**
     * 根据清洗规则ID查询API清洗规则
     *
     * @return API清洗规则
     */
    List<BusinessFilterQueryApiVO> getApiListByRuleIds(List<Integer> ruleIds);

    /**
     * 保存API清洗规则
     *
     * @return API清洗规则
     */
    ResultEnum saveApiInfo(String operationType, int ruleId, BusinessFilterSaveDTO dto);

    /**
     * 删除API清洗规则
     *
     * @return API清洗规则
     */
    ResultEnum deleteApiInfo(int ruleId);

    /**
     * 调用授权API，获取票据信息
     *
     * @return
     */
    ResultEntity<String> collAuthApi(BusinessFilterSaveDTO dto);

    /**
     * 调用API，更新业务数据
     *
     * @return
     */
    ResultEnum collApi(BusinessFilterSaveDTO dto);
}
