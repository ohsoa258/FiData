package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterEditDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterQueryDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterSortDto;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.entity.dataquality.TemplatePO;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateTypeEnum;
import com.fisk.datagovernance.map.dataquality.BusinessFilterMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterMapper;
import com.fisk.datagovernance.mapper.dataquality.DataSourceConMapper;
import com.fisk.datagovernance.mapper.dataquality.TemplateMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterQueryApiVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗
 * @date 2022/3/23 12:56
 */
@Service
public class BusinessFilterManageImpl extends ServiceImpl<BusinessFilterMapper, BusinessFilterPO> implements IBusinessFilterManageService {

    @Resource
    private BusinessFilterMapper businessFilterMapper;

    @Resource
    private BusinessFilterManageImpl businessFilterManageImpl;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private DataSourceConMapper dataSourceConMapper;

    @Resource
    private ExternalInterfaceImpl externalInterfaceImpl;

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    private BusinessFilterApiManageImpl businessFilterApiManageImpl;

    @Resource
    private UserHelper userHelper;

    @Override
    public Page<BusinessFilterVO> getAll(BusinessFilterQueryDTO query)
    {
        int idByDataSourceId = dataSourceConManageImpl.getIdByDataSourceId(query.sourceTypeEnum, query.datasourceId);
        if (query.sourceTypeEnum == SourceTypeEnum.FiData) {
            query.datasourceId = idByDataSourceId;
        }
        Page<BusinessFilterVO> all = baseMapper.getAll(query.page, query.datasourceId, query.tableUnique,
                query.tableBusinessType, query.keyword);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<BusinessFilterVO> allExtends = getAllExtends(all.getRecords());
            all.setRecords(allExtends);
        }
        return all;
    }

    private List<BusinessFilterVO> getAllExtends(List<BusinessFilterVO> source){
        List<BusinessFilterVO> result = source;
        List<BusinessFilterQueryApiVO> apiListByRuleIds = null;
        List<BusinessFilterVO> apiRules = source.stream().filter(t -> t.getTemplateType() == TemplateTypeEnum.API_FILTER_TEMPLATE).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(apiRules)) {
            List<Integer> apiRuleIds = apiRules.stream().map(BusinessFilterVO::getId).collect(Collectors.toList());
            apiListByRuleIds = businessFilterApiManageImpl.getApiListByRuleIds(apiRuleIds);
        }
        if (CollectionUtils.isNotEmpty(apiListByRuleIds)) {
            for (int i = 0; i < source.size(); i++) {
                BusinessFilterVO businessFilterVO = source.get(i);
                BusinessFilterQueryApiVO apiVO = apiListByRuleIds.stream().filter(t -> t.getRuleId() == businessFilterVO.getId()).findFirst().orElse(null);
                if (apiVO != null) {
                    businessFilterVO.setApiInfo(apiVO);
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(BusinessFilterDTO dto) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        if (dto.sourceTypeEnum == SourceTypeEnum.FiData) {
            int idByDataSourceId = dataSourceConManageImpl.getIdByDataSourceId(dto.sourceTypeEnum, dto.datasourceId);
            if (idByDataSourceId == 0) {
                return ResultEnum.DATA_QUALITY_DATASOURCE_ONTEXISTS;
            }
            dto.datasourceId = idByDataSourceId;
        }
        //第一步：验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //第二步：转换DTO对象为PO对象
        BusinessFilterPO businessFilterPO = BusinessFilterMap.INSTANCES.dtoToPo(dto);
        if (businessFilterPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存业务清洗信息
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        businessFilterPO.setCreateTime(LocalDateTime.now());
        businessFilterPO.setCreateUser(String.valueOf(loginUserInfo.getId()));
        int i = baseMapper.insertOne(businessFilterPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存业务清洗API信息
        if (templatePO.getTemplateType() == TemplateTypeEnum.API_FILTER_TEMPLATE.getValue() &&
                dto.getApiInfo() != null) {
            resultEnum = businessFilterApiManageImpl.saveApiInfo("add", Math.toIntExact(businessFilterPO.getId()), dto.getApiInfo());
            if (resultEnum != ResultEnum.SUCCESS) {
                return resultEnum;
            }
        }
        //第五步：调用元数据接口获取最新的规则信息
        externalInterfaceImpl.synchronousTableBusinessMetaData(dto.getDatasourceId(), dto.getSourceTypeEnum(), dto.getTableBusinessType(), dto.getTableUnique());
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(BusinessFilterEditDTO dto) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        //第一步：验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        BusinessFilterPO businessFilterPO = baseMapper.selectById(dto.id);
        if (businessFilterPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //第二步：转换DTO对象为PO对象
        businessFilterPO = BusinessFilterMap.INSTANCES.dtoToPo_Edit(dto);
        if (businessFilterPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存业务清洗信息
        int i = baseMapper.updateById(businessFilterPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存业务清洗API信息
        if (templatePO.getTemplateType() == TemplateTypeEnum.API_FILTER_TEMPLATE.getValue() &&
                dto.getApiInfo() != null) {
            resultEnum = businessFilterApiManageImpl.saveApiInfo("edit", Math.toIntExact(businessFilterPO.getId()), dto.getApiInfo());
            if (resultEnum != ResultEnum.SUCCESS) {
                return resultEnum;
            }
        }
        //第五步：调用元数据接口获取最新的规则信息
        externalInterfaceImpl.synchronousTableBusinessMetaData(dto.getDatasourceId(), dto.getSourceTypeEnum(), dto.getTableBusinessType(), dto.getTableUnique());
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(int id) {
        BusinessFilterPO businessFilterPO = baseMapper.selectById(id);
        if (businessFilterPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 调用元数据接口获取最新的规则信息
        DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(businessFilterPO.getDatasourceId());
        if (dataSourceConPO != null) {
            SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getEnum(dataSourceConPO.getDatasourceType());
            externalInterfaceImpl.synchronousTableBusinessMetaData(businessFilterPO.getDatasourceId(), sourceTypeEnum, businessFilterPO.getTableBusinessType(), businessFilterPO.getTableUnique());
        }
        // 删除API清洗模板扩展规则
        businessFilterApiManageImpl.deleteApiInfo(id);
        return baseMapper.deleteByIdWithFill(businessFilterPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editModuleExecSort(List<BusinessFilterSortDto> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return ResultEnum.DATA_QUALITY_REQUESTSORT_ERROR;
        }
        List<Integer> collect = dto.stream().map(BusinessFilterSortDto::getId).distinct().collect(Collectors.toList());
        QueryWrapper<BusinessFilterPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessFilterPO::getDelFlag, 1).in(BusinessFilterPO::getId, collect);
        List<BusinessFilterPO> businessFilterPOS = businessFilterMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(businessFilterPOS)) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        businessFilterPOS.forEach(e -> {
            Optional<BusinessFilterSortDto> first = dto.stream().filter(item -> item.getId() == e.getId()).findFirst();
            if (first.isPresent()) {
                BusinessFilterSortDto businessFilterSortDto = first.get();
                if (businessFilterSortDto != null) {
                    e.setRuleSort(businessFilterSortDto.getModuleExecSort());
                }
            }
        });
        boolean b = businessFilterManageImpl.updateBatchById(businessFilterPOS);
        return b ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
