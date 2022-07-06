package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataQualityClientManageService;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.datagovernance.vo.dataquality.rule.TableRuleTempVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口实现类
 * @date 2022/4/12 13:47
 */
@Service
@Slf4j
public class DataQualityClientManageImpl implements IDataQualityClientManageService {

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    private DataCheckMapper dataCheckMapper;

    @Resource
    private DataCheckExtendMapper dataCheckExtendMapper;

    @Resource
    private BusinessFilterMapper businessFilterMapper;

    @Resource
    private LifecycleMapper lifecycleMapper;

    @Resource
    private NoticeMapper noticeMapper;

    @Resource
    private NoticeExtendMapper noticeExtendMapper;

    @Override
    public ResultEntity<TableRuleInfoDTO> getTableRuleList(int dataSourceId,
                                                           String tableUnique,
                                                           int tableBusinessType) {
        if (dataSourceId == 0 || StringUtils.isEmpty(tableUnique) || tableBusinessType==0) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_ERROR, null);
        }
        // 数据校验、业务清洗、生命周期所对应的模板Id
        List<Integer> templateIdList = new ArrayList<>();
        // 数据校验、业务清洗、生命周期所对应的Id
        List<Long> ruleIdList = new ArrayList<>();

        // 校验规则
        QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
        dataCheckPOQueryWrapper.lambda().eq(DataCheckPO::getDelFlag, 1)
                .eq(DataCheckPO::getDatasourceId, dataSourceId)
                .eq(DataCheckPO::getTableUnique, tableUnique)
                .eq(DataCheckPO::getTableBusinessType,tableBusinessType)
                .eq(DataCheckPO::getRuleState, 1);
        List<DataCheckPO> dataCheckPOS = dataCheckMapper.selectList(dataCheckPOQueryWrapper);
        List<DataCheckExtendPO> dataCheckExtendPOS = null;
        if (CollectionUtils.isNotEmpty(dataCheckPOS)) {
            List<Long> ruleId = dataCheckPOS.stream().map(DataCheckPO::getId).collect(Collectors.toList());
            List<Integer> getTemplateId = dataCheckPOS.stream().map(DataCheckPO::getTemplateId).distinct().collect(Collectors.toList());
            templateIdList.addAll(getTemplateId);
            ruleIdList.addAll(ruleId);

            QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
            dataCheckExtendPOQueryWrapper.lambda().eq(DataCheckExtendPO::getDelFlag, 1)
                    .in(DataCheckExtendPO::getRuleId, ruleId);
            dataCheckExtendPOS = dataCheckExtendMapper.selectList(dataCheckExtendPOQueryWrapper);
        }

        // 清洗规则
        QueryWrapper<BusinessFilterPO> businessFilterPOQueryWrapper = new QueryWrapper<>();
        businessFilterPOQueryWrapper.lambda().eq(BusinessFilterPO::getDelFlag, 1)
                .eq(BusinessFilterPO::getDatasourceId, dataSourceId)
                .eq(BusinessFilterPO::getTableUnique, tableUnique)
                .eq(BusinessFilterPO::getTableBusinessType,tableBusinessType)
                .eq(BusinessFilterPO::getRuleState, 1);
        List<BusinessFilterPO> businessFilterPOS = businessFilterMapper.selectList(businessFilterPOQueryWrapper);
        if (CollectionUtils.isNotEmpty(businessFilterPOS)) {
            List<Integer> getTemplateId = businessFilterPOS.stream().map(BusinessFilterPO::getTemplateId).distinct().collect(Collectors.toList());
            templateIdList.addAll(getTemplateId);
            List<Long> ruleId = businessFilterPOS.stream().map(BusinessFilterPO::getId).collect(Collectors.toList());
            ruleIdList.addAll(ruleId);
        }

        // 生命周期
        QueryWrapper<LifecyclePO> lifecyclePOQueryWrapper = new QueryWrapper<>();
        lifecyclePOQueryWrapper.lambda().eq(LifecyclePO::getDelFlag, 1)
                .eq(LifecyclePO::getDatasourceId, dataSourceId)
                .eq(LifecyclePO::getTableUnique, tableUnique)
                .eq(LifecyclePO::getTableBusinessType,tableBusinessType)
                .eq(LifecyclePO::getRuleState, 1);
        List<LifecyclePO> lifecyclePOS = lifecycleMapper.selectList(lifecyclePOQueryWrapper);
        if (CollectionUtils.isNotEmpty(lifecyclePOS)) {
            List<Integer> getTemplateId = lifecyclePOS.stream().map(LifecyclePO::getTemplateId).distinct().collect(Collectors.toList());
            templateIdList.addAll(getTemplateId);
            List<Long> ruleId = lifecyclePOS.stream().map(LifecyclePO::getId).collect(Collectors.toList());
            ruleIdList.addAll(ruleId);
        }

        // 告警通知
        List<NoticePO> noticePOS = null;
        List<NoticeExtendPO> noticeExtendPOS = null;
        List<Integer> noticeModuleTypes = new ArrayList<>();
        noticeModuleTypes.add(ModuleTypeEnum.DATACHECK_MODULE.getValue());
        noticeModuleTypes.add(ModuleTypeEnum.BIZCHECK_MODULE.getValue());
        noticeModuleTypes.add(ModuleTypeEnum.LIFECYCLE_MODULE.getValue());
        if (CollectionUtils.isNotEmpty(ruleIdList)) {
            ruleIdList = ruleIdList.stream().distinct().collect(Collectors.toList());
            QueryWrapper<NoticeExtendPO> noticeExtendPOQueryWrapper = new QueryWrapper<>();
            noticeExtendPOQueryWrapper.lambda().eq(NoticeExtendPO::getDelFlag, 1)
                    .in(NoticeExtendPO::getModuleType, noticeModuleTypes)
                    .in(NoticeExtendPO::getRuleId, ruleIdList);
            noticeExtendPOS = noticeExtendMapper.selectList(noticeExtendPOQueryWrapper);
            if (CollectionUtils.isNotEmpty(noticeExtendPOS)) {
                List<Integer> noticeId = noticeExtendPOS.stream().map(NoticeExtendPO::getNoticeId).distinct().collect(Collectors.toList());
                QueryWrapper<NoticePO> noticePOQueryWrapper = new QueryWrapper<>();
                noticePOQueryWrapper.lambda().eq(NoticePO::getDelFlag, 1)
                        .eq(NoticePO::getNoticeState, 1)
                        .in(NoticePO::getId, noticeId);
                noticePOS = noticeMapper.selectList(noticePOQueryWrapper);
                if (CollectionUtils.isNotEmpty(noticePOS)) {
                    List<Integer> getTemplateId = noticePOS.stream().map(NoticePO::getTemplateId).distinct().collect(Collectors.toList());
                    templateIdList.addAll(getTemplateId);
                }
            }
        }

        List<TemplatePO> templatePOS = null;
        if (CollectionUtils.isNotEmpty(templateIdList)) {
            templateIdList = templateIdList.stream().distinct().collect(Collectors.toList());
            QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
            templatePOQueryWrapper.lambda().eq(TemplatePO::getDelFlag, 1)
                    .in(TemplatePO::getId, templateIdList);
            templatePOS = templateMapper.selectList(templatePOQueryWrapper);
        }

        TemplatePO templatePO = null;
        TableRuleTempVO tempVO = null;
        List<TableRuleTempVO> tempVOS = new ArrayList<>();

        // 循环数据校验规则
        for (DataCheckPO dataCheckPO : dataCheckPOS) {
            // 查询该校验规则对应的校验模板
            templatePO = templatePOS.stream().filter(t -> t.getId() == dataCheckPO.getTemplateId()).findFirst().orElse(null);
            if (templatePO != null) {
                // 判断该规则的应用范围是到表还是字段，如果是字段需要到扩展表中查询具体的校验规则
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                if (templateType.getRangeType() == "TABLE") {
                    tempVO = new TableRuleTempVO();
                    tempVO.setKey(dataCheckPO.getTableUnique());
                    tempVO.setRuleValue(templatePO.getSceneDesc());
                    tempVO.setType(templateType.getRangeType());
                    tempVO.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                    tempVO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
                    tempVOS.add(tempVO);
                } else if (templateType.getRangeType() == "FIELD") {
                    if (templateType == TemplateTypeEnum.FIELD_RULE_TEMPLATE) {
                        // 字段校验规则，查询扩展表得到详细的校验信息
                        List<DataCheckExtendPO> dataCheckExtendPOS1 = dataCheckExtendPOS.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(dataCheckExtendPOS1)) {
                            for (DataCheckExtendPO dataCheckExtendPO : dataCheckExtendPOS1) {
                                if (StringUtils.isNotEmpty(dataCheckExtendPO.getCheckType())) {
                                    String[] split = dataCheckExtendPO.getCheckType().split(",");
                                    if (split != null && split.length > 0) {
                                        for (String checkType : split) {
                                            CheckTypeEnum checkTypeEnum = CheckTypeEnum.getEnum(Integer.parseInt(checkType));
                                            tempVO = new TableRuleTempVO();
                                            tempVO.setKey(dataCheckExtendPO.getFieldUnique());
                                            tempVO.setType(templateType.getRangeType());
                                            tempVO.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                                            tempVO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
                                            if (checkTypeEnum == CheckTypeEnum.DATA_CHECK) {
                                                DataCheckTypeEnum dataCheckTypeEnum = DataCheckTypeEnum.getEnum(dataCheckExtendPO.getDataCheckType());
                                                tempVO.setRuleValue(dataCheckTypeEnum.getName());
                                            } else if (checkTypeEnum == CheckTypeEnum.UNIQUE_CHECK || checkTypeEnum == CheckTypeEnum.NONEMPTY_CHECK) {
                                                tempVO.setRuleValue(checkTypeEnum.getName());
                                            }
                                            tempVOS.add(tempVO);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        tempVO = new TableRuleTempVO();
                        tempVO.setKey(dataCheckPO.getTableUnique());
                        tempVO.setRuleValue(templatePO.getSceneDesc());
                        tempVO.setType(templateType.getRangeType());
                        tempVO.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                        tempVO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
                        tempVOS.add(tempVO);
                    }
                }
            }
        }

        // 循环业务清洗规则
        for (BusinessFilterPO businessFilterPO : businessFilterPOS) {
            // 查询该清洗规则对应的模板
            templatePO = templatePOS.stream().filter(t -> t.getId() == businessFilterPO.getTemplateId()).findFirst().orElse(null);
            if (templatePO != null) {
                // 判断该规则的应用范围是到表还是字段，清洗模板目前都只应用到表
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                if (templateType.getRangeType() == "TABLE") {
                    tempVO = new TableRuleTempVO();
                    tempVO.setKey(businessFilterPO.getTableUnique());
                    tempVO.setRuleValue(templatePO.getSceneDesc());
                    tempVO.setType(templateType.getRangeType());
                    tempVO.setModuleType(ModuleTypeEnum.BIZCHECK_MODULE);
                    tempVO.setRuleId(Math.toIntExact(businessFilterPO.getId()));
                    tempVOS.add(tempVO);
                }
            }
        }

        // 循环生命周期规则
        for (LifecyclePO lifecyclePO : lifecyclePOS) {
            // 查询该生命周期规则对应的模板
            templatePO = templatePOS.stream().filter(t -> t.getId() == lifecyclePO.getTemplateId()).findFirst().orElse(null);
            if (templatePO != null) {
                // 判断该规则的应用范围是到表还是字段，生命周期模板目前都只应用到表
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                if (templateType.getRangeType() == "TABLE") {
                    tempVO = new TableRuleTempVO();
                    tempVO.setKey(lifecyclePO.getTableUnique());
                    tempVO.setRuleValue(templatePO.getSceneDesc());
                    tempVO.setType(templateType.getRangeType());
                    tempVO.setModuleType(ModuleTypeEnum.LIFECYCLE_MODULE);
                    tempVO.setRuleId(Math.toIntExact(lifecyclePO.getId()));
                    tempVOS.add(tempVO);
                }
            }
        }

        TableRuleInfoDTO tableRule = new TableRuleInfoDTO();
        tableRule.setName(tableUnique);
        tableRule.setType(1);
        List<TableRuleInfoDTO> fieldRules = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(tempVOS)) {
            // 获取表一级的校验规则
            List<TableRuleTempVO> tableRules = tempVOS.stream().filter(t -> t.getType() == "TABLE"
                    && t.getRuleValue() != null && t.getRuleValue() != "").collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(tableRules)) {
                // 表的数据校验规则赋值
                tableRule.checkRules = tableRules.stream().filter(t -> t.moduleType == ModuleTypeEnum.DATACHECK_MODULE).
                        map(TableRuleTempVO::getRuleValue).distinct().collect(Collectors.toList());
                // 表的业务清洗规则赋值
                tableRule.filterRules = tableRules.stream().filter(t -> t.moduleType == ModuleTypeEnum.BIZCHECK_MODULE).
                        map(TableRuleTempVO::getRuleValue).distinct().collect(Collectors.toList());
                // 表的生命周期规则赋值
                tableRule.lifecycleRules = tableRules.stream().filter(t -> t.moduleType == ModuleTypeEnum.LIFECYCLE_MODULE).
                        map(TableRuleTempVO::getRuleValue).distinct().collect(Collectors.toList());
                // 表的通知提醒规则赋值
                if (CollectionUtils.isNotEmpty(noticePOS) && CollectionUtils.isNotEmpty(noticeExtendPOS)) {
                    List<Integer> collect = tableRules.stream().map(TableRuleTempVO::getRuleId).collect(Collectors.toList());
                    List<Integer> collect1 = noticeExtendPOS.stream().filter(t -> collect.contains(t.getRuleId())).
                            map(NoticeExtendPO::getNoticeId).distinct().collect(Collectors.toList());
                    List<Integer> collect2 = noticePOS.stream().filter(t -> collect1.contains(t.getId())).
                            map(NoticePO::getNoticeType).distinct().collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect2)) {
                        for (Integer integer : collect2) {
                            NoticeTypeEnum noticeType = NoticeTypeEnum.getEnum(integer);
                            tableRule.noticeRules.add(noticeType.getName());
                        }
                    }
                }
            }
            // 获取字段一级的校验规则
            List<TableRuleTempVO> fieldRule = tempVOS.stream().filter(t -> t.getType() == "FIELD"
                    && t.getRuleValue() != null && t.getRuleValue() != "" && t.getKey() != null && t.getKey() != "").collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(fieldRule)) {
                // 字段的数据校验规则赋值
                List<String> fieldList = fieldRule.stream().filter(t -> t.moduleType == ModuleTypeEnum.DATACHECK_MODULE).
                        map(TableRuleTempVO::getKey).distinct().collect(Collectors.toList());
                for (String field : fieldList) {
                    TableRuleInfoDTO fieldRuleInfoVO = new TableRuleInfoDTO();
                    fieldRuleInfoVO.setName(field);
                    fieldRuleInfoVO.setType(2);
                    List<String> ruleValues = fieldRule.stream().filter(t -> t.getKey().equals(field) && t.moduleType == ModuleTypeEnum.DATACHECK_MODULE).
                            map(TableRuleTempVO::getRuleValue).distinct().collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(ruleValues)) {
                        fieldRuleInfoVO.checkRules = ruleValues;
                    }
                    List<Integer> ruleIds = fieldRule.stream().filter(t -> t.getKey().equals(field) && t.moduleType == ModuleTypeEnum.DATACHECK_MODULE).
                            map(TableRuleTempVO::getRuleId).distinct().collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(ruleIds) &&
                            CollectionUtils.isNotEmpty(noticePOS) &&
                            CollectionUtils.isNotEmpty(noticeExtendPOS)) {
                        List<Integer> collect1 = noticeExtendPOS.stream().filter(t -> ruleIds.contains(t.getRuleId())).
                                map(NoticeExtendPO::getNoticeId).distinct().collect(Collectors.toList());
                        List<Integer> collect2 = noticePOS.stream().filter(t -> collect1.contains(t.getId())).
                                map(NoticePO::getNoticeType).distinct().collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(collect2)) {
                            for (Integer integer : collect2) {
                                NoticeTypeEnum noticeType = NoticeTypeEnum.getEnum(integer);
                                if (!fieldRuleInfoVO.noticeRules.contains(noticeType.getName())) {
                                    fieldRuleInfoVO.noticeRules.add(noticeType.getName());
                                }
                            }
                        }
                    }
                    fieldRules.add(fieldRuleInfoVO);
                }
                tableRule.fieldRules = fieldRules;
            }
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, tableRule);
    }

    @Override
    public ResultEntity<List<DataSourceConVO>> getAllDataSource() {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataSourceConManageImpl.getAllDataSource());
    }
}
