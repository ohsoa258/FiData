package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.Dto.Excel.*;
import com.fisk.common.core.utils.Dto.Excel.dataquality.QualityReportSummaryDTO;
import com.fisk.common.core.utils.Dto.Excel.dataquality.QualityReportSummary_BodyDTO;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.core.utils.office.excel.ExcelReportUtil;
import com.fisk.common.core.utils.office.excel.dataquality.SummaryReportUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.datagovernance.dto.dataquality.qualityreport.*;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataQualityClientManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckLogsVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.external.MetaDataFieldRuleVO;
import com.fisk.datagovernance.vo.dataquality.external.MetaDataQualityRuleVO;
import com.fisk.datagovernance.vo.dataquality.external.MetaDataTableRuleVO;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamanagement.dto.DataSet.CodeSetDTO;
import com.fisk.datamanagement.dto.standards.StandardsDTO;
import com.google.common.base.Joiner;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Duration;
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
    private DataCheckConditionMapper dataCheckConditionMapper;

    @Resource
    private BusinessFilterMapper businessFilterMapper;

    @Resource
    private LifecycleMapper lifecycleMapper;

    @Resource
    private QualityReportMapper qualityReportMapper;

    @Resource
    private QualityReportManageImpl qualityReportManage;

    @Resource
    private DataCheckLogsMapper dataCheckLogsMapper;

    @Resource
    private DataCheckLogsManageImpl dataCheckLogsManageImpl;

    @Resource
    private QualityReportRuleMapper qualityReportRuleMapper;

    @Resource
    private AttachmentInfoMapper attachmentInfoMapper;

    @Resource
    private AttachmentInfoImpl attachmentInfoImpl;

    @Resource
    private QualityReportLogMapper qualityReportLogMapper;

    @Resource
    private QualityReportRecipientMapper qualityReportRecipientMapper;

    @Resource
    private QualityReportNoticeMapper qualityReportNoticeMapper;

    @Resource
    private DatacheckStandardsGroupServiceImpl dataCheckStandardsGroupServiceImpl;

    @Resource
    private DataManageClient dataManageClient;

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

            // 创建质量报告开始计时
            String createReportStartTime = DateTimeUtils.getNow();

            // 第一步：查询质量报告基础信息
            QualityReportPO qualityReportPO = qualityReportMapper.selectById(id);
            if (qualityReportPO == null || qualityReportPO.getReportState() == RuleStateEnum.Disable.getValue()) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_NOTICE_NOTEXISTS, "");
            }
            // 质量报告主键id
            int reportId = Math.toIntExact(qualityReportPO.getId());
            // 报告批次号
            String reportBatchNumber = UUID.randomUUID().toString().replace("-", "");

            // 第二步：查询质量报告下的规则配置，并按照执行顺序排序
            List<QualityReportRulePO> qualityReportRules = qualityReportRuleMapper.getQualityReportRuleList(reportId);
            if (CollectionUtils.isEmpty(qualityReportRules)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_NOTICE_NOTEXISTS, "");
            }

            // 第三步：查询质量报告下最新的校验规则日志且评语不为空且检查不通过的检查日志
            List<DataCheckLogsVO> dataCheckLogUserCommentList = dataCheckLogsMapper.getDataCheckLogUserComment(reportId);

            // 第四步：查询质量报告下的通知方式
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

            // 第五步：查询质量报告下的接收人
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

            // 第六步：查询数据源信息
            List<DataSourceConVO> allDataSource = dataSourceConManageImpl.getAllDataSource();
            if (!CollectionUtils.isNotEmpty(allDataSource)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS, "");
            }

            // 检查规则日志
            List<DataCheckLogsPO> dataCheckLogs = new ArrayList<>();
            // 检查规则附件
            List<AttachmentInfoPO> attachmentInfos = new ArrayList<>();

            // 第七步：根据规则检查数据是否合规，不合规的数据生成规则检查报告
            ResultEnum resultEnum = ResultEnum.SUCCESS;
            switch (qualityReportPO.getReportType()) {
                case 100:
                    resultEnum = dataCheck_Rule_QualityReport_Create(qualityReportRules, allDataSource,
                            dataCheckLogs, attachmentInfos, dataCheckLogUserCommentList, reportBatchNumber);
                    break;
            }
            if (resultEnum != ResultEnum.SUCCESS) {
                log.info("【dataCheck_Rule_QualityReport_Create】规则检查返回错误信息：" + resultEnum.getMsg());
                return ResultEntityBuild.buildData(resultEnum, "");
            }

            // 第八步：根据返回的检查规则日志，生成summary报告
            QualityReportLogPO qualityReportLogPO = new QualityReportLogPO();
            AttachmentInfoPO attachmentInfoPO = new AttachmentInfoPO();
            attachmentInfoPO.setOriginalName(qualityReportPO.getReportName() + ".xlsx");
            String currentFileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
            attachmentInfoPO.setCurrentFileName(currentFileName);
            attachmentInfoPO.setExtensionName(".xlsx");
            // 绝对路径，例如：/root/nginx/app/file/qualityreport/summaryreport/20240719
            String absolutePath = uploadUrl + "summaryreport/" + DateTimeUtils.getNowToShortDate_v1() + "/";
            isExistsCreateDirectory(absolutePath);
            attachmentInfoPO.setAbsolutePath(absolutePath);
            attachmentInfoPO.setCategory(AttachmentCateGoryEnum.QUALITY_VERIFICATION_SUMMARY_REPORT.getValue());
            resultEnum = dataCheck_Summary_QualityReport_Create(qualityReportPO, qualityReportRules, dataCheckLogs,
                    attachmentInfoPO, reportBatchNumber, qualityReportLogPO);
            if (resultEnum != ResultEnum.SUCCESS) {
                log.info("【dataCheck_Summary_QualityReport_Create】创建summary返回错误信息：" + resultEnum.getMsg());
                return ResultEntityBuild.buildData(resultEnum, "");
            }

            // 第九步：发送邮件,将summary报告发给指定收件人
            ResultEntity<Object> sendResultObj = null;
            QualityReportDTO qualityReportDTO = new QualityReportDTO();
            qualityReportDTO.sendAttachment = true;
            qualityReportDTO.setAttachmentName(attachmentInfoPO.getCurrentFileName());
            qualityReportDTO.setAttachmentPath(attachmentInfoPO.getAbsolutePath());
            qualityReportDTO.setAttachmentActualName(attachmentInfoPO.getOriginalName());
            qualityReportDTO.setCompanyLogoPath("");
            qualityReportDTO.setQualityReportNotice(qualityReportNoticeDTO);
            sendResultObj = qualityReportManage.sendEmailReport(qualityReportDTO);
            String sendResult = sendResultObj != null && sendResultObj.getCode() == ResultEnum.SUCCESS.getCode() ? "已发送" : "发送失败";
            // String sendResult = "已发送";

            // 创建质量报告结束计时
            String createReportEndTime = DateTimeUtils.getNow();
            // 第十步：生成summary报告发送日志，继续补充报告日志信息
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
            qualityReportLogPO.setSendResult(sendResult);
            qualityReportLogPO.setCreateReportStartTime(createReportStartTime);
            qualityReportLogPO.setCreateReportEndTime(createReportEndTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startDate = LocalDateTime.parse(createReportStartTime, formatter);
            LocalDateTime endDate = LocalDateTime.parse(createReportEndTime, formatter);
            Duration duration = Duration.between(startDate, endDate);
            qualityReportLogPO.setCreateReportDuration(String.valueOf(duration.getSeconds()));
            // 新增后返回质量报告日志ID，用于summary报告附件表的ObjectId
            qualityReportLogMapper.insertOne(qualityReportLogPO);
            attachmentInfoPO.setObjectId(String.valueOf(qualityReportLogPO.getId()));
            attachmentInfos.add(attachmentInfoPO);

            // 第十一步：保存规则检查日志和附件日志
            dataCheckLogsManageImpl.saveBatch(dataCheckLogs);
            attachmentInfoImpl.saveBatch(attachmentInfos);
        } catch (Exception ex) {
            log.error("【createQualityReport】创建质量报告异常：" + ex);
            throw new FkException(ResultEnum.ERROR, "【createQualityReport】 ex：" + ex);
        }
        log.info("【createQualityReport】创建质量报告-结束");
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "");
    }

    public ResultEnum dataCheck_Summary_QualityReport_Create(QualityReportPO qualityReportPO, List<QualityReportRulePO> qualityReportRules,
                                                             List<DataCheckLogsPO> dataCheckLogs, AttachmentInfoPO attachmentInfoPO,
                                                             String reportBatchNumber, QualityReportLogPO qualityReportLogPO) {
        // 报告规则检查结果
        String reportRuleCheckResult = "";

        // 报告下的检查规则总条数
        Integer reportRuleCount = qualityReportRules.size();
        // 报告下的检查规则-检查通过的规则条数
        Integer checkRulePassCount = 0;
        // 报告下的检查规则-检查不通过的规则条数
        Integer checkRuleNoPassCount = 0;
        // 报告下的检查规则-表结果集为空，跳过检查的规则条数
        Integer checkRuleSkipCount = 0;

        for (DataCheckLogsPO dataCheckLogsPO : dataCheckLogs) {
            String checkResult = dataCheckLogsPO.getCheckResult();
            if (checkResult.equals("通过")) {
                checkRulePassCount++;
            } else if (checkResult.equals("不通过")) {
                checkRuleNoPassCount++;
            } else if (checkResult.equals("表结果集为空，跳过检查")) {
                checkRuleSkipCount++;
            }
        }
        reportRuleCheckResult = checkRuleNoPassCount == 0 ? "通过" : "不通过";

        // 检查规则通过率：(报告下的检查规则总条数-检查不通过的规则条数)/报告下的检查规则总条数*100
        double proportion = 100;
        double checkRuleAccuracy = (Double.parseDouble(reportRuleCount.toString())
                - Double.parseDouble(checkRuleNoPassCount.toString()))
                / Double.parseDouble(reportRuleCount.toString()) * proportion;
        // 保留两位小数，不进行四舍五入
        BigDecimal bigDecimal_CheckDataAccuracy = new BigDecimal(checkRuleAccuracy).setScale(2, BigDecimal.ROUND_DOWN);

        // 报告评估标准-优良中差
        String reportEvaluationCriteria = "";
        if (StringUtils.isNotEmpty(qualityReportPO.getReportEvaluationCriteria())) {
            List<String> evaluationCriteriaList = Arrays.asList(qualityReportPO.getReportEvaluationCriteria().split(","));
            if (checkRuleAccuracy >= Double.valueOf(evaluationCriteriaList.get(0))) {
                reportEvaluationCriteria = "优，既评估通过";
            } else if (checkRuleAccuracy >= Double.valueOf(evaluationCriteriaList.get(1))) {
                reportEvaluationCriteria = "良，既评估通过";
            } else if (checkRuleAccuracy >= Double.valueOf(evaluationCriteriaList.get(2))) {
                reportEvaluationCriteria = "中，既评估不通过";
            } else if (checkRuleAccuracy < Double.valueOf(evaluationCriteriaList.get(3))) {
                reportEvaluationCriteria = "差，既评估不通过";
            }
        }
        // 总结报告结语
        String epilogue = "结语：当前报告下共配置" + reportRuleCount + "条检查规则，其中" + checkRulePassCount + "条检查通过，" + checkRuleNoPassCount + "条检查不通过，" +
                checkRuleSkipCount + "条跳过检查，通过率为" + bigDecimal_CheckDataAccuracy.toString() + "%。" +
                "根据配置的评估标准当前得分为：" + reportEvaluationCriteria + "，详情见下方表格。";

        // 总结报告内容
        List<QualityReportSummaryDTO> qualityReportSummaryList = new ArrayList<>();
        List<QualityReportSummary_BodyDTO> qualityReportSummary_bodyList = new ArrayList<>();

        for (QualityReportRulePO qualityReportRulePO : qualityReportRules) {
            DataCheckLogsPO dataCheckLogsPO = dataCheckLogs.stream().filter(t -> t.getRuleId() == qualityReportRulePO.getRuleId()).findFirst().orElse(null);
            // 一个表下面有多个规则，检查表是否存在，不存在才添加到集合
            String tableFullName = StringUtils.isNotEmpty(dataCheckLogsPO.getSchemaName()) ? dataCheckLogsPO.getSchemaName() + "." + dataCheckLogsPO.getTableName() : dataCheckLogsPO.getTableName();
            QualityReportSummaryDTO qualityReportSummaryDTO = qualityReportSummaryList.stream().filter(t -> t.tableFullName.equals(tableFullName)).findFirst().orElse(null);
            if (qualityReportSummaryDTO == null) {
                qualityReportSummaryDTO = new QualityReportSummaryDTO();
                qualityReportSummaryDTO.setEpilogue(epilogue);
                qualityReportSummaryDTO.setReportName(qualityReportPO.getReportName());
                qualityReportSummaryDTO.setReportDesc(qualityReportPO.getReportDesc());
                qualityReportSummaryDTO.setReportPrincipal(qualityReportPO.getReportPrincipal());
                qualityReportSummaryDTO.setReportBatchNumber(reportBatchNumber);
                qualityReportSummaryDTO.setTableFullName(tableFullName);
                qualityReportSummaryList.add(qualityReportSummaryDTO);
            }
            // 添加规则检查结果到规则正文列表
            QualityReportSummary_BodyDTO qualityReportSummary_bodyDTO = new QualityReportSummary_BodyDTO();
            qualityReportSummary_bodyDTO.setRuleName(dataCheckLogsPO.getRuleName());
            qualityReportSummary_bodyDTO.setRuleIllustrate(dataCheckLogsPO.getCheckRuleIllustrate());
            qualityReportSummary_bodyDTO.setCheckDataCount(Integer.valueOf(dataCheckLogsPO.getCheckTotalCount()));
            qualityReportSummary_bodyDTO.setDataAccuracy(dataCheckLogsPO.getCheckDataAccuracy());
            qualityReportSummary_bodyDTO.setCheckStatus(dataCheckLogsPO.getCheckResult());
            qualityReportSummary_bodyDTO.setTableFullName(tableFullName);
            qualityReportSummary_bodyDTO.setFieldName(dataCheckLogsPO.getFieldName());
            qualityReportSummary_bodyDTO.setUserComment(dataCheckLogsPO.getUserComment());
            qualityReportSummary_bodyList.add(qualityReportSummary_bodyDTO);
        }

        // 将规则挂到表下面
        qualityReportSummaryList = qualityReportSummaryList.stream().sorted(Comparator.comparing(QualityReportSummaryDTO::getTableFullName)).collect(Collectors.toList());
        qualityReportSummaryList.forEach(t -> {
            List<QualityReportSummary_BodyDTO> reportSummary_bodyDTOS = qualityReportSummary_bodyList.stream().filter(f -> f.tableFullName.equals(t.tableFullName)).collect(Collectors.toList());
            t.setQualityReportSummary_body(reportSummary_bodyDTOS);
        });

        // 生成Summary Excel
        SummaryReportUtils.createSummaryQualityReport(qualityReportSummaryList, attachmentInfoPO.getAbsolutePath(), attachmentInfoPO.getCurrentFileName());

        // 补充Summary报告的日志信息
        qualityReportLogPO.setReportQualityGrade(reportEvaluationCriteria);
        qualityReportLogPO.setReportBatchNumber(reportBatchNumber);
        qualityReportLogPO.setReportRuleCheckCount(String.valueOf(reportRuleCount));
        qualityReportLogPO.setReportRuleCheckErrorCount(String.valueOf(checkRuleNoPassCount));
        qualityReportLogPO.setReportRuleCheckAccuracy(bigDecimal_CheckDataAccuracy + "%");
        qualityReportLogPO.setReportRuleCheckResult(reportRuleCheckResult);
        qualityReportLogPO.setReportRuleCheckEpilogue(epilogue);
        return ResultEnum.SUCCESS;
    }

    public ResultEnum dataCheck_Rule_QualityReport_Create(List<QualityReportRulePO> qualityReportRulePOS,
                                                          List<DataSourceConVO> allDataSource,
                                                          List<DataCheckLogsPO> dataCheckLogs,
                                                          List<AttachmentInfoPO> attachmentInfos,
                                                          List<DataCheckLogsVO> dataCheckLogUserCommentList,
                                                          String reportBatchNumber) {
        // 第一步：查询待执行的检查规则
        List<Integer> ruleIds = qualityReportRulePOS.stream().map(QualityReportRulePO::getRuleId).collect(Collectors.toList());
        QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
        dataCheckPOQueryWrapper.lambda()
                .eq(DataCheckPO::getDelFlag, 1)
                .eq(DataCheckPO::getRuleState, RuleStateEnum.Enable.getValue())
                .in(DataCheckPO::getId, ruleIds);
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

        // 第三步：查询检查规则的检查条件
        QueryWrapper<DataCheckConditionPO> dataCheckConditionPOQueryWrapper = new QueryWrapper<>();
        dataCheckConditionPOQueryWrapper.lambda()
                .eq(DataCheckConditionPO::getDelFlag, 1)
                .in(DataCheckConditionPO::getRuleId, ruleIds);
        List<DataCheckConditionPO> dataCheckConditionPOList = dataCheckConditionMapper.selectList(dataCheckConditionPOQueryWrapper);

        // 第四步：进行数据检查，将不通过的检查的数据写入Excel
        for (int i = 0; i < dataCheckPOList.size(); i++) {
            //规则检查开始计时
            String checkDataStartTime = DateTimeUtils.getNow();
            //规则日志表UUID，同时也用于附件表的ObjectId
            String dataCheckLogs_Uuid = UUID.randomUUID().toString().replace("-", "");

            //获取数据校验对象
            DataCheckPO dataCheckPO = dataCheckPOList.get(i);
            //获取数据校验对象对应的第一个扩展配置对象
            DataCheckExtendPO dataCheckExtendPO = dataCheckExtendPOList.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).findFirst().orElse(null);
            if (dataCheckExtendPO == null) {
                continue;
            }
            //获取数据检查规则的检查条件
            List<DataCheckConditionPO> dataCheckConditionPOs = dataCheckConditionPOList.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).collect(Collectors.toList());
            //当前校验规则对应的 数据校验模板
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
            if (templatePO == null) {
                continue;
            }
            //获取数据源
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getId() == dataCheckPO.getDatasourceId()).findFirst().orElse(null);
            if (dataSourceConVO == null) {
                continue;
            }
            // 如果校验不通过且用户评语不为空，回写用户评语
            String userComment = "";
            if (CollectionUtils.isNotEmpty(dataCheckLogUserCommentList)) {
                DataCheckLogsVO dataCheckLogUserCommentVO = dataCheckLogUserCommentList.stream().filter(t -> t.getRuleId() == dataCheckPO.getId()).findFirst().orElse(null);
                if (dataCheckLogUserCommentVO != null) {
                    userComment = dataCheckLogUserCommentVO.getUserComment();
                }
            }

            QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = null;
            ResultEntity<QualityReportSummary_RuleDTO> resultEntity = null;
            try {
                resultEntity = dataVerificationAndPreVerification(dataSourceConVO, dataCheckPO,
                        dataCheckExtendPO, templatePO, dataCheckConditionPOs, userComment);
                // 单个规则校验不通过，跳过
                if (resultEntity == null || resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
                    continue;
                }
                qualityReportSummary_ruleDTO = resultEntity.getData();
            } catch (Exception ex) {
                continue;
            }

            // 第五步：将检查不通过的数据写入Excel文件，如果检查出来的错误数据大于0条且不超过5000条，则写入Excel并记录附件信息
            SheetDataDto sheetDataDto = qualityReportSummary_ruleDTO.getSheetData();
            List<List<String>> columnData = null;
            if (sheetDataDto != null) {
                columnData = sheetDataDto.getColumnData();
                if (CollectionUtils.isNotEmpty(columnData) && columnData.size() > 0 && columnData.size() <= 5000) {
                    // 生成附件记录
                    AttachmentInfoPO attachmentInfoPO = new AttachmentInfoPO();
                    attachmentInfoPO.setOriginalName(dataCheckPO.getRuleName() + ".xlsx");
                    String currentFileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
                    attachmentInfoPO.setCurrentFileName(currentFileName);
                    attachmentInfoPO.setExtensionName(".xlsx");
                    // 绝对路径，例如：/root/nginx/app/file/qualityreport/rulereport/20240719
                    String absolutePath = uploadUrl + "rulereport/" + DateTimeUtils.getNowToShortDate_v1() + "/";
                    isExistsCreateDirectory(absolutePath);
                    attachmentInfoPO.setAbsolutePath(absolutePath);
                    attachmentInfoPO.setCategory(AttachmentCateGoryEnum.QUALITY_VERIFICATION_RULES_VERIFICATION_DETAIL_REPORT.getValue());
                    attachmentInfoPO.setObjectId(dataCheckLogs_Uuid);
                    attachmentInfos.add(attachmentInfoPO);

                    // 生成Excel文件
                    ExcelDto excelDto = new ExcelDto();
                    excelDto.setExcelName(attachmentInfoPO.getCurrentFileName());
                    List<SheetDto> sheets = new ArrayList<>();
                    SheetDto sheet = new SheetDto();
                    sheet.setSheetName(dataCheckPO.getRuleName());
                    List<RowDto> singRows = dataCheck_QualityReport_GetRuleSingRows(qualityReportSummary_ruleDTO);
                    sheet.setSingRows(singRows);
                    // 检查的字段，会加黄色的底色
                    sheet.setSingFields(Arrays.asList(dataCheckExtendPO.getFieldName().split(",")));
                    // 检查不通过的数据
                    sheet.setDataRows(sheetDataDto.getColumnData());
                    sheets.add(sheet);
                    excelDto.setSheets(sheets);
                    ExcelReportUtil.createExcel(excelDto, absolutePath, currentFileName, true);
                }
            }

            //规则检查结束计时
            String checkDataEndTime = DateTimeUtils.getNow();
            // 第六步：生成规则的检查日志
            DataCheckLogsPO dataCheckLogsPO = new DataCheckLogsPO();
            dataCheckLogsPO.setIdUuid(dataCheckLogs_Uuid);
            dataCheckLogsPO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
            dataCheckLogsPO.setRuleName(dataCheckPO.getRuleName());
            dataCheckLogsPO.setTemplateId(Math.toIntExact(templatePO.getId()));
            dataCheckLogsPO.setCheckTemplateName(qualityReportSummary_ruleDTO.getRuleTemplate());
            dataCheckLogsPO.setFiDatasourceId(dataSourceConVO.getDatasourceId());
            dataCheckLogsPO.setLogType(DataCheckLogTypeEnum.SUBSCRIPTION_REPORT_RULE_CHECK_LOG.getValue());
            dataCheckLogsPO.setSchemaName(dataCheckPO.getSchemaName());
            dataCheckLogsPO.setTableName(dataCheckPO.getTableName());
            dataCheckLogsPO.setFieldName(dataCheckExtendPO.getFieldName());
            dataCheckLogsPO.setCheckBatchNumber(reportBatchNumber);
            dataCheckLogsPO.setCheckSmallBatchNumber(reportBatchNumber);
            dataCheckLogsPO.setCheckTotalCount(String.valueOf(qualityReportSummary_ruleDTO.getCheckDataCount()));
            dataCheckLogsPO.setCheckFailCount(String.valueOf(qualityReportSummary_ruleDTO.getCheckErrorDataCount()));
            dataCheckLogsPO.setCheckResult(qualityReportSummary_ruleDTO.getCheckStatus());
            dataCheckLogsPO.setCheckMsg("");
            dataCheckLogsPO.setCheckRuleIllustrate(dataCheckPO.getRuleIllustrate());
            dataCheckLogsPO.setErrorData("");
            dataCheckLogsPO.setCheckDataAccuracy(qualityReportSummary_ruleDTO.getDataAccuracy());
            dataCheckLogsPO.setCheckDataStartTime(checkDataStartTime);
            dataCheckLogsPO.setCheckDataEndTime(checkDataEndTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startDate = LocalDateTime.parse(checkDataStartTime, formatter);
            LocalDateTime endDate = LocalDateTime.parse(checkDataEndTime, formatter);
            Duration duration = Duration.between(startDate, endDate);
            dataCheckLogsPO.setCheckDataDuration(String.valueOf(duration.getSeconds()));
            dataCheckLogsPO.setCheckDataSql(qualityReportSummary_ruleDTO.getCheckDataSql());
            dataCheckLogsPO.setCheckDataCountSql(qualityReportSummary_ruleDTO.getCheckTotalCountSql());
            dataCheckLogsPO.setCheckErrorDataCountSql(qualityReportSummary_ruleDTO.getCheckErrorDataCountSql());
            dataCheckLogsPO.setUserComment(qualityReportSummary_ruleDTO.getUserComment());
            dataCheckLogs.add(dataCheckLogsPO);

            // 第七步：释放集合对象
            columnData = null;
            sheetDataDto = null;
            qualityReportSummary_ruleDTO = null;
        }

        // 报告下规则数量与检查规则日志数量不一致时，检查不通过
        if (qualityReportRulePOS.size() != dataCheckLogs.size()) {
            List<Integer> ruleIdList = qualityReportRulePOS.stream().map(QualityReportRulePO::getRuleId).collect(Collectors.toList());
            List<Integer> ruleIdList_log = dataCheckLogs.stream().map(DataCheckLogsPO::getRuleId).collect(Collectors.toList());
            List<Integer> diff = new ArrayList<>(ruleIdList);
            diff.removeAll(ruleIdList_log);
            log.info("【dataCheck_QualityReport_Create】...报告下规则数量与检查规则日志数量不一致，缺少的规则id为：[{}]", JSONObject.toJSON(diff));
            return ResultEnum.DATA_QUALITY_REPORT_RULE_COUNT_NOT_EQUAL_TO_LOG_COUNT;
        }
        return ResultEnum.SUCCESS;
    }

    public ResultEntity<QualityReportSummary_RuleDTO> dataVerificationAndPreVerification(DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                                         DataCheckExtendPO dataCheckExtendPO, TemplatePO templatePO,
                                                                                         List<DataCheckConditionPO> dataCheckConditionPOs,
                                                                                         String userComment) {
        ResultEntity<QualityReportSummary_RuleDTO> resultEntity = new ResultEntity<>();
        resultEntity.setCode(ResultEnum.SUCCESS.getCode());
        try {
            //当前校验规则
            if (dataCheckPO == null) {
                resultEntity.setCode(ResultEnum.DATA_QUALITY_AFTER_SYNCHRONIZATION_PRE_VERIFICATION_RULE_CONFIG_ISNULL.getCode());
                return resultEntity;
            }
            //当前校验规则扩展属性
            if (dataCheckPO == null) {
                resultEntity.setCode(ResultEnum.DATA_QUALITY_AFTER_SYNCHRONIZATION_PRE_VERIFICATION_RULE_CONFIG_ISNULL.getCode());
                return resultEntity;
            }
            //当前校验规则对应的 数据校验模板
            if (templatePO == null) {
                resultEntity.setCode(ResultEnum.DATA_QUALITY_AFTER_SYNCHRONIZATION_PRE_VERIFICATION_TEMPLATE_DOES_NOT_EXIST.getCode());
                return resultEntity;
            }
            //当前校验规则对应的 数据源
            if (dataSourceConVO == null) {
                resultEntity.setCode(ResultEnum.DATA_QUALITY_AFTER_SYNCHRONIZATION_PRE_VERIFICATION_DATA_SOURCE_DOES_NOT_EXIST.getCode());
                return resultEntity;
            }

            // 获取表和字段信息，将其进行转义处理
            DataSourceTypeEnum dataSourceTypeEnum = dataSourceConVO.getConType();
            QualityReportSummary_ParamDTO qualityReportSummary_paramDTO = new QualityReportSummary_ParamDTO();
            String tableName = "";
            String tableNameFormat = "";
            if (StringUtils.isNotEmpty(dataCheckPO.getSchemaName())) {
                // 架构名称暂时不转义处理，如果需要转义需要考虑架构名称带库名称的情况，例如：dev_mdm_pg.test
                tableNameFormat = dataCheckPO.getSchemaName() + ".";
                tableName = dataCheckPO.getSchemaName() + ".";
            }
            tableNameFormat += qualityReport_GetSqlFieldFormat(dataSourceTypeEnum, dataCheckPO.getTableName());
            tableName += dataCheckPO.getTableName();

            // 如果是重复数据检查，fieldName可能是多个字段名称：id,id1,id2
            String fieldName = dataCheckExtendPO.getFieldName();
            String fieldNameFormat = "";
            if (StringUtils.isNotEmpty(fieldName)) {
                fieldNameFormat = qualityReport_GetSqlFieldFormat_Separate(dataSourceTypeEnum, fieldName);
            }

            String allocateFieldNames = "";
            String allocateFieldNamesFormat = "";
            if (StringUtils.isNotEmpty(dataCheckExtendPO.getAllocateFieldNames())) {
                allocateFieldNames = "," + dataCheckExtendPO.getAllocateFieldNames();
                allocateFieldNamesFormat = "," + qualityReport_GetSqlFieldFormat_Separate(dataSourceTypeEnum, dataCheckExtendPO.getAllocateFieldNames());
            }

            String fieldCheckWhereSql = "";
            if (CollectionUtils.isNotEmpty(dataCheckConditionPOs)) {
                fieldCheckWhereSql = qualityReport_GetSqlFieldCheckWhere(dataSourceTypeEnum, dataCheckConditionPOs);
            }

            qualityReportSummary_paramDTO.setSchemaName(dataCheckPO.getSchemaName());
            qualityReportSummary_paramDTO.setTableName(tableName);
            qualityReportSummary_paramDTO.setTableNameFormat(tableNameFormat);
            qualityReportSummary_paramDTO.setFieldName(fieldName);
            qualityReportSummary_paramDTO.setFieldNameFormat(fieldNameFormat);
            qualityReportSummary_paramDTO.setAllocateFieldNames(allocateFieldNames);
            qualityReportSummary_paramDTO.setAllocateFieldNamesFormat(allocateFieldNamesFormat);
            qualityReportSummary_paramDTO.setFieldCheckWhereSql(fieldCheckWhereSql);
            qualityReportSummary_paramDTO.setUserComment(userComment);
            log.info("【dataVerificationAndPreVerification】...qualityReportSummary_paramDTO参数[{}]", JSONObject.toJSON(qualityReportSummary_paramDTO));

            QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = null;
            qualityReportSummary_ruleDTO = dataCheck_QualityReport_Check(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, qualityReportSummary_paramDTO);
            resultEntity.setData(qualityReportSummary_ruleDTO);
        } catch (Exception ex) {
            log.error("【dataVerificationAndPreVerification】质量报告检查时出现异常：" + ex);
            log.error(String.format("【dataVerificationAndPreVerification】检查异常的规则id：%s、规则名称：%s", dataCheckPO.getId(), dataCheckPO.getRuleName()));
            resultEntity.setCode(ResultEnum.DATA_QUALITY_AFTER_SYNCHRONIZATION_PRE_VERIFICATION_NOT_APPROVED.getCode());
            resultEntity.setMsg(ex.getMessage());
        }
        return resultEntity;
    }

    public QualityReportSummary_RuleDTO dataCheck_QualityReport_Check(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                      DataCheckExtendPO dataCheckExtendPO, QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = null;
        switch (templateTypeEnum) {
            //空值检查
            case NULL_CHECK:
                qualityReportSummary_ruleDTO = dataCheck_QualityReport_NullCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, qualityReportSummary_paramDTO);
                break;
            //值域检查
            case RANGE_CHECK:
                qualityReportSummary_ruleDTO = dataCheck_QualityReport_RangeCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, qualityReportSummary_paramDTO);
                break;
            //规范检查
            case STANDARD_CHECK:
                qualityReportSummary_ruleDTO = dataCheck_QualityReport_StandardCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, qualityReportSummary_paramDTO);
                break;
            //重复数据检查
            case DUPLICATE_DATA_CHECK:
                qualityReportSummary_ruleDTO = dataCheck_QualityReport_DuplicateDateCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, qualityReportSummary_paramDTO);
                break;
            //波动检查
            case FLUCTUATION_CHECK:
                qualityReportSummary_ruleDTO = dataCheck_QualityReport_FluctuationCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, qualityReportSummary_paramDTO);
                break;
            //血缘检查
            case PARENTAGE_CHECK:
                qualityReportSummary_ruleDTO = dataCheck_QualityReport_ParentageCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, qualityReportSummary_paramDTO);
                break;
            //正则表达式检查
            case REGEX_CHECK:
                qualityReportSummary_ruleDTO = dataCheck_QualityReport_RegexCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, qualityReportSummary_paramDTO);
                break;
            //SQL脚本检查
            case SQL_SCRIPT_CHECK:
                qualityReportSummary_ruleDTO = dataCheck_QualityReport_SqlScriptCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, qualityReportSummary_paramDTO);
                break;
        }
        return qualityReportSummary_ruleDTO;
    }

    /**
     * @return com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportSummary_RuleDTO
     * @description 空值检查
     * @author dick
     * @date 2024/7/17 15:48
     * @version v1.0
     * @params templatePO
     * @params dataSourceConVO
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params qualityReportSummary_paramDTO
     */
    public QualityReportSummary_RuleDTO dataCheck_QualityReport_NullCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                          DataCheckExtendPO dataCheckExtendPO, QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = null;
        String t_Name = qualityReportSummary_paramDTO.getTableNameFormat(),
                tName = qualityReportSummary_paramDTO.getTableName(),
                f_Name = qualityReportSummary_paramDTO.getFieldNameFormat(),
                fName = qualityReportSummary_paramDTO.getFieldName(),
                fAllocate = qualityReportSummary_paramDTO.getAllocateFieldNames(),
                f_Allocate = qualityReportSummary_paramDTO.getAllocateFieldNamesFormat(),
                fieldCheckWhereSql = qualityReportSummary_paramDTO.getFieldCheckWhereSql();

        boolean charValid = RegexUtils.isCharValid(dataCheckExtendPO.getFieldType());
        // 查询不满足校验规则的数据明细
        String sql_QueryCheckData = "";
        // 查询不满足校验规则的数据数量，如果超过5000条则无需再查询数据明细，提高程序性能
        String sql_QueryCheckErrorDataCount = "";
        if (charValid) {
            sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s AND (%s IS NULL OR %s = '' OR %s = 'null') ",
                    f_Name, f_Allocate, t_Name, fieldCheckWhereSql, f_Name, f_Name, f_Name);
            sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s AND (%s IS NULL OR %s = '' OR %s = 'null') ",
                    t_Name, fieldCheckWhereSql, f_Name, f_Name, f_Name);
        } else {
            sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s AND %s IS NULL ",
                    f_Name, f_Allocate, t_Name, fieldCheckWhereSql, f_Name);
            sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s AND %s IS NULL ",
                    t_Name, fieldCheckWhereSql, f_Name);
        }
        // 查询校验的数据总条数
        String sql_QueryDataTotalCount = String.format("SELECT COUNT(*) AS totalCount FROM %s WHERE 1=1 %s ", t_Name, fieldCheckWhereSql);

        SheetDataDto sheetDataDto = new SheetDataDto();
        Integer errorDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryCheckErrorDataCount, "errorTotalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        if (errorDataTotalCount > 0 && errorDataTotalCount <= 5000) {
            sheetDataDto = qualityReport_QueryTableData_Sheet(dataSourceConVO, sql_QueryCheckData, dataCheckPO.getId(), dataCheckPO.getRuleName());
        }
        Integer checkDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryDataTotalCount, "totalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        qualityReportSummary_ruleDTO = dataCheck_QualityReportSummary_GetBasicInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO,
                qualityReportSummary_paramDTO, sheetDataDto, checkDataTotalCount, sql_QueryCheckData,
                sql_QueryDataTotalCount, errorDataTotalCount, sql_QueryCheckErrorDataCount);
        return qualityReportSummary_ruleDTO;
    }

    /**
     * @return com.fisk.common.core.utils.Dto.Excel.QualityReportSummary_RuleDTO
     * @description 值域检查
     * @author dick
     * @date 2024/7/17 15:49
     * @version v1.0
     * @params templatePO
     * @params dataSourceConVO
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params qualityReportSummary_paramDTO
     */
    public QualityReportSummary_RuleDTO dataCheck_QualityReport_RangeCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                           DataCheckExtendPO dataCheckExtendPO, QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = null;
        // 查询不满足校验规则的数据明细
        String sql_QueryCheckData = "";
        // 查询校验的数据总条数
        String sql_QueryDataTotalCount = "";
        // 查询不满足校验规则的数据数量，如果超过5000条则无需再查询数据明细，提高程序性能
        String sql_QueryCheckErrorDataCount = "";
        String t_Name = qualityReportSummary_paramDTO.getTableNameFormat(),
                tName = qualityReportSummary_paramDTO.getTableName(),
                f_Name = qualityReportSummary_paramDTO.getFieldNameFormat(),
                fName = qualityReportSummary_paramDTO.getFieldName(),
                fAllocate = qualityReportSummary_paramDTO.getAllocateFieldNames(),
                f_Allocate = qualityReportSummary_paramDTO.getAllocateFieldNamesFormat(),
                fieldCheckWhereSql = qualityReportSummary_paramDTO.getFieldCheckWhereSql();

        DataSourceTypeEnum dataSourceTypeEnum = dataSourceConVO.getConType();
        RangeCheckTypeEnum rangeCheckTypeEnum = RangeCheckTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckType());

        // 如果元数据组ID不为空，序列范围-指定序列范围和取值范围的配置信息取自元数据组
        StandardsDTO standardsDTO = null;
        Integer dataCheckGroupId = dataCheckPO.getDatacheckGroupId();
        if (dataCheckGroupId != null && dataCheckGroupId > 0
                && ((rangeCheckTypeEnum == RangeCheckTypeEnum.SEQUENCE_RANGE && dataCheckExtendPO.getRangeType() == 1)
                || rangeCheckTypeEnum == RangeCheckTypeEnum.VALUE_RANGE)) {
            DatacheckStandardsGroupPO groupPO = dataCheckStandardsGroupServiceImpl.getById(dataCheckGroupId);
            ResultEntity<StandardsDTO> standards = dataManageClient.getStandards(groupPO.getStandardsMenuId());
            if (standards.code == ResultEnum.SUCCESS.getCode()) {
                standardsDTO = standards.data;
            } else {
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
            if (standardsDTO == null) {
                log.info("【数据元未查询到匹配数据请检查并清理脏数据】,数据元id：" + dataCheckGroupId);
            }
        }


        switch (rangeCheckTypeEnum) {
            // 序列范围
            case SEQUENCE_RANGE:
                // 表字段序列范围
                if (dataCheckExtendPO.getRangeType() == 2) {
                    String childrenQuery = "";
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        childrenQuery = String.format("SELECT IFNULL(%s,'') FROM %s", f_Name, t_Name);
                        sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s AND IFNULL(%s,'') NOT IN (%s) ",
                                f_Name, f_Allocate, t_Name, fieldCheckWhereSql, f_Name, childrenQuery);
                        sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s AND IFNULL(%s,'') NOT IN (%s) ",
                                t_Name, fieldCheckWhereSql, f_Name, childrenQuery);
                    } else {
                        childrenQuery = String.format("SELECT %s FROM %s", f_Name, t_Name);
                        sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s) ",
                                f_Name, f_Allocate, t_Name, fieldCheckWhereSql, f_Name, childrenQuery);
                        sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s AND %s NOT IN (%s) ",
                                t_Name, fieldCheckWhereSql, f_Name, childrenQuery);
                    }
                } else {

                    // 指定序列范围
                    List<String> list = new ArrayList<>();
                    if (standardsDTO == null) {
                        list = Arrays.asList(dataCheckExtendPO.getRangeCheckValue().split(","));
                    } else {
                        List<CodeSetDTO> codeSetDTOList = standardsDTO.getCodeSetDTOList();
                        list = codeSetDTOList.stream().map(v -> v.getName()).collect(Collectors.toList());
                    }

                    // 将list里面的序列范围截取为'','',''格式的字符串
                    boolean charValid = RegexUtils.isCharValid(dataCheckExtendPO.getFieldType());
                    String isConsN = charValid ? "N" : "";

                    String sql_InString = list.stream()
                            .map(item -> dataSourceTypeEnum == DataSourceTypeEnum.DORIS ? "'" + item + "'" : "" + isConsN + "'" + item + "'")
                            .collect(Collectors.joining(", "));
                    String caseFieldStr = f_Name;
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        caseFieldStr = " IFNULL(" + f_Name + ",'') ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        caseFieldStr = " COALESCE(CAST(" + f_Name + " AS VARCHAR),'') ";
                    }
                    sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s) ",
                            f_Name, f_Allocate, t_Name, fieldCheckWhereSql, caseFieldStr, sql_InString);
                    sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s AND %s NOT IN (%s) ",
                            t_Name, fieldCheckWhereSql, caseFieldStr, sql_InString);
                }
                break;
            //取值范围
            case VALUE_RANGE:
                RangeCheckValueRangeTypeEnum rangeCheckValueRangeTypeEnum = RangeCheckValueRangeTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckValueRangeType());
                // 区间取值
                if (rangeCheckValueRangeTypeEnum == RangeCheckValueRangeTypeEnum.INTERVAL_VALUE) {
                    // 声明的类型不要用小数，否则数据库检查时会出现0!=0.00的情况
                    Integer lowerBound_Int = 0, upperBound_Int = 0;
                    if (standardsDTO == null) {
                        lowerBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[0]);
                        upperBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[1]);
                    } else {
                        lowerBound_Int = Integer.valueOf(standardsDTO.getValueRange());
                        upperBound_Int = Integer.valueOf(standardsDTO.getValueRangeMax());
                    }

                    String sql_BetweenAnd = String.format("CAST(%s AS INT) NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                    if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        // 为空也属于错误数据
                        sql_BetweenAnd = String.format("(COALESCE(CAST(%s AS VARCHAR),'')='' OR %s::NUMERIC NOT BETWEEN %s AND %s)", f_Name, f_Name, lowerBound_Int, upperBound_Int);
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        // 为空也属于错误数据
                        sql_BetweenAnd = String.format("(IFNULL(CAST(%s AS VARCHAR),'')='' OR %s NOT BETWEEN %s AND %s)", f_Name, f_Name, lowerBound_Int, upperBound_Int);
                    }
                    sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s AND %s ",
                            f_Name, f_Allocate, t_Name, fieldCheckWhereSql, sql_BetweenAnd);
                    sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s AND %s ",
                            t_Name, fieldCheckWhereSql, sql_BetweenAnd);
                } else if (rangeCheckValueRangeTypeEnum == RangeCheckValueRangeTypeEnum.UNIDIRECTIONAL_VALUE) {
                    // 单向取值，因为页面可以配置运算符，比如页面配置字段=6，所以要查的是!=6的数据，业务页面配置的规则认为是满足校验规则的数据
                    Integer rangeCheckValue = 0;
                    String rangeCheckOneWayOperator = "";
                    if (standardsDTO == null) {
                        rangeCheckValue = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue());
                        rangeCheckOneWayOperator = qualityReport_GetReverseOperator(dataCheckExtendPO.getRangeCheckOneWayOperator());
                    } else {
                        rangeCheckValue = Integer.valueOf(standardsDTO.getValueRange());
                        rangeCheckOneWayOperator = qualityReport_GetReverseOperator(standardsDTO.getSymbols());
                    }

                    String sql_BetweenAnd = String.format("CAST(%s AS INT) %s %s", f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                    if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        // 为空也属于错误数据
                        sql_BetweenAnd = String.format("(COALESCE(CAST(%s AS VARCHAR),'')='' OR %s::NUMERIC %s %s)", f_Name, f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        // 为空也属于错误数据
                        sql_BetweenAnd = String.format("(IFNULL(CAST(%s AS VARCHAR),'')='' OR %s %s %s)", f_Name, f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                    }
                    sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s AND %s",
                            f_Name, f_Allocate, t_Name, fieldCheckWhereSql, sql_BetweenAnd);
                    sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s AND %s",
                            t_Name, fieldCheckWhereSql, sql_BetweenAnd);
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
                sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1  %s AND (((%s IS NULL OR %s = '') OR (%s NOT BETWEEN '%s' AND '%s'))) ",
                        f_Name, f_Allocate, t_Name, fieldCheckWhereSql, f_Name, f_Name, f_Name, startTime, endTime);
                sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1  %s AND (((%s IS NULL OR %s = '') OR (%s NOT BETWEEN '%s' AND '%s'))) ",
                        t_Name, fieldCheckWhereSql, f_Name, f_Name, f_Name, startTime, endTime);
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
                sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s AND %s not like %s ",
                        f_Name, f_Allocate, t_Name, fieldCheckWhereSql, f_Name, likeValue);
                sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s AND %s not like %s ",
                        t_Name, fieldCheckWhereSql, f_Name, likeValue);
                break;
        }
        sql_QueryDataTotalCount = String.format("SELECT COUNT(*) AS totalCount FROM %s WHERE 1=1 %s;", t_Name, fieldCheckWhereSql);

        SheetDataDto sheetDataDto = new SheetDataDto();
        Integer errorDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryCheckErrorDataCount, "errorTotalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        log.info("值域检查返回errorDataTotalCount：" + errorDataTotalCount);
        if (errorDataTotalCount > 0 && errorDataTotalCount <= 5000) {
            sheetDataDto = qualityReport_QueryTableData_Sheet(dataSourceConVO, sql_QueryCheckData, dataCheckPO.getId(), dataCheckPO.getRuleName());
        }
        Integer checkDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryDataTotalCount, "totalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        log.info("值域检查返回checkDataTotalCount：" + checkDataTotalCount);
        qualityReportSummary_ruleDTO = dataCheck_QualityReportSummary_GetBasicInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO,
                qualityReportSummary_paramDTO, sheetDataDto, checkDataTotalCount, sql_QueryCheckData,
                sql_QueryDataTotalCount, errorDataTotalCount, sql_QueryCheckErrorDataCount);
        return qualityReportSummary_ruleDTO;
    }

    /**
     * @return com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportSummary_RuleDTO
     * @description 规范检查
     * @author dick
     * @date 2024/7/18 11:32
     * @version v1.0
     * @params templatePO
     * @params dataSourceConVO
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params qualityReportSummary_paramDTO
     */
    public QualityReportSummary_RuleDTO dataCheck_QualityReport_StandardCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                              DataCheckExtendPO dataCheckExtendPO, QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = null;
        // 查询不满足校验规则的数据明细
        String sql_QueryCheckData = "";
        // 查询校验的数据总条数
        String sql_QueryDataTotalCount = "";
        // 查询不满足校验规则的数据数量，如果超过5000条则无需再查询数据明细，提高程序性能
        String sql_QueryCheckErrorDataCount = "";

        String t_Name = qualityReportSummary_paramDTO.getTableNameFormat(),
                f_Name = qualityReportSummary_paramDTO.getFieldNameFormat(),
                f_Allocate = qualityReportSummary_paramDTO.getAllocateFieldNamesFormat(),
                fieldCheckWhereSql = qualityReportSummary_paramDTO.getFieldCheckWhereSql();

        sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s ", f_Name, f_Allocate, t_Name, fieldCheckWhereSql);
        sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s ", t_Name, fieldCheckWhereSql);
        sql_QueryDataTotalCount = String.format("SELECT COUNT(*) AS totalCount FROM %s WHERE 1=1 %s ", t_Name, fieldCheckWhereSql);
        String sqlWhere = "";

        DataSourceTypeEnum dataSourceTypeEnum = dataSourceConVO.getConType();
        StandardCheckTypeEnum standardCheckTypeEnum = StandardCheckTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckType());
        switch (standardCheckTypeEnum) {
            case DATE_FORMAT:
                // 日期格式
                List<String> dateFormatList = Arrays.asList(dataCheckExtendPO.getStandardCheckTypeDateValue().split(","));
                sqlWhere = standardCheck_GetDateFormatCheckSql(dateFormatList, dataSourceTypeEnum, qualityReportSummary_paramDTO);
                break;
            case CHARACTER_PRECISION_LENGTH_RANGE:
                // 字符范围
                StandardCheckCharRangeTypeEnum standardCheckCharRangeTypeEnum = StandardCheckCharRangeTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckCharRangeType());
                // 字符精度范围
                if (standardCheckCharRangeTypeEnum == StandardCheckCharRangeTypeEnum.CHARACTER_PRECISION_RANGE) {
                    int minFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[0]);
                    int maxFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[1]);
                    sqlWhere = standardCheck_GetCharacterAccuracyFormatCheckSql(minFieldLength, maxFieldLength, dataSourceTypeEnum, qualityReportSummary_paramDTO);
                } else if (standardCheckCharRangeTypeEnum == StandardCheckCharRangeTypeEnum.CHARACTER_LENGTH_RANGE) {
                    // 字符长度范围
                    String standardCheckTypeLengthOperator = qualityReport_GetReverseOperator(dataCheckExtendPO.getStandardCheckTypeLengthOperator());
                    int standardCheckTypeLengthValue = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue());
                    sqlWhere = standardCheck_GetCharacterLengthFormatCheckSql(standardCheckTypeLengthOperator, standardCheckTypeLengthValue, dataSourceTypeEnum, qualityReportSummary_paramDTO);
                } else {
                    log.info("同步后-规范检查-字符范围-未匹配到有效的枚举：" + standardCheckCharRangeTypeEnum.getName());
                }
                break;
            case URL_ADDRESS:
                // URL地址
                sqlWhere = standardCheck_GetURLFormatCheckSql(dataSourceTypeEnum, qualityReportSummary_paramDTO);
                break;
            case BASE64_BYTE_STREAM:
                // BASE64字节流
                sqlWhere = standardCheck_GetBase64FormatCheckSql(dataSourceTypeEnum, qualityReportSummary_paramDTO);
                break;
        }
        sql_QueryCheckData += sqlWhere;
        sql_QueryCheckErrorDataCount += sqlWhere;

        SheetDataDto sheetDataDto = new SheetDataDto();
        Integer errorDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryCheckErrorDataCount, "errorTotalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        if (errorDataTotalCount > 0 && errorDataTotalCount <= 5000) {
            sheetDataDto = qualityReport_QueryTableData_Sheet(dataSourceConVO, sql_QueryCheckData, dataCheckPO.getId(), dataCheckPO.getRuleName());
        }
        Integer checkDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryDataTotalCount, "totalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        qualityReportSummary_ruleDTO = dataCheck_QualityReportSummary_GetBasicInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO,
                qualityReportSummary_paramDTO, sheetDataDto, checkDataTotalCount, sql_QueryCheckData,
                sql_QueryDataTotalCount, errorDataTotalCount, sql_QueryCheckErrorDataCount);
        return qualityReportSummary_ruleDTO;
    }

    /**
     * @return java.lang.String
     * @description 规范检查-日期格式检查
     * @author dick
     * @date 2024/7/18 11:33
     * @version v1.0
     * @params dateFormatList
     * @params dataSourceTypeEnum
     * @params qualityReportSummary_paramDTO
     */
    public String standardCheck_GetDateFormatCheckSql(List<String> dateFormatList, DataSourceTypeEnum dataSourceTypeEnum,
                                                      QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {

        String f_Name = qualityReportSummary_paramDTO.getFieldNameFormat();
        String sql = "";

        for (String dateFormat : dateFormatList) {
            String tempSql = "", numberType = "", castFieldStr = f_Name;
            switch (dateFormat) {
                case "HH:mm":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 5\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 5\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 5\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 5\n";
                        numberType = " UNSIGNED ";
                    }
                    tempSql += " OR SUBSTRING( " + castFieldStr + " , 3, 1) != ':'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 1, 2) AS " + numberType + ") NOT BETWEEN '00' AND '23'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 3, 2) AS " + numberType + ") NOT BETWEEN '00' AND '59') \n";
                    break;
                case "HHmm":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 4\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 4\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 4\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 4\n";
                        numberType = " UNSIGNED ";
                    }
                    tempSql += " OR CAST(SUBSTRING( " + castFieldStr + " , 1, 2) AS " + numberType + ") NOT BETWEEN '00' AND '23'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 3, 2) AS " + numberType + ") NOT BETWEEN '00' AND '59') \n";
                    break;
                case "yyyyMM":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 6\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 6\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 6\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 6\n";
                        numberType = " UNSIGNED ";
                    }
                    tempSql += " OR CAST(SUBSTRING( " + castFieldStr + " , 1, 4) AS " + numberType + ") NOT BETWEEN '0000' AND '9999'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 5, 2) AS " + numberType + ") NOT BETWEEN '01' AND '12') \n";
                    break;
                case "yyyy-MM":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 7\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 7\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 7\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 7\n";
                        numberType = " UNSIGNED ";
                    }
                    tempSql += " OR SUBSTRING( " + castFieldStr + " , 5, 1) != '-'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 1, 4) AS " + numberType + ") NOT BETWEEN '0000' AND '9999'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 6, 2) AS " + numberType + ") NOT BETWEEN '01' AND '12') \n";
                    break;
                case "yyyyMMdd":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 8\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 8\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 8\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 8\n";
                        numberType = " UNSIGNED ";
                    }
                    tempSql += " OR CAST(SUBSTRING( " + castFieldStr + " , 1, 4) AS " + numberType + ") NOT BETWEEN '0000' AND '9999'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 5, 2) AS " + numberType + ") NOT BETWEEN '01' AND '12'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 7, 2) AS " + numberType + ") NOT BETWEEN '01' AND '31') \n";
                    break;
                case "yyyy-MM-dd":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 10\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 10\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 10\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 10\n";
                        numberType = " UNSIGNED ";
                    }
                    tempSql += " OR SUBSTRING( " + castFieldStr + " , 5, 1) != '-'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 8, 1) != '-'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 1, 4) AS " + numberType + ") NOT BETWEEN '0000' AND '9999'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 6, 2) AS " + numberType + ") NOT BETWEEN '01' AND '12'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 9, 2) AS " + numberType + ") NOT BETWEEN '01' AND '31') \n";
                    break;
                case "yyyy/MM-dd":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 10\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 10\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 10\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 10\n";
                        numberType = " UNSIGNED ";
                    }
                    tempSql += " OR SUBSTRING( " + castFieldStr + " , 5, 1) != '/'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 8, 1) != '-'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 1, 4) AS " + numberType + ") NOT BETWEEN '0000' AND '9999'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 6, 2) AS " + numberType + ") NOT BETWEEN '01' AND '12'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 9, 2) AS " + numberType + ") NOT BETWEEN '01' AND '31') \n";
                    break;
                case "yyyy-MM-dd HH:mm":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 16\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 16\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 16\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 16\n";
                        numberType = " UNSIGNED ";
                    }
                    tempSql += " OR SUBSTRING( " + castFieldStr + " , 5, 1) != '-'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 8, 1) != '-'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 1, 4) AS " + numberType + ") NOT BETWEEN '0000' AND '9999'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 6, 2) AS " + numberType + ") NOT BETWEEN '01' AND '12'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 9, 2) AS " + numberType + ") NOT BETWEEN '01' AND '31'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 11, 1) != ' '\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 12, 2) AS " + numberType + ") NOT BETWEEN '00' AND '23'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 14, 1) != ':'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 15, 2) AS " + numberType + ") NOT BETWEEN '00' AND '59') \n";
                    break;
                case "yyyy-MM-dd HH:mm:ss":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 19\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 19\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 19\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 19\n";
                        numberType = " UNSIGNED ";
                    }
                    tempSql += " OR SUBSTRING( " + castFieldStr + " , 5, 1) != '-'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 8, 1) != '-'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 1, 4) AS " + numberType + ") NOT BETWEEN '0000' AND '9999'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 6, 2) AS " + numberType + ") NOT BETWEEN '01' AND '12'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 9, 2) AS " + numberType + ") NOT BETWEEN '01' AND '31'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 11, 1) != ' '\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 12, 2) AS " + numberType + ") NOT BETWEEN '00' AND '23'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 14, 1) != ':'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 17, 1) != ':'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 15, 2) AS " + numberType + ") NOT BETWEEN '00' AND '59'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 18, 2) AS " + numberType + ") NOT BETWEEN '00' AND '59') \n";
                    break;
                case "yyyy/MM/dd HH:mm:ss":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 19\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 19\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 19\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 19\n";
                        numberType = " UNSIGNED ";
                    }
                    tempSql += " OR SUBSTRING( " + castFieldStr + " , 5, 1) != '/'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 8, 1) != '/'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 1, 4) AS " + numberType + ") NOT BETWEEN '0000' AND '9999'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 6, 2) AS " + numberType + ") NOT BETWEEN '01' AND '12'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 9, 2) AS " + numberType + ") NOT BETWEEN '01' AND '31'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 11, 1) != ' '\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 12, 2) AS " + numberType + ") NOT BETWEEN '00' AND '23'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 14, 1) != ':'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 17, 1) != ':'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 15, 2) AS " + numberType + ") NOT BETWEEN '00' AND '59'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 18, 2) AS " + numberType + ") NOT BETWEEN '00' AND '59') \n";
                    break;
                case "yyyy-MM-dd HH:mm:ss.SSS":
                    if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL( " + f_Name + " , '')) != 23\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                        tempSql += " AND (LEN(ISNULL(" + f_Name + ", '')) != 23\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                        castFieldStr = "CAST(" + f_Name + " AS VARCHAR)";
                        tempSql += " AND (CHAR_LENGTH(COALESCE(" + castFieldStr + ", '')) != 23\n";
                        numberType = " INT ";
                    } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                        tempSql += " AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) != 23\n";
                        numberType = " UNSIGNED ";
                    }
