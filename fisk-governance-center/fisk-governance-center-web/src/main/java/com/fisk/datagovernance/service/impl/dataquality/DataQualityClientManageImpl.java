package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.Dto.Excel.*;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.core.utils.office.excel.ExcelReportUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckSyncParamDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.TableRuleSqlDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportNoticeDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportRecipientDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataQualityClientManageService;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.rule.TableRuleTempVO;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private QualityReportMapper qualityReportMapper;

    @Resource
    private QualityReportManageImpl qualityReportManage;

    @Resource
    private QualityReportRuleMapper qualityReportRuleMapper;

    @Resource
    private AttachmentInfoMapper attachmentInfoMapper;

    @Resource
    private QualityReportLogMapper qualityReportLogMapper;

    @Resource
    private QualityReportRecipientMapper qualityReportRecipientMapper;

    @Resource
    private QualityReportNoticeMapper qualityReportNoticeMapper;

    @Value("${file.uploadUrl}")
    private String uploadUrl;

    @Override
    public ResultEntity<TableRuleInfoDTO> getTableRuleList(int dataSourceId, String tableUnique, int tableBusinessType) {
        if (dataSourceId == 0 || StringUtils.isEmpty(tableUnique)) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_ERROR, null);
        }
        // FiData数据源ID
        int filndDataSourceId = dataSourceId;
        DataSourceConVO dataSourceConVO = dataSourceConManageImpl.getAllDataSource().stream().filter(t -> t.getDatasourceId() == filndDataSourceId && t.getDatasourceType() == SourceTypeEnum.FiData).findFirst().orElse(null);
        if (dataSourceConVO == null) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_ERROR, null);
        }
        // 数据质量数据源ID
        dataSourceId = dataSourceConVO.getId();

        // 数据校验、业务清洗、生命周期所对应的Id
        List<Long> ruleIdList = new ArrayList<>();
