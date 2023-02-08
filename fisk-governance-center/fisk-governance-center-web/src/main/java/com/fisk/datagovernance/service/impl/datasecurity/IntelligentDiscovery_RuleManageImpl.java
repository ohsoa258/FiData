package com.fisk.datagovernance.service.impl.datasecurity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.CronUtils;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.Dto.Excel.*;
import com.fisk.common.core.utils.Dto.cron.NextCronTimeDTO;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.core.utils.office.excel.ExcelReportUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.governance.BuildGovernanceHelper;
import com.fisk.common.service.dbBEBuild.governance.IBuildGovernanceSqlCommand;
import com.fisk.common.service.dbBEBuild.governance.dto.KeyValueMapDto;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.*;
import com.fisk.datagovernance.entity.dataquality.AttachmentInfoPO;
import com.fisk.datagovernance.entity.datasecurity.*;
import com.fisk.datagovernance.enums.dataquality.RuleStateEnum;
import com.fisk.datagovernance.enums.datasecurity.ScanReceptionTypeEnum;
import com.fisk.datagovernance.map.datasecurity.*;
import com.fisk.datagovernance.mapper.dataquality.AttachmentInfoMapper;
import com.fisk.datagovernance.mapper.datasecurity.*;
import com.fisk.datagovernance.service.datasecurity.IIntelligentDiscovery_RuleManageService;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.*;
import com.fisk.datagovernance.dto.GetConfigDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.UnifiedControlDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IntelligentDiscovery_RuleManageImpl extends ServiceImpl<IntelligentDiscovery_RuleMapper, IntelligentDiscovery_RulePO> implements IIntelligentDiscovery_RuleManageService {

    @Resource
    private GetConfigDTO getConfig;

    @Resource
    private GetMetadata getMetadata;

    @Resource
    private GenerateCondition generateCondition;

    @Resource
    private UserHelper userHelper;

    @Resource
    private IntelligentDiscovery_ScanMapper intelligentDiscovery_scanMapper;

    @Resource
    private IntelligentDiscovery_UserMapper intelligentDiscovery_userMapper;

    @Resource
    private IntelligentDiscovery_LogsMapper intelligentDiscovery_logsMapper;

    @Resource
    private IntelligentDiscovery_WhiteListMapper intelligentDiscovery_whiteListMapper;

    @Resource
    private IntelligentDiscovery_NoticeMapper intelligentDiscovery_noticeMapper;

    @Resource
    private IntelligentDiscovery_ScanManageImpl intelligentDiscovery_scanManage;

    @Resource
    private IntelligentDiscovery_UserManageImpl intelligentDiscovery_userManage;

    @Resource
    private IntelligentDiscovery_NoticeManageImpl intelligentDiscovery_noticeManage;

    @Resource
    private AttachmentInfoMapper attachmentInfoMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Value("${file.uploadUrl}")
    private String uploadUrl;
    //@Value("${file.echoPath}")
    private String echoPath;
    //@Value("${file.logoPath}")
    private String logoPaht;

    @Override
    public List<FilterFieldDTO> getSearchColumn() {
        try {
            MetaDataConfigDTO dto = new MetaDataConfigDTO();
            dto.url = getConfig.url;
            dto.userName = getConfig.username;
            dto.password = getConfig.password;
            dto.driver = getConfig.driver;
            dto.tableName = "tb_Intelligentdiscovery_rule";
            dto.filterSql = FilterSqlConstants.DATA_SECURITY_INTELLIGENT_DISCOVERY_SQL;
            return getMetadata.getMetadataList(dto);
        } catch (Exception ex) {
            log.error("【getSearchColumn】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
    }

    @Override
    public Page<IntelligentDiscovery_RuleVO> getRulePageList(IntelligentDiscovery_RuleQueryDTO dto) {
        Page<IntelligentDiscovery_RuleVO> rulesVOPage = null;
        try {
            // 第一步：设置查询条件
            StringBuilder querySql = new StringBuilder();
            querySql.append(generateCondition.getCondition(dto.getDto()));
            IntelligentDiscovery_RulePageDTO data = new IntelligentDiscovery_RulePageDTO();
            data.setPage(dto.getPage());
            data.setWhere(querySql.toString());
            // 第一步：查询智能发现规则基本配置
            rulesVOPage = baseMapper.filter(dto.getPage(), data);
            if (rulesVOPage == null || CollectionUtils.isEmpty(rulesVOPage.getRecords())) {
                return rulesVOPage;
            }
            List<Integer> ruleIdList = rulesVOPage.getRecords().stream().map(IntelligentDiscovery_RuleVO::getId).collect(Collectors.toList());
            // 第二步：查询智能发现通知方式配置
            QueryWrapper<IntelligentDiscovery_NoticePO> intelligentDiscovery_noticePOQueryWrapper = new QueryWrapper<>();
            intelligentDiscovery_noticePOQueryWrapper.lambda()
                    .eq(IntelligentDiscovery_NoticePO::getDelFlag, 1)
                    .in(IntelligentDiscovery_NoticePO::getRuleId, ruleIdList);
            List<IntelligentDiscovery_NoticePO> intelligentDiscovery_noticePOS = intelligentDiscovery_noticeMapper.selectList(intelligentDiscovery_noticePOQueryWrapper);
            // 第三步：查询智能发现扫描配置
            QueryWrapper<IntelligentDiscovery_ScanPO> intelligentDiscovery_scanPOQueryWrapper = new QueryWrapper<>();
            intelligentDiscovery_scanPOQueryWrapper.lambda()
                    .eq(IntelligentDiscovery_ScanPO::getDelFlag, 1)
                    .in(IntelligentDiscovery_ScanPO::getRuleId, ruleIdList);
            List<IntelligentDiscovery_ScanPO> intelligentDiscovery_scanPOS = intelligentDiscovery_scanMapper.selectList(intelligentDiscovery_scanPOQueryWrapper);
            // 第四步：查询智能发现扫描结果接收人
            QueryWrapper<IntelligentDiscovery_UserPO> intelligentDiscovery_userPOQueryWrapper = new QueryWrapper<>();
            intelligentDiscovery_userPOQueryWrapper.lambda()
                    .eq(IntelligentDiscovery_UserPO::getDelFlag, 1)
                    .in(IntelligentDiscovery_UserPO::getRuleId, ruleIdList);
            List<IntelligentDiscovery_UserPO> intelligentDiscovery_userPOS = intelligentDiscovery_userMapper.selectList(intelligentDiscovery_userPOQueryWrapper);
            // 第五步：循环设置每个智能发现规则的扫描配置和接收人配置
            rulesVOPage.getRecords().forEach(t -> {
                // 回显设置Cron表达式下次执行时间
                if (StringUtils.isNotEmpty(t.getScanPeriod())) {
                    String cronExpress = CronUtils.getCronExpress(t.getScanPeriod());
                    Date date = null;
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cronExpress);
                    } catch (ParseException e) {
                        log.error("【getRulePageList】 时间转换异常：" + e);
                    }
                    String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
                    t.setNextScanTime(now);
                }

                // 回显设置智能发现规则的关键字规则配置
                if (t.getRuleType() == 1 && StringUtils.isNotEmpty(t.getRuleValue())) {
                    List<IntelligentDiscovery_KeyWordRuleVO> keyWordRuleVOList = JSONObject.parseArray(t.getRuleValue(), IntelligentDiscovery_KeyWordRuleVO.class);
                    t.setKeyWordRules(keyWordRuleVOList);
                }

                // 回显设置智能发现通知配置
                if (CollectionUtils.isNotEmpty(intelligentDiscovery_noticePOS)) {
                    IntelligentDiscovery_NoticePO noticePO = intelligentDiscovery_noticePOS.stream().filter(notice -> notice.getRuleId() == t.getId()).findFirst().orElse(null);
                    if (noticePO != null) {
                        IntelligentDiscovery_NoticeVO noticeVO = IntelligentDiscovery_NoticeMap.INSTANCES.poToVo(noticePO);
                        t.setNotice(noticeVO);
                    }
                }

                // 回显设置智能发现规则的扫描配置
                if (CollectionUtils.isNotEmpty(intelligentDiscovery_scanPOS)) {
                    List<IntelligentDiscovery_ScanPO> scanPOList = intelligentDiscovery_scanPOS.stream().filter(scan -> scan.getRuleId() == t.getId()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(scanPOList)) {
                        List<IntelligentDiscovery_ScanVO> scanVOList = IntelligentDiscovery_ScanMap.INSTANCES.poToVo(scanPOList);
                        t.setScans(scanVOList);
                    }
                }

                // 回显设置智能发现扫描结果接收人信息
                if (CollectionUtils.isNotEmpty(intelligentDiscovery_userPOS)) {
                    List<IntelligentDiscovery_UserPO> userPOList = intelligentDiscovery_userPOS.stream().filter(user -> user.getRuleId() == t.getId()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(userPOList)) {
                        List<IntelligentDiscovery_UserVO> userVOList = IntelligentDiscovery_UserMap.INSTANCES.poToVo(userPOList);
                        t.setUsers(userVOList);
                    }
                }
            });
        } catch (Exception ex) {
            log.error("【getRulePageList】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return rulesVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addRule(IntelligentDiscovery_RuleDTO dto) {
        try {
            if (dto == null || StringUtils.isEmpty(dto.getRuleName())) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            QueryWrapper<IntelligentDiscovery_RulePO> intelligentDiscovery_rulePOQueryWrapper = new QueryWrapper<>();
            intelligentDiscovery_rulePOQueryWrapper.lambda()
                    .eq(IntelligentDiscovery_RulePO::getDelFlag, 1)
                    .eq(IntelligentDiscovery_RulePO::getRuleName, dto.getRuleName());
            List<IntelligentDiscovery_RulePO> intelligentDiscovery_rulePOS = baseMapper.selectList(intelligentDiscovery_rulePOQueryWrapper);
            if (CollectionUtils.isNotEmpty(intelligentDiscovery_rulePOS)) {
                return ResultEnum.INTELLIGENT_DISCOVERY_RULE_NAME_ALREADY_EXISTS;
            }
            IntelligentDiscovery_NoticePO intelligentDiscovery_noticePO = null;
            List<IntelligentDiscovery_ScanPO> intelligentDiscovery_scanPOList = null;
            List<IntelligentDiscovery_UserPO> intelligentDiscovery_userPOList = null;
            if (dto.getNotice() != null) {
                intelligentDiscovery_noticePO = IntelligentDiscovery_NoticeMap.INSTANCES.dtoToPo(dto.getNotice());
            }
            if (CollectionUtils.isNotEmpty(dto.getScans())) {
                intelligentDiscovery_scanPOList = IntelligentDiscovery_ScanMap.INSTANCES.dtoToPo(dto.getScans());
            }
            if (CollectionUtils.isNotEmpty(dto.getUsers())) {
                intelligentDiscovery_userPOList = IntelligentDiscovery_UserMap.INSTANCES.dtoToPo(dto.getUsers());
            }
            if (dto.getRuleType() == 1 && CollectionUtils.isNotEmpty(dto.getKeyWordRules())) {
                String ruleValue_Json = JSON.toJSONString(dto.getKeyWordRules());
                dto.setRuleValue(ruleValue_Json);
            }
            IntelligentDiscovery_RulePO intelligentDiscovery_rulePO = IntelligentDiscovery_RuleMap.INSTANCES.dtoToPo(dto);
            intelligentDiscovery_rulePO.setCreateTime(LocalDateTime.now());
            intelligentDiscovery_rulePO.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
            int i = baseMapper.insertOne(intelligentDiscovery_rulePO);
            if (i <= 0) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            int ruleId = Math.toIntExact(intelligentDiscovery_rulePO.getId());
            if (CollectionUtils.isNotEmpty(intelligentDiscovery_scanPOList)) {
                intelligentDiscovery_scanPOList.forEach(t -> {
                    t.setRuleId(ruleId);
                });
                intelligentDiscovery_scanManage.saveBatch(intelligentDiscovery_scanPOList);
            }
            if (CollectionUtils.isNotEmpty(intelligentDiscovery_userPOList)) {
                intelligentDiscovery_userPOList.forEach(t -> {
                    t.setRuleId(ruleId);
                });
                intelligentDiscovery_userManage.saveBatch(intelligentDiscovery_userPOList);
            }
            if (intelligentDiscovery_noticePO != null) {
                intelligentDiscovery_noticePO.setRuleId(ruleId);
                intelligentDiscovery_noticeMapper.insert(intelligentDiscovery_noticePO);
            }
            publishBuild_unifiedControlTask(ruleId, intelligentDiscovery_rulePO.getRuleState(), intelligentDiscovery_rulePO.getScanPeriod());
        } catch (Exception ex) {
            log.error("【addRule】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editRule(IntelligentDiscovery_RuleDTO dto) {
        try {
            if (dto == null || dto.getId() == 0 || StringUtils.isEmpty(dto.getRuleName())) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            IntelligentDiscovery_RulePO intelligentDiscovery_rulePO = baseMapper.selectById(dto.getId());
            if (intelligentDiscovery_rulePO == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            QueryWrapper<IntelligentDiscovery_RulePO> intelligentDiscovery_rulePOQueryWrapper = new QueryWrapper<>();
            intelligentDiscovery_rulePOQueryWrapper.lambda()
                    .eq(IntelligentDiscovery_RulePO::getDelFlag, 1)
                    .eq(IntelligentDiscovery_RulePO::getRuleName, dto.getRuleName())
                    .ne(IntelligentDiscovery_RulePO::getId, dto.getId());
            List<IntelligentDiscovery_RulePO> intelligentDiscovery_rulePOS = baseMapper.selectList(intelligentDiscovery_rulePOQueryWrapper);
            if (CollectionUtils.isNotEmpty(intelligentDiscovery_rulePOS)) {
                return ResultEnum.INTELLIGENT_DISCOVERY_RULE_NAME_ALREADY_EXISTS;
            }
            intelligentDiscovery_rulePO = IntelligentDiscovery_RuleMap.INSTANCES.dtoToPo(dto);
            IntelligentDiscovery_NoticePO intelligentDiscovery_noticePO = null;
            List<IntelligentDiscovery_ScanPO> intelligentDiscovery_scanPOList = null;
            List<IntelligentDiscovery_UserPO> intelligentDiscovery_userPOList = null;
            if (dto.getNotice() != null) {
                intelligentDiscovery_noticePO = IntelligentDiscovery_NoticeMap.INSTANCES.dtoToPo(dto.getNotice());
            }
            if (CollectionUtils.isNotEmpty(dto.getScans())) {
                intelligentDiscovery_scanPOList = IntelligentDiscovery_ScanMap.INSTANCES.dtoToPo(dto.getScans());
            }
            if (CollectionUtils.isNotEmpty(dto.getUsers())) {
                intelligentDiscovery_userPOList = IntelligentDiscovery_UserMap.INSTANCES.dtoToPo(dto.getUsers());
            }
            if (dto.getRuleType() == 1 && CollectionUtils.isNotEmpty(dto.getKeyWordRules())) {
                String ruleValue_Json = JSON.toJSONString(dto.getKeyWordRules());
                dto.setRuleValue(ruleValue_Json);
            }
            int ruleId = Math.toIntExact(dto.getId());
            int i = baseMapper.updateById(intelligentDiscovery_rulePO);
            if (i <= 0) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            if (CollectionUtils.isNotEmpty(intelligentDiscovery_scanPOList)) {
                intelligentDiscovery_scanPOList.forEach(t -> {
                    t.setRuleId(ruleId);
                });
                intelligentDiscovery_scanMapper.updateByRuleId(ruleId);
                intelligentDiscovery_scanManage.saveBatch(intelligentDiscovery_scanPOList);
            }
            if (CollectionUtils.isNotEmpty(intelligentDiscovery_userPOList)) {
                intelligentDiscovery_userPOList.forEach(t -> {
                    t.setRuleId(ruleId);
                });
                intelligentDiscovery_userMapper.updateByRuleId(ruleId);
                intelligentDiscovery_userManage.saveBatch(intelligentDiscovery_userPOList);
            }
            if (intelligentDiscovery_noticePO != null) {
                intelligentDiscovery_noticePO.setRuleId(ruleId);
                intelligentDiscovery_noticeMapper.updateByRuleId(ruleId);
                intelligentDiscovery_noticeMapper.insert(intelligentDiscovery_noticePO);
            }
            publishBuild_unifiedControlTask(ruleId, intelligentDiscovery_rulePO.getRuleState(), intelligentDiscovery_rulePO.getScanPeriod());
        } catch (Exception ex) {
            log.error("【editRule】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editRuleState(int id) {
        try {
            if (id == 0) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            IntelligentDiscovery_RulePO intelligentDiscovery_rulePO = baseMapper.selectById(id);
            if (intelligentDiscovery_rulePO == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            intelligentDiscovery_rulePO.setRuleState(intelligentDiscovery_rulePO.getRuleState() == 1 ? 0 : 1);
            int i = baseMapper.updateById(intelligentDiscovery_rulePO);
            if (i <= 0) {
                return ResultEnum.UPDATE_DATA_ERROR;
            }
            publishBuild_unifiedControlTask(id, intelligentDiscovery_rulePO.getRuleState(), intelligentDiscovery_rulePO.getScanPeriod());
        } catch (Exception ex) {
            log.error("【editRuleState】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteRule(int id) {
        try {
            if (id == 0) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            IntelligentDiscovery_RulePO intelligentDiscovery_rulePO = baseMapper.selectById(id);
            if (intelligentDiscovery_rulePO == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            int i = baseMapper.deleteByIdWithFill(intelligentDiscovery_rulePO);
            if (i <= 0) {
                return ResultEnum.DELETE_ERROR;
            }
            intelligentDiscovery_noticeMapper.updateByRuleId(id);
            intelligentDiscovery_scanMapper.updateByRuleId(id);
            intelligentDiscovery_userMapper.updateByRuleId(id);
            publishBuild_unifiedControlTask(id, RuleStateEnum.Disable.getValue(), intelligentDiscovery_rulePO.getScanPeriod());
        } catch (Exception ex) {
            log.error("【deleteRule】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public Page<IntelligentDiscovery_LogsVO> getRuleScanLogPageList(IntelligentDiscovery_LogsQueryDTO dto) {
        Page<IntelligentDiscovery_LogsVO> ruleLogsVOPage = null;
        try {
            ruleLogsVOPage = intelligentDiscovery_logsMapper.filter(dto.getPage(), dto.getRuleId(), dto.getKeyWord());
            if (ruleLogsVOPage == null || CollectionUtils.isEmpty(ruleLogsVOPage.getRecords())) {
                return ruleLogsVOPage;
            }
            List<String> uniqueIdList = ruleLogsVOPage.getRecords().stream().map(IntelligentDiscovery_LogsVO::getUniqueId).collect(Collectors.toList());
            QueryWrapper<AttachmentInfoPO> attachmentInfoPOQueryWrapper = new QueryWrapper<>();
            attachmentInfoPOQueryWrapper.lambda().eq(AttachmentInfoPO::getDelFlag, 1)
                    .eq(AttachmentInfoPO::getCategory, 300)
                    .in(AttachmentInfoPO::getObjectId, uniqueIdList);
            List<AttachmentInfoPO> attachmentInfoPOList = attachmentInfoMapper.selectList(attachmentInfoPOQueryWrapper);
            if (CollectionUtils.isNotEmpty(attachmentInfoPOList)) {
                for (IntelligentDiscovery_LogsVO logsVO : ruleLogsVOPage.getRecords()) {
                    AttachmentInfoPO attachmentInfoPO = attachmentInfoPOList.stream().filter(t -> t.getObjectId().equals(logsVO.getUniqueId())).findFirst().orElse(null);
                    if (attachmentInfoPO != null && StringUtils.isNotEmpty(attachmentInfoPO.getAbsolutePath())) {
                        String filePath = attachmentInfoPO.getAbsolutePath() + File.separator + attachmentInfoPO.getCurrentFileName();
                        File file = new File(filePath);
                        if (file.exists()) {
                            logsVO.setExistReport(true);
                            logsVO.setOriginalName(attachmentInfoPO.getOriginalName());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("【getRuleScanLogPageList】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ruleLogsVOPage;
    }

    @Override
    public void downloadRuleScanResult(String uniqueId, HttpServletResponse response) {
        try {
            if (StringUtils.isEmpty(uniqueId)) {
                return;
            }
            QueryWrapper<AttachmentInfoPO> attachmentInfoPOQueryWrapper = new QueryWrapper<>();
            attachmentInfoPOQueryWrapper.lambda().eq(AttachmentInfoPO::getDelFlag, 1)
                    .eq(AttachmentInfoPO::getCategory, 300)
                    .eq(AttachmentInfoPO::getObjectId, uniqueId);
            AttachmentInfoPO attachmentInfoPO = attachmentInfoMapper.selectOne(attachmentInfoPOQueryWrapper);
            if (attachmentInfoPO == null) {
                return;
            }
            String filePath = attachmentInfoPO.getAbsolutePath() + File.separator + attachmentInfoPO.getCurrentFileName();
            log.info("【downloadRuleScanRecord】文件路径：" + filePath);
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
            log.error("【downloadRuleScanRecord】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
    }

    @Override
    public IntelligentDiscovery_RuleExtInfoVO getRuleExtInfo() {
        IntelligentDiscovery_RuleExtInfoVO intelligentDiscovery_ruleExtInfoVO = new IntelligentDiscovery_RuleExtInfoVO();
        List<IntelligentDiscovery_RuleExtInfo_UserVO> fiDataUsers = new ArrayList<>();
        List<IntelligentDiscovery_RuleExtInfo_DataSourceVO> dataSources = new ArrayList<>();
        List<IntelligentDiscovery_RuleExtInfo_EmailServiceVO> emailServices = new ArrayList<>();
        IntelligentDiscovery_RuleExtInfo_UserInfoVO userInfo = new IntelligentDiscovery_RuleExtInfo_UserInfoVO();

        try {
            // 数据源信息
            ResultEntity<List<DataSourceDTO>> dataDataSourceResult = userClient.getAll();
            List<DataSourceDTO> dataSourceList = dataDataSourceResult != null && dataDataSourceResult.getCode() == 0 && CollectionUtils.isNotEmpty(dataDataSourceResult.getData()) ? dataDataSourceResult.getData() : new ArrayList<>();
            dataSourceList = dataSourceList.stream().filter(t -> t.getConType() == DataSourceTypeEnum.SQLSERVER || t.getConType() == DataSourceTypeEnum.POSTGRESQL).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(dataSourceList)) {
                // 获取数据源信息的Schema和Table
                List<IntelligentDiscovery_RuleExtInfo_SchemaVO> schema_tableList = getSchema_TableList(dataSourceList);
                for (DataSourceDTO t : dataSourceList) {
                    IntelligentDiscovery_RuleExtInfo_DataSourceVO ruleExtInfo_dataSourceVO = new IntelligentDiscovery_RuleExtInfo_DataSourceVO();
                    ruleExtInfo_dataSourceVO.setDataSourceId(t.getId());
                    ruleExtInfo_dataSourceVO.setName(t.getName());
                    ruleExtInfo_dataSourceVO.setSourceType(t.getSourceType());
                    ruleExtInfo_dataSourceVO.setConDbname(t.getConDbname());
                    ruleExtInfo_dataSourceVO.setConIp(t.getConIp());
                    ruleExtInfo_dataSourceVO.setConPort(t.getConPort());
                    ruleExtInfo_dataSourceVO.setPlatform(t.getPlatform());
                    ruleExtInfo_dataSourceVO.setPurpose(t.getPurpose());
                    ruleExtInfo_dataSourceVO.setConType(t.getConType());
                    if (CollectionUtils.isNotEmpty(schema_tableList)) {
                        List<IntelligentDiscovery_RuleExtInfo_SchemaVO> schemaList = schema_tableList.stream()
                                .filter(s -> s.getIp().equals(t.getConIp()) && s.getDataBaseName().equals(t.getConDbname()))
                                .collect(Collectors.toList());
                        ruleExtInfo_dataSourceVO.setSchemas(schemaList);
                    }
                    dataSources.add(ruleExtInfo_dataSourceVO);
                }
            }

            // 用户列表
            ResultEntity<List<UserDTO>> userListResult = userClient.getAllUserList();
            List<UserDTO> userList = userListResult != null && userListResult.getCode() == 0 && CollectionUtils.isNotEmpty(userListResult.getData()) ? userListResult.getData() : null;
            if (CollectionUtils.isNotEmpty(userList)) {
                userList.forEach(t -> {
                    IntelligentDiscovery_RuleExtInfo_UserVO ruleExtInfo_userVO = new IntelligentDiscovery_RuleExtInfo_UserVO();
                    ruleExtInfo_userVO.setId(t.id);
                    ruleExtInfo_userVO.setUsername(t.getUsername());
                    ruleExtInfo_userVO.setUserAccount(t.getUserAccount());
                    ruleExtInfo_userVO.setEmail(t.getEmail());
                    ruleExtInfo_userVO.setValid(t.isValid());
                    fiDataUsers.add(ruleExtInfo_userVO);
                });
            }

            // 邮件服务器列表
            ResultEntity<List<EmailServerVO>> emailServerListResult = userClient.getEmailServerList();
            List<EmailServerVO> emailServerList = emailServerListResult != null && emailServerListResult.getCode() == 0 && CollectionUtils.isNotEmpty(emailServerListResult.getData()) ? emailServerListResult.getData() : null;
            if (CollectionUtils.isNotEmpty(emailServerList)) {
                emailServerList.forEach(t -> {
                    IntelligentDiscovery_RuleExtInfo_EmailServiceVO emailService = new IntelligentDiscovery_RuleExtInfo_EmailServiceVO();
                    emailService.setId(t.getId());
                    emailService.setName(t.getName());
                    emailServices.add(emailService);
                });
            }

            // 登录用户信息
            UserInfo loginUserInfo = userHelper.getLoginUserInfo();
            if (loginUserInfo != null) {
                userInfo.setId(loginUserInfo.getId());
                userInfo.setUsername(loginUserInfo.getUsername());
            }

            intelligentDiscovery_ruleExtInfoVO.setDataSources(dataSources);
            intelligentDiscovery_ruleExtInfoVO.setFiDataUsers(fiDataUsers);
            intelligentDiscovery_ruleExtInfoVO.setEmailServices(emailServices);
            intelligentDiscovery_ruleExtInfoVO.setUserInfo(userInfo);
        } catch (Exception ex) {
            log.error("【getRuleExtInfo】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return intelligentDiscovery_ruleExtInfoVO;
    }

    @Override
    public List<String> getNextCronExeTime(String cron) {
        try {
            NextCronTimeDTO dto = new NextCronTimeDTO();
            dto.setCronExpression(cron);
            dto.setNumber(3);
            return CronUtils.nextCronExeTime(dto);
        } catch (Exception ex) {
            log.error("【getNextCronExeTime】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
    }

    @Override
    public ResultEntity<List<IntelligentDiscovery_ScanResultVO>> previewScanResult(String absolutePath) {
        List<IntelligentDiscovery_ScanResultVO> scanResultVOS = new ArrayList<>();
        try {
            if (StringUtils.isEmpty(absolutePath)) {
                return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, scanResultVOS);
            }
            File file = new File(absolutePath);
            if (!file.exists()) {
                return ResultEntityBuild.buildData(ResultEnum.FILE_DOES_NOT_EXIST, scanResultVOS);
            }
            // 查询智能发现白名单配置
            QueryWrapper<IntelligentDiscovery_WhiteListPO> whiteListPOQueryWrapper = new QueryWrapper<>();
            whiteListPOQueryWrapper.lambda()
                    .eq(IntelligentDiscovery_WhiteListPO::getValidity, 1)
                    .eq(IntelligentDiscovery_WhiteListPO::getDelFlag, 1);
            List<IntelligentDiscovery_WhiteListPO> whiteListPOS = intelligentDiscovery_whiteListMapper.selectList(whiteListPOQueryWrapper);

            List<String> columns = new ArrayList<>();
            columns.add("DatabaseIP");
            columns.add("DatabaseName");
            columns.add("Schema");
            columns.add("TableName");
            columns.add("FieldName");
            List<HashMap<String, String>> mapList = ExcelReportUtil.readExcel(absolutePath, columns);
            if (CollectionUtils.isNotEmpty(mapList)) {
                for (Map<String, String> map : mapList) {
                    String dataBaseIp = map.get("DatabaseIP");
                    String dataBaseName = map.get("DatabaseName");
                    String schema = map.get("Schema");
                    String tableName = map.get("TableName");
                    String fieldName = map.get("FieldName");
                    int fieldState = 2;

                    if (CollectionUtils.isNotEmpty(whiteListPOS)) {
                        IntelligentDiscovery_WhiteListPO intelligentDiscovery_whiteListPO =
                                whiteListPOS.stream().filter(t -> t.getScanDatabaseIp().equals(dataBaseIp)
                                                && t.getScanDatabase().equals(dataBaseName)
                                                && t.getScanSchema().equals(schema)
                                                && t.getScanTable().equals(tableName)
                                                && t.getScanField().equals(fieldName))
                                        .findFirst().orElse(null);
                        if (intelligentDiscovery_whiteListPO != null) {
                            fieldState = 1;
                        }
                    }

                    IntelligentDiscovery_ScanResultVO scanResultVO = new IntelligentDiscovery_ScanResultVO();
                    scanResultVO.setScanDatabaseIp(dataBaseIp);
                    scanResultVO.setScanDatabase(dataBaseName);
                    scanResultVO.setScanSchema(schema);
                    scanResultVO.setScanTable(tableName);
                    scanResultVO.setScanField(fieldName);
                    scanResultVO.setFieldState(fieldState);
                    scanResultVOS.add(scanResultVO);
                }
            }
        } catch (Exception ex) {
            log.error("【previewRuleScanRecord】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, scanResultVOS);
    }

    @Override
    public ResultEntity<Object> createScanReport(int id) {
        try {
            if (id == 0) {
                return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, "");
            }
            // 查询智能发现基本信息
            IntelligentDiscovery_RulePO rulePO = baseMapper.selectById(id);
            if (rulePO == null) {
                return ResultEntityBuild.buildData(ResultEnum.INTELLIGENT_DISCOVERY_CONFIGURATION_DOES_NOT_EXIST, "");
            }
            // 查询智能发现通知配置
            QueryWrapper<IntelligentDiscovery_NoticePO> intelligentDiscovery_noticePOQueryWrapper = new QueryWrapper<>();
            intelligentDiscovery_noticePOQueryWrapper.lambda()
                    .eq(IntelligentDiscovery_NoticePO::getDelFlag, 1)
                    .eq(IntelligentDiscovery_NoticePO::getRuleId, id);
            IntelligentDiscovery_NoticePO noticePO = intelligentDiscovery_noticeMapper.selectOne(intelligentDiscovery_noticePOQueryWrapper);
            // 查询智能发现的发现规则配置
            String regExpRule = "";
            List<IntelligentDiscovery_KeyWordRuleVO> keyWordRules = null;
            if (StringUtils.isNotEmpty(rulePO.getRuleValue())) {
                if (rulePO.getRuleType() == 1) {
                    List<IntelligentDiscovery_KeyWordRuleVO> keyWordRuleVOList = JSONObject.parseArray(rulePO.getRuleValue(), IntelligentDiscovery_KeyWordRuleVO.class);
                    keyWordRules = keyWordRuleVOList;
                } else if (rulePO.getRuleType() == 2) {
                    regExpRule = rulePO.getRuleValue();
                }
            }
            // 查询智能发现扫描配置
            QueryWrapper<IntelligentDiscovery_ScanPO> scanPOQueryWrapper = new QueryWrapper<>();
            scanPOQueryWrapper.lambda()
                    .eq(IntelligentDiscovery_ScanPO::getDelFlag, 1)
                    .eq(IntelligentDiscovery_ScanPO::getRuleId, id);
            List<IntelligentDiscovery_ScanPO> scanPOS = intelligentDiscovery_scanMapper.selectList(scanPOQueryWrapper);
            if (CollectionUtils.isEmpty(scanPOS)) {
                return ResultEntityBuild.buildData(ResultEnum.INTELLIGENT_DISCOVERY_SCAN_CONFIGURATION_DOES_NOT_EXIST, "");
            }
            List<Integer> dataSourceIdList = scanPOS.stream().map(IntelligentDiscovery_ScanPO::getDatasourceId).distinct().collect(Collectors.toList());
            // 查询智能发现扫描配置的数据源信息
            ResultEntity<List<DataSourceDTO>> dataDataSourceResult = userClient.getAll();
            List<DataSourceDTO> dataSourceList = dataDataSourceResult != null && dataDataSourceResult.getCode() == 0 && CollectionUtils.isNotEmpty(dataDataSourceResult.getData()) ? dataDataSourceResult.getData() : new ArrayList<>();
            dataSourceList = dataSourceList.stream().filter(t -> dataSourceIdList.contains(t.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(dataSourceList)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_SOURCE_ERROR, "");
            }
            // 查询智能发现扫描结果接收人
            QueryWrapper<IntelligentDiscovery_UserPO> userPOQueryWrapper = new QueryWrapper<>();
            userPOQueryWrapper.lambda()
                    .eq(IntelligentDiscovery_UserPO::getDelFlag, 1)
                    .eq(IntelligentDiscovery_UserPO::getRuleId, id);
            List<IntelligentDiscovery_UserPO> userPOS = intelligentDiscovery_userMapper.selectList(userPOQueryWrapper);
            String recipientEmailStr = "";
            if (CollectionUtils.isNotEmpty(userPOS)) {
                List<String> toAddressList = userPOS.stream().filter(t -> StringUtils.isNotEmpty(t.getRecipientEmail())).map(IntelligentDiscovery_UserPO::getRecipientEmail).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(toAddressList)) {
                    recipientEmailStr = Joiner.on(";").join(toAddressList);
                }
            }
            // 查询智能发现白名单配置
            QueryWrapper<IntelligentDiscovery_WhiteListPO> whiteListPOQueryWrapper = new QueryWrapper<>();
            whiteListPOQueryWrapper.lambda()
                    .eq(IntelligentDiscovery_WhiteListPO::getValidity, 1)
                    .eq(IntelligentDiscovery_WhiteListPO::getDelFlag, 1);
            List<IntelligentDiscovery_WhiteListPO> whiteListPOS = intelligentDiscovery_whiteListMapper.selectList(whiteListPOQueryWrapper);

            // 第一步：根据配置扫描数据库表字段
            List<IntelligentDiscovery_ScanResultVO> schema_table_fieldList = getSchema_Table_FieldList(whiteListPOS, scanPOS, dataSourceList, regExpRule, keyWordRules);
            if (CollectionUtils.isEmpty(schema_table_fieldList)) {
                return ResultEntityBuild.buildData(ResultEnum.INTELLIGENT_DISCOVERY_NO_RISK_FIELDS_FOUND, "");
            }
            // 第二步：扫描出来的表字段写入到Excel并记录到附件信息
            AttachmentInfoPO attachmentInfoPO = new AttachmentInfoPO();
            String currentFileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
            attachmentInfoPO.setCurrentFileName(currentFileName);
            attachmentInfoPO.setExtensionName(".xlsx");
            attachmentInfoPO.setAbsolutePath(uploadUrl);
            attachmentInfoPO.setRelativePath(echoPath);
            attachmentInfoPO.setOriginalName(String.format("数据安全智能发现报告%s.xlsx", DateTimeUtils.getNowToShortDate().replace("-", "")));
            attachmentInfoPO.setCategory(300);

            ExcelDto excelDto = new ExcelDto();
            excelDto.setExcelName(attachmentInfoPO.getCurrentFileName());
            List<SheetDto> sheets = new ArrayList<>();
            SheetDto sheet = new SheetDto();
            sheet.setSheetName("智能发现报告");

            List<RowDto> headerRows = new ArrayList<>();
            RowDto headerRowDto = new RowDto();
            headerRowDto.setRowIndex(0);
            List<String> columns = new ArrayList<>();
            columns.add("DatabaseIP");
            columns.add("DatabaseName");
            columns.add("Schema");
            columns.add("TableName");
            columns.add("FieldName");
            headerRowDto.setColumns(columns);
            headerRows.add(headerRowDto);

            List<List<String>> dataRows = new ArrayList<>();
            for (IntelligentDiscovery_ScanResultVO scanResultVO : schema_table_fieldList) {
                List<String> dataRow = new ArrayList<>();
                dataRow.add(scanResultVO.getScanDatabaseIp());
                dataRow.add(scanResultVO.getScanDatabase());
                dataRow.add(scanResultVO.getScanSchema());
                dataRow.add(scanResultVO.getScanTable());
                dataRow.add(scanResultVO.getScanField());
                dataRows.add(dataRow);
            }

            sheet.setSingRows(headerRows);
            sheet.setDataRows(dataRows);
            sheets.add(sheet);
            if (CollectionUtils.isNotEmpty(sheets)) {
                excelDto.setSheets(sheets);
                ExcelReportUtil.createExcel(excelDto, attachmentInfoPO.getAbsolutePath(), attachmentInfoPO.getCurrentFileName(), false);
            }
            File file = new File(attachmentInfoPO.getAbsolutePath() + attachmentInfoPO.getCurrentFileName());
            if (!file.exists()) {
                return ResultEntityBuild.buildData(ResultEnum.SMART_DISCOVERY_REPORT_FAILED_TO_GENERATE_ATTACHMENT, "");
            }
            // 第三步：发送通知提醒给指定用户并记录发送日志
            if (noticePO != null) {
                IntelligentDiscovery_LogsPO logsPO = new IntelligentDiscovery_LogsPO();
                String logUniqueId = UUID.randomUUID().toString().replace("-", "");
                attachmentInfoPO.setObjectId(logUniqueId);
                logsPO.setRuleId(id);
                logsPO.setUniqueId(logUniqueId);
                logsPO.setRuleName(rulePO.getRuleName());
                logsPO.setScanReceptionTypeName(ScanReceptionTypeEnum.getEnum(noticePO.getScanReceptionType()).getName());
                logsPO.setScanRiskCount(Math.toIntExact(schema_table_fieldList.stream().count()));
                logsPO.setSendTime(DateTimeUtils.getNow());
                logsPO.setRecipientEmails(recipientEmailStr);

                if (noticePO.getScanReceptionType() == ScanReceptionTypeEnum.EMAIL_NOTICE.getValue()) {
                    ResultEntity<EmailServerVO> emailServerById = userClient.getEmailServerById(noticePO.getEmailServerId());
                    if (emailServerById == null || emailServerById.getCode() != ResultEnum.SUCCESS.getCode() || emailServerById.getData() == null) {
                        return ResultEntityBuild.buildData(ResultEnum.THE_MAIL_SERVER_DOES_NOT_EXIST, "");
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
                    mailSenderDTO.setSubject(noticePO.getSubject());
                    mailSenderDTO.setBody(noticePO.getBody());
                    mailSenderDTO.setToAddress(recipientEmailStr);
                    mailSenderDTO.setSendAttachment(true);
                    mailSenderDTO.setAttachmentName(attachmentInfoPO.getCurrentFileName());
                    mailSenderDTO.setAttachmentPath(attachmentInfoPO.getAbsolutePath());
                    mailSenderDTO.setAttachmentActualName(attachmentInfoPO.getOriginalName());
                    mailSenderDTO.setCompanyLogoPath(logoPaht);
                    try {
                        MailSenderUtils.send(mailServeiceDTO, mailSenderDTO);
                        logsPO.setSendResult("已发送");
                    } catch (Exception emailEx) {
                        logsPO.setSendResult("发送失败");
                    }
                }
                intelligentDiscovery_logsMapper.insert(logsPO);
                attachmentInfoMapper.insert(attachmentInfoPO);
            }

            // 第四步：回写扫描风险数量到配置表
            rulePO.setScanRiskCount(Math.toIntExact(schema_table_fieldList.stream().count()));
            baseMapper.updateById(rulePO);
        } catch (Exception ex) {
            log.error("【collScan】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "");
    }

    @Override
    public ResultEnum saveWhiteList(IntelligentDiscovery_WhiteListDTO dto) {
        try {
            if (dto == null) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            QueryWrapper<IntelligentDiscovery_WhiteListPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(IntelligentDiscovery_WhiteListPO::getScanDatabaseIp, dto.getScanDatabaseIp())
                    .eq(IntelligentDiscovery_WhiteListPO::getScanDatabase, dto.getScanDatabase())
                    .eq(IntelligentDiscovery_WhiteListPO::getScanSchema, dto.getScanSchema())
                    .eq(IntelligentDiscovery_WhiteListPO::getScanTable, dto.getScanTable())
                    .eq(IntelligentDiscovery_WhiteListPO::getScanField, dto.getScanField())
                    .eq(IntelligentDiscovery_WhiteListPO::getDelFlag, 1);
            IntelligentDiscovery_WhiteListPO intelligentDiscovery_whiteListPO = intelligentDiscovery_whiteListMapper.selectOne(queryWrapper);

            if (intelligentDiscovery_whiteListPO != null) {
                intelligentDiscovery_whiteListPO.setValidity(dto.getValidity());
                return intelligentDiscovery_whiteListMapper.updateById(intelligentDiscovery_whiteListPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
            } else {
                intelligentDiscovery_whiteListPO = IntelligentDiscovery_WhiteListMap.INSTANCES.dtoToPo(dto);
                return intelligentDiscovery_whiteListMapper.insert(intelligentDiscovery_whiteListPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
            }
            // 前台根据Validity字段给提示消息："已移入白名单" OR "已移出白名单"
        } catch (Exception ex) {
            log.error("【saveWhiteList】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
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
            unifiedControlDTO.setTopic(MqConstants.QueueConstants.DataSecurityTopicConstants.BUILD_DATA_SECURITY_INTELLIGENT_DISCOVERY_FLOW);
            unifiedControlDTO.setType(OlapTableEnum.DATASECURITY);
            unifiedControlDTO.setDataClassifyEnum(DataClassifyEnum.UNIFIEDCONTROL);
            unifiedControlDTO.setDeleted(isDelTask);
            log.info("【数据安全】【publishBuild_unifiedControlTask】创建nifi调度任务请求参数：" + JSON.toJSONString(unifiedControlDTO));
            ResultEntity<Object> result = publishTaskClient.publishBuildunifiedControlTask(unifiedControlDTO);
            if (result != null) {
                resultEnum = ResultEnum.getEnum(result.getCode());
            }
        } catch (Exception ex) {
            log.error("【数据安全】【publishBuild_unifiedControlTask】ex：" + ex);
            return ResultEnum.SUCCESS;
        }
        return resultEnum;
    }

    public List<IntelligentDiscovery_RuleExtInfo_SchemaVO> getSchema_TableList(List<DataSourceDTO> dataSourceList) {
        List<IntelligentDiscovery_RuleExtInfo_SchemaVO> schemaList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dataSourceList)) {
            return schemaList;
        }
        for (DataSourceDTO dataSource : dataSourceList) {
            // 通过ip+dataBaseName判断数据源配置是否已处理
            List<IntelligentDiscovery_RuleExtInfo_SchemaVO> processedDataSource = schemaList
                    .stream()
                    .filter(t -> t.getIp().equals(dataSource.getConIp())
                            && t.getDataBaseName().equals(dataSource.getConDbname()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(processedDataSource)) {
                continue;
            }

            IBuildGovernanceSqlCommand dbCommand = BuildGovernanceHelper.getDBCommand(dataSource.getConType());
            String buildQuerySchemaSql = dbCommand.buildQuerySchemaSql();
            AbstractCommonDbHelper dbHelper = new AbstractCommonDbHelper();
            Connection conn = dbHelper.connection(dataSource.getConStr(), dataSource.getConAccount(), dataSource.getConPassword(), dataSource.getConType());
            // 查询数据库Schema元数据信息
            List<Map<String, Object>> schema_Maps = dbHelper.batchExecQueryResultMaps_noClose(buildQuerySchemaSql, conn);
            List<String> querySchemaList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(schema_Maps)) {
                for (Map<String, Object> map : schema_Maps) {
                    String schema = map.get("schema").toString();
                    querySchemaList.add(schema);
                }
            }
            // 查询数据库Schema下Table元数据信息
            if (CollectionUtils.isNotEmpty(querySchemaList)) {
                String buildQuerySchema_tableSql = dbCommand.buildQuerySchema_TableSql(querySchemaList);
                List<Map<String, Object>> schema_TableMaps = dbHelper.batchExecQueryResultMaps_noClose(buildQuerySchema_tableSql, conn);
                if (CollectionUtils.isNotEmpty(schema_TableMaps)) {
                    for (Map<String, Object> map : schema_TableMaps) {
                        String schema = map.get("schema").toString();
                        String tableName = map.get("tablename").toString();
                        IntelligentDiscovery_RuleExtInfo_SchemaVO schemaVO = schemaList
                                .stream()
                                .filter(t -> t.getIp().equals(dataSource.getConIp())
                                        && t.getDataBaseName().equals(dataSource.getConDbname())
                                        && t.getSchema().equals(schema))
                                .findFirst()
                                .orElse(null);
                        if (schemaVO == null) {
                            schemaVO = new IntelligentDiscovery_RuleExtInfo_SchemaVO();
                            schemaVO.setSchema(schema);
                            schemaVO.setIp(dataSource.getConIp());
                            schemaVO.setDataBaseName(dataSource.getConDbname());
                            schemaVO.tableNameList = new ArrayList<>();
                            schemaVO.tableNameList.add(tableName);
                            schemaList.add(schemaVO);
                        } else {
                            schemaVO.tableNameList.add(tableName);
                        }
                    }
                }
            }
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return schemaList;
    }

    public List<IntelligentDiscovery_ScanResultVO> getSchema_Table_FieldList(List<IntelligentDiscovery_WhiteListPO> whiteList, List<IntelligentDiscovery_ScanPO> scanList, List<DataSourceDTO> dataSourceList, String regExpRule, List<IntelligentDiscovery_KeyWordRuleVO> keyWordRules) {
        List<IntelligentDiscovery_ScanResultVO> scanResult = new ArrayList<>();

        List<KeyValueMapDto> queryFieldList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(keyWordRules)) {
            // 智能发现方式-关键字
            keyWordRules.forEach(t -> {
                KeyValueMapDto keyValueMapDto = new KeyValueMapDto();
                keyValueMapDto.setKey(t.getOperator());
                keyValueMapDto.setValue(t.getValue());
                queryFieldList.add(keyValueMapDto);
            });
        } else if (StringUtils.isNotEmpty(regExpRule)) {
            // 智能发现方式-正则表达式
        }

        for (IntelligentDiscovery_ScanPO scanPO : scanList) {
            DataSourceDTO dataSource = dataSourceList.stream()
                    .filter(t -> t.getId().equals(scanPO.getDatasourceId())
                            && (t.getConType() == DataSourceTypeEnum.SQLSERVER || t.getConType() == DataSourceTypeEnum.POSTGRESQL))
                    .findFirst()
                    .orElse(null);
            if (dataSource == null) {
                continue;
            }

            IBuildGovernanceSqlCommand dbCommand = BuildGovernanceHelper.getDBCommand(dataSource.getConType());
            AbstractCommonDbHelper dbHelper = new AbstractCommonDbHelper();
            Connection conn = dbHelper.connection(dataSource.getConStr(), dataSource.getConAccount(), dataSource.getConPassword(), dataSource.getConType());

            List<String> querySchemaList = new ArrayList<>();
            if (StringUtils.isEmpty(scanPO.getScanSchema())) {
                String buildQuerySchemaSql = dbCommand.buildQuerySchemaSql();
                // 查询数据库Schema元数据信息
                List<Map<String, Object>> schema_Maps = dbHelper.batchExecQueryResultMaps_noClose(buildQuerySchemaSql, conn);
                if (CollectionUtils.isNotEmpty(schema_Maps)) {
                    for (Map<String, Object> map : schema_Maps) {
                        querySchemaList.add(map.get("schema").toString());
                    }
                }
            } else {
                querySchemaList.add(scanPO.getScanSchema());
            }

            List<String> queryTableList = new ArrayList<>();
            if (StringUtils.isNotEmpty(scanPO.getScanTable())) {
                queryTableList.add(scanPO.getScanTable());
            }

            if (CollectionUtils.isNotEmpty(querySchemaList)) {
                String buildQuerySchema_table_fieldSql = dbCommand.buildQuerySchema_Table_FieldSql(querySchemaList, queryTableList, queryFieldList);
                // 查询数据库Field元数据信息
                List<Map<String, Object>> schema_Table_Field_Maps = dbHelper.batchExecQueryResultMaps_noClose(buildQuerySchema_table_fieldSql, conn);
                if (CollectionUtils.isNotEmpty(schema_Table_Field_Maps)) {
                    IntelligentDiscovery_ScanResultVO scanResultVO = null;
                    for (Map<String, Object> map : schema_Table_Field_Maps) {
                        boolean isMatch = true;
                        if (StringUtils.isNotEmpty(regExpRule)) {
                            isMatch = Pattern.matches(regExpRule, map.get("fieldname").toString());
                        }
                        if (!isMatch) {
                            continue;
                        }
                        String dataBaseIp = dataSource.getConIp();
                        String dataBaseName = dataSource.getConDbname();
                        String schema = map.get("schema").toString();
                        String tableName = map.get("tablename").toString();
                        String fieldName = map.get("fieldname").toString();
                        String fieldType = map.get("fieldtype").toString();
                        String fieldLength = map.get("fieldlength").toString();
                        String fieldComment = map.get("fieldcomment").toString();
                        String fieldIsPrimaryKey = map.get("fieldisprimarykey").toString();
                        String fieldDefaultValue = map.get("fielddefaultvalue").toString();
                        String fieldIsAllowNull = map.get("fieldisallownull").toString();

                        scanResultVO = scanResult.stream().filter(t -> t.getScanDatabaseIp().equals(dataBaseIp)
                                        && t.getScanDatabase().equals(dataBaseName)
                                        && t.getScanSchema().equals(schema)
                                        && t.getScanTable().equals(tableName)
                                        && t.getScanField().equals(fieldName))
                                .findFirst().orElse(null);
                        if (scanResultVO != null) {
                            continue;
                        }

                        if (CollectionUtils.isNotEmpty(whiteList)) {
                            IntelligentDiscovery_WhiteListPO intelligentDiscovery_whiteListPO =
                                    whiteList.stream().filter(t -> t.getScanDatabaseIp().equals(dataBaseIp)
                                                    && t.getScanDatabase().equals(dataBaseName)
                                                    && t.getScanSchema().equals(schema)
                                                    && t.getScanTable().equals(tableName)
                                                    && t.getScanField().equals(fieldName))
                                            .findFirst().orElse(null);
                            if (intelligentDiscovery_whiteListPO != null) {
                                continue;
                            }
                        }

                        scanResultVO = new IntelligentDiscovery_ScanResultVO();
                        scanResultVO.setScanDatabaseIp(dataBaseIp);
                        scanResultVO.setScanDatabase(dataBaseName);
                        scanResultVO.setScanSchema(schema);
                        scanResultVO.setScanTable(tableName);
                        scanResultVO.setScanField(fieldName);
                        scanResultVO.setFieldType(fieldType);
                        scanResultVO.setFieldLength(fieldLength);
                        scanResultVO.setFieldComment(fieldComment);
                        scanResultVO.setFieldIsPrimaryKey(fieldIsPrimaryKey);
                        scanResultVO.setFieldDefaultValue(fieldDefaultValue);
                        scanResultVO.setFieldIsAllowNull(fieldIsAllowNull);
                        scanResult.add(scanResultVO);
                    }
                }
            }
        }
        return scanResult;
    }
}