//                        -- -- 检查 '-' 字符在正确位置
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 5, 1); -- = '-'
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 8, 1); -- = '-'
//                        -- -- 检查年份在正确范围内
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 1, 4) ;-- BETWEEN '0000' AND '9999'
//                        -- -- 检查月份在正确范围内
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 6, 2) ;-- BETWEEN '01' AND '12'
//                        -- -- 检查日期在正确范围内
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 9, 2) ;-- BETWEEN '01' AND '31'
//                        -- -- 检查空格在正确位置
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 11, 1) ;-- = ' '
//                        -- -- 检查小时在正确范围内
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 12, 2) ;-- BETWEEN '00' AND '23'
//                        -- -- 检查 ':' 字符在正确位置
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 14, 1); -- = ':'
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 17, 1); -- = ':'
//                        -- -- 检查分钟在正确范围内
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 15, 2); -- BETWEEN '00' AND '59'
//                        -- -- 检查秒在正确范围内
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 18, 2); -- BETWEEN '00' AND '59'
//                        -- -- 检查毫秒在正确范围内
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 20, 1); -- = '.'
//                        -- SELECT SUBSTRING('2024-07-01 00:00:00.000', 21, 3); -- BETWEEN '000' AND '999'
                    tempSql += " OR SUBSTRING( " + castFieldStr + " , 5, 1) != '-'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 8, 1) != '-'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 1, 4) AS " + numberType + ") NOT BETWEEN '0000' AND '9999'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 6, 2) AS " + numberType + ") NOT BETWEEN '01' AND '12'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 9, 2) AS " + numberType + ") NOT BETWEEN '01' AND '31'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 11, 1) != ' '\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 12, 2) AS " + numberType + ") NOT BETWEEN '00' AND '23'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 14, 1) != ':'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 17, 1) != ':'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 15, 2) AS " + numberType + ") NOT BETWEEN '00' AND '59'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 18, 2) AS " + numberType + ") NOT BETWEEN '00' AND '59'\n" +
                            "OR SUBSTRING( " + castFieldStr + " , 20, 1) != '.'\n" +
                            "OR CAST(SUBSTRING( " + castFieldStr + " , 21, 3) AS " + numberType + ") NOT BETWEEN '000' AND '999') \n";

                    break;
            }
            sql += tempSql;
        }
        return sql;
    }

    /**
     * @return java.lang.String
     * @description 规范检查-字符范围-字符精度范围检查
     * @author dick
     * @date 2024/7/18 11:33
     * @version v1.0
     * @params minFieldLength
     * @params maxFieldLength
     * @params dataSourceTypeEnum
     * @params qualityReportSummary_paramDTO
     */
    public String standardCheck_GetCharacterAccuracyFormatCheckSql(int minFieldLength, int maxFieldLength, DataSourceTypeEnum dataSourceTypeEnum,
                                                                   QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        String f_Name = qualityReportSummary_paramDTO.getFieldNameFormat();
        String sql = "";

        if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "CHAR_LENGTH(SUBSTRING(CAST(" + f_Name + " AS STRING), INSTR(CAST(" + f_Name + " AS STRING), '.') + 1)) NOT BETWEEN " + minFieldLength + " AND " + maxFieldLength + ")\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "LEN(SUBSTRING(CAST(" + f_Name + " AS NVARCHAR), CHARINDEX('.', CAST(" + f_Name + " AS NVARCHAR)) + 1, LEN(CAST(" + f_Name + " AS NVARCHAR)))) NOT BETWEEN " + minFieldLength + " AND " + maxFieldLength + ")\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "LENGTH(SUBSTRING(CAST(" + f_Name + " AS TEXT) FROM POSITION('.' IN CAST(" + f_Name + " AS TEXT)) + 1)) NOT BETWEEN " + minFieldLength + " AND " + maxFieldLength + ")\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "CHAR_LENGTH(SUBSTRING(CAST(" + f_Name + " AS CHAR), LOCATE('.', CAST(" + f_Name + " AS CHAR)) + 1)) NOT BETWEEN " + minFieldLength + " AND " + maxFieldLength + ")\n";
        }
        return sql;
    }

    /**
     * @return java.lang.String
     * @description 规范检查-字符范围-字符长度范围检查
     * @author dick
     * @date 2024/7/18 11:34
     * @version v1.0
     * @params standardCheckTypeLengthOperator
     * @params standardCheckTypeLengthValue
     * @params dataSourceTypeEnum
     * @params qualityReportSummary_paramDTO
     */
    public String standardCheck_GetCharacterLengthFormatCheckSql(String standardCheckTypeLengthOperator, int standardCheckTypeLengthValue, DataSourceTypeEnum dataSourceTypeEnum,
                                                                 QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        String f_Name = qualityReportSummary_paramDTO.getFieldNameFormat();
        String sql = "";

        if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
            sql += String.format(" AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) %s %s)\n", standardCheckTypeLengthOperator, standardCheckTypeLengthValue);
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
            sql += String.format(" AND (LEN(ISNULL(" + f_Name + ", '')) %s %s)\n", standardCheckTypeLengthOperator, standardCheckTypeLengthValue);
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
            sql += String.format(" AND (LENGTH(COALESCE(" + f_Name + ", '')) %s %s)\n", standardCheckTypeLengthOperator, standardCheckTypeLengthValue);
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
            sql += String.format(" AND (CHAR_LENGTH(IFNULL(" + f_Name + ", '')) %s %s)\n", standardCheckTypeLengthOperator, standardCheckTypeLengthValue);
        }
        return sql;
    }

    /**
     * @return java.lang.String
     * @description 规范检查-URL格式检查
     * @author dick
     * @date 2024/7/18 11:34
     * @version v1.0
     * @params dataSourceTypeEnum
     * @params qualityReportSummary_paramDTO
     */
    public String standardCheck_GetURLFormatCheckSql(DataSourceTypeEnum dataSourceTypeEnum, QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        String f_Name = qualityReportSummary_paramDTO.getFieldNameFormat();
        String sql = "";

        if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "(LEFT(" + f_Name + ", 7) != 'http://' AND LEFT(" + f_Name + ", 8) != 'https://' AND LEFT(" + f_Name + ", 6) != 'ftp://'))\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "(" + f_Name + " NOT LIKE 'http://%' AND " + f_Name + " NOT LIKE 'https://%' AND " + f_Name + " NOT LIKE 'ftp://%'))\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "(" + f_Name + " !~ '^http://' AND " + f_Name + " !~ '^https://' AND " + f_Name + " !~ '^ftp://'))\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "(" + f_Name + " NOT LIKE 'http://%' AND " + f_Name + " NOT LIKE 'https://%' AND " + f_Name + " NOT LIKE 'ftp://%'))\n";
        }
        return sql;
    }

    /**
     * @return java.lang.String
     * @description 规范检查-Base64字节流检查
     * @author dick
     * @date 2024/7/18 11:43
     * @version v1.0
     * @params dataSourceTypeEnum
     * @params qualityReportSummary_paramDTO
     */
    public String standardCheck_GetBase64FormatCheckSql(DataSourceTypeEnum dataSourceTypeEnum, QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        String f_Name = qualityReportSummary_paramDTO.getFieldNameFormat();
        String sql = "";

        if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "(CHAR_LENGTH(" + f_Name + ") % 4 != 0 OR " +
                    "REGEXP_LIKE(" + f_Name + ", '[^A-Za-z0-9+/=]'))\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "(LEN(" + f_Name + ") % 4 != 0 OR " +
                    "PATINDEX('%[^A-Za-z0-9+/=]%', " + f_Name + ") > 0))\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "(LENGTH(" + f_Name + ") % 4 != 0 OR " +
                    f_Name + " ~ '[^A-Za-z0-9+/=]'))\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
            sql += " AND (" + f_Name + " IS NULL OR " +
                    "(CHAR_LENGTH(" + f_Name + ") % 4 != 0 OR " +
                    f_Name + " REGEXP '[^A-Za-z0-9+/=]'))\n";
        }
        return sql;
    }

    /**
     * @return com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportSummary_RuleDTO
     * @description 重复数据检查
     * @author dick
     * @date 2024/7/18 11:57
     * @version v1.0
     * @params templatePO
     * @params dataSourceConVO
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params qualityReportSummary_paramDTO
     */
    public QualityReportSummary_RuleDTO dataCheck_QualityReport_DuplicateDateCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO,
                                                                                   QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        // 重复数据检查
        QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = null;
        String t_Name = qualityReportSummary_paramDTO.getTableNameFormat(),
                tName = qualityReportSummary_paramDTO.getTableName(),
                f_Name = qualityReportSummary_paramDTO.getFieldNameFormat(),
                fName = qualityReportSummary_paramDTO.getFieldName(),
                fAllocate = qualityReportSummary_paramDTO.getAllocateFieldNames(),
                f_Allocate = qualityReportSummary_paramDTO.getAllocateFieldNamesFormat(),
                fieldCheckWhereSql = qualityReportSummary_paramDTO.getFieldCheckWhereSql();

        DataSourceTypeEnum dataSourceTypeEnum = dataSourceConVO.getConType();
        String ailsName = dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL ? "" + "数据重复条数" + "" : "'数据重复条数'";

        String sql_QueryCheckData = String.format("SELECT %s, COUNT(*) AS %s FROM %s WHERE 1=1 %s " +
                "GROUP BY %s HAVING COUNT(*) > 1 ", f_Name, ailsName, t_Name, fieldCheckWhereSql, f_Name);
        String sql_QueryDataTotalCount = String.format("SELECT COUNT(*) AS totalCount FROM %s WHERE 1=1 %s ", t_Name, fieldCheckWhereSql);
        String sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM ( %s ) t", sql_QueryCheckData);

        SheetDataDto sheetDataDto = new SheetDataDto();
        Integer errorDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryCheckErrorDataCount, "errorTotalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        if (errorDataTotalCount > 0 && errorDataTotalCount <= 5000) {
            sheetDataDto = qualityReport_QueryTableData_Sheet(dataSourceConVO, sql_QueryCheckData, dataCheckPO.getId(), dataCheckPO.getRuleName());
        }
        Integer checkDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryDataTotalCount, "totalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        qualityReportSummary_ruleDTO = dataCheck_QualityReportSummary_GetBasicInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO,
                qualityReportSummary_paramDTO, sheetDataDto, checkDataTotalCount, sql_QueryCheckData,
                sql_QueryDataTotalCount, errorDataTotalCount, sql_QueryCheckErrorDataCount);
        return qualityReportSummary_ruleDTO;
    }

    /**
     * @return com.fisk.common.core.utils.Dto.Excel.QualityReportSummary_RuleDTO
     * @description 波动检查
     * @author dick
     * @date 2024/7/18 12:03
     * @version v1.0
     * @params templatePO
     * @params dataSourceConVO
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params qualityReportSummary_paramDTO
     */
    public QualityReportSummary_RuleDTO dataCheck_QualityReport_FluctuationCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                                 DataCheckExtendPO dataCheckExtendPO, QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = null;
        String t_Name = qualityReportSummary_paramDTO.getTableNameFormat(),
                tName = qualityReportSummary_paramDTO.getTableName(),
                f_Name = qualityReportSummary_paramDTO.getFieldNameFormat(),
                fName = qualityReportSummary_paramDTO.getFieldName(),
                fAllocate = qualityReportSummary_paramDTO.getAllocateFieldNames(),
                f_Allocate = qualityReportSummary_paramDTO.getAllocateFieldNamesFormat(),
                fieldCheckWhereSql = qualityReportSummary_paramDTO.getFieldCheckWhereSql();

        String sql_QueryCheckData = "", sql_QueryDataTotalCount = "", sql_QueryCheckErrorDataCount = "";
        boolean isValid = true;
        double thresholdValue = dataCheckExtendPO.getFluctuateCheckValue();
        double realityValue = 0.0;
        FluctuateCheckTypeEnum fluctuateCheckTypeEnum = FluctuateCheckTypeEnum.getEnum(dataCheckExtendPO.getFluctuateCheckType());
        switch (fluctuateCheckTypeEnum) {
            case AVG:
                sql_QueryCheckData = String.format("SELECT AVG(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s ", f_Name, t_Name, fieldCheckWhereSql);
                break;
            case MIN:
                sql_QueryCheckData = String.format("SELECT MIN(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, fieldCheckWhereSql);
                break;
            case MAX:
                sql_QueryCheckData = String.format("SELECT MAX(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, fieldCheckWhereSql);
                break;
            case SUM:
                sql_QueryCheckData = String.format("SELECT SUM(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, fieldCheckWhereSql);
                break;
            case COUNT:
                sql_QueryCheckData = String.format("SELECT COUNT(%s) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, fieldCheckWhereSql);
                break;
        }

        List<Map<String, Object>> maps = qualityReport_QueryTableData_Maps(dataSourceConVO, sql_QueryCheckData, dataCheckPO.getId(), dataCheckPO.getRuleName());
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

        // 检查不通过，查询不通过的数据
        SheetDataDto sheetDataDto = new SheetDataDto();
        Integer errorDataTotalCount = 0;
        if (!isValid) {
            sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s ", f_Name, f_Allocate, t_Name, fieldCheckWhereSql);
            sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s ", t_Name, fieldCheckWhereSql);

            errorDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryCheckErrorDataCount, "errorTotalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
            if (errorDataTotalCount > 0 && errorDataTotalCount <= 5000) {
                sheetDataDto = qualityReport_QueryTableData_Sheet(dataSourceConVO, sql_QueryCheckData, dataCheckPO.getId(), dataCheckPO.getRuleName());
            }
        }
        sql_QueryDataTotalCount = String.format("SELECT COUNT(*) AS totalCount FROM %s WHERE 1=1 %s ", t_Name, fieldCheckWhereSql);
        Integer checkDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryDataTotalCount, "totalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        qualityReportSummary_ruleDTO = dataCheck_QualityReportSummary_GetBasicInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO,
                qualityReportSummary_paramDTO, sheetDataDto, checkDataTotalCount, sql_QueryCheckData,
                sql_QueryDataTotalCount, errorDataTotalCount, sql_QueryCheckErrorDataCount);
        return qualityReportSummary_ruleDTO;
    }

    /**
     * @return com.fisk.common.core.utils.Dto.Excel.QualityReportSummary_RuleDTO
     * @description 血缘检查
     * @author dick
     * @date 2024/7/18 14:58
     * @version v1.0
     * @params templatePO
     * @params dataSourceConVO
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params dataCheckSyncParamDTO
     */
    public QualityReportSummary_RuleDTO dataCheck_QualityReport_ParentageCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                               DataCheckExtendPO dataCheckExtendPO, QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = new QualityReportSummary_RuleDTO();
        return qualityReportSummary_ruleDTO;
    }

    /**
     * @return com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportSummary_RuleDTO
     * @description 正则表达式检查
     * @author dick
     * @date 2024/7/18 14:59
     * @version v1.0
     * @params templatePO
     * @params dataSourceConVO
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params dataCheckSyncParamDTO
     */
    public QualityReportSummary_RuleDTO dataCheck_QualityReport_RegexCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                           DataCheckExtendPO dataCheckExtendPO, QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = null;
        String t_Name = qualityReportSummary_paramDTO.getTableNameFormat(),
                tName = qualityReportSummary_paramDTO.getTableName(),
                f_Name = qualityReportSummary_paramDTO.getFieldNameFormat(),
                fName = qualityReportSummary_paramDTO.getFieldName(),
                fAllocate = qualityReportSummary_paramDTO.getAllocateFieldNames(),
                f_Allocate = qualityReportSummary_paramDTO.getAllocateFieldNamesFormat(),
                fieldCheckWhereSql = qualityReportSummary_paramDTO.getFieldCheckWhereSql();

        String sql_QueryCheckData = String.format("SELECT %s %s FROM %s WHERE 1=1 %s ", f_Name, f_Allocate, t_Name, fieldCheckWhereSql);
        String sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM %s WHERE 1=1 %s ", t_Name, fieldCheckWhereSql);
        String sql_QueryDataTotalCount = String.format("SELECT COUNT(*) AS totalCount FROM %s WHERE 1=1 %s ", t_Name, fieldCheckWhereSql);
        String sqlWhere = "";

        String regexpCheckValue = dataCheckExtendPO.getRegexpCheckValue();
        DataSourceTypeEnum dataSourceTypeEnum = dataSourceConVO.getConType();
        if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
            sqlWhere += " AND (NOT " + f_Name + " REGEXP '" + regexpCheckValue + "')\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
            // SQLSERVER数据库没有内置的正则表达式函数
            return qualityReportSummary_ruleDTO;
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
            sqlWhere += " AND (NOT " + f_Name + " ~ '" + regexpCheckValue + "')\n";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
            sqlWhere += " AND (NOT " + f_Name + " REGEXP '" + regexpCheckValue + "')\n";
        }
        sql_QueryCheckData += sqlWhere;
        sql_QueryCheckErrorDataCount += sqlWhere;

        SheetDataDto sheetDataDto = new SheetDataDto();
        Integer errorDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryCheckErrorDataCount, "errorTotalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        if (errorDataTotalCount > 0 && errorDataTotalCount <= 5000) {
            sheetDataDto = qualityReport_QueryTableData_Sheet(dataSourceConVO, sql_QueryCheckData, dataCheckPO.getId(), dataCheckPO.getRuleName());
        }
        Integer checkDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryDataTotalCount, "totalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
        qualityReportSummary_ruleDTO = dataCheck_QualityReportSummary_GetBasicInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO,
                qualityReportSummary_paramDTO, sheetDataDto, checkDataTotalCount, sql_QueryCheckData,
                sql_QueryDataTotalCount, errorDataTotalCount, sql_QueryCheckErrorDataCount);
        return qualityReportSummary_ruleDTO;
    }

    /**
     * @return com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportSummary_RuleDTO
     * @description SQL脚本检查
     * @author dick
     * @date 2024/7/18 15:54
     * @version v1.0
     * @params templatePO
     * @params dataSourceConVO
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params qualityReportSummary_paramDTO
     */
    public QualityReportSummary_RuleDTO dataCheck_QualityReport_SqlScriptCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                                               DataCheckExtendPO dataCheckExtendPO, QualityReportSummary_ParamDTO qualityReportSummary_paramDTO) {
        QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = null;

        String sql_QueryCheckData = dataCheckExtendPO.getSqlCheckValue();
        String sql_QueryCheckErrorDataCount = String.format("SELECT COUNT(*) AS errorTotalCount FROM ( %s ) t", sql_QueryCheckData);
        String sql_QueryDataTotalCount = String.format("SELECT COUNT(*) AS totalCount FROM ( %s ) t", sql_QueryCheckData);

        SheetDataDto sheetDataDto = new SheetDataDto();
        Integer errorDataTotalCount = 0;
        Integer checkDataTotalCount = 0;

        try {
            errorDataTotalCount = qualityReport_QueryTableTotalCount(dataSourceConVO, sql_QueryCheckErrorDataCount, "errorTotalCount", dataCheckPO.getId(), dataCheckPO.getRuleName());
            checkDataTotalCount = errorDataTotalCount;
        } catch (Exception ex) {
            log.error("【dataCheck_QualityReport_SqlScriptCheck】-嵌套查询错误数据总条数SQL执行异常，查询SQL：" + sql_QueryCheckData);
            log.error("【dataCheck_QualityReport_SqlScriptCheck】-嵌套查询错误数据总条数SQL执行异常，嵌套后SQL：" + sql_QueryCheckErrorDataCount);
            // 说明嵌套SQL执行异常，重新赋值，检查总条数和错误条数直接取检查结果条数
            sql_QueryCheckErrorDataCount = sql_QueryCheckData;
            sql_QueryDataTotalCount = sql_QueryCheckData;
        }

        // 错误数据条数小于5000，查询具体的错误明细数据
        if (errorDataTotalCount > 0 && errorDataTotalCount <= 5000) {
            sheetDataDto = qualityReport_QueryTableData_Sheet(dataSourceConVO, sql_QueryCheckData, dataCheckPO.getId(), dataCheckPO.getRuleName());
        }

        // 因为SQL脚本可能连表多条件查询，因此此处查询表数据条数也用检查的SQL
        qualityReportSummary_ruleDTO = dataCheck_QualityReportSummary_GetBasicInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO,
                qualityReportSummary_paramDTO, sheetDataDto, checkDataTotalCount, sql_QueryCheckData,
                sql_QueryDataTotalCount, errorDataTotalCount, sql_QueryCheckErrorDataCount);
        return qualityReportSummary_ruleDTO;
    }

    /**
     * @return com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportSummary_RuleDTO
     * @description 返回质量报告总结的基础信息
     * @author dick
     * @date 2024/7/23 20:03
     * @version v1.0
     * @params templatePO
     * @params dataSourceConVO
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params qualityReportSummary_paramDTO
     * @params sheetDataDto
     * @params checkDataTotalCount
     * @params sql_QueryCheckData
     * @params sql_QueryDataTotalCount
     * @params errorDataTotalCount
     * @params sql_QueryCheckErrorDataCount
     */
    public QualityReportSummary_RuleDTO dataCheck_QualityReportSummary_GetBasicInfo(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO,
                                                                                    QualityReportSummary_ParamDTO qualityReportSummary_paramDTO, SheetDataDto sheetDataDto, Integer checkDataTotalCount,
                                                                                    String sql_QueryCheckData, String sql_QueryDataTotalCount, Integer errorDataTotalCount,
                                                                                    String sql_QueryCheckErrorDataCount) {
        QualityReportSummary_RuleDTO qualityReportSummary_ruleDTO = new QualityReportSummary_RuleDTO();
        qualityReportSummary_ruleDTO.setRuleName(dataCheckPO.getRuleName());
        qualityReportSummary_ruleDTO.setRuleDescribe(dataCheckPO.getRuleDescribe());
        qualityReportSummary_ruleDTO.setTableFullName(qualityReportSummary_paramDTO.getTableName());
        qualityReportSummary_ruleDTO.setFieldName(dataCheckExtendPO.getFieldName());
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        qualityReportSummary_ruleDTO.setRuleTemplate(templateTypeEnum.getName());
        qualityReportSummary_ruleDTO.setRuleIllustrate(dataCheckPO.getRuleIllustrate());
        double checkDataAccuracy = 0.00;
        String checkStatus = errorDataTotalCount == 0 ? "通过" : "不通过";
        if (checkDataTotalCount == 0) {
            if (templatePO.getTemplateType() == TemplateTypeEnum.SQL_SCRIPT_CHECK.getValue()) {
                // SQL脚本检查，检查的数据总条数为0也就表示检查出来的错误数据为0条，设置为检查通过
                checkDataAccuracy = 100.00;
                checkStatus = "通过";
            } else {
                checkDataAccuracy = 0.00;
                checkStatus = "表结果集为空，跳过检查";
            }
        } else {
            double proportion = 100;
            checkDataAccuracy = (Double.parseDouble(checkDataTotalCount.toString())
                    - Double.parseDouble(errorDataTotalCount.toString()))
                    / Double.parseDouble(checkDataTotalCount.toString()) * proportion;
        }
        // 保留两位小数，不进行四舍五入
        BigDecimal bigDecimal_CheckDataAccuracy = new BigDecimal(checkDataAccuracy).setScale(2, BigDecimal.ROUND_DOWN);

        if (checkStatus != "通过") {
            qualityReportSummary_ruleDTO.setUserComment(qualityReportSummary_paramDTO.getUserComment());
        }

        qualityReportSummary_ruleDTO.setCheckDataCount(checkDataTotalCount);
        qualityReportSummary_ruleDTO.setCheckErrorDataCount(errorDataTotalCount);
        qualityReportSummary_ruleDTO.setDataAccuracy(bigDecimal_CheckDataAccuracy.toString() + "%");
        qualityReportSummary_ruleDTO.setCheckStatus(checkStatus);
        qualityReportSummary_ruleDTO.setSheetData(sheetDataDto);
        qualityReportSummary_ruleDTO.setCheckDataSql(sql_QueryCheckData);
        qualityReportSummary_ruleDTO.setCheckTotalCountSql(sql_QueryDataTotalCount);
        qualityReportSummary_ruleDTO.setCheckErrorDataCountSql(sql_QueryCheckErrorDataCount);
        return qualityReportSummary_ruleDTO;
    }

    /**
     * @return java.lang.String
     * @description 为架构/表/字段名称添加转义符
     * @author dick
     * @date 2024/7/17 14:18
     * @version v1.0
     * @params dataSourceTypeEnum
     * @params fieldName
     */
    public String qualityReport_GetSqlFieldFormat(DataSourceTypeEnum dataSourceTypeEnum, String fieldName) {
        String sqlFieldStr = dataSourceTypeEnum == DataSourceTypeEnum.MYSQL
                ? "`%s`" :
                dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER
                        ? "[%s]" :
                        dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL
                                ? "\"%s\"" :
                                dataSourceTypeEnum == DataSourceTypeEnum.DORIS
                                        ? "`%s`" : "%s";
        sqlFieldStr = String.format(sqlFieldStr, fieldName);
        return sqlFieldStr;
    }

    /**
     * @return java.lang.String
     * @description 为架构/表/字段名称添加转义符，多个值逗号分隔
     * @author dick
     * @date 2024/7/17 14:16
     * @version v1.0
     * @params dataSourceTypeEnum
     * @params fieldNames
     */
    public String qualityReport_GetSqlFieldFormat_Separate(DataSourceTypeEnum dataSourceTypeEnum, String fieldNames) {
        List<String> fieldNameList = Arrays.asList(fieldNames.split(","));
        String fileNameFormat = "";
        for (String fieldName : fieldNameList) {
            fileNameFormat += qualityReport_GetSqlFieldFormat(dataSourceTypeEnum, fieldName) + ",";
        }
        // 去掉最后一个逗号
        fileNameFormat = fileNameFormat.substring(0, fileNameFormat.length() - 1);
        return fileNameFormat;
    }

    /**
     * @return java.lang.String
     * @description 以AND格式拼接检查条件
     * @author dick
     * @date 2024/7/17 14:15
     * @version v1.0
     * @params dataSourceTypeEnum
     * @params dataCheckConditionPOS
     */
    public String qualityReport_GetSqlFieldCheckWhere(DataSourceTypeEnum dataSourceTypeEnum, List<DataCheckConditionPO> dataCheckConditionPOS) {
        String sqlWhere = "";
        if (CollectionUtils.isNotEmpty(dataCheckConditionPOS)) {
            for (DataCheckConditionPO dataCheckConditionPO : dataCheckConditionPOS) {
                if (StringUtils.isEmpty(dataCheckConditionPO.getFieldName())) {
                    continue;
                }
                String sqlWhereStr = qualityReport_GetSqlFieldWhereFormat(dataSourceTypeEnum,
                        dataCheckConditionPO.getFieldOperator(), false);
                sqlWhere += " AND " + String.format(sqlWhereStr, dataCheckConditionPO.getFieldName(), dataCheckConditionPO.getFieldValue());
            }
            if (StringUtils.isNotEmpty(sqlWhere)) {
                sqlWhere = " AND ( " + sqlWhere.substring(5) + " ) ";
            }
        }
        return sqlWhere;
    }

    /**
     * @return java.lang.String
     * @description 返回格式化后的键值
     * @author dick
     * @date 2024/7/17 14:14
     * @version v1.0
     * @params dataSourceTypeEnum
     * @params operator
     */
    public String qualityReport_GetSqlFieldWhereFormat(DataSourceTypeEnum dataSourceTypeEnum,
                                                       String operator, boolean setQuotationMarks) {
        String sqlWhereStr = dataSourceTypeEnum == DataSourceTypeEnum.MYSQL
                ? "`%s`" + operator + "'%s'" :
                dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER
                        ? "[%s]" + operator + "'%s'" :
                        dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL
                                ? "\"%s\"" + operator + "'%s'" :
                                dataSourceTypeEnum == DataSourceTypeEnum.DORIS && setQuotationMarks
                                        ? "`%s`" + operator + "'%s'" :
                                        dataSourceTypeEnum == DataSourceTypeEnum.DORIS && !setQuotationMarks
                                                ? "`%s`" + operator + "%s" : "%s" + operator + "'%s'";
        return sqlWhereStr;
    }

    /**
     * @return java.lang.Integer
     * @description 执行SQL返回数据总条数
     * @author dick
     * @date 2024/7/25 14:36
     * @version v1.0
     * @params dataSourceConVO
     * @params sql
     * @params fieldName
     * @params ruleId
     * @params ruleName
     */
    public Integer qualityReport_QueryTableTotalCount(DataSourceConVO dataSourceConVO, String sql, String fieldName,
                                                      long ruleId, String ruleName) {

        DataSourceTypeEnum dataSourceTypeEnum = dataSourceConVO.getConType();
        // PGSQL中，查询的列不加引号默认都会被转小写，比如 SELECT totalCount就会输出为totalcount
        if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL
                && (fieldName.equals("totalCount") || fieldName.equals("errorTotalCount"))) {
            fieldName = fieldName.toLowerCase();
        }
        String totalCount = "0";
        List<Map<String, Object>> mapList = qualityReport_QueryTableData_Maps(dataSourceConVO, sql, ruleId, ruleName);
        if (CollectionUtils.isNotEmpty(mapList)) {
            totalCount = mapList.get(0).get(fieldName).toString();
        }
        return Integer.valueOf(totalCount);
    }

    /**
     * @return java.lang.String
     * @description 获取反向运算符
     * @author dick
     * @date 2024/7/30 14:20
     * @version v1.0
     * @params operator
     */
    public String qualityReport_GetReverseOperator(String operator) {
        String reverseOperator = operator;
        if (StringUtils.isEmpty(operator)) {
            return reverseOperator;
        }
        if (operator.equals("=")) {
            reverseOperator = "!=";
        } else if (operator.equals("!=")) {
            reverseOperator = "=";
        } else if (operator.equals(">")) {
            reverseOperator = "<=";
        } else if (operator.equals(">=")) {
            reverseOperator = "<";
        } else if (operator.equals("<")) {
            reverseOperator = ">=";
        } else if (operator.equals("<=")) {
            reverseOperator = ">";
        }
        return reverseOperator;
    }

    /**
     * @return java.util.List<com.fisk.common.core.utils.Dto.Excel.RowDto>
     * @description 检查规则粒度-获取规则质量报告标识行
     * @author dick
     * @date 2024/7/18 16:43
     * @version v1.0
     * @params tableName
     * @params templateName
     * @params fields
     * @params ruleIllustrate
     * @params ruleDescribe
     */
    public List<RowDto> dataCheck_QualityReport_GetRuleSingRows(QualityReportSummary_RuleDTO qualityReportSummaryRuleDTO) {
        List<RowDto> singRows = new ArrayList<>();
        RowDto rowDto = new RowDto();
        rowDto.setRowIndex(0);
        List<String> columns = new ArrayList<>();
        columns.add("检查的表名称");
        columns.add("检查字段名称");
        columns.add("检查规则名称");
        columns.add("检查规则描述");
        columns.add("检查规则类型");
        columns.add("检查数据条数");
        columns.add("数据的正确率");
        columns.add("是否通过检查");
        columns.add("用户评语");
        rowDto.setColumns(columns);
        singRows.add(rowDto);

        rowDto = new RowDto();
        rowDto.setRowIndex(1);
        columns = new ArrayList<>();
        columns.add(qualityReportSummaryRuleDTO.getTableFullName());
        columns.add(qualityReportSummaryRuleDTO.getFieldName());
        columns.add(qualityReportSummaryRuleDTO.getRuleName());
        columns.add(qualityReportSummaryRuleDTO.getRuleDescribe());
        columns.add(qualityReportSummaryRuleDTO.getRuleTemplate());
        columns.add(String.valueOf(qualityReportSummaryRuleDTO.getCheckDataCount()));
        columns.add(qualityReportSummaryRuleDTO.getDataAccuracy());
        columns.add(qualityReportSummaryRuleDTO.getCheckStatus());
        columns.add(qualityReportSummaryRuleDTO.getUserComment());
        rowDto.setColumns(columns);
        singRows.add(rowDto);

        rowDto = new RowDto();
        rowDto.setRowIndex(4);
        columns = new ArrayList<>();
        columns.add("检查不通过的数据明细");
        rowDto.setColumns(columns);
        singRows.add(rowDto);

        rowDto = new RowDto();
        rowDto.setRowIndex(5);
        columns = new ArrayList<>();
        columns.addAll(qualityReportSummaryRuleDTO.getSheetData().getColumns());
        rowDto.setColumns(columns);
        singRows.add(rowDto);
        return singRows;
    }

    /**
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @description 执行SQL返回Maps
     * @author dick
     * @date 2024/7/25 14:40
     * @version v1.0
     * @params dataSourceConVO
     * @params sql
     * @params ruleId
     * @params ruleName
     */
    public List<Map<String, Object>> qualityReport_QueryTableData_Maps(DataSourceConVO dataSourceConVO, String sql,
                                                                       long ruleId, String ruleName) {
        log.info(String.format("【qualityReport_QueryTableData_Maps】执行SQL返回Maps，规则id：%s、规则名称：%s、SQL语句：%s：", ruleId, ruleName, sql));
        // 实时建立数据库连接实时释放，防止连接等待时间过长导致超时异常
        Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        List<Map<String, Object>> mapList = AbstractCommonDbHelper.execQueryResultMaps(sql, connection);
        return mapList;
    }

    /**
     * @return com.fisk.common.core.utils.Dto.Excel.SheetDataDto
     * @description 执行SQL返回Sheet
     * @author dick
     * @date 2024/7/25 14:39
     * @version v1.0
     * @params dataSourceConVO
     * @params sql
     * @params ruleId
     * @params ruleName
     */
    public SheetDataDto qualityReport_QueryTableData_Sheet(DataSourceConVO dataSourceConVO, String sql,
                                                           long ruleId, String ruleName) {
        log.info(String.format("【qualityReport_QueryTableData_Sheet】执行SQL返回Sheet，规则id：%s、规则名称：%s、SQL语句：%s：", ruleId, ruleName, sql));
        // 实时建立数据库连接实时释放，防止连接等待时间过长导致超时异常
        Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        SheetDataDto sheetDataDto = AbstractCommonDbHelper.execQueryResultSheet(sql, connection);
        return sheetDataDto;
    }

    /**
     * @return com.alibaba.fastjson.JSONArray
     * @description 执行SQL返回JSONArray
     * @author dick
     * @date 2024/7/25 14:39
     * @version v1.0
     * @params dataSourceConVO
     * @params sql
     * @params ruleId
     * @params ruleName
     */
    public JSONArray qualityReport_QueryTableData_Array(DataSourceConVO dataSourceConVO, String sql,
                                                        long ruleId, String ruleName) {
        log.info(String.format("【qualityReport_QueryTableData_Array】执行SQL返回JSONArray，规则id：%s、规则名称：%s、SQL语句：%s：", ruleId, ruleName, sql));
        // 实时建立数据库连接实时释放，防止连接等待时间过长导致超时异常
        Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        JSONArray jsonArray = AbstractCommonDbHelper.execQueryResultArrays(sql, connection);
        return jsonArray;
    }

    /**
     * @return void
     * @description 检查报告保存的路径是否存在，不存在则创建
     * @author dick
     * @date 2024/7/19 17:24
     * @version v1.0
     * @params directoryPath
     */
    public void isExistsCreateDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        // 判断路径是否存在
        if (!directory.exists()) {
            // 如果不存在，则创建目录
            directory.mkdirs();
        }
    }
}

