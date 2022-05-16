package com.fisk.datagovernance.service.impl.dataquality;

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
import com.fisk.datagovernance.vo.dataquality.notice.NoticeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
            List<NoticeExtendPO> noticeExtendPOS =new ArrayList<>();
            dto.noticeExtends.forEach(t->{
                NoticeExtendPO noticeExtendPO=new NoticeExtendPO();
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
            List<NoticeExtendPO> noticeExtendPOS =new ArrayList<>();
            dto.noticeExtends.forEach(t->{
                NoticeExtendPO noticeExtendPO=new NoticeExtendPO();
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
}