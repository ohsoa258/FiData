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
import com.fisk.common.core.utils.Dto.cron.NextCronTimeDTO;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.datagovernance.dto.GetConfigDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckRulePageDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.*;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.map.dataquality.QualityReportMap;
import com.fisk.datagovernance.map.dataquality.QualityReportNoticeMap;
import com.fisk.datagovernance.map.dataquality.QualityReportRecipientMap;
import com.fisk.datagovernance.map.dataquality.QualityReportRuleMap;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IQualityReportManageService;
import com.fisk.datagovernance.vo.appcount.AppRuleCountVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckExtendVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.*;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.UnifiedControlDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
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
    private QualityReportRecipientMapper qualityReportRecipientMapper;

    @Resource
    private QualityReportRecipientManageImpl qualityReportRecipientManage;

    @Resource
    private QualityReportNoticeMapper qualityReportNoticeMapper;

    @Resource
    private DataSourceConManageImpl dataSourceConManage;

    @Resource
    private DataCheckExtendMapper dataCheckExtendMapper;

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
                // 数据源集合
                List<DataSourceConVO> allDataSource = dataSourceConManage.getAllDataSource();

                // 质量报告Id集合
                List<Integer> reportIdList = all.getRecords().stream().map(QualityReportVO::getId).collect(Collectors.toList());

                // 质量报告下的质量规则
                QueryWrapper<QualityReportRulePO> qualityReportRulePOQueryWrapper = new QueryWrapper<>();
                qualityReportRulePOQueryWrapper.lambda().eq(QualityReportRulePO::getDelFlag, 1)
                        .in(QualityReportRulePO::getReportId, reportIdList);
                List<QualityReportRulePO> qualityReportRulePOS = qualityReportRuleMapper.selectList(qualityReportRulePOQueryWrapper);

                // 质量报告下的质量规则的详细信息
                List<DataCheckPO> dataCheckPOList = null;
                List<DataCheckExtendPO> dataCheckExtendPOList = null;
                if (CollectionUtils.isNotEmpty(qualityReportRulePOS)) {
                    // 查询质量报告下的质量规则详细信息
                    List<Integer> dataCheck_RuleIdList = qualityReportRulePOS.stream().filter(t -> t.getReportType() == 100).map(QualityReportRulePO::getRuleId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(dataCheck_RuleIdList)) {
                        QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
                        dataCheckPOQueryWrapper.lambda().eq(DataCheckPO::getDelFlag, 1)
                                .in(DataCheckPO::getId, dataCheck_RuleIdList);
                        dataCheckPOList = dataCheckMapper.selectList(dataCheckPOQueryWrapper);

                        QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
                        dataCheckExtendPOQueryWrapper.lambda().eq(DataCheckExtendPO::getDelFlag, 1)
                                .in(DataCheckExtendPO::getRuleId, dataCheck_RuleIdList);
                        dataCheckExtendPOList = dataCheckExtendMapper.selectList(dataCheckExtendPOQueryWrapper);
                    }
                }

                // 质量报告的通知方式
                QueryWrapper<QualityReportNoticePO> qualityReportNoticePOQueryWrapper = new QueryWrapper<>();
                qualityReportNoticePOQueryWrapper.lambda().eq(QualityReportNoticePO::getDelFlag, 1)
                        .in(QualityReportNoticePO::getReportId, reportIdList);
                List<QualityReportNoticePO> qualityReportNoticePOList = qualityReportNoticeMapper.selectList(qualityReportNoticePOQueryWrapper);

                // 质量报告的接收人
                QueryWrapper<QualityReportRecipientPO> qualityReportRecipientPOQueryWrapper = new QueryWrapper<>();
                qualityReportRecipientPOQueryWrapper.lambda().eq(QualityReportRecipientPO::getDelFlag, 1)
                        .in(QualityReportRecipientPO::getReportId, reportIdList);
                List<QualityReportRecipientPO> qualityReportRecipientPOList = qualityReportRecipientMapper.selectList(qualityReportRecipientPOQueryWrapper);

                // 质量报告下的数据校验规则数量
                List<AppRuleCountVO> appRuleCount = qualityReportRuleMapper.getAppRuleCount();
                int totalCount = 0;

                List<DataCheckPO> finalDataCheckPOList = dataCheckPOList;
                List<DataCheckExtendPO> finalDataCheckExtendPOList = dataCheckExtendPOList;

                for (QualityReportVO t : all.getRecords()) {

                    // 获取Cron表达式下次执行时间
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

                    // 质量报告下的质量规则的详细信息
                    List<QualityReportRuleVO> rules = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(qualityReportRulePOS)) {
                        List<QualityReportRulePO> qualityReportRulePOList = qualityReportRulePOS.stream().filter(rule -> rule.getReportId() == t.getId()).sorted(Comparator.comparing(QualityReportRulePO::getRuleSort)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(qualityReportRulePOList) && CollectionUtils.isNotEmpty(finalDataCheckPOList)) {
                            List<QualityReportRuleVO> qualityReportRuleVOS = QualityReportRuleMap.INSTANCES.poToVo(qualityReportRulePOList);

                            qualityReportRuleVOS.forEach(rule -> {

                                DataCheckPO dataCheckPO = finalDataCheckPOList.stream().filter(r -> r.getId() == rule.getRuleId()).findFirst().orElse(null);
                                if (dataCheckPO == null) {
                                    return;
                                }
                                DataCheckExtendPO dataCheckExtendPO = finalDataCheckExtendPOList.stream().filter(r -> r.getRuleId() == rule.getRuleId()).findFirst().orElse(null);

                                DataSourceConVO dataSourceConVO = allDataSource.stream().filter(s -> s.getId() == dataCheckPO.getDatasourceId()).findFirst().orElse(null);
                                if (dataSourceConVO == null) {
                                    return;
                                }
                                rule.setRuleName(dataCheckPO.getRuleName());
                                rule.setRuleDescribe(dataCheckPO.getRuleDescribe());
                                rule.setRuleStateName(dataCheckPO.getRuleState() == 1 ? "启用" : "禁用");
                                rule.setTableBusinessType(dataCheckPO.getTableBusinessType());
                                rule.setTableTypeName(dataCheckPO.getTableType() == 1 ? "TABLE" : dataCheckPO.getTableType() == 2 ? "VIEW" : "");
                                rule.setDataSourceId(dataCheckPO.getDatasourceId());
                                rule.setIp(dataSourceConVO.getConIp());
                                rule.setDbName(dataSourceConVO.getConDbname());
                                rule.setSourceTypeName(dataSourceConVO.getDatasourceType() == SourceTypeEnum.FiData ? "FiData" : "Customize");
                                String tableNameFormat = "";
                                if (StringUtils.isNotEmpty(dataCheckPO.getSchemaName())) {
                                    tableNameFormat = dataCheckPO.getSchemaName() + ".";
                                }
                                tableNameFormat += dataCheckPO.getTableName();
                                rule.setTableName(tableNameFormat);
                                if (dataCheckExtendPO != null) {
                                    rule.setFieldName(dataCheckExtendPO.getFieldName());
                                }

                                if (StringUtils.isNotEmpty(rule.getRuleName())) {
                                    rules.add(rule);
                                }

                            });
                        }
                    }
                    t.setRules(rules);

                    // 质量报告的通知方式
                    if (CollectionUtils.isNotEmpty(qualityReportNoticePOList)) {
                        QualityReportNoticePO qualityReportNoticePO = qualityReportNoticePOList.stream().filter(n -> n.getReportId() == t.getId()).findFirst().orElse(null);
                        if (qualityReportNoticePO != null) {
                            t.setNotice(QualityReportNoticeMap.INSTANCES.poToVo(qualityReportNoticePO));
                        }
                    }

                    // 质量报告的接收人
                    if (CollectionUtils.isNotEmpty(qualityReportRecipientPOList) && t.getNotice() != null) {
                        List<QualityReportRecipientPO> qualityReportRecipientPOS = qualityReportRecipientPOList.stream().filter(r -> r.getReportId() == t.getId()).sorted(Comparator.comparing(QualityReportRecipientPO::getCreateTime).reversed()).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(qualityReportRecipientPOS)) {
                            List<QualityReportRecipientVO> qualityReportRecipientVOS = QualityReportRecipientMap.INSTANCES.poToVo(qualityReportRecipientPOS);
//                            qualityReportRecipientVOS.forEach(r -> {
//                                if (r.getUserId() != 0 && r.getUserType() == 1 && CollectionUtils.isNotEmpty(finalUserDTOList)) {
//                                    UserDTO userDTO = finalUserDTOList.stream().filter(u -> u.getId() == r.getUserId()).findFirst().orElse(null);
//                                    if (userDTO != null) {
//                                        r.setUserName(userDTO.getUsername());
//                                        r.setRecipient(userDTO.getEmail());
//                                    }
//                                }
//                            });
                            t.getNotice().setRecipients(qualityReportRecipientVOS);
                        }
                    }

                    // 质量报告下的数据校验规则
                    if (CollectionUtils.isNotEmpty(appRuleCount)) {
                        AppRuleCountVO appServiceCountVO = appRuleCount.stream().filter(k -> k.getAppId() == t.getId()).findFirst().orElse(null);
                        if (appServiceCountVO != null) {
                            t.setItemCount(appServiceCountVO.getCount());
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(appRuleCount)) {
                    totalCount = appRuleCount.stream().collect(Collectors.summingInt(AppRuleCountVO::getCount));
                    all.getRecords().get(0).setTotalCount(totalCount);
                }

            }
        } catch (Exception ex) {
            log.error("【getAll】 查询质量报告异常：" + ex);
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
        try {
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
            int reportId = Math.toIntExact(qualityReportPO.id);

            //第三步：保存质量报告规则配置
            if (CollectionUtils.isNotEmpty(dto.getQualityReportRule())) {
                List<QualityReportRulePO> qualityReportRulePOS = new ArrayList<>();
                for (int j = 0; j < dto.getQualityReportRule().size(); j++) {
                    QualityReportRulePO qualityReportRulePO = QualityReportRuleMap.INSTANCES.dtoToPo(dto.getQualityReportRule().get(j));
                    qualityReportRulePO.setReportId(reportId);
                    qualityReportRulePO.setReportType(qualityReportPO.getReportType());
                    qualityReportRulePO.setRuleSort(j);
                    qualityReportRulePOS.add(qualityReportRulePO);
                }
                qualityReportRuleManage.saveBatch(qualityReportRulePOS);
            }
            // 第四步：保存质量报告通知配置
            if (dto.getQualityReportNotice() != null) {
                QualityReportNoticePO qualityReportNoticePO = QualityReportNoticeMap.INSTANCES.dtoToPo(dto.getQualityReportNotice());
                qualityReportNoticePO.setReportId(reportId);
                qualityReportNoticeMapper.insert(qualityReportNoticePO);
            }
            // 第五步：保存质量报告接收人配置
            if (dto.getQualityReportNotice() != null && CollectionUtils.isNotEmpty(dto.qualityReportNotice.getQualityReportRecipient())) {
                List<QualityReportRecipientPO> qualityReportRecipientPOS = QualityReportRecipientMap.INSTANCES.dtoToPo(dto.getQualityReportNotice().getQualityReportRecipient());
                qualityReportRecipientPOS.forEach(t -> t.setReportId(reportId));
                qualityReportRecipientManage.saveBatch(qualityReportRecipientPOS);
            }
            //第六步：保存调度任务
            publishBuild_unifiedControlTask(reportId, qualityReportPO.getReportState(), qualityReportPO.getRunTimeCron());
        } catch (Exception ex) {
            log.error("【addData】 新增质量报告异常：" + ex);
            throw new FkException(ResultEnum.ERROR, "【addData】 ex：" + ex);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(QualityReportEditDTO dto) {
        try {
            if (dto == null) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            QualityReportPO qualityReportPO = baseMapper.selectById(dto.id);
            if (qualityReportPO == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            // 第一步：转换DTO对象为PO对象
            qualityReportPO = QualityReportMap.INSTANCES.dtoToPo_Edit(dto);
            if (qualityReportPO == null) {
                return ResultEnum.PARAMTER_ERROR;
            }
            // 第二步：保存质量报告配置
            int reportId = Math.toIntExact(qualityReportPO.id);
            int i = baseMapper.updateById(qualityReportPO);
            if (i <= 0) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            // 第三步：保存质量报告规则配置
            if (CollectionUtils.isNotEmpty(dto.getQualityReportRule())) {
                qualityReportRuleMapper.updateByReportId(reportId);
                List<QualityReportRulePO> qualityReportRulePOS = new ArrayList<>();
                for (int j = 0; j < dto.getQualityReportRule().size(); j++) {
                    QualityReportRulePO qualityReportRulePO = QualityReportRuleMap.INSTANCES.dtoToPo(dto.getQualityReportRule().get(j));
                    qualityReportRulePO.setReportId(reportId);
                    qualityReportRulePO.setReportType(qualityReportPO.getReportType());
                    qualityReportRulePO.setRuleSort(j);
                    qualityReportRulePOS.add(qualityReportRulePO);
                }
                qualityReportRuleManage.saveBatch(qualityReportRulePOS);
            }
            // 第四步：保存质量报告通知配置
            if (dto.getQualityReportNotice() != null) {
                QualityReportNoticePO qualityReportNoticePO = QualityReportNoticeMap.INSTANCES.dtoToPo(dto.getQualityReportNotice());
                qualityReportNoticeMapper.updateById(qualityReportNoticePO);
            }
            // 第五步：保存质量报告接收人配置
            if (dto.getQualityReportNotice() != null && CollectionUtils.isNotEmpty(dto.qualityReportNotice.getQualityReportRecipient())) {
                qualityReportRecipientMapper.updateByReportId(reportId);
                List<QualityReportRecipientPO> qualityReportRecipientPOS = QualityReportRecipientMap.INSTANCES.dtoToPo(dto.getQualityReportNotice().getQualityReportRecipient());
                qualityReportRecipientPOS.forEach(t -> t.setReportId(reportId));
                qualityReportRecipientManage.saveBatch(qualityReportRecipientPOS);
            }
            // 第六步：保存调度任务
            publishBuild_unifiedControlTask(reportId, qualityReportPO.getReportState(), qualityReportPO.getRunTimeCron());
        } catch (Exception ex) {
            log.error("【editData】 编辑质量报告异常：" + ex);
            throw new FkException(ResultEnum.ERROR, "【editData】 ex：" + ex);
        }
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
        try {
            if (id == 0) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            QualityReportPO qualityReportPO = baseMapper.selectById(id);
            if (qualityReportPO == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            qualityReportRuleMapper.updateByReportId(id);
            qualityReportNoticeMapper.updateByReportId(id);
            qualityReportRecipientMapper.updateByReportId(id);
            resultEnum = baseMapper.deleteByIdWithFill(qualityReportPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
            publishBuild_unifiedControlTask(Math.toIntExact(qualityReportPO.getId()), RuleStateEnum.Disable.getValue(), qualityReportPO.getRunTimeCron());
        } catch (Exception ex) {
            log.error("【deleteData】 删除质量报告异常：" + ex);
            throw new FkException(ResultEnum.ERROR, "【deleteData】 ex：" + ex);
        }
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
    public QualityReportExtVO getMailServerAndUserInfo() {
        QualityReportExtVO qualityReportExtVO = new QualityReportExtVO();
        List<QualityReportExt_EmailVO> emails = new ArrayList<>();
        List<QualityReportExt_UserVO> users = new ArrayList<>();

        // 查询邮件服务配置
        ResultEntity<List<EmailServerVO>> emailServerList = userClient.getEmailServerList();
        if (emailServerList != null && emailServerList.getCode() == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(emailServerList.getData())) {
            emailServerList.getData().forEach(t -> {
                QualityReportExt_EmailVO email = new QualityReportExt_EmailVO();
                email.setId(Long.valueOf(t.getId()));
                email.setName(t.getName());
                emails.add(email);
            });
        }

        // 查询用户列表
        ResultEntity<List<UserDTO>> userList = userClient.getAllUserList();
        if (userList.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(userList.getData())) {
            userList.getData().forEach(t -> {
                QualityReportExt_UserVO systemUserVO = new QualityReportExt_UserVO();
                systemUserVO.setId(t.id);
                systemUserVO.setUsername(t.getUsername());
                systemUserVO.setUserAccount(t.getUserAccount());
                systemUserVO.setEmail(t.getEmail());
                systemUserVO.setValid(t.isValid());
                users.add(systemUserVO);
            });
        }

        qualityReportExtVO.setEmails(emails);
        qualityReportExtVO.setUsers(users);
        return qualityReportExtVO;
    }

    @Override
    public Page<QualityReportExt_RuleVO> getQualityReportRuleList(DataCheckRulePageDTO query) {
        Page<QualityReportExt_RuleVO> all = new Page<>();
        List<QualityReportExt_RuleVO> qualityReportExt_ruleVOS = new ArrayList<>();

        // 查询校验规则
        Page<DataCheckVO> pageAllRule = dataCheckMapper.getPageAllRule(query.getPage(), query);

        if (pageAllRule != null && CollectionUtils.isNotEmpty(pageAllRule.getRecords())) {

            // 查询数据源信息
            List<DataSourceConVO> allDataSource = dataSourceConManage.getAllDataSource();
            if (CollectionUtils.isEmpty(allDataSource)) {
                return all;
            }
            // 查询校验规则扩展信息
            List<Integer> ruleIds = pageAllRule.getRecords().stream().map(DataCheckVO::getId).distinct().collect(Collectors.toList());
            List<DataCheckExtendVO> dataCheckExtendVOList = dataCheckExtendMapper.getDataCheckExtendByRuleIdList(ruleIds);

            pageAllRule.getRecords().forEach(t -> {
                QualityReportExt_RuleVO cRule = new QualityReportExt_RuleVO();
                DataSourceConVO dataSourceConVO = allDataSource.stream().filter(s -> s.getId() == t.getDatasourceId()).findFirst().orElse(null);
                if (dataSourceConVO == null) {
                    return;
                }
                if (CollectionUtils.isNotEmpty(dataCheckExtendVOList)) {
                    DataCheckExtendVO dataCheckExtendVO = dataCheckExtendVOList.stream().filter(k -> k.getRuleId() == t.getId()).findFirst().orElse(null);
                    if (dataCheckExtendVO != null) {
                        cRule.setFieldName(dataCheckExtendVO.getFieldName());
                    }
                }

                cRule.setId(Long.valueOf(t.getId()));
                cRule.setName(t.getRuleName());
                cRule.setDescribe(t.getRuleDescribe());
                cRule.setStateName(t.getRuleState().getName());
                cRule.setIp(dataSourceConVO.getConIp());
                cRule.setDbName(dataSourceConVO.getConDbname());
                cRule.setSourceTypeName(dataSourceConVO.getDatasourceType() == SourceTypeEnum.FiData ? "FiData" : "Customize");
                String tableNameFormat = "";
                if (StringUtils.isNotEmpty(t.getSchemaName())) {
                    tableNameFormat = t.getSchemaName() + ".";
                }
                tableNameFormat += t.getTableName();
                cRule.setTableName(tableNameFormat);
                cRule.setCheckGroupName(t.getCheckGroupName());
                qualityReportExt_ruleVOS.add(cRule);
            });
        }

        all.setPages(pageAllRule.getPages());
        all.setTotal(pageAllRule.getTotal());
        all.setCurrent(pageAllRule.getCurrent());
        all.setSize(pageAllRule.getSize());
        all.setRecords(qualityReportExt_ruleVOS);
        return all;
    }

    @Override
    public Page<QualityReportLogVO> getDataCheckQualityReportLog(QualityReportLogQueryDTO dto) {
        // 第一步：查询数据校验Summary报告日志
        Page<QualityReportLogVO> all = qualityReportLogMapper.getAll(dto.getPage(), dto.getReportId(), dto.getKeyword(),
                dto.getReportBatchNumber(), dto.getCreateReportStartTime(), dto.getCreateReportEndTime());

        // 第二步：查询数据校验Summary报告的附件ID
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<String> reportLogIdList = all.getRecords().stream().map(QualityReportLogVO::getId).collect(Collectors.toList());
            QueryWrapper<AttachmentInfoPO> attachmentInfoPOQueryWrapper = new QueryWrapper<>();
            attachmentInfoPOQueryWrapper.lambda().eq(AttachmentInfoPO::getDelFlag, 1)
                    .eq(AttachmentInfoPO::getCategory, AttachmentCateGoryEnum.QUALITY_VERIFICATION_SUMMARY_REPORT.getValue())
                    .in(AttachmentInfoPO::getObjectId, reportLogIdList);
            // 查询附件信息
            List<AttachmentInfoPO> attachmentInfoPOS = attachmentInfoMapper.selectList(attachmentInfoPOQueryWrapper);

            for (QualityReportLogVO qualityReportLogVO : all.getRecords()) {
                // 设置附件ID
                if (CollectionUtils.isNotEmpty(attachmentInfoPOS)) {
                    AttachmentInfoPO attachmentInfoPO = attachmentInfoPOS.stream().filter(t -> t.getObjectId().equals(qualityReportLogVO.getId())).findFirst().orElse(null);
                    if (attachmentInfoPO != null && StringUtils.isNotEmpty(attachmentInfoPO.getAbsolutePath())) {
                        String filePath = "";
                        if (attachmentInfoPO.getAbsolutePath().endsWith("/")) {
                            filePath = attachmentInfoPO.getAbsolutePath() + attachmentInfoPO.getCurrentFileName();
                        } else {
                            filePath = attachmentInfoPO.getAbsolutePath() + File.separator + attachmentInfoPO.getCurrentFileName();
                        }
                        File file = new File(filePath);
                        if (file.exists()) {
                            qualityReportLogVO.setExistReport(true);
                            qualityReportLogVO.setOriginalName(attachmentInfoPO.getOriginalName());
                        }
                    }
                }
                // 秒转分
                if (StringUtils.isNotEmpty(qualityReportLogVO.getCreateReportDuration())) {
                    int minutes = Integer.parseInt(qualityReportLogVO.getCreateReportDuration()) / 60;
                    int seconds = Integer.parseInt(qualityReportLogVO.getCreateReportDuration()) % 60;
                    qualityReportLogVO.setCreateReportDuration(minutes + "分" + seconds + "秒");
                }
            }

        }
        return all;
    }

    @Override
    public void downloadReportRecord(int reportLogId, HttpServletResponse response) {
        try {
            if (reportLogId == 0) {
                return;
            }
            QueryWrapper<AttachmentInfoPO> attachmentInfoPOQueryWrapper = new QueryWrapper<>();
            attachmentInfoPOQueryWrapper.lambda().eq(AttachmentInfoPO::getDelFlag, 1)
                    .eq(AttachmentInfoPO::getCategory, AttachmentCateGoryEnum.QUALITY_VERIFICATION_SUMMARY_REPORT.getValue())
                    .eq(AttachmentInfoPO::getObjectId, reportLogId);
            AttachmentInfoPO attachmentInfoPO = attachmentInfoMapper.selectOne(attachmentInfoPOQueryWrapper);
            if (attachmentInfoPO == null) {
                return;
            }
            String filePath = "";
            if (attachmentInfoPO.getAbsolutePath().endsWith("/")) {
                filePath = attachmentInfoPO.getAbsolutePath() + attachmentInfoPO.getCurrentFileName();
            } else {
                filePath = attachmentInfoPO.getAbsolutePath() + File.separator + attachmentInfoPO.getCurrentFileName();
            }
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
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (Exception ex) {
            log.error("【downloadReportRecord】 系统异常：" + ex);
            throw new FkException(ResultEnum.ERROR, "【downloadReportRecord】 ex：" + ex);
        }
        return;
    }

    @Override
    public void downloadExcelReport(int attachmentId, HttpServletResponse response) {
        try {
            if (attachmentId == 0) {
                return;
            }
            AttachmentInfoPO attachmentInfoPO = attachmentInfoMapper.selectById(attachmentId);
            if (attachmentInfoPO == null) {
                return;
            }
            String filePath = "";
            if (attachmentInfoPO.getAbsolutePath().endsWith("/")) {
                filePath = attachmentInfoPO.getAbsolutePath() + attachmentInfoPO.getCurrentFileName();
            } else {
                filePath = attachmentInfoPO.getAbsolutePath() + File.separator + attachmentInfoPO.getCurrentFileName();
            }
            File file = new File(filePath);
            // 取得文件名
            String filename = attachmentInfoPO.getOriginalName();
            // 以流的形式下载文件
            InputStream fis = new BufferedInputStream(new FileInputStream(filePath));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (Exception ex) {
            log.error("【downloadExcelReport】 系统异常：" + ex);
            throw new FkException(ResultEnum.ERROR, "【downloadExcelReport】 ex：" + ex);
        }
        return;
    }

    @Override
    public List<PreviewQualityReportVO> previewReportRecord(int reportLogId) {
        return null;
    }

    @Override
    public List<String> getNextCronExeTime(String cron) {
        NextCronTimeDTO dto = new NextCronTimeDTO();
        dto.setCronExpression(cron);
        dto.setNumber(3);
        return CronUtils.nextCronExeTime(dto);
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

        QualityReportNoticeDTO qualityReportNotice = dto.getQualityReportNotice();
        if (qualityReportNotice == null) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, "发送邮件时，通知信息参数为空");
        }
        List<QualityReportRecipientDTO> qualityReportRecipient = qualityReportNotice.getQualityReportRecipient();
        if (CollectionUtils.isEmpty(qualityReportRecipient)) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, "发送邮件时，接收人信息参数为空");
        }
        String toAddressStr = "";
        List<String> toAddressList = qualityReportRecipient.stream().map(QualityReportRecipientDTO::getRecipient).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(toAddressList)) {
            toAddressStr = Joiner.on(";").join(toAddressList);
        }
        log.info("【sendEmailReport】收件人为：" + toAddressStr);

        //第一步：查询邮件服务器设置
        ResultEntity<EmailServerVO> emailServerById = userClient.getEmailServerById(qualityReportNotice.getEmailServerId());
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
        mailSenderDTO.setSubject(qualityReportNotice.getSubject());
        mailSenderDTO.setBody(qualityReportNotice.getBody());
        mailSenderDTO.setToAddress(toAddressStr);
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
            unifiedControlDTO.setTopic(MqConstants.QueueConstants.GovernanceTopicConstants.BUILD_GOVERNANCE_TEMPLATE_FLOW);
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