/*
        // 校验规则
        QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
        dataCheckPOQueryWrapper.lambda().eq(DataCheckPO::getDelFlag, 1)
                .eq(DataCheckPO::getDatasourceId, dataSourceId)
                .eq(DataCheckPO::getTableUnique, tableUnique)
                .eq(DataCheckPO::getTableBusinessType, tableBusinessType)
                .eq(DataCheckPO::getRuleState, 1);
        List<DataCheckPO> dataCheckPOS = dataCheckMapper.selectList(dataCheckPOQueryWrapper);
        List<DataCheckExtendPO> dataCheckExtendPOS = null;
        if (CollectionUtils.isNotEmpty(dataCheckPOS)) {
            List<Long> ruleIds = dataCheckPOS.stream().map(DataCheckPO::getId).collect(Collectors.toList());
            ruleIdList.addAll(ruleIds);
            QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
            dataCheckExtendPOQueryWrapper.lambda().eq(DataCheckExtendPO::getDelFlag, 1)
                    .in(DataCheckExtendPO::getRuleId, ruleIds);
            dataCheckExtendPOS = dataCheckExtendMapper.selectList(dataCheckExtendPOQueryWrapper);
        }

        // 清洗规则
//        QueryWrapper<BusinessFilterPO> businessFilterPOQueryWrapper = new QueryWrapper<>();
//        businessFilterPOQueryWrapper.lambda().eq(BusinessFilterPO::getDelFlag, 1)
//                .eq(BusinessFilterPO::getDatasourceId, dataSourceId)
//                .eq(BusinessFilterPO::getTableUnique, tableUnique)
//                .eq(BusinessFilterPO::getTableBusinessType, tableBusinessType)
//                .eq(BusinessFilterPO::getRuleState, 1);
//        List<BusinessFilterPO> businessFilterPOS = businessFilterMapper.selectList(businessFilterPOQueryWrapper);
//        if (CollectionUtils.isNotEmpty(businessFilterPOS)) {
//            List<Long> ruleIds = businessFilterPOS.stream().map(BusinessFilterPO::getId).collect(Collectors.toList());
//            ruleIdList.addAll(ruleIds);
//        }

        // 生命周期
        QueryWrapper<LifecyclePO> lifecyclePOQueryWrapper = new QueryWrapper<>();
        lifecyclePOQueryWrapper.lambda().eq(LifecyclePO::getDelFlag, 1)
                .eq(LifecyclePO::getDatasourceId, dataSourceId)
                .eq(LifecyclePO::getTableUnique, tableUnique)
                .eq(LifecyclePO::getTableBusinessType, tableBusinessType)
                .eq(LifecyclePO::getRuleState, 1);
        List<LifecyclePO> lifecyclePOS = lifecycleMapper.selectList(lifecyclePOQueryWrapper);
        if (CollectionUtils.isNotEmpty(lifecyclePOS)) {
            List<Long> ruleIds = lifecyclePOS.stream().map(LifecyclePO::getId).collect(Collectors.toList());
            ruleIdList.addAll(ruleIds);
        }

//        // 告警通知
//        List<NoticePO> noticePOS = null;
//        List<NoticeExtendPO> noticeExtendPOS = null;
//        List<Integer> noticeModuleTypes = new ArrayList<>();
//        noticeModuleTypes.add(ModuleTypeEnum.DATACHECK_MODULE.getValue());
//        noticeModuleTypes.add(ModuleTypeEnum.BIZCHECK_MODULE.getValue());
//        noticeModuleTypes.add(ModuleTypeEnum.LIFECYCLE_MODULE.getValue());
//        if (CollectionUtils.isNotEmpty(ruleIdList)) {
//            ruleIdList = ruleIdList.stream().distinct().collect(Collectors.toList());
//            QueryWrapper<NoticeExtendPO> noticeExtendPOQueryWrapper = new QueryWrapper<>();
//            noticeExtendPOQueryWrapper.lambda().eq(NoticeExtendPO::getDelFlag, 1)
//                    .in(NoticeExtendPO::getModuleType, noticeModuleTypes)
//                    .in(NoticeExtendPO::getRuleId, ruleIdList);
//            noticeExtendPOS = noticeExtendMapper.selectList(noticeExtendPOQueryWrapper);
//            if (CollectionUtils.isNotEmpty(noticeExtendPOS)) {
//                List<Integer> noticeIds = noticeExtendPOS.stream().map(NoticeExtendPO::getNoticeId).distinct().collect(Collectors.toList());
//                QueryWrapper<NoticePO> noticePOQueryWrapper = new QueryWrapper<>();
//                noticePOQueryWrapper.lambda().eq(NoticePO::getDelFlag, 1)
//                        .eq(NoticePO::getNoticeState, 1)
//                        .in(NoticePO::getId, noticeIds);
//                noticePOS = noticeMapper.selectList(noticePOQueryWrapper);
//            }
//        }

        // 查询数据校验、业务清洗、生命周期、告警设置所对应的模板信息
        QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
        templatePOQueryWrapper.lambda().eq(TemplatePO::getDelFlag, 1);
        List<TemplatePO> templatePOS = templateMapper.selectList(templatePOQueryWrapper);

        // 查询实际的表字段名称
        List<DataTableFieldDTO> dtoList = new ArrayList<>();
        DataTableFieldDTO dataTableFieldDTO = new DataTableFieldDTO();
        dataTableFieldDTO.setId(tableUnique);
        dataTableFieldDTO.setDataSourceConfigEnum(DataSourceConfigEnum.getEnum(dataSourceConVO.getDatasourceId()));
        dataTableFieldDTO.setTableBusinessTypeEnum(TableBusinessTypeEnum.getEnum(tableBusinessType));
        dtoList.add(dataTableFieldDTO);
        List<FiDataMetaDataDTO> fiDataMetaDatas = dataSourceConManageImpl.getTableFieldName(dtoList);
        if (CollectionUtils.isEmpty(fiDataMetaDatas)) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_REDIS_NOTEXISTSTABLEFIELD, null);
        }
        FiDataMetaDataTreeDTO fiDataMetaData_Table = fiDataMetaDatas.get(0).getChildren().get(0);
        List<FiDataMetaDataTreeDTO> fiDataMetaData_Fields = fiDataMetaData_Table.getChildren();
        String tableName = "";
        if (fiDataMetaData_Table != null) {
            tableName = fiDataMetaData_Table.getLabel();
        }

        TemplatePO templatePO = null;
        TableRuleTempVO tempVO_TableField = null;
        List<TableRuleTempVO> tempVOS = new ArrayList<>();

        // 循环数据校验规则
        for (DataCheckPO dataCheckPO : dataCheckPOS) {
            // 查询该校验规则对应的校验模板
            templatePO = templatePOS.stream().filter(t -> t.getId() == dataCheckPO.getTemplateId()).findFirst().orElse(null);
            if (templatePO != null) {
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                List<DataCheckExtendPO> dataCheckExtendFilter = null;
                switch (templateType) {
                    case FIELD_RULE_TEMPLATE:
                        // 字段规则模板
                        dataCheckExtendFilter = dataCheckExtendPOS.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(dataCheckExtendFilter)) {
                            for (DataCheckExtendPO dataCheckExtendPO : dataCheckExtendFilter) {
                                String fieldName = "";
                                FiDataMetaDataTreeDTO fiDataMetaData_Field = fiDataMetaData_Fields.stream().
                                        filter(f -> f.getId().equals(dataCheckExtendPO.getFieldUnique())).findFirst().orElse(null);
                                if (fiDataMetaData_Field != null) {
                                    fieldName = fiDataMetaData_Field.getLabel();
                                }
                                if (StringUtils.isEmpty(fieldName)) {
                                    continue;
                                }
                                if (StringUtils.isNotEmpty(dataCheckExtendPO.getCheckType())) {
                                    String[] split = dataCheckExtendPO.getCheckType().split(",");
                                    for (String checkType : split) {
                                        CheckTypeEnum checkTypeEnum = CheckTypeEnum.getEnum(Integer.parseInt(checkType));
                                        tempVO_TableField = new TableRuleTempVO();
                                        tempVO_TableField.setRuleId(dataCheckPO.getId());
                                        tempVO_TableField.setRuleName(dataCheckPO.getRuleName());
                                        tempVO_TableField.setFieldUnique(dataCheckExtendPO.getFieldUnique());
                                        tempVO_TableField.setType("FIELD");
                                        tempVO_TableField.setFieldName(fieldName);
                                        tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                                        if (checkTypeEnum == CheckTypeEnum.DATA_CHECK) {
                                            DataCheckTypeEnum dataCheckTypeEnum = DataCheckTypeEnum.getEnum(dataCheckExtendPO.getDataCheckType());
                                            switch (dataCheckTypeEnum) {
                                                case TEXTLENGTH_CHECK:
                                                    tempVO_TableField.setTableFieldRule("文本长度");
                                                    break;
                                                case DATEFORMAT_CHECK:
                                                    tempVO_TableField.setTableFieldRule("日期格式");
                                                    break;
                                                case SEQUENCERANGE_CHECK:
                                                    tempVO_TableField.setTableFieldRule("序列范围");
                                                    break;
                                            }
                                        } else if (checkTypeEnum == CheckTypeEnum.UNIQUE_CHECK) {
                                            tempVO_TableField.setTableFieldRule("唯一");
                                        } else if (checkTypeEnum == CheckTypeEnum.NONEMPTY_CHECK) {
                                            tempVO_TableField.setTableFieldRule("非空");
                                        }
                                        tempVO_TableField.setTemplateType(templateType);
                                        tempVOS.add(tempVO_TableField);
                                    }
                                }
                            }
                        }
                        break;
                    case FIELD_AGGREGATE_TEMPLATE:
                        // 字段聚合波动阈值模板
                        dataCheckExtendFilter = dataCheckExtendPOS.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(dataCheckExtendFilter)) {
                            for (DataCheckExtendPO dataCheckExtendPO : dataCheckExtendFilter) {
                                String fieldName = "";
                                FiDataMetaDataTreeDTO fiDataMetaData_Field = fiDataMetaData_Fields.stream().
                                        filter(f -> f.getId().equals(dataCheckExtendPO.getFieldUnique())).findFirst().orElse(null);
                                if (fiDataMetaData_Field != null) {
                                    fieldName = fiDataMetaData_Field.getLabel();
                                }
                                if (StringUtils.isEmpty(fieldName)) {
                                    continue;
                                }
                                tempVO_TableField = new TableRuleTempVO();
                                tempVO_TableField.setRuleId(dataCheckPO.getId());
                                tempVO_TableField.setRuleName(dataCheckPO.getRuleName());
                                tempVO_TableField.setFieldUnique(dataCheckExtendPO.getFieldUnique());
                                tempVO_TableField.setType("FIELD");
                                tempVO_TableField.setFieldName(fieldName);
                                tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                                tempVO_TableField.setTableFieldRule(dataCheckExtendPO.getFieldAggregate());
                                tempVO_TableField.setTemplateType(templateType);
                                tempVOS.add(tempVO_TableField);
                            }
                        }
                        break;
                    case TABLECOUNT_TEMPLATE:
                        // 表行数波动阈值模板
                        tempVO_TableField = new TableRuleTempVO();
                        tempVO_TableField.setRuleId(dataCheckPO.getId());
                        tempVO_TableField.setRuleName(dataCheckPO.getRuleName());
                        tempVO_TableField.setType("TABLE");
                        tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                        tempVO_TableField.setTableFieldRule("验证表行数波动是否超过阈值");
                        tempVO_TableField.setTemplateType(templateType);
                        tempVOS.add(tempVO_TableField);
                        break;
                    case EMPTY_TABLE_CHECK_TEMPLATE:
                        // 空表校验模板
                        tempVO_TableField = new TableRuleTempVO();
                        tempVO_TableField.setRuleId(dataCheckPO.getId());
                        tempVO_TableField.setRuleName(dataCheckPO.getRuleName());
                        tempVO_TableField.setType("TABLE");
                        tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                        tempVO_TableField.setTableFieldRule("验证表是否为空");
                        tempVO_TableField.setTemplateType(templateType);
                        tempVOS.add(tempVO_TableField);
                        break;
                    case UPDATE_TABLE_CHECK_TEMPLATE:
                        // 表更新校验模板
                        tempVO_TableField = new TableRuleTempVO();
                        tempVO_TableField.setRuleId(dataCheckPO.getId());
                        tempVO_TableField.setRuleName(dataCheckPO.getRuleName());
                        tempVO_TableField.setType("TABLE");
                        tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                        tempVO_TableField.setTableFieldRule("验证表数据是否存在更新");
                        tempVO_TableField.setTemplateType(templateType);
                        tempVOS.add(tempVO_TableField);
                        break;
                    case TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE:
                        // 表血缘断裂校验模板
                        tempVO_TableField = new TableRuleTempVO();
                        tempVO_TableField.setRuleId(dataCheckPO.getId());
                        tempVO_TableField.setRuleName(dataCheckPO.getRuleName());
                        tempVO_TableField.setType("TABLE");
                        tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                        tempVO_TableField.setTableFieldRule("验证表血缘关系是否断裂");
                        tempVO_TableField.setTemplateType(templateType);
                        tempVOS.add(tempVO_TableField);
                        break;
                    case BUSINESS_CHECK_TEMPLATE:
                        // 业务验证模板
                        tempVO_TableField = new TableRuleTempVO();
                        tempVO_TableField.setRuleId(dataCheckPO.getId());
                        tempVO_TableField.setRuleName(dataCheckPO.getRuleName());
                        tempVO_TableField.setType("TABLE");
                        tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                        tempVO_TableField.setTableFieldRule("定时执行配置的SQL验证脚本");
                        tempVO_TableField.setTemplateType(templateType);
                        tempVOS.add(tempVO_TableField);
                        break;
                    case SIMILARITY_TEMPLATE:
                        // 相似度模板
                        break;
                    case DATA_MISSING_TEMPLATE:
                        // 数据缺失模板
                        dataCheckExtendFilter = dataCheckExtendPOS.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(dataCheckExtendFilter)) {
                            for (DataCheckExtendPO dataCheckExtendPO : dataCheckExtendFilter) {
                                String fieldName = "";
                                FiDataMetaDataTreeDTO fiDataMetaData_Field = fiDataMetaData_Fields.stream().
                                        filter(f -> f.getId().equals(dataCheckExtendPO.getFieldUnique())).findFirst().orElse(null);
                                if (fiDataMetaData_Field != null) {
                                    fieldName = fiDataMetaData_Field.getLabel();
                                }
                                if (StringUtils.isEmpty(fieldName)) {
                                    continue;
                                }
                                tempVO_TableField = new TableRuleTempVO();
                                tempVO_TableField.setRuleId(dataCheckPO.getId());
                                tempVO_TableField.setRuleName(dataCheckPO.getRuleName());
                                tempVO_TableField.setFieldUnique(dataCheckExtendPO.getFieldUnique());
                                tempVO_TableField.setType("FIELD");
                                tempVO_TableField.setFieldName(fieldName);
                                tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                                tempVO_TableField.setTemplateType(templateType);
                                tempVO_TableField.setTableFieldRule("数据缺失");
                                tempVOS.add(tempVO_TableField);
                            }
                        }
                        break;
                }
            }
        }

        // 数据校验字段校验规则需在表中体现
        if (CollectionUtils.isNotEmpty(tempVOS)) {
            List<TableRuleTempVO> tableFieldRulesTemp = tempVOS.stream().filter(t -> t.getType() == "FIELD").collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(tableFieldRulesTemp)) {
                List<TableRuleTempVO> collect = tableFieldRulesTemp.stream().filter(t -> t.getTemplateType() == TemplateTypeEnum.DATA_MISSING_TEMPLATE).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    tempVO_TableField = new TableRuleTempVO();
                    tempVO_TableField.setType("TABLE");
                    tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                    tempVO_TableField.setTemplateType(TemplateTypeEnum.DATA_MISSING_TEMPLATE);
                    List<String> fieldNames = collect.stream().map(TableRuleTempVO::getFieldName).distinct().collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(fieldNames)) {
                        String tableRule = Joiner.on(",").join(fieldNames) + "字段进行数据缺失校验";
                        tempVO_TableField.setTableFieldRule(tableRule);
                        tempVOS.add(tempVO_TableField);
                    }
                }
                collect = tableFieldRulesTemp.stream().filter(t -> t.getTemplateType() == TemplateTypeEnum.FIELD_RULE_TEMPLATE).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    tempVO_TableField = new TableRuleTempVO();
                    tempVO_TableField.setType("TABLE");
                    tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                    tempVO_TableField.setTemplateType(TemplateTypeEnum.FIELD_RULE_TEMPLATE);
                    List<String> list = collect.stream().map(TableRuleTempVO::getTableFieldRule).distinct().collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(list)) {
                        for (int i = 0; i < list.size(); i++) {
                            String value = list.get(i);
                            List<TableRuleTempVO> collect1 = collect.stream().filter(t -> t.getTableFieldRule() == value).collect(Collectors.toList());
                            if (CollectionUtils.isNotEmpty(collect1)) {
                                List<String> fieldNames = collect1.stream().map(TableRuleTempVO::getFieldName).collect(Collectors.toList());
                                if (CollectionUtils.isNotEmpty(fieldNames)) {
                                    String tableRule = Joiner.on(",").join(fieldNames) + "字段进行" + value + "校验";
                                    tempVO_TableField.setTableFieldRule(tableRule);
                                    tempVOS.add(tempVO_TableField);
                                }
                            }
                        }
                    }
                }
                collect = tableFieldRulesTemp.stream().filter(t -> t.getTemplateType() == TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    tempVO_TableField = new TableRuleTempVO();
                    tempVO_TableField.setType("TABLE");
                    tempVO_TableField.setModuleType(ModuleTypeEnum.DATACHECK_MODULE);
                    tempVO_TableField.setTemplateType(TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE);
                    List<String> fieldNames = collect.stream().map(TableRuleTempVO::getFieldName).distinct().collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(fieldNames)) {
                        String tableRule = Joiner.on(",").join(fieldNames) + "字段进行聚合后校验波动阈值";
                        tempVO_TableField.setTableFieldRule(tableRule);
                        tempVOS.add(tempVO_TableField);
                    }
                }
            }
        }

        // 循环业务清洗规则
//        for (BusinessFilterPO businessFilterPO : businessFilterPOS) {
//
//        }

        // 循环生命周期规则
        for (LifecyclePO lifecyclePO : lifecyclePOS) {
            // 查询该生命周期规则对应的模板
            templatePO = templatePOS.stream().filter(t -> t.getId() == lifecyclePO.getTemplateId()).findFirst().orElse(null);
            if (templatePO != null) {
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                switch (templateType) {
                    case SPECIFY_TIME_RECYCLING_TEMPLATE:
                        break;
                    case EMPTY_TABLE_RECOVERY_TEMPLATE:
                        break;
                    case NO_REFRESH_DATA_RECOVERY_TEMPLATE:
                        break;
                    case DATA_BLOOD_KINSHIP_RECOVERY_TEMPLATE:
                        break;
                }
            }
        }

        TableRuleInfoDTO tableRuleInfoDTO = new TableRuleInfoDTO();
        tableRuleInfoDTO.setTableFieldUnique(tableUnique);
        tableRuleInfoDTO.setName(tableName);
        tableRuleInfoDTO.setType(1);
        List<TableRuleInfoDTO> fieldRules = new ArrayList<>();

        // 拼接数据校验规则--表维度
        List<TableRuleTempVO> dataCheckTemp_TableRules = tempVOS.stream().
                filter(t -> t.getModuleType() == ModuleTypeEnum.DATACHECK_MODULE && t.getType().equals("TABLE")).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dataCheckTemp_TableRules)) {
            // 表的校验规则
            dataCheckTemp_TableRules.forEach(t -> {
                if (!tableRuleInfoDTO.getCheckRules().contains(t.getTableFieldRule())) {
                    tableRuleInfoDTO.checkRules.add(t.getTableFieldRule());
                }
            });
        }

        // 拼接业务清洗规则--表维度
        List<TableRuleTempVO> businessFilterTemp_TableRules = tempVOS.stream().
                filter(t -> t.getModuleType() == ModuleTypeEnum.BIZCHECK_MODULE && t.getType().equals("TABLE")).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(businessFilterTemp_TableRules)) {
            // 表的校验规则
            businessFilterTemp_TableRules.forEach(t -> {
                if (!tableRuleInfoDTO.getFilterRules().contains(t.getTableFieldRule())) {
                    tableRuleInfoDTO.filterRules.add(t.getTableFieldRule());
                }
            });
        }

        // 拼接生命周期规则--表维度
        List<TableRuleTempVO> lifecycleTemp_TableRules = tempVOS.stream().
                filter(t -> t.getModuleType() == ModuleTypeEnum.LIFECYCLE_MODULE && t.getType().equals("TABLE")).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(lifecycleTemp_TableRules)) {
            // 表的校验规则
            lifecycleTemp_TableRules.forEach(t -> {
                if (!tableRuleInfoDTO.getLifecycleRules().contains(t.getTableFieldRule())) {
                    tableRuleInfoDTO.lifecycleRules.add(t.getTableFieldRule());
                }
            });
        }

        // 拼接告警通知规则--表维度
//        if (CollectionUtils.isNotEmpty(noticePOS)) {
//            // 表的校验规则
//            for (NoticePO noticePO : noticePOS) {
//                NoticeTypeEnum noticeType = NoticeTypeEnum.getEnum(noticePO.getNoticeType());
//                if (!tableRuleInfoDTO.noticeRules.contains(noticeType.getName())) {
//                    tableRuleInfoDTO.noticeRules.add(noticeType.getName());
//                }
//            }
//        }

        // 循环字段，查询每个字段的数据校验、业务清洗、生命周期规则
        List<TableRuleTempVO> dataCheckTemp_FieldRules = tempVOS.stream().filter(t -> t.getType().equals("FIELD")).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dataCheckTemp_FieldRules)) {
            List<String> fieldNames = dataCheckTemp_FieldRules.stream().map(TableRuleTempVO::getFieldName).distinct().collect(Collectors.toList());
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                List<TableRuleTempVO> fieldRuleList = dataCheckTemp_FieldRules.stream().filter(t -> t.getFieldName().equals(fieldName)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(fieldRuleList)) {

                    TableRuleInfoDTO fieldRuleInfoDTO = new TableRuleInfoDTO();
                    fieldRuleInfoDTO.setTableFieldUnique(fieldRuleList.get(0).getFieldUnique());
                    fieldRuleInfoDTO.setName(fieldName);
                    fieldRuleInfoDTO.setType(2);

                    // 数据校验规则--字段维度
                    List<TableRuleTempVO> dataCheck_FieldRules = fieldRuleList.stream().filter(t -> t.getModuleType() == ModuleTypeEnum.DATACHECK_MODULE).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(dataCheck_FieldRules)) {
                        List<String> ruleList = new ArrayList<>();
                        dataCheck_FieldRules.forEach(t -> {
                            switch (t.getTemplateType()) {
                                case FIELD_RULE_TEMPLATE:
                                    ruleList.add(t.getTableFieldRule() + "校验");
                                    break;
                                case FIELD_AGGREGATE_TEMPLATE:
                                    ruleList.add("校验字段" + t.getTableFieldRule() + "聚合后是否超过阈值");
                                    break;
                                case DATA_MISSING_TEMPLATE:
                                    ruleList.add("数据缺失校验");
                                    break;
                            }
                        });
                        fieldRuleInfoDTO.setCheckRules(ruleList);
                    }
                    // 业务清洗规则--字段维度,暂不支持设置到字段维度
                    List<TableRuleTempVO> businessFilter_FieldRules = fieldRuleList.stream().filter(t -> t.getModuleType() == ModuleTypeEnum.BIZCHECK_MODULE).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(businessFilter_FieldRules)) {
                    }
                    // 生命周期规则--字段维度,暂不支持设置到字段维度
                    List<TableRuleTempVO> lifecycle_FieldRules = fieldRuleList.stream().filter(t -> t.getModuleType() == ModuleTypeEnum.BIZCHECK_MODULE).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(lifecycle_FieldRules)) {
                    }
                    fieldRules.add(fieldRuleInfoDTO);
                }
            }
        }
        tableRuleInfoDTO.setFieldRules(fieldRules);
        */
