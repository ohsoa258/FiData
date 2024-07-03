package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.Dto.Excel.ExcelDto;
import com.fisk.common.core.utils.Dto.Excel.RowDto;
import com.fisk.common.core.utils.Dto.Excel.SheetDataDto;
import com.fisk.common.core.utils.Dto.Excel.SheetDto;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.core.utils.office.excel.ExcelReportUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckSyncParamDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportNoticeDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportRecipientDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataQualityClientManageService;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.external.MetaDataFieldRuleVO;
import com.fisk.datagovernance.vo.dataquality.external.MetaDataQualityRuleVO;
import com.fisk.datagovernance.vo.dataquality.external.MetaDataTableRuleVO;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
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
    public ResultEntity<List<MetaDataQualityRuleVO>> getTableRuleList(int fiDataSourceId, String tableUnique, int tableBusinessType) {
        // 第一步：检查参数是否合规
        if (fiDataSourceId == 0 || StringUtils.isEmpty(tableUnique)) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_ERROR, null);
        }
        DataSourceConVO dataSourceConVO = dataSourceConManageImpl.getAllDataSource()
                .stream().
                filter(t -> t.getDatasourceId() == fiDataSourceId && t.getDatasourceType() == SourceTypeEnum.FiData)
                .findFirst().orElse(null);
        if (dataSourceConVO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS, null);
        }

        // 第二步：查询模板信息
        QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
        templatePOQueryWrapper.lambda().eq(TemplatePO::getDelFlag, 1)
                .eq(TemplatePO::getTemplateState, 1);
        List<TemplatePO> templatePOList = templateMapper.selectList(templatePOQueryWrapper);
        if (CollectionUtils.isEmpty(templatePOList)) {
            return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
        }

        List<MetaDataQualityRuleVO> qualityRuleList = new ArrayList<>();

        // 第三步：查询数据检查的规则
        QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
        dataCheckPOQueryWrapper.lambda().eq(DataCheckPO::getDelFlag, 1)
                .eq(DataCheckPO::getRuleState, RuleStateEnum.Enable.getValue())
                .eq(DataCheckPO::getTableUnique, tableUnique)
                .eq(DataCheckPO::getTableBusinessType, tableBusinessType);
        List<DataCheckPO> dataCheckPOList = dataCheckMapper.selectList(dataCheckPOQueryWrapper);

        if (CollectionUtils.isNotEmpty(dataCheckPOList)) {
            List<Long> ruleIdList = dataCheckPOList.stream().map(DataCheckPO::getId).collect(Collectors.toList());
            QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
            dataCheckExtendPOQueryWrapper.lambda().eq(DataCheckExtendPO::getDelFlag, 1)
                    .in(DataCheckExtendPO::getRuleId, ruleIdList);
            List<DataCheckExtendPO> dataCheckExtendPOList = dataCheckExtendMapper.selectList(dataCheckExtendPOQueryWrapper);

            // 表检查规则
            List<MetaDataTableRuleVO> tableRuleList = new ArrayList<>();
            // 字段检查规则
            List<MetaDataFieldRuleVO> fieldRuleList = new ArrayList<>();

            for (DataCheckPO dataCheckPO : dataCheckPOList) {
                // 根据检查规则类型判断该规则是基于表设置的还是基于字段设置的
                TemplatePO templatePO = templatePOList.stream().filter(t -> t.getId() == dataCheckPO.getTemplateId()).findFirst().orElse(null);
                if (templatePO == null) {
                    continue;
                }
                TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                switch (templateTypeEnum) {
                    case NULL_CHECK:
                    case RANGE_CHECK:
                    case STANDARD_CHECK:
                    case DUPLICATE_DATA_CHECK:
                    case FLUCTUATION_CHECK:
                    case REGEX_CHECK:
                        // 基于字段设置的检查规则
                        DataCheckExtendPO dataCheckExtendPO = dataCheckExtendPOList.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).findFirst().orElse(null);
                        if (dataCheckExtendPO == null) {
                            continue;
                        }
                        MetaDataFieldRuleVO metaDataFieldRule = new MetaDataFieldRuleVO();
                        metaDataFieldRule.setFieldUnique(dataCheckExtendPO.getFieldUnique());
                        metaDataFieldRule.setFieldName(dataCheckExtendPO.getFieldName());
                        metaDataFieldRule.setRuleIllustrate(dataCheckPO.getRuleIllustrate());
                        fieldRuleList.add(metaDataFieldRule);
                        break;
                    case PARENTAGE_CHECK:
                    case SQL_SCRIPT_CHECK:
                        // 基于表设置的检查规则
                        MetaDataTableRuleVO metaDataTableRule = new MetaDataTableRuleVO();
                        metaDataTableRule.setTableUnique(dataCheckPO.getTableUnique());
                        metaDataTableRule.setTableName(dataCheckPO.getTableName());
                        metaDataTableRule.setSchemaName(dataCheckPO.getSchemaName());
                        metaDataTableRule.setRuleIllustrate(dataCheckPO.getRuleIllustrate());
                        tableRuleList.add(metaDataTableRule);
                        break;
                }
            }

            MetaDataQualityRuleVO metaDataQualityRule = new MetaDataQualityRuleVO();
            metaDataQualityRule.setModuleTypeEnum(ModuleTypeEnum.DATA_CHECK_MODULE);
            metaDataQualityRule.setTableRuleList(tableRuleList);
            metaDataQualityRule.setFieldRuleList(fieldRuleList);
            qualityRuleList.add(metaDataQualityRule);
        }

        // 第三步：查询业务清洗的规则

        // 第三步：查询生命周期的规则

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, qualityRuleList);
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
                log.info("【质量报告】id参数为空，报告创建失败");
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
                    //参数：规则配置，数据源，以及将要新建的excel文件的信息
                    resultEnum = dataCheck_QualityReport_CreateExcel(noticeExtendPOS, allDataSource, attachmentInfoPO);
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

        List<DataTableFieldDTO> dtoList = new ArrayList<>();
        // fiData平台数据源的id集合
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
        List<FiDataMetaDataDTO> fiDataMetaDataList = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            fiDataMetaDataList = dataSourceConManageImpl.getTableFieldName(dtoList);
            if (CollectionUtils.isEmpty(fiDataMetaDataList)) {
                return ResultEnum.DATA_QUALITY_REDIS_NOTEXISTSTABLEFIELD;
            }
        }

        // 第三步：填充Excel文档Dto
        ExcelDto excelDto = new ExcelDto();
        excelDto.setExcelName(attachmentInfoPO.getCurrentFileName());
        List<SheetDto> sheets = new ArrayList<>();
        for (int i = 0; i < dataCheckPOList.size(); i++) {

            //获取数据校验对象
            DataCheckPO dataCheckPO = dataCheckPOList.get(i);
            //获取数据校验对象对应的第一个扩展配置对象
            DataCheckExtendPO dataCheckExtendPO = dataCheckExtendPOList.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).findFirst().orElse(null);
            if (dataCheckExtendPO == null) {
                continue;
            }

            List<DataCheckExtendPO> dataCheckExtendPOs = dataCheckExtendPOList.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(dataCheckExtendPOs)) {
                continue;
            }

            //当前校验规则对应的 数据校验模板 tb_template_config
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
            if (templatePO == null) {
                continue;
            }

            //获取数据源
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getId() == dataCheckPO.getDatasourceId()).findFirst().orElse(null);
            if (dataSourceConVO == null) {
                continue;
            }

            String tblName = "";
            HashMap<String, String> fields = new HashMap<>();
            if (dataSourceConVO.getDatasourceType() == SourceTypeEnum.FiData) {
                FiDataMetaDataDTO fiDataMetaDataDTO = fiDataMetaDataList.stream().filter(t -> t.getDataSourceId() == dataSourceConVO.getDatasourceId()).findFirst().orElse(null);
                if (fiDataMetaDataDTO == null) {
                    continue;
                }
                FiDataMetaDataTreeDTO fiDataMetaDataTree_Table = fiDataMetaDataDTO.getChildren().stream().filter(t -> t.getId().equals(dataCheckPO.getTableUnique()) && t.getLabelBusinessType() == dataCheckPO.getTableBusinessType()).findFirst().orElse(null);
                if (fiDataMetaDataTree_Table != null) {
                    tblName = fiDataMetaDataTree_Table.getLabel();
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
                tblName = dataCheckPO.getTableUnique();
                List<String> collect = dataCheckExtendPOs.stream().map(DataCheckExtendPO::getFieldUnique).collect(Collectors.toList());
                collect.forEach(t -> {
                    fields.put(t, "");
                });
            }

            // 获取表和字段信息，将其进行转义处理
            DataCheckSyncParamDTO dataCheckSyncParamDTO = new DataCheckSyncParamDTO();
            String tableName = "";
            String tableNameFormat = "";
            if (StringUtils.isNotEmpty(dataCheckPO.getSchemaName())) {
                tableNameFormat = QualityReport_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckPO.getSchemaName()) + ".";
                tableName = dataCheckPO.getSchemaName() + ".";
            }
            tableNameFormat += QualityReport_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckPO.getTableName());
            tableName += dataCheckPO.getTableName();

            String fieldName = "";
            String fieldNameFormat = "";
            if (StringUtils.isNotEmpty(dataCheckExtendPO.getFieldName())) {
                fieldNameFormat = QualityReport_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckExtendPO.getFieldName());
                fieldName = dataCheckExtendPO.getFieldName();
            }
            dataCheckSyncParamDTO.setTableName(tableName);
            dataCheckSyncParamDTO.setTableNameFormat(tableNameFormat);
            dataCheckSyncParamDTO.setFieldName(fieldName);
            dataCheckSyncParamDTO.setFieldNameFormat(fieldNameFormat);

            log.info("【dataCheck_QualityReport_CreateExcel】...dataCheckSyncParamDTO参数[{}]", JSONObject.toJSON(dataCheckSyncParamDTO));

            SheetDataDto sheetDataDto = null;
            try {
                sheetDataDto = dataCheck_QualityReport_Create(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
            } catch (Exception ex) {
                log.error("质量报告拼接SQL执行异常：" + ex);
                continue;
            }
            if (sheetDataDto == null) {
                log.info(dataCheckPO.getRuleName() + "，质量报告SQL检查结果为空，跳过。");
                continue;
            }
            SheetDto sheet = new SheetDto();
            sheet.setSheetName(dataCheckPO.getRuleName());
            List<RowDto> singRows = dataCheck_QualityReport_GetSingRows(tableName, templatePO.getTemplateName(),
                    sheetDataDto.getColumns(), dataCheckPO.getRuleIllustrate(), dataCheckPO.getRuleDescribe());
            sheet.setSingRows(singRows);
            sheet.setSingFields(fields.keySet().stream().collect(Collectors.toList()));
            sheet.setDataRows(sheetDataDto.columnData);
            sheets.add(sheet);
        }
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
            //空值检查
            case NULL_CHECK:
                sheetDataDto = dataCheck_QualityReport_NullCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            //值域检查
            case RANGE_CHECK:
                sheetDataDto = dataCheck_QualityReport_RangeCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            //规范检查
            case STANDARD_CHECK:
                sheetDataDto = dataCheck_QualityReport_StandardCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            //重复数据检查
            case DUPLICATE_DATA_CHECK:
                sheetDataDto = dataCheck_QualityReport_DuplicateDateCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            //波动检查
            case FLUCTUATION_CHECK:
                sheetDataDto = dataCheck_QualityReport_FluctuationCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            //血缘检查
            case PARENTAGE_CHECK:
                sheetDataDto = dataCheck_QualityReport_ParentageCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            //正则表达式检查
            case REGEX_CHECK:
                sheetDataDto = dataCheck_QualityReport_RegexCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
            //SQL脚本检查
            case SQL_SCRIPT_CHECK:
                sheetDataDto = dataCheck_QualityReport_SqlScriptCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                break;
        }
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_NullCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                          DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto = new SheetDataDto();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();
        boolean charValid = RegexUtils.isCharValid(dataCheckExtendPO.getFieldType());
        String sql_QueryCheckData = "";
        if (charValid) {
            sql_QueryCheckData = String.format("SELECT COUNT(*) AS '值为空的数据条数' FROM %s WHERE %s IS NULL OR %s = '' OR %s = 'null'; ",
                    t_Name, f_Name, f_Name, f_Name);
        } else {
            sql_QueryCheckData = String.format("SELECT COUNT(*) AS '值为空的数据条数' FROM %s WHERE %s IS NULL; ",
                    t_Name, f_Name);
        }
        sheetDataDto = QualityReport_QueryTableData_sheet(dataSourceConVO, sql_QueryCheckData);
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_RangeCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                           DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto = new SheetDataDto();
        String sql_QueryCheckData = "";
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();

        DataSourceTypeEnum dataSourceTypeEnum = dataSourceConVO.getConType();
        RangeCheckTypeEnum rangeCheckTypeEnum = RangeCheckTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckType());
        switch (rangeCheckTypeEnum) {
            case SEQUENCE_RANGE:
                if (dataCheckExtendPO.rangeType == 2) {
                    String childrenQuery = "";
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        childrenQuery = String.format("SELECT IFNULL(%s,'') FROM %s", f_Name, t_Name);
                        sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE IFNULL(%s,'') NOT IN (%s)", f_Name, t_Name, f_Name, childrenQuery);
                    } else {
                        childrenQuery = String.format("SELECT %s FROM %s", f_Name, t_Name);
                        sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE %s NOT IN (%s)", f_Name, t_Name, f_Name, childrenQuery);
                    }
                } else {
                    // 序列范围
                    List<String> list = Arrays.asList(dataCheckExtendPO.getRangeCheckValue().split(","));
                    // 将list里面的序列范围截取为'','',''格式的字符串
                    String sql_InString = list.stream()
                            .map(item -> dataSourceTypeEnum == DataSourceTypeEnum.DORIS ? "'" + item + "'" : "N'" + item + "'")
                            .collect(Collectors.joining(", "));
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE IFNULL(%s,'') NOT IN (%s)", f_Name, t_Name, f_Name, sql_InString);
                    } else {
                        sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE %s NOT IN (%s)", f_Name, t_Name, f_Name, sql_InString);
                    }
                }
                break;
            case VALUE_RANGE:
                RangeCheckValueRangeTypeEnum rangeCheckValueRangeTypeEnum = RangeCheckValueRangeTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckValueRangeType());
                if (rangeCheckValueRangeTypeEnum == RangeCheckValueRangeTypeEnum.INTERVAL_VALUE) {
                    // 取值范围
                    Integer lowerBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[0]);
                    Integer upperBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[1]);
                    String sql_BetweenAnd = String.format("CAST(%s AS INT) NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                    if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        sql_BetweenAnd = String.format("%s::NUMERIC NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        sql_BetweenAnd = String.format("%s NOT BETWEEN '%s' AND '%s'", f_Name, lowerBound_Int, upperBound_Int);
                    }
                    sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE %s", f_Name, t_Name, sql_BetweenAnd);
                } else if (rangeCheckValueRangeTypeEnum == RangeCheckValueRangeTypeEnum.UNIDIRECTIONAL_VALUE) {
                    Double rangeCheckValue = Double.valueOf(dataCheckExtendPO.getRangeCheckValue());
                    String rangeCheckOneWayOperator = dataCheckExtendPO.getRangeCheckOneWayOperator();
                    String sql_BetweenAnd = String.format("CAST(%s AS INT) %s %s", f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                    if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        sql_BetweenAnd = String.format("%s::NUMERIC %s %s", f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        sql_BetweenAnd = String.format("%s %s '%s'", f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                    }
                    sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE %s", f_Name, t_Name, sql_BetweenAnd);
                } else {
                    log.info("同步后-值域检查-取值范围-未匹配到有效的枚举：" + rangeCheckValueRangeTypeEnum.getName());
                }
                break;
            case DATE_RANGE:
                // 日期范围
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String timeRangeString = dataCheckExtendPO.getRangeCheckValue();
                String[] timeRange = timeRangeString.split("~");
                LocalDateTime startTime = LocalDateTime.parse(timeRange[0], formatter);
                LocalDateTime endTime = LocalDateTime.parse(timeRange[1], formatter);
                sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE ((%s IS NULL OR %s = '') OR (%s NOT BETWEEN '%s' AND '%s'))",
                        f_Name, t_Name, f_Name, f_Name, f_Name, startTime, endTime);
                break;
            case KEYWORDS_INCLUDE:
                // 关键字包含
                RangeCheckKeywordIncludeTypeEnum rangeCheckKeywordIncludeTypeEnum = RangeCheckKeywordIncludeTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckKeywordIncludeType());
                String rangeCheckValue = dataCheckExtendPO.getRangeCheckValue();
                String likeValue = "";
                switch (rangeCheckKeywordIncludeTypeEnum) {
                    case CONTAINS_KEYWORDS:
                        likeValue = "'%" + rangeCheckValue + "%'";
                        break;
                    case INCLUDE_KEYWORDS_BEFORE:
                        likeValue = "'" + rangeCheckValue + "%'";
                        break;
                    case INCLUDE_KEYWORDS_AFTER:
                        likeValue = "'%" + rangeCheckValue + "'";
                        break;
                    default:
                        log.info("同步后-值域检查-关键字包含-未匹配到有效的枚举: " + rangeCheckKeywordIncludeTypeEnum.getName());
                        break;
                }
                sql_QueryCheckData = String.format("SELECT %s FROM %s WHERE %s not like %s", f_Name, t_Name, f_Name, likeValue);
                break;
        }
        sheetDataDto = QualityReport_QueryTableData_sheet(dataSourceConVO, sql_QueryCheckData);
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_StandardCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                              DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto = new SheetDataDto();
        //列名
        List<String> columns = new ArrayList<>();
        //相当于excel里的所有列的数据
        List<List<String>> columnDatas = new ArrayList<>();

        JSONArray errorDataList = new JSONArray();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();
        String sql_QueryCheckData = String.format("SELECT %s FROM %s", f_Name, t_Name);
        JSONArray jsonArray = QualityReport_QueryTableData_Array(dataSourceConVO, sql_QueryCheckData);
        StandardCheckTypeEnum standardCheckTypeEnum = StandardCheckTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckType());
        for (int i = 0; i < jsonArray.size(); i++) {
            //相当于每行数据
            List<String> columnData = new ArrayList<>();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            // 获取所有列名
            if (i == 0) {
//                columns.add(String.valueOf(jsonObject.keySet()));
                columns.add(fName);
            }
            Object fieldValue = jsonObject.get(fName);
            switch (standardCheckTypeEnum) {
                case DATE_FORMAT:
                    // 日期格式
                    List<String> list = Arrays.asList(dataCheckExtendPO.getStandardCheckTypeDateValue().split(","));
                    if (fieldValue == null || fieldValue.toString().equals("")) {
                        errorDataList.add(jsonObject);
                        columnData.add(fieldValue == null ? "null" : fieldValue.toString());
                    } else {
                        boolean validDateFormat = false;
//                        if (fieldValue.toString().length() > 10) {
//                            validDateFormat = DateTimeUtils.isValidDateTimeFormat(fieldValue.toString(), list);
//                        } else {
//                            validDateFormat = DateTimeUtils.isValidDateFormat(fieldValue.toString(), list);
//                        }
                        validDateFormat = DateTimeUtils.isValidDateOrTimeFormat(fieldValue.toString(), list);
                        if (!validDateFormat) {
                            errorDataList.add(jsonObject);
                            columnData.add(fieldValue.toString());
                        }
                    }
                    if (CollectionUtils.isNotEmpty(columnData)) {
                        columnDatas.add(columnData);
                    }
                    break;
                case CHARACTER_PRECISION_LENGTH_RANGE:
                    // 字符范围
                    StandardCheckCharRangeTypeEnum standardCheckCharRangeTypeEnum = StandardCheckCharRangeTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckCharRangeType());
                    if (standardCheckCharRangeTypeEnum == StandardCheckCharRangeTypeEnum.CHARACTER_PRECISION_RANGE) {
                        int minFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[0]);
                        int maxFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[1]);
                        if (fieldValue == null || fieldValue.toString().equals("")) {
                            errorDataList.add(jsonObject);
                            columnData.add(fieldValue == null ? "null" : fieldValue.toString());
                        } else {
                            List<String> values = Arrays.asList(fieldValue.toString().split(dataCheckExtendPO.getStandardCheckTypeLengthSeparator()));
                            if (values.stream().count() >= 2) {
                                String value = values.get(Math.toIntExact(values.stream().count() - 1));
                                if (value.length() < minFieldLength || value.length() > maxFieldLength) {
                                    errorDataList.add(jsonObject);
                                    columnData.add(fieldValue.toString());
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(columnData)) {
                            columnDatas.add(columnData);
                        }
                    } else if (standardCheckCharRangeTypeEnum == StandardCheckCharRangeTypeEnum.CHARACTER_LENGTH_RANGE) {
                        String standardCheckTypeLengthOperator = dataCheckExtendPO.getStandardCheckTypeLengthOperator();
                        int standardCheckTypeLengthValue = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue());
                        if (fieldValue == null || fieldValue.toString().equals("")) {
                            errorDataList.add(jsonObject);
                            columnData.add(fieldValue == null ? "null" : fieldValue.toString());
                        } else {
                            int checkValueLength = fieldValue.toString().length();
                            boolean isValid = false;
                            switch (standardCheckTypeLengthOperator) {
                                case ">":
                                    isValid = checkValueLength > standardCheckTypeLengthValue;
                                    break;
                                case ">=":
                                    isValid = checkValueLength >= standardCheckTypeLengthValue;
                                    break;
                                case "<":
                                    isValid = checkValueLength < standardCheckTypeLengthValue;
                                    break;
                                case "<=":
                                    isValid = checkValueLength <= standardCheckTypeLengthValue;
                                    break;
                                case "=":
                                    isValid = checkValueLength == standardCheckTypeLengthValue;
                                    break;
                                case "!=":
                                    isValid = checkValueLength != standardCheckTypeLengthValue;
                                    break;
                                default:
                                    log.info("同步后-规范检查-字符范围-字符长度范围-未匹配到有效的运算符：" + standardCheckTypeLengthOperator);
                                    break;
                            }
                            if (isValid) {
                                errorDataList.add(jsonObject);
                                columnData.add(fieldValue.toString());
                            }
                        }
                    } else {
                        log.info("同步后-规范检查-字符范围-未匹配到有效的枚举：" + standardCheckCharRangeTypeEnum.getName());
                    }
                    if (CollectionUtils.isNotEmpty(columnData)) {
                        columnDatas.add(columnData);
                    }
                    break;
                case URL_ADDRESS:
                    // URL地址
                    if (fieldValue == null || fieldValue.toString().equals("")) {
                        errorDataList.add(jsonObject);
                        columnData.add(fieldValue == null ? "null" : fieldValue.toString());
                    } else {
                        boolean validURL = RegexUtils.isValidURL(fieldValue.toString(), false);
                        if (!validURL) {
                            errorDataList.add(jsonObject);
                            columnData.add(fieldValue.toString());
                        }
                    }
                    if (CollectionUtils.isNotEmpty(columnData)) {
                        columnDatas.add(columnData);
                    }
                    break;
                case BASE64_BYTE_STREAM:
                    // BASE64字节流
                    if (fieldValue == null || fieldValue.toString().equals("")) {
                        errorDataList.add(jsonObject);
                        columnData.add(fieldValue == null ? "null" : fieldValue.toString());
                    } else {
                        boolean validBase64String = RegexUtils.isBase64String(fieldValue.toString(), false);
                        if (!validBase64String) {
                            errorDataList.add(jsonObject);
                            columnData.add(fieldValue.toString());
                        }
                    }
                    if (CollectionUtils.isNotEmpty(columnData)) {
                        columnDatas.add(columnData);
                    }
                    break;
            }
        }
        sheetDataDto.setColumns(columns);
        sheetDataDto.setColumnData(columnDatas);
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_DuplicateDateCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                   DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto = new SheetDataDto();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = "",
                fName = dataCheckSyncParamDTO.getFieldName();
        List<String> fieldNames = Arrays.asList(dataCheckExtendPO.getFieldName().split(","));
        for (String item : fieldNames) {
            String fieldFormat = QualityReport_GetSqlFieldFormat(dataSourceConVO.getConType(), item);
            f_Name += fieldFormat + ",";
        }

        String sql_QueryCheckData = String.format("SELECT %s COUNT(*) AS '数据重复条数' FROM %s WHERE 1=1 \n" +
                "GROUP BY %s HAVING COUNT(*) > 1;", f_Name, t_Name, f_Name.replaceAll(",+$", ""));
        sheetDataDto = QualityReport_QueryTableData_sheet(dataSourceConVO, sql_QueryCheckData);
        return sheetDataDto;
    }

