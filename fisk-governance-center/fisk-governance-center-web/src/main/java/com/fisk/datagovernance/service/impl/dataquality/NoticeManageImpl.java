package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
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
import com.fisk.datagovernance.map.dataquality.NoticeMap;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.INoticeManageService;
import com.fisk.datagovernance.vo.dataquality.emailserver.EmailServerVO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeDetailVO;
import com.fisk.datagovernance.vo.dataquality.notice.ComponentNotificationVO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeModuleVO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.UnifiedControlDTO;
import com.fisk.task.enums.DataClassifyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 告警通知实现类
 * @date 2022/3/23 12:56
 */
@Service
public class NoticeManageImpl extends ServiceImpl<NoticeMapper, NoticePO> implements INoticeManageService {

    @Resource
    private ComponentNotificationMapImpl componentNotificationMapImpl;

    @Resource
    private ComponentNotificationMapper componentNotificationMapper;

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

    @Resource
    PublishTaskClient publishTaskClient;

    @Override
    public Page<NoticeVO> getAll(NoticeQueryDTO query) {
        return baseMapper.getAll(query.page, query.keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(NoticeDTO dto) {
        //验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //第一步：转换DTO对象为PO对象
        NoticePO noticePO = NoticeMap.INSTANCES.dtoToPo(dto);
        if (noticePO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        noticePO.setCreateTime(LocalDateTime.now());
        noticePO.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
        int i = baseMapper.insertOne(noticePO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存数据组件通知信息
        ResultEnum resultEnum = componentNotificationMapImpl.saveNoticeData(noticePO.getId(), dto.componentNotificationDTOS, false);
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(NoticeEditDTO dto) {
        //验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        NoticePO noticePO = baseMapper.selectById(dto.id);
        if (noticePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //第一步：转换DTO对象为PO对象
        noticePO = NoticeMap.INSTANCES.dtoToPo_Edit(dto);
        if (noticePO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        int i = baseMapper.updateById(noticePO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存数据组件通知信息
        ResultEnum resultEnum = componentNotificationMapImpl.saveNoticeData(noticePO.getId(), dto.componentNotificationDTOS, true);
        return resultEnum;
    }

    @Override
    public ResultEnum deleteData(int id) {
        NoticePO noticePO = baseMapper.selectById(id);
        if (noticePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        componentNotificationMapImpl.updateDelFlag(noticePO.getId(), 0, 0);
        return baseMapper.deleteByIdWithFill(noticePO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public NoticeDetailVO getNotificationInfo() {
        NoticeDetailVO noticeDetailVO = new NoticeDetailVO();
        List<ComponentNotificationVO> notificationVOS = new ArrayList<>();
        //第一步：查询模板组件关联信息
        QueryWrapper<ComponentNotificationPO> notificationPOQueryWrapper = new QueryWrapper<>();
        notificationPOQueryWrapper.lambda().eq(ComponentNotificationPO::getDelFlag, 1);
        List<ComponentNotificationPO> componentNotificationPOS = componentNotificationMapper.selectList(notificationPOQueryWrapper);

        final int[] id = {1};
        //第一步：根据查询所有模板组件信息
        QueryWrapper<DataCheckPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataCheckPO::getDelFlag, 1);
        List<DataCheckPO> dataCheckPOS = dataCheckMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(dataCheckPOS)) {
            dataCheckPOS.forEach(e -> {
                ComponentNotificationVO notificationVO = new ComponentNotificationVO();
                if (CollectionUtils.isNotEmpty(componentNotificationPOS)) {
                    List<ComponentNotificationPO> collect = componentNotificationPOS.stream().filter(
                            item -> item.getModuleId() == e.getId() && item.getTemplateId() == e.getTemplateId()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect)) {
                        notificationVO.noticeIds = collect.stream().map(ComponentNotificationPO::getNoticeId);
                    }
                }
                notificationVO.id = id[0];
                notificationVO.moduleId = Math.toIntExact(e.getId());
                notificationVO.templateId = e.getTemplateId();
                notificationVO.moduleName = e.getModuleName();
                notificationVO.templateModules = TemplateModulesTypeEnum.DATACHECK_MODULE;
                notificationVOS.add(notificationVO);
                id[0]++;
            });
        }

        QueryWrapper<BusinessFilterPO> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda().eq(BusinessFilterPO::getDelFlag, 1);
        List<BusinessFilterPO> businessFilterPOS = businessFilterMapper.selectList(queryWrapper1);
        if (CollectionUtils.isNotEmpty(businessFilterPOS)) {
            businessFilterPOS.forEach(e -> {
                ComponentNotificationVO notificationVO = new ComponentNotificationVO();
                if (CollectionUtils.isNotEmpty(componentNotificationPOS)) {
                    List<ComponentNotificationPO> collect = componentNotificationPOS.stream().filter(
                            item -> item.getModuleId() == e.getId() && item.getTemplateId() == e.getTemplateId()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect)) {
                        notificationVO.noticeIds = collect.stream().map(ComponentNotificationPO::getNoticeId);
                    }
                }
                notificationVO.id = id[0];
                notificationVO.moduleId = Math.toIntExact(e.getId());
                notificationVO.templateId = e.getTemplateId();
                notificationVO.moduleName = e.getModuleName();
                notificationVO.templateModules = TemplateModulesTypeEnum.BIZCHECK_MODULE;
                notificationVOS.add(notificationVO);
                id[0]++;
            });
        }

        QueryWrapper<LifecyclePO> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.lambda().eq(LifecyclePO::getDelFlag, 1);
        List<LifecyclePO> lifecyclePOS = lifecycleMapper.selectList(queryWrapper2);
        if (CollectionUtils.isNotEmpty(lifecyclePOS)) {
            lifecyclePOS.forEach(e -> {
                ComponentNotificationVO notificationVO = new ComponentNotificationVO();
                if (CollectionUtils.isNotEmpty(componentNotificationPOS)) {
                    List<ComponentNotificationPO> collect = componentNotificationPOS.stream().filter(
                            item -> item.getModuleId() == e.getId() && item.getTemplateId() == e.getTemplateId()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect)) {
                        notificationVO.noticeIds = collect.stream().map(ComponentNotificationPO::getNoticeId);
                    }
                }
                notificationVO.id = id[0];
                notificationVO.moduleId = Math.toIntExact(e.getId());
                notificationVO.templateId = e.getTemplateId();
                notificationVO.moduleName = e.getModuleName();
                notificationVO.templateModules = TemplateModulesTypeEnum.LIFECYCLE_MODULE;
                notificationVOS.add(notificationVO);
                id[0]++;
            });
        }

        //第三步:获取邮件服务器信息
        List<EmailServerVO> emailServerVOS = new ArrayList<>();
        QueryWrapper<EmailServerPO> queryWrapper3 = new QueryWrapper<>();
        queryWrapper3.lambda().eq(EmailServerPO::getDelFlag, 1);
        List<EmailServerPO> emailServerPOS = emailServerMapper.selectList(queryWrapper3);
        if (CollectionUtils.isNotEmpty(emailServerPOS)) {
            emailServerPOS.forEach(e -> {
                EmailServerVO emailServerVO = new EmailServerVO();
                emailServerVO.setId(Math.toIntExact(e.getId()));
                emailServerVO.setName(e.getName());
                emailServerVOS.add(emailServerVO);
            });
        }
        noticeDetailVO.notificationVOList = notificationVOS;
        noticeDetailVO.emailServerVOS = emailServerVOS;
        return noticeDetailVO;
    }

    @Override
    public List<NoticeModuleVO> getModuleNoticeList() {
        List<NoticeModuleVO> noticeModuleVOS = new ArrayList<>();
        NoticeModuleVO noticeModule_VO_email = new NoticeModuleVO();
        noticeModule_VO_email.setNoticeName("邮件通知");
        NoticeModuleVO noticeModule_VO_system = new NoticeModuleVO();
        noticeModule_VO_system.setNoticeName("系统通知");

        List<NoticeModuleVO> noticeModules_emailVO = new ArrayList<>();
        List<NoticeModuleVO> noticeModules_systemVO = new ArrayList<>();

        QueryWrapper<NoticePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NoticePO::getDelFlag, 1);
        List<NoticePO> noticePOS = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(noticePOS)) {
            noticePOS.forEach(e -> {
                NoticeModuleVO noticeModuleVO = new NoticeModuleVO();
                noticeModuleVO.setNoticeId(Math.toIntExact(e.getId()));
                noticeModuleVO.setNoticeName(e.getModuleName());
                if (e.getNoticeType() == NoticeTypeEnum.EMAIL_NOTICE.getValue()) {
                    noticeModules_emailVO.add(noticeModuleVO);
                } else if (e.getNoticeType() == NoticeTypeEnum.SYSTEM_NOTICE.getValue()) {
                    noticeModules_systemVO.add(noticeModuleVO);
                }
            });
        }
        noticeModule_VO_email.setNoticeModuleVOS(noticeModules_emailVO);
        noticeModule_VO_system.setNoticeModuleVOS(noticeModules_systemVO);
        noticeModuleVOS.add(noticeModule_VO_email);
        noticeModuleVOS.add(noticeModule_VO_system);
        return noticeModuleVOS;
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
//        boolean openSsl = emailServerPO.getEnableSsl() != null && emailServerPO.getEnableSsl() == 1 ? true : false;
//        mailServeiceDTO.setOpenSsl(openSsl);
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
        mailSenderDTO.setSendAttachment(dto.sendAttachment);
        mailSenderDTO.setAttachmentName(dto.attachmentName);
        mailSenderDTO.setAttachmentPath(dto.attachmentPath);
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
     * @params userId 创建人id
     * @params typeEnum 模块类型
     * @params templateTypeEnum 模板类型
     * @params stateEnum 状态
     * @params cron 表达式
     */
    public ResultEnum publishBuildunifiedControlTask(long id, long userId, TemplateModulesTypeEnum typeEnum,
                                                     TemplateTypeEnum templateTypeEnum, ModuleStateEnum stateEnum,
                                                     String cron) {
        /*
         * 逻辑：
         * 1、数据校验、业务清洗、生命周期都通过此方法生成调度任务
         * 2、Task接口会生成相应的调度任务和nifi组件
         * 3、在Task服务中的KafkaConsumer类中定义对应每个模板的消费类
         * */
        // 暂时直接返回成功，功能测试阶段再开启如下代码
        return  ResultEnum.SUCCESS;
//        ResultEnum resultEnum = ResultEnum.TASK_NIFI_DISPATCH_ERROR;
//        //调用task服务提供的API生成调度任务
//        if (id == 0 || typeEnum == TemplateModulesTypeEnum.NONE
//                || cron == null || cron.isEmpty()) {
//            return ResultEnum.DATA_QUALITY_SCHEDULE_TASK_PARAMTER_ERROR;
//        }
//        boolean isDelTask = stateEnum != ModuleStateEnum.Enable;
//        UnifiedControlDTO unifiedControlDTO = new UnifiedControlDTO();
//        unifiedControlDTO.setUserId(userId);
//        unifiedControlDTO.setId(Math.toIntExact(id));
//        unifiedControlDTO.setDeleted(isDelTask);
//        unifiedControlDTO.setTemplateModulesType(typeEnum);
//        unifiedControlDTO.setScheduleType(SchedulingStrategyTypeEnum.CRON);
//        unifiedControlDTO.setTopic(templateTypeEnum.getTopic());
//        unifiedControlDTO.setDataClassifyEnum(DataClassifyEnum.UNIFIEDCONTROL);
//        unifiedControlDTO.setScheduleExpression(cron);
//        ResultEntity<Object> result = publishTaskClient.publishBuildunifiedControlTask(unifiedControlDTO);
//        if (result != null) {
//            resultEnum = ResultEnum.getEnum(result.getCode());
//        }
//        return resultEnum;
    }
}