//        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, tableRuleInfoDTO);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
    }

    @Override
    public ResultEntity<List<DataSourceConVO>> getAllDataSource() {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataSourceConManageImpl.getAllDataSource());
    }

    @Override
    public ResultEntity<Object> createQualityReport(int id) {
        log.info("【创建质量报告-开始】质量报告开始执行");
        try {
            if (id == 0) {
                return ResultEntityBuild.buildData(ResultEnum.PARAMTER_ERROR, "");
            }
            // 第一步：查询质量报告基础信息
            QualityReportPO qualityReportPO = qualityReportMapper.selectById(id);
            if (qualityReportPO == null || qualityReportPO.getReportState() == RuleStateEnum.Disable.getValue()) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_NOTICE_NOTEXISTS, "");
            }
            // 质量报告主键id
            int reportId = Math.toIntExact(qualityReportPO.getId());

            // 第二步：查询质量报告下的规则配置
            QueryWrapper<QualityReportRulePO> qualityReportRulePOQueryWrapper = new QueryWrapper<>();
            qualityReportRulePOQueryWrapper.lambda()
                    .eq(QualityReportRulePO::getReportId, reportId)
                    .eq(QualityReportRulePO::getDelFlag, 1);
            List<QualityReportRulePO> noticeExtendPOS = qualityReportRuleMapper.selectList(qualityReportRulePOQueryWrapper).stream().sorted(Comparator.comparing(QualityReportRulePO::getRuleSort)).collect(Collectors.toList());
            if (!CollectionUtils.isNotEmpty(noticeExtendPOS)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_NOTICE_NOTEXISTS, "");
            }

            // 第三步：查询质量报告下的通知方式
            QualityReportNoticeDTO qualityReportNoticeDTO = new QualityReportNoticeDTO();
            QueryWrapper<QualityReportNoticePO> qualityReportNoticePOQueryWrapper = new QueryWrapper<>();
            qualityReportNoticePOQueryWrapper.lambda().eq(QualityReportNoticePO::getDelFlag, 1)
                    .eq(QualityReportNoticePO::getReportId, reportId);
            QualityReportNoticePO qualityReportNoticePO = qualityReportNoticeMapper.selectOne(qualityReportNoticePOQueryWrapper);
            if (qualityReportNoticePO == null) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_NOTICE_CONFIG_ISNULL, "");
            }
            qualityReportNoticeDTO.setId(Math.toIntExact(qualityReportNoticePO.getId()));
            qualityReportNoticeDTO.setReportId(qualityReportNoticePO.getReportId());
            qualityReportNoticeDTO.setReportNoticeType(qualityReportNoticePO.getReportNoticeType());
            qualityReportNoticeDTO.setBody(qualityReportNoticePO.getBody());
            qualityReportNoticeDTO.setSubject(qualityReportNoticePO.getSubject());
            qualityReportNoticeDTO.setEmailServerId(qualityReportNoticePO.getEmailServerId());

            // 第四步：查询质量报告下的接收人
            List<QualityReportRecipientDTO> qualityReportRecipientDTOs = new ArrayList<>();
            QueryWrapper<QualityReportRecipientPO> qualityReportRecipientPOQueryWrapper = new QueryWrapper<>();
            qualityReportRecipientPOQueryWrapper.lambda().eq(QualityReportRecipientPO::getDelFlag, 1)
                    .eq(QualityReportRecipientPO::getReportId, reportId);
            List<QualityReportRecipientPO> qualityReportRecipientPOs = qualityReportRecipientMapper.selectList(qualityReportRecipientPOQueryWrapper);
            if (!CollectionUtils.isNotEmpty(qualityReportRecipientPOs)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_NOTICE_RECIPIENT_ISNULL, "");
            }
            qualityReportRecipientPOs.forEach(t -> {
                QualityReportRecipientDTO qualityReportRecipientDTO = new QualityReportRecipientDTO();
                qualityReportRecipientDTO.setId(Math.toIntExact(t.id));
                qualityReportRecipientDTO.setReportId(t.getReportId());
                qualityReportRecipientDTO.setUserId(t.getUserId());
                qualityReportRecipientDTO.setUserType(t.getUserType());
                qualityReportRecipientDTO.setUserName(t.getUserName());
                qualityReportRecipientDTO.setRecipient(t.getRecipient());
                qualityReportRecipientDTOs.add(qualityReportRecipientDTO);
            });
            qualityReportNoticeDTO.setQualityReportRecipient(qualityReportRecipientDTOs);
            String toAddressStr = "";
            List<String> toAddressList = qualityReportRecipientDTOs.stream().map(QualityReportRecipientDTO::getRecipient).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(toAddressList)) {
                toAddressStr = Joiner.on(";").join(toAddressList);
            }

            // 第五步：查询数据源信息
            List<DataSourceConVO> allDataSource = dataSourceConManageImpl.getAllDataSource();
            if (!CollectionUtils.isNotEmpty(allDataSource)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS, "");
            }

            // 第六步：判断质量报告属于哪个业务模块 质量检查报告/数据清洗报告
            AttachmentInfoPO attachmentInfoPO = new AttachmentInfoPO();
            String currentFileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
            attachmentInfoPO.setCurrentFileName(currentFileName);
            attachmentInfoPO.setExtensionName(".xlsx");
            attachmentInfoPO.setAbsolutePath(uploadUrl);
            attachmentInfoPO.setRelativePath("");
            ResultEnum resultEnum = ResultEnum.SUCCESS;
            switch (qualityReportPO.getReportType()) {
                case 100:
                    // 生成数据校验质量报告
                    attachmentInfoPO.setOriginalName(String.format("数据检查质量报告%s.xlsx", DateTimeUtils.getNowToShortDate().replace("-", "")));
                    attachmentInfoPO.setCategory(100);
                    resultEnum = dataCheck_QualityReport_CreateExcel(noticeExtendPOS, allDataSource, attachmentInfoPO);
                    break;
                case 200:
                    // 生成业务清洗质量报告
                    attachmentInfoPO.setOriginalName(String.format("业务清洗质量报告%s.xlsx", DateTimeUtils.getNowToShortDate().replace("-", "")));
                    attachmentInfoPO.setCategory(200);
                    break;
            }
            if (qualityReportPO.getReportType() != 100 || qualityReportNoticePO.getReportNoticeType() != 1) {
                log.info("质量报告类型暂不支持非质量校验报告的其他方式");
                log.info("质量报告通知方式暂不支持非邮件通知的其他方式");
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_CURRENTLY_ONLY_NON_INSPECTION_QUALITY_REPORTS_ARE_SUPPORTED, "");
            }

            // 第七步：发送邮件
            ResultEntity<Object> sendResult = null;
            QualityReportDTO qualityReportDTO = new QualityReportDTO();
            qualityReportDTO.sendAttachment = true;
            qualityReportDTO.setAttachmentName(attachmentInfoPO.getCurrentFileName());
            qualityReportDTO.setAttachmentPath(attachmentInfoPO.getAbsolutePath());
            qualityReportDTO.setAttachmentActualName(attachmentInfoPO.getOriginalName());
            qualityReportDTO.setCompanyLogoPath("");
            qualityReportDTO.setQualityReportNotice(qualityReportNoticeDTO);
            sendResult = qualityReportManage.sendEmailReport(qualityReportDTO);

            // 第八步：生成质量报告发送日志
            QualityReportLogPO qualityReportLogPO = new QualityReportLogPO();
            qualityReportLogPO.setReportId(Math.toIntExact(qualityReportPO.getId()));
            qualityReportLogPO.setReportName(qualityReportPO.getReportName());
            qualityReportLogPO.setReportType(qualityReportPO.getReportType());
            qualityReportLogPO.setReportTypeName(qualityReportPO.getReportTypeName());
            qualityReportLogPO.setReportDesc(qualityReportPO.getReportDesc());
            qualityReportLogPO.setReportPrincipal(qualityReportPO.getReportPrincipal());
            qualityReportLogPO.setReportNoticeType(qualityReportNoticeDTO.getReportNoticeType());
            qualityReportLogPO.setEmailServerId(qualityReportNoticeDTO.getEmailServerId());
            qualityReportLogPO.setSubject(qualityReportNoticeDTO.getSubject());
            qualityReportLogPO.setRecipient(toAddressStr);
            qualityReportLogPO.setBody(qualityReportNoticeDTO.getBody());
            qualityReportLogPO.setSendTime(DateTimeUtils.getNow());
            if (sendResult != null && sendResult.getCode() == ResultEnum.SUCCESS.getCode()) {
                qualityReportLogPO.setSendResult("已发送");
            } else {
                qualityReportLogPO.setSendResult("发送失败");
            }
            qualityReportLogMapper.insertOne(qualityReportLogPO);

            // 第九步：生成质量报告附件
            attachmentInfoPO.setObjectId(String.valueOf(qualityReportLogPO.getId()));
            attachmentInfoMapper.insert(attachmentInfoPO);
        } catch (Exception ex) {
            log.error("【createQualityReport】创建质量报告异常：" + ex);
            throw new FkException(ResultEnum.ERROR, "【createQualityReport】 ex：" + ex);
        }
        log.info("【createQualityReport】创建质量报告-结束");
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "");
    }

    public ResultEnum dataCheck_QualityReport_CreateExcel(List<QualityReportRulePO> qualityReportRulePOS, List<DataSourceConVO> allDataSource
            , AttachmentInfoPO attachmentInfoPO) {
        // 第一步：查询待执行的检查规则
        List<Integer> ruleIds = qualityReportRulePOS.stream().map(QualityReportRulePO::getRuleId).collect(Collectors.toList());
        QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
        dataCheckPOQueryWrapper.lambda()
                .eq(DataCheckPO::getDelFlag, 1)
                .eq(DataCheckPO::getRuleState, RuleStateEnum.Enable.getValue())
                .in(DataCheckPO::getId, ruleIds)
                .orderByAsc(DataCheckPO::getRuleExecuteSort);
        List<DataCheckPO> dataCheckPOList = dataCheckMapper.selectList(dataCheckPOQueryWrapper);
        if (!CollectionUtils.isNotEmpty(dataCheckPOList)) {
            return ResultEnum.DATA_QUALITY_RULE_NOTEXISTS;
        }

        // 第二步：查询检查规则的扩展属性
        QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
        dataCheckExtendPOQueryWrapper.lambda()
                .eq(DataCheckExtendPO::getDelFlag, 1)
                .in(DataCheckExtendPO::getRuleId, ruleIds);
        List<DataCheckExtendPO> dataCheckExtendPOList = dataCheckExtendMapper.selectList(dataCheckExtendPOQueryWrapper);

        // 第三步：填充Excel文档Dto
        ExcelDto excelDto = new ExcelDto();
        excelDto.setExcelName(attachmentInfoPO.getCurrentFileName());
        List<SheetDto> sheets = new ArrayList<>();
//        for (int i = 0; i < dataCheckPOList.size(); i++) {
//            DataCheckPO dataCheckPO = dataCheckPOList.get(i);
//            DataCheckExtendPO dataCheckExtendPO = dataCheckExtendPOList.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).findFirst().orElse(null);
//            if (dataCheckExtendPO == null) {
//                continue;
//            }
//            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
//            if (templatePO == null) {
//                continue;
//            }
//            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getId() == dataCheckPO.getDatasourceId()).findFirst().orElse(null);
//            if (dataSourceConVO == null) {
//                continue;
//            }
//
//            // 获取表和字段信息，将其进行转义处理
//            DataCheckSyncParamDTO dataCheckSyncParamDTO = new DataCheckSyncParamDTO();
//            String tableName = "";
//            String tableNameFormat = "";
//            if (StringUtils.isNotEmpty(dataCheckPO.getSchemaName())) {
//                tableNameFormat = QualityReport_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckPO.getSchemaName()) + ".";
//                tableName = dataSourceConVO.getConType() + ".";
//            }
//            tableNameFormat += QualityReport_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckPO.getTableName());
//            tableName += dataCheckPO.getTableName();
//
//            String fieldName = "";
//            String fieldNameFormat = "";
//            if (StringUtils.isNotEmpty(dataCheckExtendPO.getFieldName())) {
//                fieldNameFormat = QualityReport_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckExtendPO.getFieldName());
//                fieldName = dataCheckExtendPO.getFieldName();
//            }
//            dataCheckSyncParamDTO.setTableName(tableName);
//            dataCheckSyncParamDTO.setTableNameFormat(tableNameFormat);
//            dataCheckSyncParamDTO.setFieldName(fieldName);
//            dataCheckSyncParamDTO.setFieldNameFormat(fieldNameFormat);
//
//            SheetDataDto sheetDataDto = null;
//            try {
//                sheetDataDto = dataCheck_QualityReport_Create(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
//            } catch (Exception ex) {
//                log.error("质量报告拼接SQL执行异常：" + ex);
//                continue;
//            }
//            if (sheetDataDto == null) {
//                log.info(dataCheckPO.getRuleName() + "，质量报告SQL检查结果为空，跳过。");
//                continue;
//            }
//            SheetDto sheet = new SheetDto();
//            sheet.setSheetName(dataCheckPO.getRuleName());
//            SheetDataDto sheetDataDto = resultSetToMap(dataSourceConVO, roleSql);
//            List<RowDto> singRows = dataCheck_QualityReport_GetSingRows(tableName, templatePO.getTemplateName(), sheetDataDto.getColumns());
//            sheet.setSingRows(singRows);
//            sheet.setSingFields(fields.keySet().stream().collect(Collectors.toList()));
//            sheet.setDataRows(sheetDataDto.columnData);
//            sheets.add(sheet);
//        }
        if (CollectionUtils.isNotEmpty(sheets)) {
            excelDto.setSheets(sheets);
            ExcelReportUtil.createExcel(excelDto, attachmentInfoPO.absolutePath, attachmentInfoPO.currentFileName, true);
        }
        return ResultEnum.SUCCESS;
    }

    public SheetDataDto dataCheck_QualityReport_Create(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                       DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        SheetDataDto sheetDataDto = null;
        switch (templateTypeEnum) {
            case NULL_CHECK:
                sheetDataDto = dataCheck_QualityReport_NullCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            case RANGE_CHECK:
                sheetDataDto = dataCheck_QualityReport_RangeCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            case STANDARD_CHECK:
                sheetDataDto = dataCheck_QualityReport_StandardCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            case DUPLICATE_DATA_CHECK:
                sheetDataDto = dataCheck_QualityReport_DuplicateDateCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            case FLUCTUATION_CHECK:
                sheetDataDto = dataCheck_QualityReport_FluctuationCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            case PARENTAGE_CHECK:
                sheetDataDto = dataCheck_QualityReport_ParentageCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            case REGEX_CHECK:
                sheetDataDto = dataCheck_QualityReport_RegexCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            case SQL_SCRIPT_CHECK:
                sheetDataDto = dataCheck_QualityReport_SqlScriptCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
        }
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_NullCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                       DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto=new SheetDataDto();
        boolean charValid = RegexUtils.isCharValid(dataCheckExtendPO.getFieldType());
        String sql_QueryCheckData = "";
        if (charValid) {
            sql_QueryCheckData = String.format("SELECT * FROM %s WHERE %s IS NULL OR %s = '' OR %s = 'null'; ",
                    dataCheckPO.getTableName(), dataCheckSyncParamDTO.getFieldName(), dataCheckSyncParamDTO.getFieldName(), dataCheckSyncParamDTO.getFieldName());
        } else {
            sql_QueryCheckData = String.format("SELECT * FROM %s WHERE %s IS NULL; ", dataCheckPO.getTableName(), dataCheckSyncParamDTO.getFieldName());
        }
        JSONArray jsonArray = QualityReport_QueryTableData_Array(dataSourceConVO, sql_QueryCheckData);
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_RangeCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                        DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto=new SheetDataDto();
        String sql_QueryCheckData = "";
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();

        DataSourceTypeEnum dataSourceTypeEnum = dataSourceConVO.getConType();
        RangeCheckTypeEnum rangeCheckTypeEnum = RangeCheckTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckType());
        switch (rangeCheckTypeEnum) {
            case SEQUENCE_RANGE:
                // 序列范围
                List<String> list = Arrays.asList(dataCheckExtendPO.getRangeCheckValue().split(","));
                String sql_InString = list.stream().map(s -> "'" + "'").collect(Collectors.joining(", "));
                sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE %s NOT IN (%s)", f_Name, t_Name, f_Name, sql_InString);
                break;
            case VALUE_RANGE:
                // 取值范围
                Integer lowerBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[0]);
                Integer upperBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[1]);
                String sql_BetweenAnd = String.format("CASE(%s AS NUMERIC) NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                    sql_BetweenAnd = String.format("%s::NUMERIC NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                }
                sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE %s", f_Name, t_Name, sql_BetweenAnd);
                break;
            case DATE_RANGE:
                // 日期范围
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String timeRangeString = dataCheckExtendPO.getRangeCheckValue();
                String[] timeRange = timeRangeString.split("~");
                LocalDateTime startTime = LocalDateTime.parse(timeRange[0], formatter);
                LocalDateTime endTime = LocalDateTime.parse(timeRange[1], formatter);
                sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE ((%s IS NULL OR %s = '') OR (%s NOT BETWEEN '%s' AND %s))",
                        f_Name, t_Name, f_Name, f_Name, f_Name, startTime, endTime);
                break;
        }
        JSONArray jsonArray = QualityReport_QueryTableData_Array(dataSourceConVO, sql_QueryCheckData);
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_StandardCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                           DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto=new SheetDataDto();
        JSONArray errorDataList = new JSONArray();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();
        String sql_QueryCheckData = String.format("SELECT %s FROM %s", f_Name, t_Name);
        JSONArray jsonArray = QualityReport_QueryTableData_Array(dataSourceConVO, sql_QueryCheckData);
        StandardCheckTypeEnum standardCheckTypeEnum = StandardCheckTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckType());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Object fieldValue = jsonObject.get(fName);
            switch (standardCheckTypeEnum) {
                case DATE_FORMAT:
                    // 日期格式
                    List<String> list = Arrays.asList(dataCheckExtendPO.getStandardCheckTypeDateValue().split(","));
                    if (fieldValue == null || fieldValue.toString().equals("")) {
                        errorDataList.add(jsonObject);
                    } else {
                        boolean hasValidDate = DateTimeUtils.isValidDateFormat(fieldValue.toString(), list);
                        if (!hasValidDate) {
                            errorDataList.add(jsonObject);
                        }
                    }
                    break;
                case URL_ADDRESS:
                    // 字符精度长度范围
                    int minFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[0]);
                    int maxFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[1]);
                    if (fieldValue == null || fieldValue.toString().equals("")) {
                        errorDataList.add(jsonObject);
                    } else {
                        List<String> values = Arrays.asList(fieldValue.toString().split(dataCheckExtendPO.getStandardCheckTypeLengthSeparator()));
                        if (values.stream().count() >= 2) {
                            String value = values.get(Math.toIntExact(values.stream().count() - 1));
                            if (value.length() < minFieldLength || value.length() > maxFieldLength) {
                                errorDataList.add(jsonObject);
                            }
                        }
                    }
                    break;
                case BASE64_BYTE_STREAM:
                    // URL地址
                    if (fieldValue == null || fieldValue.toString().equals("")) {
                        errorDataList.add(jsonObject);
                    } else {
                        boolean validURL = RegexUtils.isValidURL(fieldValue.toString(), false);
                        if (!validURL) {
                            errorDataList.add(jsonObject);
                        }
                    }
                    break;
                case CHARACTER_PRECISION_LENGTH_RANGE:
                    // BASE64字节流
                    if (fieldValue == null || fieldValue.toString().equals("")) {
                        errorDataList.add(jsonObject);
                    } else {
                        boolean validBase64String = RegexUtils.isBase64String(fieldValue.toString(), false);
                        if (!validBase64String) {
                            errorDataList.add(jsonObject);
                        }
                    }
                    break;
            }
        }
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_DuplicateDateCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto=new SheetDataDto();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = "",
                fName = dataCheckSyncParamDTO.getFieldName();
        String sql_QueryCheckData = String.format("SELECT %s, COUNT(*) AS repetitionCount FROM %s WHERE 1=1 %s \n" +
                "GROUP BY %s HAVING COUNT(*) > 1;", f_Name, t_Name, f_Name);
        JSONArray jsonArray = QualityReport_QueryTableData_Array(dataSourceConVO, sql_QueryCheckData);
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_FluctuationCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                              DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto=new SheetDataDto();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();
        String sql_QueryCheckData = "";
        boolean isValid = true;
        double thresholdValue = dataCheckExtendPO.getFluctuateCheckValue();
        double realityValue = 0.0;

        FluctuateCheckTypeEnum fluctuateCheckTypeEnum = FluctuateCheckTypeEnum.getEnum(dataCheckExtendPO.getFluctuateCheckType());
        switch (fluctuateCheckTypeEnum) {
            case AVG:
                sql_QueryCheckData = String.format("SELECT AVG(%s) AS realityValue FROM %s", f_Name, t_Name);
                break;
            case MIN:
                sql_QueryCheckData = String.format("SELECT MIN(%s) AS realityValue FROM %s", f_Name, t_Name);
                break;
            case MAX:
                sql_QueryCheckData = String.format("SELECT MAX(%s) AS realityValue FROM %s", f_Name, t_Name);
                break;
            case SUM:
                sql_QueryCheckData = String.format("SELECT SUM(%s) AS realityValue FROM %s", f_Name, t_Name);
                break;
            case COUNT:
                sql_QueryCheckData = String.format("SELECT COUNT(%s) AS realityValue FROM %s", f_Name, t_Name);
                break;
        }
        List<Map<String, Object>> maps = QualityReport_QueryTableData_Maps(dataSourceConVO, sql_QueryCheckData);
        if (CollectionUtils.isNotEmpty(maps)) {
            realityValue = Double.parseDouble(maps.get(0).get("realityValue").toString());
        }

        FluctuateCheckOperatorEnum fluctuateCheckOperatorEnum = FluctuateCheckOperatorEnum.getEnumByName(dataCheckExtendPO.getFluctuateCheckOperator());
        switch (fluctuateCheckOperatorEnum) {
            case GREATER_THAN:
                if (realityValue > thresholdValue) {
                    isValid = false;
                }
                break;
            case GREATER_THAN_OR_EQUAL:
                if (realityValue >= thresholdValue) {
                    isValid = false;
                }
                break;
            case EQUAL:
                if (realityValue == thresholdValue) {
                    isValid = false;
                }
                break;
            case LESS_THAN:
                if (realityValue < thresholdValue) {
                    isValid = false;
                }
                break;
            case LESS_THAN_OR_EQUAL:
                if (realityValue <= thresholdValue) {
                    isValid = false;
                }
                break;
        }
        JSONArray jsonArray = new JSONArray();
        if (!isValid) {
            JSONObject jsonObject = new JSONObject();
            // 字段名称
            jsonObject.put("FieldName", fName);
            // 聚合值
            jsonObject.put("AggregateValue", realityValue);
            // 波动阈值
            jsonObject.put("ThresholdValue", thresholdValue);
            jsonArray.add(jsonObject);
        }
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_ParentageCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                            DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto=new SheetDataDto();
        JSONArray jsonArray = new JSONArray();
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_RegexCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                        DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto=new SheetDataDto();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();
        String sql_QueryCheckData = String.format("SELECT %s FROM %s", f_Name, t_Name);

        JSONArray errorDataList = new JSONArray();
        JSONArray data = QualityReport_QueryTableData_Array(dataSourceConVO, sql_QueryCheckData);
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                // 判断字段值是否通过正则表达式验证
                String fieldValue = jsonObject.getString(fName);
                boolean isValid = RegexUtils.isValidPattern(fieldValue, dataCheckExtendPO.getRegexpCheckValue(), false);
                if (!isValid) {
                    errorDataList.add(jsonObject);
                }
            }
        }
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_SqlScriptCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                            DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto=new SheetDataDto();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();
        JSONArray jsonArray = QualityReport_QueryTableData_Array(dataSourceConVO, dataCheckExtendPO.getSqlCheckValue());

        // 固定返回checkstate，通过为1，未通过为0，取第一行的checkstate字段判断
        boolean isValid = false;
        String checkState = "0";
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if (jsonObject.containsKey("checkstate")) {
                checkState = jsonObject.getString("checkstate");
                if (StringUtils.isNotEmpty(checkState) && checkState.equals("1")) {
                    isValid = true;
                }
            }
        }

        if (!isValid) {
            return sheetDataDto;
        }
        return null;
    }

    public List<RowDto> dataCheck_QualityReport_GetSingRows(String tableName, String templatenName, List<String> fields) {
        List<RowDto> singRows = new ArrayList<>();
        RowDto rowDto = new RowDto();
        rowDto.setRowIndex(0);
        List<String> Columns = new ArrayList<>();
        Columns.add("表名称");
        Columns.add("模板名称");
        rowDto.setColumns(Columns);
        singRows.add(rowDto);

        rowDto = new RowDto();
        rowDto.setRowIndex(1);
        Columns = new ArrayList<>();
        Columns.add(tableName);
        Columns.add(templatenName);
        rowDto.setColumns(Columns);
        singRows.add(rowDto);

        rowDto = new RowDto();
        rowDto.setRowIndex(3);
        Columns = new ArrayList<>();
        Columns.add("质量报告明细");
        rowDto.setColumns(Columns);
        singRows.add(rowDto);

        rowDto = new RowDto();
        rowDto.setRowIndex(4);
        Columns = new ArrayList<>();
        Columns.addAll(fields);
        rowDto.setColumns(Columns);
        singRows.add(rowDto);
        return singRows;
    }

    public String QualityReport_GetSqlFieldFormat(DataSourceTypeEnum dataSourceTypeEnum, String fieldName) {
        String sqlFieldStr = dataSourceTypeEnum == DataSourceTypeEnum.MYSQL
                ? "`%s`" :
                dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER
                        ? "[%s]" :
                        dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL
                                ? "\"%s\"" :
                                "";
        if (StringUtils.isNotEmpty(sqlFieldStr)) {
            sqlFieldStr = String.format(sqlFieldStr, fieldName);
        }
        return sqlFieldStr;
    }

    public List<Map<String, Object>> QualityReport_QueryTableData_Maps(DataSourceConVO dataSourceConVO, String sql) {
        // 实时建立数据库连接实时释放，防止连接等待时间过长导致超时异常
        log.info("【nifiSync_UpdateTableData】待执行SQL：" + sql);
        Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        List<Map<String, Object>> mapList = AbstractCommonDbHelper.execQueryResultMaps(sql, connection);
        return mapList;
    }

    public JSONArray QualityReport_QueryTableData_Array(DataSourceConVO dataSourceConVO, String sql) {
        // 实时建立数据库连接实时释放，防止连接等待时间过长导致超时异常
        log.info("【dataCheck_QualityReport_QueryTableData】待执行SQL：" + sql);
        Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        JSONArray dataArray = AbstractCommonDbHelper.execQueryResultArrays(sql, connection);
        return dataArray;
    }
}

