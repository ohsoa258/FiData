package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeEditDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeQueryDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.map.dataquality.NoticeExtendMap;
import com.fisk.datagovernance.map.dataquality.NoticeMap;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.INoticeManageService;
import com.fisk.datagovernance.vo.dataquality.emailserver.EmailServerVO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeDetailVO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeModuleVO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 通知实现类
 * @date 2022/3/23 12:56
 */
@Service
public class NoticeManageImpl extends ServiceImpl<NoticeMapper, NoticePO> implements INoticeManageService {

    @Resource
    private NoticeExtendMapper noticeExtendMapper;

    @Resource
    private NoticeExtendManageImpl noticeExtendManageImpl;

    @Resource
    private DataCheckMapper dataCheckMapper;

    @Resource
    private BusinessFilterMapper businessFilterMapper;

    @Resource
    private LifecycleMapper lifecycleMapper;

    @Resource
    private EmailServerMapper emailServerMapper;

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    UserHelper userHelper;

    @Override
    public Page<NoticeVO> getAll(NoticeQueryDTO query) {
        return baseMapper.getAll(query.page, query.keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(NoticeDTO dto) {
        //第一步：验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //第二步：转换DTO对象为PO对象
        NoticePO noticePO = NoticeMap.INSTANCES.dtoToPo(dto);
        if (noticePO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存通知信息
        noticePO.setCreateTime(LocalDateTime.now());
        noticePO.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
        int i = baseMapper.insertOne(noticePO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存通知扩展信息
        if (CollectionUtils.isNotEmpty(dto.noticeExtends)) {
            List<NoticeExtendPO> noticeExtendPOS = new ArrayList<>();
            dto.noticeExtends.forEach(t -> {
                NoticeExtendPO noticeExtendPO = new NoticeExtendPO();
                noticeExtendPO.setNoticeId(Math.toIntExact(noticePO.id));
                noticeExtendPO.setModuleType(t.moduleType.getValue());
                noticeExtendPO.setRuleId(t.ruleId);
                noticeExtendPOS.add(noticeExtendPO);
            });
            noticeExtendManageImpl.saveBatch(noticeExtendPOS);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(NoticeEditDTO dto) {
        //第一步：验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        NoticePO noticePO = baseMapper.selectById(dto.id);
        if (noticePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //第二步：转换DTO对象为PO对象
        noticePO = NoticeMap.INSTANCES.dtoToPo_Edit(dto);
        if (noticePO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存通知信息
        int i = baseMapper.updateById(noticePO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存通知扩展信息
        if (CollectionUtils.isNotEmpty(dto.noticeExtends)) {
            noticeExtendMapper.updateByNoticeId(dto.id);
            List<NoticeExtendPO> noticeExtendPOS = new ArrayList<>();
            dto.noticeExtends.forEach(t -> {
                NoticeExtendPO noticeExtendPO = new NoticeExtendPO();
                noticeExtendPO.setNoticeId(t.noticeId);
                noticeExtendPO.setModuleType(t.moduleType.getValue());
                noticeExtendPO.setRuleId(t.ruleId);
                noticeExtendPOS.add(noticeExtendPO);
            });
            noticeExtendManageImpl.saveBatch(noticeExtendPOS);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(int id) {
        NoticePO noticePO = baseMapper.selectById(id);
        if (noticePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        noticeExtendMapper.updateByNoticeId(id);
        return baseMapper.deleteByIdWithFill(noticePO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<Object> sendEmialNotice(NoticeDTO dto) {
        //第一步：查询邮件服务器设置
        EmailServerPO emailServerPO = emailServerMapper.selectById(dto.emailServerId);
        if (emailServerPO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_NOTEXISTS, "邮件服务器不存在");
        }
        MailServeiceDTO mailServeiceDTO = new MailServeiceDTO();
        mailServeiceDTO.setOpenAuth(true);
        mailServeiceDTO.setOpenDebug(true);
        mailServeiceDTO.setHost(emailServerPO.getEmailServer());
        mailServeiceDTO.setProtocol(EmailServerTypeEnum.getEnum(emailServerPO.getEmailServerType()).getName());
        mailServeiceDTO.setUser(emailServerPO.getEmailServerAccount());
        mailServeiceDTO.setPassword(emailServerPO.getEmailServerPwd());
        mailServeiceDTO.setPort(mailServeiceDTO.getPort());
        MailSenderDTO mailSenderDTO = new MailSenderDTO();
        mailSenderDTO.setUser(emailServerPO.getEmailServerAccount());
        mailSenderDTO.setSubject(dto.emailSubject);
        mailSenderDTO.setBody(dto.body);
        mailSenderDTO.setToAddress(dto.emailConsignee);
        mailSenderDTO.setToCc(dto.emailCc);
        try {
            //第二步：调用邮件发送方法
            MailSenderUtils.send(mailServeiceDTO, mailSenderDTO);
        } catch (Exception ex) {
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "发送完成");
    }

    @Override
    public ResultEntity<NoticeDetailVO> getNoticeRuleInfo(int noticeId) {
        NoticeDetailVO noticeDetailVO = new NoticeDetailVO();

        List<NoticeModuleVO> noticeRule_DataCheck = new ArrayList<>();
        List<NoticeModuleVO> noticeRule_BusinessFilter = new ArrayList<>();
        List<NoticeModuleVO> noticeRule_Lifecycle = new ArrayList<>();
        List<EmailServerVO> emailServerVOS = new ArrayList<>();
        List<Long> noticeIds_DataCheck = new ArrayList<>();
        List<Long> noticeIds_BusinessFilter = new ArrayList<>();
        List<Long> noticeIds_Lifecycle = new ArrayList<>();

        //第一步：查询模板组件关联信息
        QueryWrapper<NoticeExtendPO> noticeExtendPOQueryWrapper = new QueryWrapper<>();
        if (noticeId > 0) {
            noticeExtendPOQueryWrapper.lambda().eq(NoticeExtendPO::getDelFlag, 1)
                    .eq(NoticeExtendPO::getNoticeId, noticeId);
        } else {
            noticeExtendPOQueryWrapper.lambda().eq(NoticeExtendPO::getDelFlag, 1);
        }
        List<NoticeExtendPO> noticeExtendPOS = noticeExtendMapper.selectList(noticeExtendPOQueryWrapper);

        //第二步：查询所有质量报告模板
        List<Integer> templateScene = new ArrayList<>();
        templateScene.add(TemplateSceneEnum.DATACHECK_QUALITYREPORT.getValue());
        templateScene.add(TemplateSceneEnum.BUSINESSFILTER_FILTERREPORT.getValue());
        templateScene.add(TemplateSceneEnum.LIFECYCLE_REPORT.getValue());
        QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
        templatePOQueryWrapper.lambda().eq(TemplatePO::getDelFlag, 1)
                .in(TemplatePO::getTemplateScene, templateScene);
        List<TemplatePO> templatePOS = templateMapper.selectList(templatePOQueryWrapper);
        if (CollectionUtils.isEmpty(templatePOS)) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS, noticeDetailVO);
        }

        //第三步：查询数据校验质量报告规则信息
        List<Long> templateIds = templatePOS.stream().
                filter(t -> t.moduleType == ModuleTypeEnum.DATACHECK_MODULE.getValue())
                .map(m -> m.getId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(templateIds)) {
            QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
            dataCheckPOQueryWrapper.lambda().eq(DataCheckPO::getDelFlag, 1)
                    .in(DataCheckPO::getTemplateId, templateIds)
                    .orderByAsc(DataCheckPO::getRuleSort);
            List<DataCheckPO> dataCheckPOS = dataCheckMapper.selectList(dataCheckPOQueryWrapper);
            if (CollectionUtils.isNotEmpty(dataCheckPOS)) {
                dataCheckPOS.forEach(e -> {
                    NoticeModuleVO noticeModuleVO = new NoticeModuleVO();
                    NoticeExtendPO noticeExtendPO = null;
                    if (CollectionUtils.isNotEmpty(noticeExtendPOS)) {
                        noticeExtendPO = noticeExtendPOS.stream().filter
                                (item -> item.moduleType == ModuleTypeEnum.DATACHECK_MODULE.getValue()
                                        && item.ruleId == e.id).findFirst().orElse(null);
                    }
                    if (noticeExtendPO != null) {
                        noticeModuleVO.checkd = 1;
                        noticeModuleVO.noticeExtId = noticeExtendPO.id;
                        noticeIds_DataCheck.add(e.id);
                    }
                    noticeModuleVO.noticeId = noticeId;
                    noticeModuleVO.moduleType = ModuleTypeEnum.DATACHECK_MODULE;
                    noticeModuleVO.ruleId = Math.toIntExact(e.id);
                    noticeModuleVO.ruleName = e.ruleName;
                    noticeRule_DataCheck.add(noticeModuleVO);
                });
            }
        }

        //第四步：查询业务清洗质量报告规则信息
        templateIds = templatePOS.stream().
                filter(t -> t.moduleType == ModuleTypeEnum.BIZCHECK_MODULE.getValue())
                .map(m -> m.getId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(templateIds)) {
            QueryWrapper<BusinessFilterPO> businessFilterPOQueryWrapper = new QueryWrapper<>();
            businessFilterPOQueryWrapper.lambda().eq(BusinessFilterPO::getDelFlag, 1)
                    .in(BusinessFilterPO::getTemplateId, templateIds)
                    .orderByAsc(BusinessFilterPO::getRuleSort);
            List<BusinessFilterPO> businessFilterPOS = businessFilterMapper.selectList(businessFilterPOQueryWrapper);
            if (CollectionUtils.isNotEmpty(businessFilterPOS)) {
                businessFilterPOS.forEach(e -> {
                    NoticeModuleVO noticeModuleVO = new NoticeModuleVO();
                    NoticeExtendPO noticeExtendPO = null;
                    if (CollectionUtils.isNotEmpty(noticeExtendPOS)) {
                        noticeExtendPO = noticeExtendPOS.stream().filter
                                (item -> item.moduleType == ModuleTypeEnum.BIZCHECK_MODULE.getValue()
                                        && item.ruleId == e.id).findFirst().orElse(null);
                    }
                    if (noticeExtendPO != null) {
                        noticeModuleVO.checkd = 1;
                        noticeModuleVO.noticeExtId = noticeExtendPO.id;
                        noticeIds_BusinessFilter.add(e.id);
                    }
                    noticeModuleVO.noticeId = noticeId;
                    noticeModuleVO.moduleType = ModuleTypeEnum.BIZCHECK_MODULE;
                    noticeModuleVO.ruleId = Math.toIntExact(e.id);
                    noticeModuleVO.ruleName = e.ruleName;
                    noticeRule_BusinessFilter.add(noticeModuleVO);
                });
            }
        }

        //第五步：查询生命周期质量报告规则信息
        templateIds = templatePOS.stream().
                filter(t -> t.moduleType == ModuleTypeEnum.LIFECYCLE_MODULE.getValue())
                .map(m -> m.getId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(templateIds)) {
            QueryWrapper<LifecyclePO> lifecyclePOQueryWrapper = new QueryWrapper<>();
            lifecyclePOQueryWrapper.lambda().eq(LifecyclePO::getDelFlag, 1)
                    .in(LifecyclePO::getTemplateId, templateIds);
            List<LifecyclePO> lifecyclePOS = lifecycleMapper.selectList(lifecyclePOQueryWrapper);
            if (CollectionUtils.isNotEmpty(lifecyclePOS) && CollectionUtils.isNotEmpty(noticeExtendPOS)) {
                lifecyclePOS.forEach(e -> {
                    NoticeModuleVO noticeModuleVO = new NoticeModuleVO();
                    NoticeExtendPO noticeExtendPO = null;
                    if (CollectionUtils.isNotEmpty(noticeExtendPOS)) {
                        noticeExtendPO = noticeExtendPOS.stream().filter
                                (item -> item.moduleType == ModuleTypeEnum.LIFECYCLE_MODULE.getValue()
                                        && item.ruleId == e.id).findFirst().orElse(null);
                    }
                    if (noticeExtendPO != null) {
                        noticeModuleVO.checkd = 1;
                        noticeModuleVO.noticeExtId = noticeExtendPO.id;
                        noticeIds_Lifecycle.add(e.id);
                    }
                    noticeModuleVO.noticeId = noticeId;
                    noticeModuleVO.moduleType = ModuleTypeEnum.LIFECYCLE_MODULE;
                    noticeModuleVO.ruleId = Math.toIntExact(e.id);
                    noticeModuleVO.ruleName = e.ruleName;
                    noticeRule_Lifecycle.add(noticeModuleVO);
                });
            }
        }

        //第六步：获取邮件服务器信息
        QueryWrapper<EmailServerPO> emailServerPOQueryWrapper = new QueryWrapper<>();
        emailServerPOQueryWrapper.lambda().eq(EmailServerPO::getDelFlag, 1);
        List<EmailServerPO> emailServerPOS = emailServerMapper.selectList(emailServerPOQueryWrapper);
        if (CollectionUtils.isNotEmpty(emailServerPOS)) {
            emailServerPOS.forEach(e -> {
                EmailServerVO emailServerVO = new EmailServerVO();
                emailServerVO.setId(Math.toIntExact(e.getId()));
                emailServerVO.setName(e.getName());
                emailServerVOS.add(emailServerVO);
            });
        }

        noticeDetailVO.noticeRule_DataCheck = noticeRule_DataCheck;
        noticeDetailVO.noticeRule_BusinessFilter = noticeRule_BusinessFilter;
        noticeDetailVO.noticeRule_Lifecycle = noticeRule_Lifecycle;
        noticeDetailVO.noticeIds_DataCheck = noticeIds_DataCheck;
        noticeDetailVO.noticeIds_BusinessFilter = noticeIds_BusinessFilter;
        noticeDetailVO.noticeIds_Lifecycle = noticeIds_Lifecycle;
        noticeDetailVO.emailServerVOS = emailServerVOS;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, noticeDetailVO);
    }
}