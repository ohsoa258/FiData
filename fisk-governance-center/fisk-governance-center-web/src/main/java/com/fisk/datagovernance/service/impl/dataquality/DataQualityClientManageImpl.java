package com.fisk.datagovernance.service.impl.dataquality;

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
import com.fisk.common.core.utils.office.excel.ExcelReportUtil;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.TableRuleSqlDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataQualityClientManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.datagovernance.vo.dataquality.rule.TableRuleTempVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
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

    @Resource
    private RedisUtil redisUtil;

    @Value("${file.uploadUrl}")
    private String uploadUrl;
    @Value("${file.echoPath}")
    private String echoPath;

    @Override
    public ResultEntity<TableRuleInfoDTO> getTableRuleList(int dataSourceId, String tableUnique, int tableBusinessType) {
        if (dataSourceId == 0 || StringUtils.isEmpty(tableUnique) || tableBusinessType == 0) {
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
                .eq(DataCheckPO::getTableBusinessType, tableBusinessType)
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
                .eq(BusinessFilterPO::getTableBusinessType, tableBusinessType)
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
                .eq(LifecyclePO::getTableBusinessType, tableBusinessType)
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

    @Override
    public ResultEntity<Object> createQualityReport(int id) {
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
        switch (templateSceneEnum) {
            case NOTICE_DATACHECK:
                // 生成数据校验质量报告
                attachmentInfoPO.setOriginalName(String.format("数据校验质量报告%s.xlsx",
                        DateTimeUtils.getNowToShortDate().replace("-", "")));
                attachmentInfoPO.setCategory(100);
                createDataCheckQualityReport(noticeExtendPOS, allDataSource, attachmentInfoPO);
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
            noticeManageImpl.sendEmailNotice(noticeDTO);
        }

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
    public ResultEnum createDataCheckQualityReport(List<NoticeExtendPO> noticeExtendPOS, List<DataSourceConVO> allDataSource, AttachmentInfoPO attachmentInfoPO) {

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
        List<FiDataMetaDataDTO> fiDataMetaDatas = dataSourceConManageImpl.getTableFieldName(dtoList);
        if (CollectionUtils.isEmpty(fiDataMetaDatas)) {
            return ResultEnum.DATA_QUALITY_REDIS_NOTEXISTSTABLEFIELD;
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
            DataCheckExtendPO dataCheckExtendPO = dataCheckExtendPOList.get(0);
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.templateId);
            if (templatePO == null) {
                continue;
            }
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getId() == dataCheckPO.getDatasourceId()).findFirst().orElse(null);
            if (dataSourceConVO == null) {
                continue;
            }

            String tableName = "";
            List<String> fieldNames = new ArrayList<>();
            if (dataSourceConVO.getDatasourceType() == SourceTypeEnum.FiData) {
                FiDataMetaDataDTO fiDataMetaDataDTO = fiDataMetaDatas.stream().filter(t -> t.getDataSourceId() == dataSourceConVO.getDatasourceId()).findFirst().orElse(null);
                if (fiDataMetaDataDTO == null) {
                    continue;
                }
                FiDataMetaDataTreeDTO fiDataMetaDataTree_Table = fiDataMetaDataDTO.getChildren().stream().filter(t -> t.getId().equals(dataCheckPO.getTableUnique()) && t.getLabelBusinessType() == dataCheckPO.getTableBusinessType()).findFirst().orElse(null);
                if (fiDataMetaDataTree_Table != null) {
                    tableName = fiDataMetaDataTree_Table.getLabel();
                    if (CollectionUtils.isNotEmpty(fiDataMetaDataTree_Table.getChildren())) {
                        for (int k = 0; k < dataCheckExtendPOList.size(); k++) {
                            DataCheckExtendPO dataCheckExtendPO1 = dataCheckExtendPOList.get(k);
                            if (StringUtils.isNotEmpty(dataCheckExtendPO1.getFieldUnique())) {
                                FiDataMetaDataTreeDTO fiDataMetaDataTree_Field = fiDataMetaDataTree_Table.getChildren().stream().
                                        filter(f -> f.getId().equals(dataCheckExtendPO1.getFieldUnique())).findFirst().orElse(null);
                                if (fiDataMetaDataTree_Field != null) {
                                    fieldNames.add(fiDataMetaDataTree_Field.label);
                                }
                            }
                        }
                    }
                }
            } else {
                tableName = dataCheckPO.getTableUnique();
                fieldNames = dataCheckExtendPOs.stream().map(DataCheckExtendPO::getFieldUnique).collect(Collectors.toList());
            }
            TableRuleSqlDTO tableRuleSqlDTO = new TableRuleSqlDTO();
            tableRuleSqlDTO.setTableName(tableName);
            if (CollectionUtils.isNotEmpty(fieldNames)) {
                tableRuleSqlDTO.setFieldName(fieldNames.get(0));
                tableRuleSqlDTO.setFieldNames(fieldNames);
            }
            tableRuleSqlDTO.setTemplateTypeEnum(TemplateTypeEnum.getEnum(templatePO.getTemplateType()));
            tableRuleSqlDTO.setFieldAggregate(dataCheckExtendPO.getFieldAggregate());
            tableRuleSqlDTO.setThresholdValue(dataCheckPO.getThresholdValue());
            tableRuleSqlDTO.setSql(dataCheckPO.getCreateRule());
            String roleSql = createRole(dataSourceConVO, tableRuleSqlDTO);
            if (StringUtils.isEmpty(roleSql)) {
                continue;
            }

            SheetDto sheet = new SheetDto();
            sheet.setSheetName(dataCheckPO.getRuleName());
            SheetDataDto sheetDataDto = resultSetToMap(dataSourceConVO, roleSql);
            List<RowDto> singRows = getSingRows(tableName, templatePO.templatenName, sheetDataDto.columns);
            sheet.setSingRows(singRows);
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
            // 数据源类型
            DataSourceTypeEnum sourceTypeEnum = dataSourceCon.getConType();
            // 数据库连接对象
            conn = dataSourceConManageImpl.getStatement(sourceTypeEnum.getDriverName(), dataSourceCon.getConStr(),
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
                sql = createData_MissingRule(tableRuleSqlDTO);
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
    public String createData_MissingRule(TableRuleSqlDTO tableRuleSqlDTO) {
        String sql = String.format("SELECT * FROM %s WHERE %s IS NULL OR %s = '' ",
                tableRuleSqlDTO.getTableName(), tableRuleSqlDTO.getFieldName(), tableRuleSqlDTO.getFieldName());
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

            conn = dataSourceConManageImpl.getStatement(sourceTypeEnum.getDriverName(), dataSourceConPO.getConStr(),
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

