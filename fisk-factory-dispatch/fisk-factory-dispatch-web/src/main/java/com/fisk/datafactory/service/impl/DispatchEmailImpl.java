package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datafactory.dto.customworkflow.DispatchEmailDTO;
import com.fisk.datafactory.entity.DispatchEmailPO;
import com.fisk.datafactory.map.DispatchEmailMap;
import com.fisk.datafactory.mapper.DispatchEmailMapper;
import com.fisk.datafactory.service.IDispatchEmail;
import com.fisk.system.client.UserClient;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.task.enums.NifiStageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author cfk
 */
@Service
@Slf4j
public class DispatchEmailImpl extends ServiceImpl<DispatchEmailMapper, DispatchEmailPO> implements IDispatchEmail {

    @Resource
    private UserClient userClient;

    @Override
    public DispatchEmailDTO getDispatchEmail(int nifiCustomWorkflowId) {
        DispatchEmailPO dispatchEmail = this.query().eq("nifi_custom_workflow_id", nifiCustomWorkflowId).one();
        return DispatchEmailMap.INSTANCES.poToDto(dispatchEmail);
    }

    @Override
    public ResultEnum saveOrupdate(DispatchEmailDTO dispatchEmail) {
        if (Objects.nonNull(dispatchEmail)) {
            this.saveOrUpdate(DispatchEmailMap.INSTANCES.dtoToPo(dispatchEmail));
            return ResultEnum.SUCCESS;
        } else {
            log.error("参数为空");
            throw new FkException(ResultEnum.DISPATCHEMAIL_NOT_EXISTS);
        }

    }

    @Override
    public ResultEnum deleteDispatchEmail(DispatchEmailDTO dispatchEmail) {
        this.removeById(dispatchEmail.id);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum pipelineSendEmails(DispatchEmailDTO dispatchEmail) {
        // 发邮件
        DispatchEmailPO email = this.query().eq("nifi_custom_workflow_id", dispatchEmail.nifiCustomWorkflowId).one();
        //第一步：查询邮件服务器设置
        ResultEntity<EmailServerVO> emailServerById = userClient.getEmailServerById(email.emailserverConfigId);
        if (emailServerById == null || emailServerById.getCode() != ResultEnum.SUCCESS.getCode() ||
                emailServerById.getData() == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        boolean contains = dispatchEmail.msg.contains(NifiStageTypeEnum.RUN_FAILED.getName());
        boolean sendMode = email.sendMode;
        if (sendMode) {
            log.info("满足模式");
        } else {
            if (contains) {
                log.info("满足模式,并且msg报错");
            } else {
                log.info("满足模式,但是msg没报错");
                return ResultEnum.SUCCESS;
            }
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
        //邮件标题
        mailSenderDTO.setSubject("邮件标题");
        //邮件正文
        mailSenderDTO.setBody("邮件正文");
        //邮件收件人
        mailSenderDTO.setToAddress(email.recipients);
        //mailSenderDTO.setToCc("邮件抄送人");
        //mailSenderDTO.setSendAttachment("是否发送附件");
        //mailSenderDTO.setAttachmentName("附件名称");
        //mailSenderDTO.setAttachmentPath("附件地址");
        //mailSenderDTO.setAttachmentActualName("附件实际名称");
        //mailSenderDTO.setCompanyLogoPath("公司logo地址");
        try {
            //第二步：调用邮件发送方法
            MailSenderUtils.send(mailServeiceDTO, mailSenderDTO);
        } catch (Exception ex) {
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return null;
    }
}
