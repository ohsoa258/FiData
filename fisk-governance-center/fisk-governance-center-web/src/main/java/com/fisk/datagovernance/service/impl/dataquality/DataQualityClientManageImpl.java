package com.fisk.datagovernance.service.impl.dataquality;

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
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.TableRuleSqlDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeDTO;
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
    private NoticeMapper noticeMapper;

    @Resource
    private NoticeManageImpl noticeManageImpl;

    @Resource
    private NoticeExtendMapper noticeExtendMapper;

    @Resource
    private AttachmentInfoMapper attachmentInfoMapper;

    @Value("${file.uploadUrl}")
    private String uploadUrl;
    //@Value("${file.echoPath}")
    private String echoPath;
    //@Value("${file.logoPath}")
    private String logoPaht;

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
        QueryWrapper<BusinessFilterPO> businessFilterPOQueryWrapper = new QueryWrapper<>();
        businessFilterPOQueryWrapper.lambda().eq(BusinessFilterPO::getDelFlag, 1)
                .eq(BusinessFilterPO::getDatasourceId, dataSourceId)
                .eq(BusinessFilterPO::getTableUnique, tableUnique)
                .eq(BusinessFilterPO::getTableBusinessType, tableBusinessType)
                .eq(BusinessFilterPO::getRuleState, 1);
        List<BusinessFilterPO> businessFilterPOS = businessFilterMapper.selectList(businessFilterPOQueryWrapper);
        if (CollectionUtils.isNotEmpty(businessFilterPOS)) {
            List<Long> ruleIds = businessFilterPOS.stream().map(BusinessFilterPO::getId).collect(Collectors.toList());
            ruleIdList.addAll(ruleIds);
        }

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
                List<Integer> noticeIds = noticeExtendPOS.stream().map(NoticeExtendPO::getNoticeId).distinct().collect(Collectors.toList());
                QueryWrapper<NoticePO> noticePOQueryWrapper = new QueryWrapper<>();
                noticePOQueryWrapper.lambda().eq(NoticePO::getDelFlag, 1)
                        .eq(NoticePO::getNoticeState, 1)
                        .in(NoticePO::getId, noticeIds);
                noticePOS = noticeMapper.selectList(noticePOQueryWrapper);
            }
        }

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
        for (BusinessFilterPO businessFilterPO : businessFilterPOS) {
            // 查询该清洗规则对应的模板
            templatePO = templatePOS.stream().filter(t -> t.getId() == businessFilterPO.getTemplateId()).findFirst().orElse(null);
            if (templatePO != null) {
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                switch (templateType) {
                    case API_FILTER_TEMPLATE:
                        break;
                    case SYNC_FILTER_TEMPLATE:
                        break;
                    case FILTER_REPORT_TEMPLATE:
                        break;
                }
            }
        }

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
        if (CollectionUtils.isNotEmpty(noticePOS)) {
            // 表的校验规则
            for (NoticePO noticePO : noticePOS) {
                NoticeTypeEnum noticeType = NoticeTypeEnum.getEnum(noticePO.getNoticeType());
                if (!tableRuleInfoDTO.noticeRules.contains(noticeType.getName())) {
                    tableRuleInfoDTO.noticeRules.add(noticeType.getName());
                }
            }
        }

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
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, tableRuleInfoDTO);
    }

    @Override
    public ResultEntity<List<DataSourceConVO>> getAllDataSource() {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataSourceConManageImpl.getAllDataSource());
    }

    @Override
    public ResultEntity<Object> createQualityReport(int id) {
        log.info("质量报告开始执行");
        if (id == 0) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_ERROR, "");
        }
        NoticePO noticePO = noticeMapper.selectById(id);
        if (noticePO == null || noticePO.noticeState == RuleStateEnum.Disable.getValue()) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_NOTICE_NOTEXISTS, "");
        }
        QueryWrapper<NoticeExtendPO> noticeExtendPOQueryWrapper = new QueryWrapper<>();
        noticeExtendPOQueryWrapper.lambda()
                .eq(NoticeExtendPO::getNoticeId, noticePO.getId())
                .eq(NoticeExtendPO::getDelFlag, 1);
        List<NoticeExtendPO> noticeExtendPOS = noticeExtendMapper.selectList(noticeExtendPOQueryWrapper);
        if (!CollectionUtils.isNotEmpty(noticeExtendPOS)) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_NOTICE_NOTEXISTS, "");
        }
        List<DataSourceConVO> allDataSource = dataSourceConManageImpl.getAllDataSource();
        if (!CollectionUtils.isNotEmpty(allDataSource)) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_ONTEXISTS, "");
        }
        TemplatePO templatePO = templateMapper.selectById(noticePO.templateId);
        if (templatePO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS, "");
        }
        // 第一步：判断质量报告属于哪个业务模块 数据校验/业务清洗/生命周期
        TemplateSceneEnum templateSceneEnum = TemplateSceneEnum.getEnum(templatePO.getTemplateScene());
        AttachmentInfoPO attachmentInfoPO = new AttachmentInfoPO();
        String currentFileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
        attachmentInfoPO.setCurrentFileName(currentFileName);
        attachmentInfoPO.setExtensionName(".xlsx");
        attachmentInfoPO.setAbsolutePath(uploadUrl);
        attachmentInfoPO.setRelativePath(echoPath);
        attachmentInfoPO.setObjectId(String.valueOf(id));
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        switch (templateSceneEnum) {
            case NOTICE_DATACHECK:
                // 生成数据校验质量报告
                attachmentInfoPO.setOriginalName(String.format("数据校验质量报告%s.xlsx",
                        DateTimeUtils.getNowToShortDate().replace("-", "")));
                attachmentInfoPO.setCategory(100);
                resultEnum = createDataCheckQualityReport(noticeExtendPOS, allDataSource, attachmentInfoPO);
                break;
            case NOTICE_BUSINESSFILTER:
                // 生成业务清洗质量报告
                attachmentInfoPO.setOriginalName(String.format("业务清洗质量报告%s.xlsx",
                        DateTimeUtils.getNowToShortDate().replace("-", "")));
                attachmentInfoPO.setCategory(200);
                break;
            case NOTICE_LIFECYCLE:
                // 生成生命周期质量报告
                attachmentInfoPO.setOriginalName(String.format("生命周期质量报告%s.xlsx",
                        DateTimeUtils.getNowToShortDate().replace("-", "")));
                attachmentInfoPO.setCategory(300);
                break;
        }
        if (resultEnum != ResultEnum.SUCCESS) {
            log.info("质量报告执行异常：" + resultEnum.getMsg());
            return ResultEntityBuild.buildData(resultEnum, "");
        }
        // 第二步：保存质量报告信息到附件信息表
        attachmentInfoMapper.insert(attachmentInfoPO);

        // 第三步：是否需要发送邮件
        if (noticePO.getNoticeType() == NoticeTypeEnum.EMAIL_NOTICE.getValue()) {
            NoticeDTO noticeDTO = new NoticeDTO();
            noticeDTO.emailServerId = noticePO.getEmailServerId();
            noticeDTO.emailSubject = noticePO.getEmailSubject();
            noticeDTO.body = noticePO.getBody();
            noticeDTO.emailConsignee = noticePO.getEmailConsignee();
            noticeDTO.emailCc = noticePO.getEmailCc();
            noticeDTO.sendAttachment = true;
            noticeDTO.attachmentName = attachmentInfoPO.getCurrentFileName();
            noticeDTO.attachmentPath = attachmentInfoPO.getAbsolutePath();
            noticeDTO.attachmentActualName = attachmentInfoPO.getOriginalName();
            noticeDTO.companyLogoPath = logoPaht;
            noticeManageImpl.sendEmailNotice(noticeDTO);
        }
        log.info("质量报告执行结束");
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "");
    }

    /**
     * @return void
     * @description 生成数据校验质量报告
     * @author dick
     * @date 2022/8/15 11:04
     * @version v1.0
     * @params noticePO 通知PO
     * @params noticeExtendPOS 通知扩展PO
     * @params templatePO 模板PO
     * @params attachmentInfoPO 附件PO
     */
    public ResultEnum createDataCheckQualityReport(List<NoticeExtendPO> noticeExtendPOS, List<DataSourceConVO> allDataSource, AttachmentInfoPO
            attachmentInfoPO) {

        List<Integer> ruleIds = noticeExtendPOS.stream().map(NoticeExtendPO::getRuleId).collect(Collectors.toList());
        QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
        dataCheckPOQueryWrapper.lambda()
                .eq(DataCheckPO::getDelFlag, 1)
                .eq(DataCheckPO::getRuleState, RuleStateEnum.Enable.getValue())
                .in(DataCheckPO::getId, ruleIds)
                .orderByAsc(DataCheckPO::getRuleSort);
        List<DataCheckPO> dataCheckPOList = dataCheckMapper.selectList(dataCheckPOQueryWrapper);
        if (!CollectionUtils.isNotEmpty(dataCheckPOList)) {
            return ResultEnum.DATA_QUALITY_RULE_NOTEXISTS;
        }

        QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
        dataCheckExtendPOQueryWrapper.lambda()
                .eq(DataCheckExtendPO::getDelFlag, 1)
                .in(DataCheckExtendPO::getRuleId, ruleIds);
        List<DataCheckExtendPO> dataCheckExtendPOList = dataCheckExtendMapper.selectList(dataCheckExtendPOQueryWrapper);

        List<DataTableFieldDTO> dtoList = new ArrayList<>();
        List<Integer> fiDataIds = allDataSource.stream().filter(t -> t.getDatasourceType() == SourceTypeEnum.FiData).map(DataSourceConVO::getId).collect(Collectors.toList());
        for (int j = 0; j < dataCheckPOList.size(); j++) {
            DataCheckPO dataCheckPO = dataCheckPOList.get(j);
            DataTableFieldDTO dataTableFieldDTO = dtoList.stream().filter(t -> t.getId().equals(dataCheckPO.getTableUnique()) && t.getTableBusinessTypeEnum().getValue() == dataCheckPO.getTableBusinessType()).findFirst().orElse(null);
            if (dataTableFieldDTO != null || !fiDataIds.contains(dataCheckPO.getDatasourceId())) {
                continue;
            }
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getId() == dataCheckPO.getDatasourceId()).findFirst().orElse(null);
            dataTableFieldDTO = new DataTableFieldDTO();
            dataTableFieldDTO.setId(dataCheckPO.getTableUnique());
            dataTableFieldDTO.setDataSourceConfigEnum(DataSourceConfigEnum.getEnum(dataSourceConVO.getDatasourceId()));
            dataTableFieldDTO.setTableBusinessTypeEnum(TableBusinessTypeEnum.getEnum(dataCheckPO.getTableBusinessType()));
            dtoList.add(dataTableFieldDTO);
        }
        List<FiDataMetaDataDTO> fiDataMetaDatas = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            fiDataMetaDatas = dataSourceConManageImpl.getTableFieldName(dtoList);
            if (CollectionUtils.isEmpty(fiDataMetaDatas)) {
                return ResultEnum.DATA_QUALITY_REDIS_NOTEXISTSTABLEFIELD;
            }
        }

        ExcelDto excelDto = new ExcelDto();
        excelDto.setExcelName(attachmentInfoPO.getCurrentFileName());
        List<SheetDto> sheets = new ArrayList<>();
        for (int i = 0; i < dataCheckPOList.size(); i++) {
            DataCheckPO dataCheckPO = dataCheckPOList.get(i);
            List<DataCheckExtendPO> dataCheckExtendPOs = dataCheckExtendPOList.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(dataCheckExtendPOs)) {
                continue;
            }
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.templateId);
            if (templatePO == null) {
                continue;
            }
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getId() == dataCheckPO.getDatasourceId()).findFirst().orElse(null);
            if (dataSourceConVO == null) {
                continue;
            }

            String tableName = "";
            HashMap<String, String> fields = new HashMap<>();
            if (dataSourceConVO.getDatasourceType() == SourceTypeEnum.FiData) {
                FiDataMetaDataDTO fiDataMetaDataDTO = fiDataMetaDatas.stream().filter(t -> t.getDataSourceId() == dataSourceConVO.getDatasourceId()).findFirst().orElse(null);
                if (fiDataMetaDataDTO == null) {
                    continue;
                }
                FiDataMetaDataTreeDTO fiDataMetaDataTree_Table = fiDataMetaDataDTO.getChildren().stream().filter(t -> t.getId().equals(dataCheckPO.getTableUnique()) && t.getLabelBusinessType() == dataCheckPO.getTableBusinessType()).findFirst().orElse(null);
                if (fiDataMetaDataTree_Table != null) {
                    tableName = fiDataMetaDataTree_Table.getLabel();
                    if (CollectionUtils.isNotEmpty(fiDataMetaDataTree_Table.getChildren())) {
                        for (int k = 0; k < dataCheckExtendPOs.size(); k++) {
                            DataCheckExtendPO dataCheckExtendPO1 = dataCheckExtendPOs.get(k);
                            if (StringUtils.isNotEmpty(dataCheckExtendPO1.getFieldUnique())) {
                                FiDataMetaDataTreeDTO fiDataMetaDataTree_Field = fiDataMetaDataTree_Table.getChildren().stream().
                                        filter(f -> f.getId().equals(dataCheckExtendPO1.getFieldUnique())).findFirst().orElse(null);
                                if (fiDataMetaDataTree_Field != null) {
                                    fields.put(fiDataMetaDataTree_Field.label, fiDataMetaDataTree_Field.getLabelType());
                                }
                            }
                        }
                    }
                }
            } else {
                tableName = dataCheckPO.getTableUnique();
                List<String> collect = dataCheckExtendPOs.stream().map(DataCheckExtendPO::getFieldUnique).collect(Collectors.toList());
                collect.forEach(t -> {
                    fields.put(t, "");
                });
            }
            TableRuleSqlDTO tableRuleSqlDTO = new TableRuleSqlDTO();
            tableRuleSqlDTO.setTableName(tableName);
            if (CollectionUtils.isNotEmpty(fields)) {
                Map.Entry<String, String> next = fields.entrySet().iterator().next();
                tableRuleSqlDTO.setFieldName(next.getKey());
                tableRuleSqlDTO.setFieldType(next.getValue());
                tableRuleSqlDTO.setFields(fields);
            }
            tableRuleSqlDTO.setTemplateTypeEnum(TemplateTypeEnum.getEnum(templatePO.getTemplateType()));
            tableRuleSqlDTO.setFieldAggregate(dataCheckExtendPOs.get(0).getFieldAggregate());
            tableRuleSqlDTO.setThresholdValue(dataCheckPO.getThresholdValue());
            tableRuleSqlDTO.setSql(dataCheckPO.getCreateRule());
            String roleSql = "";
            try {
                roleSql = createRole(dataSourceConVO, tableRuleSqlDTO);
            } catch (Exception ex) {
                log.error("质量报告Sql执行异常：" + ex);
                continue;
            }
            if (StringUtils.isEmpty(roleSql)) {
                log.info(dataCheckPO.getRuleName() + ",质量报告sql为空");
                continue;
            }
            SheetDto sheet = new SheetDto();
            sheet.setSheetName(dataCheckPO.getRuleName());
            SheetDataDto sheetDataDto = resultSetToMap(dataSourceConVO, roleSql);
            List<RowDto> singRows = getSingRows(tableName, templatePO.templateName, sheetDataDto.columns);
            sheet.setSingRows(singRows);
            sheet.setSingFields(fields.keySet().stream().collect(Collectors.toList()));
            sheet.setDataRows(sheetDataDto.columnData);
            sheets.add(sheet);
        }
        if (CollectionUtils.isNotEmpty(sheets)) {
            excelDto.setSheets(sheets);
            ExcelReportUtil.createExcel(excelDto, attachmentInfoPO.absolutePath, attachmentInfoPO.currentFileName);
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * @return java.util.List<com.fisk.common.core.utils.Dto.Excel.RowDto>
     * @description 获取Excel标识行
     * @author dick
     * @date 2022/8/18 17:38
     * @version v1.0
     * @params tableName
     * @params templatenName
     * @params fields
     */
    public List<RowDto> getSingRows(String tableName, String templatenName, List<String> fields) {
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

    /**
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @description 根据SQL查询，转Map
     * @author dick
     * @date 2022/4/18 11:00
     * @version v1.0
     * @params dataSourceConPO
     * @params sql
     * @params templateTypeEnum
     */
    public SheetDataDto resultSetToMap(DataSourceConVO dataSourceCon, String sql) {
        SheetDataDto sheetDataDto = new SheetDataDto();
        List<String> columnList = new ArrayList<>();
        List<List<DataDto>> mapList = new ArrayList<>();
        Statement st = null;
        Connection conn = null;
        try {
            // 数据库连接对象
            conn = dataSourceConManageImpl.getStatement(dataSourceCon.getConType(), dataSourceCon.getConStr(),
                    dataSourceCon.getConAccount(), dataSourceCon.getConPassword());
            // JDBC 读取大量数据时的 ResultSet resultSetType 设置TYPE_FORWARD_ONLY
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                columnList.add(metaData.getColumnLabel(columnIndex));
            }
            while (rs.next()) {
                List<DataDto> objectMap = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(columnName);
                    DataDto dataDto = new DataDto();
                    dataDto.setFieldName(columnName);
                    dataDto.setFieldValue(value != null ? value.toString() : "");
                    objectMap.add(dataDto);
                }
                mapList.add(objectMap);
            }
            rs.close();
            sheetDataDto.setColumns(columnList);
            sheetDataDto.setColumnData(mapList);
        } catch (Exception ex) {
            log.error("resultSetToMap触发异常，详细报错:", ex);
        } finally {
            try {
                if (st != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                // do nothing
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                log.error("resultSetToMap数据库关闭异常，详细报错:", ex);
            }
        }
        return sheetDataDto;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 生成规则
     * @author dick
     * @date 2022/4/2 15:51
     * @version v1.0
     * @params dto
     * @params templateTypeEnum
     */
    public String createRole(DataSourceConVO dataSourceConVO, TableRuleSqlDTO tableRuleSqlDTO) {
        TemplateTypeEnum templateTypeEnum = tableRuleSqlDTO.getTemplateTypeEnum();
        String sql = "";
        switch (templateTypeEnum) {
            case DATA_MISSING_TEMPLATE:
                // 数据缺失模板
                sql = createData_MissingRule(dataSourceConVO, tableRuleSqlDTO);
                break;
            case FIELD_AGGREGATE_TEMPLATE:
                // 字段聚合波动阈值模板
                sql = createField_AggregateRule(dataSourceConVO, tableRuleSqlDTO);
                break;
            case BUSINESS_CHECK_TEMPLATE:
                // 业务验证模板
                sql = tableRuleSqlDTO.getSql();
                break;
        }
        return sql;
    }

    /**
     * @return java.lang.String
     * @description 生成数据缺失模板规则
     * @author dick
     * @date 2022/3/25 13:59
     * @version v1.0
     * @params dataSourceTypeEnum 数据源类型
     * @params dto 请求参数DTO
     */
    public String createData_MissingRule(DataSourceConVO dataSourceConVO, TableRuleSqlDTO tableRuleSqlDTO) {
        if (tableRuleSqlDTO == null || StringUtils.isEmpty(tableRuleSqlDTO.getTableName())
                || StringUtils.isEmpty(tableRuleSqlDTO.getFieldName())) {
            return "";
        }
        String fieldType = tableRuleSqlDTO.fieldType;
        boolean charValid = true;
        if (dataSourceConVO.getConType() == DataSourceTypeEnum.POSTGRESQL) {
            charValid = RegexUtils.isCharValid(fieldType);
        }
        String sql = "";
        if (charValid) {
            sql = String.format("SELECT * FROM %s WHERE %s IS NULL OR %s = '' OR %s = 'null' ",
                    tableRuleSqlDTO.getTableName(), tableRuleSqlDTO.getFieldName(), tableRuleSqlDTO.getFieldName(), tableRuleSqlDTO.getFieldName());

        } else {
            sql = String.format("SELECT * FROM %s WHERE %s IS NULL",
                    tableRuleSqlDTO.getTableName(), tableRuleSqlDTO.getFieldName(), tableRuleSqlDTO.getFieldName(), tableRuleSqlDTO.getFieldName());
        }
        return sql;
    }

    /**
     * @return java.lang.String
     * @description 生成字段聚合波动阈值规则
     * @author dick
     * @date 2022/3/25 13:59
     * @version v1.0
     * @params tableName 表名称
     * @params fieldName 字段名称
     * @params fieldAggregate 字段聚合函数
     * @params thresholdValue 波动阈值
     */
    public String createField_AggregateRule(DataSourceConVO dataSourceConVO, TableRuleSqlDTO tableRuleSqlDTO) {
        if (tableRuleSqlDTO == null || StringUtils.isEmpty(tableRuleSqlDTO.getTableName()) || StringUtils.isEmpty(tableRuleSqlDTO.getFieldName())) {
            return "";
        }
        String sql = "SELECT\n" +
                "\t'%s' AS checkDataBase,\n" +
                "\t'%s' AS checkTable,\n" +
                "\t'%s' AS checkField,\n" +
                "\t'%s' AS checkDesc,\n" +
                "\t'%s' AS checkType,\n" +
                "CASE\n" +
                "\t\t\n" +
                "\t\tWHEN %s >= %s THEN\n" +
                "\t\t'fail' ELSE 'success' \n" +
                "\tEND AS checkResult \n" +
                "FROM\n" +
                "\t'%s';";
        String dataBase = dataSourceConVO.conDbname;
        switch (tableRuleSqlDTO.getFieldAggregate()) {
            case "SUM":
                sql = String.format(sql, dataBase, tableRuleSqlDTO.getTableName(), tableRuleSqlDTO.getFieldName(), TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE.getName(),
                        "SUM", "SUM(" + tableRuleSqlDTO.getFieldName() + ")", tableRuleSqlDTO.getThresholdValue(), tableRuleSqlDTO.getTableName());
                break;
            case "COUNT":
                sql = String.format(sql, dataBase, tableRuleSqlDTO.getTableName(), tableRuleSqlDTO.getFieldName(), TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE.getName(),
                        "COUNT", "COUNT(" + tableRuleSqlDTO.getFieldName() + ")", tableRuleSqlDTO.getThresholdValue(), tableRuleSqlDTO.getTableName());
                break;
            case "AVG":
                sql = String.format(sql, dataBase, tableRuleSqlDTO.getTableName(), tableRuleSqlDTO.getFieldName(), TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE.getName(),
                        "AVG", "AVG(CAST(" + tableRuleSqlDTO.getFieldName() + " AS decimal(10, 2)))", tableRuleSqlDTO.getThresholdValue(), tableRuleSqlDTO.getTableName());
                break;
            case "MAX":
                sql = String.format(sql, dataBase, tableRuleSqlDTO.getTableName(), tableRuleSqlDTO.getFieldName(), TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE.getName(),
                        "MAX", "MAX(" + tableRuleSqlDTO.getFieldName() + ")", tableRuleSqlDTO.getThresholdValue(), tableRuleSqlDTO.getTableName());
                break;
            case "MIN":
                sql = String.format(sql, dataBase, tableRuleSqlDTO.getTableName(), tableRuleSqlDTO.getFieldName(), TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE.getName(),
                        "MIN", "MIN(" + tableRuleSqlDTO.getFieldName() + ")", tableRuleSqlDTO.getThresholdValue(), tableRuleSqlDTO.getTableName());
                break;
            default:
                return "";
        }
        return sql;
    }

    /**
     * @return int
     * @description 执行sql，返回结果
     * @author dick
     * @date 2022/4/18 11:15
     * @version v1.0
     * @params dataSourceConPO
     * @params sql
     * @params templateTypeEnum
     */
    public int executeSql(DataSourceConPO dataSourceConPO, String sql, TemplateTypeEnum templateTypeEnum) {
        int affectedCount = 0;
        Statement st = null;
        Connection conn = null;
        try {
            // 数据源类型
            DataSourceTypeEnum sourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];
            // 数据库连接对象

            conn = dataSourceConManageImpl.getStatement(sourceTypeEnum, dataSourceConPO.getConStr(),
                    dataSourceConPO.getConAccount(), dataSourceConPO.getConPassword());
            st = conn.createStatement();
            assert st != null;
            /*
              boolean execute(String sql)
              允许执行查询语句、更新语句、DDL语句。
              返回值为true时，表示执行的是查询语句，可以通过getResultSet方法获取结果；
              返回值为false时，执行的是更新语句或DDL语句，getUpdateCount方法获取更新的记录数量。

              int executeUpdate(String sql)
              执行给定 SQL 语句，该语句可能为 INSERT、UPDATE、DELETE、DROP 语句，或者不返回任何内容的 SQL 语句（如 SQL DDL 语句）。
              返回值是更新的记录数量
             */
            affectedCount = st.executeUpdate(sql);
        } catch (Exception ex) {
            log.error("executeSql触发异常，详细报错:", ex);
        } finally {
            try {
                if (st != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                // do nothing
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                log.error("executeSql数据库连接关闭异常，详细报错:", ex);
            }
        }
        return affectedCount;
    }

}