//    public String nifiSync_GetSqlFieldFormat(DataSourceTypeEnum dataSourceTypeEnum, String fieldName) {
//        String sqlFieldStr = dataSourceTypeEnum == DataSourceTypeEnum.MYSQL
//                ? "`%s`" :
//                dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER
//                        ? "[%s]" :
//                        dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL
//                                ? "\"%s\"" :
//                                "";
//        if (StringUtils.isNotEmpty(sqlFieldStr)) {
//            sqlFieldStr = String.format(sqlFieldStr, fieldName);
//        }
//        return sqlFieldStr;
//    }

    public SheetDataDto dataCheck_QualityReport_FluctuationCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                 DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto = new SheetDataDto();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();
        String sql_QueryCheckData = "";
        boolean isValid = true;
        double thresholdValue = dataCheckExtendPO.getFluctuateCheckValue();
        double realityValue = 0.0;
        //相当于excel里的所有列的数据
        List<List<String>> columnDatas = new ArrayList<>();
        //列名
        List<String> columns = new ArrayList<>();

        FluctuateCheckTypeEnum fluctuateCheckTypeEnum = FluctuateCheckTypeEnum.getEnum(dataCheckExtendPO.getFluctuateCheckType());
        switch (fluctuateCheckTypeEnum) {
            case AVG:
                sql_QueryCheckData = String.format("SELECT AVG(CAST(%s as int)) AS realityValue FROM %s", f_Name, t_Name);
                break;
            case MIN:
                sql_QueryCheckData = String.format("SELECT MIN(CAST(%s as int)) AS realityValue FROM %s", f_Name, t_Name);
                break;
            case MAX:
                sql_QueryCheckData = String.format("SELECT MAX(CAST(%s as int)) AS realityValue FROM %s", f_Name, t_Name);
                break;
            case SUM:
                sql_QueryCheckData = String.format("SELECT SUM(CAST(%s as int)) AS realityValue FROM %s", f_Name, t_Name);
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

            //excel列名
            // 字段名称
            columns.add("FieldName");
            // 聚合值
            columns.add("AggregateValue");
            // 波动阈值
            columns.add("ThresholdValue");
            //相当于每行数据
            List<String> columnData = new ArrayList<>();
            // 字段名称
            columnData.add(fName);
            // 聚合值
            columnData.add(String.valueOf(realityValue));
            // 波动阈值
            columnData.add(String.valueOf(thresholdValue));
            columnDatas.add(columnData);
            sheetDataDto.setColumns(columns);
            sheetDataDto.setColumnData(columnDatas);
            return sheetDataDto;
        }
        return null;
    }

    public SheetDataDto dataCheck_QualityReport_ParentageCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                               DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto = new SheetDataDto();
        JSONArray jsonArray = new JSONArray();
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_RegexCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                           DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto = new SheetDataDto();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();
        String sql_QueryCheckData = String.format("SELECT %s FROM %s", f_Name, t_Name);
        //列名
        List<String> columns = new ArrayList<>();
        //相当于excel里的所有列的数据
        List<List<String>> columnDatas = new ArrayList<>();

        JSONArray errorDataList = new JSONArray();
        JSONArray data = QualityReport_QueryTableData_Array(dataSourceConVO, sql_QueryCheckData);
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 0; i < data.size(); i++) {
                //相当于每行数据
                List<String> columnData = new ArrayList<>();
                // 获取所有列名
                if (i == 0) {
                    //JSONObject jsonObject1 = data.getJSONObject(i);
                    //columns.add(String.valueOf(jsonObject1.keySet()));
                    columns.add(fName);
                }
                JSONObject jsonObject = data.getJSONObject(i);
                // 判断字段值是否通过正则表达式验证
                String fieldValue = jsonObject.getString(fName);
                boolean isValid = RegexUtils.isValidPattern(fieldValue, dataCheckExtendPO.getRegexpCheckValue(), false);
                if (!isValid) {
                    errorDataList.add(jsonObject);
                    columnData.add(fieldValue);
                }
                if (CollectionUtils.isNotEmpty(columnData)) {
                    columnDatas.add(columnData);
                }
            }
        }
        sheetDataDto.setColumns(columns);
        sheetDataDto.setColumnData(columnDatas);
        return sheetDataDto;
    }

    public SheetDataDto dataCheck_QualityReport_SqlScriptCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                               DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        SheetDataDto sheetDataDto = new SheetDataDto();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName();
        sheetDataDto = QualityReport_QueryTableData_sheet(dataSourceConVO, dataCheckExtendPO.getSqlCheckValue());
        if (sheetDataDto != null && sheetDataDto.getColumnData() != null && sheetDataDto.getColumnData().size() > 0) {
            return sheetDataDto;
        }
        log.info("【dataCheck_QualityReport_SqlScriptCheck】Sql查询结果为空-校验通过！");
        return null;
    }

    public List<RowDto> dataCheck_QualityReport_GetSingRows(String tableName, String templateName,
                                                            List<String> fields, String ruleIllustrate, String ruleDescribe) {
        List<RowDto> singRows = new ArrayList<>();
        RowDto rowDto = new RowDto();
        rowDto.setRowIndex(0);
        List<String> Columns = new ArrayList<>();
        Columns.add("表名称");
        Columns.add("检查规则类型");
        Columns.add("检查规则内容");
        Columns.add("检查规则描述");
        rowDto.setColumns(Columns);
        singRows.add(rowDto);

        rowDto = new RowDto();
        rowDto.setRowIndex(1);
        Columns = new ArrayList<>();
        Columns.add(tableName);
        Columns.add(templateName);
        Columns.add(ruleIllustrate);
        Columns.add(ruleDescribe);
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
                                dataSourceTypeEnum == DataSourceTypeEnum.DORIS
                                        ? "" : "";
        if (StringUtils.isNotEmpty(sqlFieldStr)) {
            sqlFieldStr = String.format(sqlFieldStr, fieldName);
        } else {
            sqlFieldStr = fieldName;
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

    public SheetDataDto QualityReport_QueryTableData_sheet(DataSourceConVO dataSourceConVO, String sql) {
        // 实时建立数据库连接实时释放，防止连接等待时间过长导致超时异常
        log.info("【dataCheck_QualityReport_QueryTableData】待执行SQL：" + sql);
        Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        SheetDataDto sheetDataDto = AbstractCommonDbHelper.execQueryResultSheet(sql, connection);
        return sheetDataDto;
    }

    public JSONArray QualityReport_QueryTableData_Array(DataSourceConVO dataSourceConVO, String sql) {
        // 实时建立数据库连接实时释放，防止连接等待时间过长导致超时异常
        log.info("【dataCheck_QualityReport_QueryTableData】待执行SQL：" + sql);
        Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        JSONArray jsonArray = AbstractCommonDbHelper.execQueryResultArrays(sql, connection);
        return jsonArray;
    }
}

