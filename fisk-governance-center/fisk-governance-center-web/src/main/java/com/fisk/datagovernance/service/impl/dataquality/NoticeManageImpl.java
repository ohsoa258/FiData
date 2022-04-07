package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeEditDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeQueryDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.EmailServerTypeEnum;
import com.fisk.datagovernance.enums.dataquality.NoticeTypeEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateModulesTypeEnum;
import com.fisk.datagovernance.map.dataquality.NoticeMap;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.INoticeManageService;
import com.fisk.datagovernance.vo.dataquality.emailserver.EmailServerVO;
import com.fisk.datagovernance.vo.dataquality.notice.AddNoticeVO;
import com.fisk.datagovernance.vo.dataquality.notice.ComponentNotificationVO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeModule;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    UserHelper userHelper;

    @Override
    public Page<NoticeVO> getAll(NoticeQueryDTO query) {
        return baseMapper.getAll(query.page, query.keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(NoticeDTO dto) {
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
        return baseMapper.deleteByIdWithFill(noticePO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public AddNoticeVO getNotificationInfo() {
        AddNoticeVO addNoticeVO = new AddNoticeVO();
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
                    Optional<ComponentNotificationPO> first = componentNotificationPOS.stream().filter(
                            item -> item.getModuleId() == e.getId() && item.getTemplateId() == e.getTemplateId()).findFirst();
                    if (first.isPresent()) {
                        ComponentNotificationPO componentNotificationPO = first.get();
                        if (componentNotificationPO != null) {
                            notificationVO.noticeId = componentNotificationPO.getNoticeId();
                        }
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
                    Optional<ComponentNotificationPO> first = componentNotificationPOS.stream().filter(
                            item -> item.getModuleId() == e.getId() && item.getTemplateId() == e.getTemplateId()).findFirst();
                    if (first.isPresent()) {
                        ComponentNotificationPO componentNotificationPO = first.get();
                        if (componentNotificationPO != null) {
                            notificationVO.noticeId = componentNotificationPO.getNoticeId();
                        }
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
                    Optional<ComponentNotificationPO> first = componentNotificationPOS.stream().filter(
                            item -> item.getModuleId() == e.getId() && item.getTemplateId() == e.getTemplateId()).findFirst();
                    if (first.isPresent()) {
                        ComponentNotificationPO componentNotificationPO = first.get();
                        if (componentNotificationPO != null) {
                            notificationVO.noticeId = componentNotificationPO.getNoticeId();
                        }
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
        addNoticeVO.notificationVOList = notificationVOS;
        addNoticeVO.emailServerVOS = emailServerVOS;
        return addNoticeVO;
    }

    @Override
    public List<NoticeModule> getModuleNoticeList() {
        List<NoticeModule> noticeModules = new ArrayList<>();
        NoticeModule noticeModule_email = new NoticeModule();
        noticeModule_email.setNoticeName("邮件通知");
        NoticeModule noticeModule_system = new NoticeModule();
        noticeModule_system.setNoticeName("系统通知");

        List<NoticeModule> noticeModules_email = new ArrayList<>();
        List<NoticeModule> noticeModules_system = new ArrayList<>();

        QueryWrapper<NoticePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NoticePO::getDelFlag, 1);
        List<NoticePO> noticePOS = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(noticePOS)) {
            noticePOS.forEach(e -> {
                NoticeModule noticeModule = new NoticeModule();
                noticeModule.setNoticeId(Math.toIntExact(e.getId()));
                noticeModule.setNoticeName(e.getModuleName());
                if (e.getNoticeType() == NoticeTypeEnum.EMAIL_NOTICE.getValue()) {
                    noticeModules_email.add(noticeModule);
                } else if (e.getNoticeType() == NoticeTypeEnum.SYSTEM_NOTICE.getValue()) {
                    noticeModules_system.add(noticeModule);
                }
            });
        }
        noticeModule_email.setNoticeModules(noticeModules_email);
        noticeModule_system.setNoticeModules(noticeModules_system);
        noticeModules.add(noticeModule_email);
        noticeModules.add(noticeModule_system);
        return noticeModules;
    }

    @Override
    public ResultEnum testSend(NoticeDTO dto) {
        //第一步：查询邮件服务器设置
        EmailServerPO emailServerPO = emailServerMapper.selectById(dto.emailServerId);
        if (emailServerPO == null) {
            return ResultEnum.PARAMTER_ERROR;
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
        try {
            //第二步：调用邮件发送方法
            MailSenderUtils.send(mailServeiceDTO, mailSenderDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultEnum.SUCCESS;
    }
}