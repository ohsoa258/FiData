package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.CronUtils;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.datagovernance.dto.GetConfigDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.*;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.map.dataquality.QualityReportMap;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IQualityReportManageService;
import com.fisk.datagovernance.vo.dataquality.qualityreport.*;
import com.fisk.system.client.UserClient;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.UnifiedControlDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告
 * @date 2022/3/23 12:56
 */
@Service
@Slf4j
public class QualityReportManageImpl extends ServiceImpl<QualityReportMapper, QualityReportPO> implements IQualityReportManageService {
    @Resource
    private DataQualityClientManageImpl dataQualityClientManage;

    @Resource
    private QualityReportRuleManageImpl qualityReportRuleManage;

    @Resource
    private QualityReportRuleMapper qualityReportRuleMapper;

    @Resource
    private QualityReportLogMapper qualityReportLogMapper;

    @Resource
    private DataCheckMapper dataCheckMapper;

    @Resource
    private BusinessFilterMapper businessFilterMapper;

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private AttachmentInfoMapper attachmentInfoMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private UserHelper userHelper;

    @Resource
    private GetMetadata getMetadata;

    @Resource
    private GenerateCondition generateCondition;

    @Resource
    private GetConfigDTO getConfig;

    @Override
    public Page<QualityReportVO> getAll(QualityReportQueryDTO query) {
        Page<QualityReportVO> all = null;
        try {
            StringBuilder querySql = new StringBuilder();
            // 拼接原生筛选条件
            querySql.append(generateCondition.getCondition(query.dto));
            QualityReportPageDTO data = new QualityReportPageDTO();
            data.page = query.page;
            // 筛选器左边的模糊搜索查询SQL拼接
            data.where = querySql.toString();
            all = baseMapper.getAll(query.page, data);
            if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
                List<Integer> reportIdList = all.getRecords().stream().map(QualityReportVO::getId).collect(Collectors.toList());
                QueryWrapper<QualityReportRulePO> qualityReportRulePOQueryWrapper = new QueryWrapper<>();
                qualityReportRulePOQueryWrapper.lambda().eq(QualityReportRulePO::getDelFlag, 1)
                        .in(QualityReportRulePO::getReportId, reportIdList);
                List<QualityReportRulePO> qualityReportRulePOS = qualityReportRuleMapper.selectList(qualityReportRulePOQueryWrapper);
                all.getRecords().forEach(t -> {
                    if (StringUtils.isNotEmpty(t.getRunTimeCron())) {
                        String cronExpress = CronUtils.getCronExpress(t.getRunTimeCron());
                        Date date = null;
                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cronExpress);
                        } catch (ParseException e) {
                            log.error("【getAll】 时间转换异常：" + e);
                        }
                        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
                        t.setNextRunTime(now);
                    }
                    if (CollectionUtils.isNotEmpty(qualityReportRulePOS)) {
                        List<Integer> ruleIds = qualityReportRulePOS.stream().filter(rule -> rule.getReportId() == t.getId()).map(QualityReportRulePO::getRuleId).collect(Collectors.toList());
                        t.setRules(ruleIds);
                    }
                });
            }
        } catch (Exception ex) {
            log.error("【getAll】 质量报告执行异常：" + ex);
            throw new FkException(ResultEnum.ERROR, "【getAll】 ex：" + ex);
        }
        return all;
    }

    @Override
    public List<FilterFieldDTO> getColumn() {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.driver = getConfig.driver;
        dto.tableName = "tb_quality_report";
        dto.filterSql = FilterSqlConstants.DATA_GOVERNANCE_QUALITY_REPORT_SQL;
        return getMetadata.getMetadataList(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(QualityReportDTO dto) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        //第一步：转换DTO对象为PO对象
        QualityReportPO qualityReportPO = QualityReportMap.INSTANCES.dtoToPo(dto);
        if (qualityReportPO == null) {
            return ResultEnum.PARAMTER_ERROR;
        }
        //第二步：保存质量报告配置
        qualityReportPO.setCreateTime(LocalDateTime.now());
        qualityReportPO.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
        int i = baseMapper.insertOne(qualityReportPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存质量报告规则配置
        if (CollectionUtils.isNotEmpty(dto.getQualityReportRule())) {
            List<QualityReportRulePO> qualityReportRulePOS = new ArrayList<>();
            dto.getQualityReportRule().forEach(t -> {
                QualityReportRulePO qualityReportRulePO = new QualityReportRulePO();
                qualityReportRulePO.setReportId(Math.toIntExact(qualityReportPO.id));
                qualityReportRulePO.setRuleId(t.ruleId);
                qualityReportRulePOS.add(qualityReportRulePO);
            });
            qualityReportRuleManage.saveBatch(qualityReportRulePOS);
        }
        //第四步：保存调度任务
        publishBuild_unifiedControlTask(Math.toIntExact(qualityReportPO.getId()), qualityReportPO.getReportState(),
                qualityReportPO.getRunTimeCron());
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(QualityReportEditDTO dto) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        QualityReportPO qualityReportPO = baseMapper.selectById(dto.id);
        if (qualityReportPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //第一步：转换DTO对象为PO对象
        qualityReportPO = QualityReportMap.INSTANCES.dtoToPo_Edit(dto);
        if (qualityReportPO == null) {
            return ResultEnum.PARAMTER_ERROR;
        }
        //第二步：保存质量报告配置
        int id = Math.toIntExact(qualityReportPO.id);
        int i = baseMapper.updateById(qualityReportPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存质量报告规则配置
        if (CollectionUtils.isNotEmpty(dto.getQualityReportRule())) {
            qualityReportRuleMapper.updateByReportId(id);
            List<QualityReportRulePO> qualityReportRulePOS = new ArrayList<>();
            dto.getQualityReportRule().forEach(t -> {
                QualityReportRulePO qualityReportRulePO = new QualityReportRulePO();
                qualityReportRulePO.setReportId(id);
                qualityReportRulePO.setRuleId(t.ruleId);
                qualityReportRulePOS.add(qualityReportRulePO);
            });
            qualityReportRuleManage.saveBatch(qualityReportRulePOS);
        }
        //第四步：保存调度任务
        publishBuild_unifiedControlTask(Math.toIntExact(qualityReportPO.getId()), qualityReportPO.getReportState(), qualityReportPO.getRunTimeCron());
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editState(int id) {
        ResultEnum resultEnum;
        if (id == 0) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        QualityReportPO qualityReportPO = baseMapper.selectById(id);
        if (qualityReportPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        qualityReportPO.setReportState(qualityReportPO.getReportState() == 1 ? 0 : 1);
        resultEnum = baseMapper.updateById(qualityReportPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        publishBuild_unifiedControlTask(Math.toIntExact(qualityReportPO.getId()), qualityReportPO.getReportState(), qualityReportPO.getRunTimeCron());
        return resultEnum;
    }

    @Override
    public ResultEnum deleteData(int id) {
        ResultEnum resultEnum;
        if (id == 0) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        QualityReportPO qualityReportPO = baseMapper.selectById(id);
        if (qualityReportPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        qualityReportRuleMapper.updateByReportId(id);
        resultEnum = baseMapper.deleteByIdWithFill(qualityReportPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        publishBuild_unifiedControlTask(Math.toIntExact(qualityReportPO.getId()), RuleStateEnum.Disable.getValue(), qualityReportPO.getRunTimeCron());
        return resultEnum;
    }

    @Override
    public ResultEnum collReport(int id) {
        if (id == 0) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        QualityReportPO qualityReportPO = baseMapper.selectById(id);
        if (qualityReportPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        ResultEntity<Object> qualityReport = dataQualityClientManage.createQualityReport(id);
        return ResultEnum.getEnum(qualityReport.code);
    }

    @Override
    public QualityReportExtVO getReportExt() {
        QualityReportExtVO qualityReportExtVO = new QualityReportExtVO();
        List<QualityReportExtMapVO> cRules = new ArrayList<>();
        List<QualityReportExtMapVO> bRules = new ArrayList<>();
        List<QualityReportExtMapVO> emails = new ArrayList<>();

        // 第一步：查询报告模板
        List<Integer> moduleType = new ArrayList<>();
        moduleType.add(ModuleTypeEnum.DATACHECK_MODULE.getValue());
        moduleType.add(ModuleTypeEnum.BIZCHECK_MODULE.getValue());
        List<Integer> templateScene = new ArrayList<>();
        templateScene.add(TemplateSceneEnum.DATACHECK_QUALITYREPORT.getValue());
        templateScene.add(TemplateSceneEnum.BUSINESSFILTER_FILTERREPORT.getValue());
        QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
        templatePOQueryWrapper.lambda().eq(TemplatePO::getDelFlag, 1)
                .in(TemplatePO::getModuleType, moduleType)
                .in(TemplatePO::getTemplateScene, templateScene);
        List<TemplatePO> templatePOS = templateMapper.selectList(templatePOQueryWrapper);
        if (CollectionUtils.isNotEmpty(templatePOS)) {
            // 第二步：查询报告模板生成的报告规则
            List<Long> templateIdList = templatePOS.stream().filter(t -> t.getModuleType() == ModuleTypeEnum.DATACHECK_MODULE.getValue()).map(TemplatePO::getId).collect(Collectors.toList());
            QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
            dataCheckPOQueryWrapper.lambda().eq(DataCheckPO::getDelFlag, 1)
                    .eq(DataCheckPO::getRuleState, RuleStateEnum.Enable.getValue())
                    .in(DataCheckPO::getTemplateId, templateIdList);
            List<DataCheckPO> dataCheckPOList = dataCheckMapper.selectList(dataCheckPOQueryWrapper);
            dataCheckPOList.forEach(t -> {
                QualityReportExtMapVO cRule = new QualityReportExtMapVO();
                cRule.setId(t.getId());
                cRule.setName(t.getRuleName());
                cRules.add(cRule);
            });

            templateIdList = templatePOS.stream().filter(t -> t.getModuleType() == ModuleTypeEnum.BIZCHECK_MODULE.getValue()).map(TemplatePO::getId).collect(Collectors.toList());
            QueryWrapper<BusinessFilterPO> businessFilterPOQueryWrapper = new QueryWrapper<>();
            businessFilterPOQueryWrapper.lambda().eq(BusinessFilterPO::getDelFlag, 1)
                    .eq(BusinessFilterPO::getRuleState, RuleStateEnum.Enable.getValue())
                    .in(BusinessFilterPO::getTemplateId, templateIdList);
            List<BusinessFilterPO> businessFilterPOS = businessFilterMapper.selectList(businessFilterPOQueryWrapper);
            businessFilterPOS.forEach(t -> {
                QualityReportExtMapVO bRule = new QualityReportExtMapVO();
                bRule.setId(t.getId());
                bRule.setName(t.getRuleName());
                bRules.add(bRule);
            });

        }

        // 第三步：查询邮件服务配置
        ResultEntity<List<EmailServerVO>> emailServerList = userClient.getEmailServerList();
        if (emailServerList != null && emailServerList.getCode() == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(emailServerList.getData())) {
            emailServerList.getData().forEach(t -> {
                QualityReportExtMapVO email = new QualityReportExtMapVO();
                email.setId(Long.valueOf(t.getId()));
                email.setName(t.getName());
                emails.add(email);
            });
        }
        qualityReportExtVO.setRules_c(cRules);
        qualityReportExtVO.setRules_b(bRules);
        qualityReportExtVO.setEmails(emails);
        return qualityReportExtVO;
    }

    @Override
    public Page<QualityReportLogVO> getAllReportLog(QualityReportLogQueryDTO dto) {
        Page<QualityReportLogVO> all = qualityReportLogMapper.getAll(dto.getPage(), dto.getReportId(), dto.getKeyword());
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<Integer> categoryList = new ArrayList<>();
            categoryList.add(100); // 质量校验报告
            categoryList.add(200); // 数据清洗报告
            List<String> reportLogIdList = all.getRecords().stream().map(QualityReportLogVO::getId).collect(Collectors.toList());
            QueryWrapper<AttachmentInfoPO> attachmentInfoPOQueryWrapper = new QueryWrapper<>();
            attachmentInfoPOQueryWrapper.lambda().eq(AttachmentInfoPO::getDelFlag, 1)
                    .in(AttachmentInfoPO::getCategory, categoryList)
                    .in(AttachmentInfoPO::getObjectId, reportLogIdList);
            List<AttachmentInfoPO> attachmentInfoPOS = attachmentInfoMapper.selectList(attachmentInfoPOQueryWrapper);
            if (CollectionUtils.isNotEmpty(attachmentInfoPOS)) {
                for (QualityReportLogVO qualityReportLogVO : all.getRecords()) {
                    AttachmentInfoPO attachmentInfoPO = attachmentInfoPOS.stream().filter(t -> t.getObjectId().equals(qualityReportLogVO.getId())).findFirst().orElse(null);
                    if (attachmentInfoPO != null && StringUtils.isNotEmpty(attachmentInfoPO.getAbsolutePath())) {
                        String filePath = attachmentInfoPO.getAbsolutePath() + File.separator + attachmentInfoPO.getCurrentFileName();
                        log.info("【getAllReportLog】文件路径：" + filePath);
                        File file = new File(filePath);
                        if (file.exists()) {
                            qualityReportLogVO.setExistReport(true);
                        }
                    }
                }
            }
        }
        return all;
    }

    @Override
    public HttpServletResponse downloadReportRecord(int reportLogId, HttpServletResponse response) {
        try {
            if (reportLogId == 0) {
                return response;
            }
            List<Integer> categoryList = new ArrayList<>();
            categoryList.add(100); // 质量校验报告
            categoryList.add(200); // 数据清洗报告
            QueryWrapper<AttachmentInfoPO> attachmentInfoPOQueryWrapper = new QueryWrapper<>();
            attachmentInfoPOQueryWrapper.lambda().eq(AttachmentInfoPO::getDelFlag, 1)
                    .in(AttachmentInfoPO::getCategory, categoryList)
                    .eq(AttachmentInfoPO::getObjectId, reportLogId);
            AttachmentInfoPO attachmentInfoPO = attachmentInfoMapper.selectOne(attachmentInfoPOQueryWrapper);
            if (attachmentInfoPO == null) {
                return response;
            }
            String filePath = attachmentInfoPO.getAbsolutePath() + File.separator + attachmentInfoPO.getCurrentFileName();
            log.info("【downloadReportRecord】文件路径：" + filePath);
            File file = new File(filePath);
            // 取得文件名
            String filename = attachmentInfoPO.getOriginalName();
            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(filePath));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    @Override
    public List<PreviewQualityReportVO> previewReportRecord(int reportLogId) {
        return null;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.Object>
     * @description 发送邮件报告
     * @author dick
     * @date 2022/11/29 17:49
     * @version v1.0
     * @params dto
     */
    public ResultEntity<Object> sendEmailReport(QualityReportDTO dto) {
        //第一步：查询邮件服务器设置
        ResultEntity<EmailServerVO> emailServerById = userClient.getEmailServerById(dto.getEmailServerId());
        if (emailServerById == null || emailServerById.getCode() != ResultEnum.SUCCESS.getCode() ||
                emailServerById.getData() == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_NOTEXISTS, "邮件服务器不存在");
        }
        EmailServerVO emailServerVO = emailServerById.getData();
        MailServeiceDTO mailServeiceDTO = new MailServeiceDTO();
        mailServeiceDTO.setOpenAuth(true);
        mailServeiceDTO.setOpenDebug(true);
        mailServeiceDTO.setHost(emailServerVO.getEmailServer());
        mailServeiceDTO.setProtocol(emailServerVO.getEmailServerType().getName());
        mailServeiceDTO.setUser(emailServerVO.getEmailServerAccount());
        mailServeiceDTO.setPassword(emailServerVO.getEmailServerPwd());
        mailServeiceDTO.setPort(emailServerVO.getEmailServerPort());
        MailSenderDTO mailSenderDTO = new MailSenderDTO();
        mailSenderDTO.setUser(emailServerVO.getEmailServerAccount());
        mailSenderDTO.setSubject(dto.getEmailSubject());
        mailSenderDTO.setBody(dto.getBody());
        mailSenderDTO.setToAddress(dto.getEmailConsignee());
        mailSenderDTO.setToCc(dto.getEmailCc());
        mailSenderDTO.setSendAttachment(dto.sendAttachment);
        mailSenderDTO.setAttachmentName(dto.getAttachmentName());
        mailSenderDTO.setAttachmentPath(dto.getAttachmentPath());
        mailSenderDTO.setAttachmentActualName(dto.getAttachmentActualName());
        mailSenderDTO.setCompanyLogoPath(dto.getCompanyLogoPath());
        try {
            //第二步：调用邮件发送方法
            MailSenderUtils.send(mailServeiceDTO, mailSenderDTO);
        } catch (Exception ex) {
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "发送完成");
    }

    /**
     * @return ResultEnum
     * @description 调用task服务提供的API，创建调度任务
     * @author dick
     * @date 2022/4/8 10:59
     * @version v1.0
     * @params id 组件id
     * @params stateEnum 状态
     */
    public ResultEnum publishBuild_unifiedControlTask(int id, int state, String cron) {
        ResultEnum resultEnum = ResultEnum.TASK_NIFI_DISPATCH_ERROR;
        try {
            //调用task服务提供的API生成调度任务
            if (id == 0) {
                return ResultEnum.SAVE_VERIFY_ERROR;
            }
            long userId = userHelper.getLoginUserInfo().getId();
            boolean isDelTask = state != RuleStateEnum.Enable.getValue();
            if (StringUtils.isEmpty(cron)) {
                isDelTask = true;
            }
            UnifiedControlDTO unifiedControlDTO = new UnifiedControlDTO();
            unifiedControlDTO.setUserId(userId);
            unifiedControlDTO.setId(id);
            unifiedControlDTO.setScheduleType(SchedulingStrategyTypeEnum.CRON);
            unifiedControlDTO.setScheduleExpression(cron);
            unifiedControlDTO.setTopic(MqConstants.QueueConstants.BUILD_GOVERNANCE_TEMPLATE_FLOW);
            unifiedControlDTO.setType(OlapTableEnum.GOVERNANCE);
            unifiedControlDTO.setDataClassifyEnum(DataClassifyEnum.UNIFIEDCONTROL);
            unifiedControlDTO.setDeleted(isDelTask);
            log.info("【publishBuild_unifiedControlTask】创建nifi调度任务请求参数：" + JSON.toJSONString(unifiedControlDTO));
            ResultEntity<Object> result = publishTaskClient.publishBuildunifiedControlTask(unifiedControlDTO);
            if (result != null) {
                resultEnum = ResultEnum.getEnum(result.getCode());
            }
        } catch (Exception ex) {
            log.error("【publishBuild_unifiedControlTask】ex：" + ex);
            return ResultEnum.SUCCESS;
        }
        return resultEnum;
    }
}