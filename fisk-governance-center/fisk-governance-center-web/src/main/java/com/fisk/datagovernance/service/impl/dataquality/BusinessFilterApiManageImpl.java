package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterSaveDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiConfigPO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiParmPO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiResultPO;
import com.fisk.datagovernance.map.dataquality.BusinessFilterApiMap;
import com.fisk.datagovernance.map.dataquality.BusinessFilterApiParmMap;
import com.fisk.datagovernance.map.dataquality.BusinessFilterApiResultMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiMapper;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiParmMapper;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiResultMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterApiManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterQueryApiVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/10/8 17:05
 */
@Service
public class BusinessFilterApiManageImpl extends ServiceImpl<BusinessFilterApiMapper, BusinessFilterApiConfigPO> implements IBusinessFilterApiManageService {

    @Resource
    private BusinessFilterApiParmMapper businessFilterApiParmMapper;

    @Resource
    private BusinessFilterApiResultMapper businessFilterApiResultMapper;

    @Resource
    private BusinessFilterApiParmManageImpl businessFilterApiParmManageImpl;

    @Resource
    private BusinessFilterApiResultManageImpl businessFilterApiResultManageImpl;

    @Override
    public List<BusinessFilterQueryApiVO> getApiListByRuleIds(List<Integer> ruleIds) {
        List<BusinessFilterQueryApiVO> queryApiVOS = new ArrayList<>();

        if (CollectionUtils.isEmpty(ruleIds)) {
            return queryApiVOS;
        }
        // 查询API基础配置信息
        QueryWrapper<BusinessFilterApiConfigPO> apiConfigPOQueryWrapper = new QueryWrapper<>();
        apiConfigPOQueryWrapper.lambda()
                .in(BusinessFilterApiConfigPO::getRuleId, ruleIds)
                .eq(BusinessFilterApiConfigPO::getDelFlag, 1);
        List<BusinessFilterApiConfigPO> businessFilterApiConfigPOS = baseMapper.selectList(apiConfigPOQueryWrapper);
        if (CollectionUtils.isEmpty(businessFilterApiConfigPOS)) {
            return queryApiVOS;
        }
        // 查询API参数配置信息
        QueryWrapper<BusinessFilterApiParmPO> apiParmPOQueryWrapper = new QueryWrapper<>();
        apiParmPOQueryWrapper.lambda()
                .in(BusinessFilterApiParmPO::getRuleId, ruleIds)
                .eq(BusinessFilterApiParmPO::getDelFlag, 1);
        List<BusinessFilterApiParmPO> businessFilterApiParmPOS = businessFilterApiParmMapper.selectList(apiParmPOQueryWrapper);
        // 查询API结果配置信息
        QueryWrapper<BusinessFilterApiResultPO> apiResultPOQueryWrapper = new QueryWrapper<>();
        apiResultPOQueryWrapper.lambda()
                .in(BusinessFilterApiResultPO::getRuleId, ruleIds)
                .eq(BusinessFilterApiResultPO::getDelFlag, 1);
        List<BusinessFilterApiResultPO> businessFilterApiResultPOS = businessFilterApiResultMapper.selectList(apiResultPOQueryWrapper);

        businessFilterApiConfigPOS.forEach(t -> {
            BusinessFilterQueryApiVO apiVO = new BusinessFilterQueryApiVO();
            apiVO.setRuleId(Integer.valueOf(t.getRuleId()));
            apiVO.setApiConfig(BusinessFilterApiMap.INSTANCES.poToVo(t));
            if (CollectionUtils.isNotEmpty(businessFilterApiParmPOS)) {
                List<BusinessFilterApiParmPO> parmList = businessFilterApiParmPOS.stream().filter(parm -> parm.getApiId() == t.getId()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(parmList)) {
                    apiVO.setApiParmConfig(BusinessFilterApiParmMap.INSTANCES.poToVo(parmList));
                }
            }
            if (CollectionUtils.isNotEmpty(businessFilterApiResultPOS)) {
                List<BusinessFilterApiResultPO> resultList = businessFilterApiResultPOS.stream().filter(result -> result.getApiId() == t.getId()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(resultList)) {
                    apiVO.setApiResultConfig(BusinessFilterApiResultMap.INSTANCES.poToVo(resultList));
                }
            }
            queryApiVOS.add(apiVO);
        });

        return queryApiVOS;
    }

    @Override
    public ResultEnum saveApiInfo(int ruleId, BusinessFilterSaveDTO dto) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        if (ruleId != 0) {
            baseMapper.updateByRuleId(ruleId);
            businessFilterApiParmMapper.updateByRuleId(ruleId);
            businessFilterApiResultMapper.updateByRuleId(ruleId);
        }
        int insertCount = 0;
        BusinessFilterApiConfigPO businessFilterApiConfigPO = BusinessFilterApiMap.INSTANCES.dtoToPo(dto.getApiConfig());
        if (businessFilterApiConfigPO != null) {
            insertCount = baseMapper.insert(businessFilterApiConfigPO);
        }
        if (insertCount > 0) {
            List<BusinessFilterApiParmPO> businessFilterApiParmPOS = BusinessFilterApiParmMap.INSTANCES.dtoToPo(dto.getApiParmConfig());
            if (CollectionUtils.isNotEmpty(businessFilterApiParmPOS)) {
                businessFilterApiParmManageImpl.saveBatch(businessFilterApiParmPOS);
            }
            List<BusinessFilterApiResultPO> businessFilterApiResultPOS = BusinessFilterApiResultMap.INSTANCES.dtoToPo(dto.getApiResultConfig());
            if (CollectionUtils.isNotEmpty(businessFilterApiResultPOS)) {
                businessFilterApiResultManageImpl.saveBatch(businessFilterApiResultPOS);
            }
        }
        return insertCount > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteApiInfo(int ruleId) {
        if (ruleId == 0) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        baseMapper.updateByRuleId(ruleId);
        businessFilterApiParmMapper.updateByRuleId(ruleId);
        businessFilterApiResultMapper.updateByRuleId(ruleId);
        return ResultEnum.SUCCESS;
    }
}
