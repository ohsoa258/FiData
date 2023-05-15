package com.fisk.datafactory.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datafactory.dto.customworkflow.DispatchEmailDTO;
import com.fisk.datafactory.dto.customworkflow.RecipientsDtO;
import com.fisk.datafactory.dto.UserInfoDTO;
import com.fisk.datafactory.entity.DispatchEmailPO;
import com.fisk.datafactory.entity.RecipientsPO;
import com.fisk.datafactory.enums.SendModeEnum;
import com.fisk.datafactory.map.DispatchEmailMap;
import com.fisk.datafactory.mapper.DispatchEmailMapper;
import com.fisk.datafactory.service.IDispatchEmail;
import com.fisk.datafactory.service.IRecipients;
import com.fisk.system.client.UserClient;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.task.enums.NifiStageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;

import static com.fisk.datafactory.utils.HttpUtils.HttpGet;
import static com.fisk.datafactory.utils.HttpUtils.HttpPost;

/**
 * @author cfk
 */
@Service
@Slf4j
public class DispatchEmailImpl extends ServiceImpl<DispatchEmailMapper, DispatchEmailPO> implements IDispatchEmail {

    @Resource
    private UserClient userClient;

    @Resource
    private  IRecipients recipients;
    @Override
    public DispatchEmailDTO getDispatchEmail(int nifiCustomWorkflowId) {

        //获取单个管道邮件配置
        DispatchEmailPO dispatchEmail = this.query().eq("nifi_custom_workflow_id", nifiCustomWorkflowId).one();
        if (dispatchEmail != null)
        {
            //根据管道邮件配置的id查出收件人
            List<RecipientsPO> re = recipients.query().select("wechat_user_id","wechat_user_name","user_id","user_name").eq("dispatch_email_id",dispatchEmail.id).list();
            DispatchEmailDTO emailDTO =  DispatchEmailMap.INSTANCES.poToDto(dispatchEmail);
            List<UserInfoDTO> userInfoList = new ArrayList<>();
            for (RecipientsPO recipientsPO : re) {
                UserInfoDTO userInfoDTO = new UserInfoDTO();
                userInfoDTO.wechatUserId = recipientsPO.wechatUserId;
                userInfoDTO.wechatUserName = recipientsPO.wechatUserName;
                userInfoDTO.userId = recipientsPO.userId;
                userInfoDTO.usetName = recipientsPO.userName;
                userInfoList.add(userInfoDTO);
            }
            emailDTO.setUserInfo(userInfoList);
            return emailDTO;
        }

        return null;

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
        //根据管道邮件配置的id查出收件人
        List<RecipientsPO> userList = recipients.query().select("wechat_user_id","wechat_user_name","user_id","user_name").eq("dispatch_email_id",email.id).list();
        //第一步：查询邮件服务器设置
        ResultEntity<EmailServerVO> emailServerById = userClient.getEmailServerById(email.emailserverConfigId);
        if (emailServerById == null || emailServerById.getCode() != ResultEnum.SUCCESS.getCode() ||
                emailServerById.getData() == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        //true是失败
        boolean contains = dispatchEmail.msg.contains(NifiStageTypeEnum.RUN_FAILED.getName());
        Integer sendMode = email.sendMode;
        if (Objects.equals(SendModeEnum.failure.getValue(), sendMode)) {
            if (contains) {
                log.info("满足模式,并且msg报错");
            } else {
                log.info("满足模式,但是msg没报错");
                return ResultEnum.SUCCESS;
            }
        } else if (Objects.equals(SendModeEnum.finish.getValue(), sendMode)) {
            log.info("所有模式都发通知");
        } else if (Objects.equals(SendModeEnum.success.getValue(), sendMode)) {
            if (!contains) {
                log.info("满足模式,并且msg没报错");
            } else {
                log.info("满足模式,但是msg报错");
                return ResultEnum.SUCCESS;
            }
        }

        if (email.type == 1)
        {
            EmailServerVO emailServerVO = emailServerById.getData();
            MailServeiceDTO mailServeiceDTO = new MailServeiceDTO();
            mailServeiceDTO.setOpenAuth(false);
            mailServeiceDTO.setOpenDebug(true);
            mailServeiceDTO.setHost(emailServerVO.getEmailServer());
            mailServeiceDTO.setProtocol(emailServerVO.getEmailServerType().getName());
            mailServeiceDTO.setUser(emailServerVO.getEmailServerAccount());
            mailServeiceDTO.setPassword(emailServerVO.getEmailServerPwd());
            mailServeiceDTO.setPort(emailServerVO.getEmailServerPort());
            MailSenderDTO mailSenderDTO = new MailSenderDTO();
            mailSenderDTO.setUser(emailServerVO.getEmailServerAccount());
            //邮件标题
            mailSenderDTO.setSubject("FiData数据管道运行结果通知");
            //邮件正文
            String body = "";

            mailSenderDTO.setBody(JSON.toJSONString(dispatchEmail.body));
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
        }
        else if (email.type == 2) //发送企业微信
        {
            //获取企业微信token
            String accessTokenUrl  = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + emailServerById.data.wechatCorpId + "&corpsecret=" + emailServerById.data.wechatAppSecret + "";
            String stringAccessToken = HttpGet(accessTokenUrl);
            JSONObject json = JSONObject.parseObject(stringAccessToken);
            String accessToken = json.getString("access_token");

            //取出dispatchEmail.body的key和value值并将key和value拼接成一段 HTML 格式的字符串
            StringBuffer sb = new StringBuffer();
            int i = 0;
            for (Map.Entry<String, String> entry : dispatchEmail.body.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key).append(": ").append(value);
                //如果是最后一条数据不加br
                if (i != dispatchEmail.body.size() - 1) {
                    sb.append("<br>");
                }
                i++;
            }
            String content = sb.toString();

            for (RecipientsPO user :userList)
            {
                //构造卡片消息内容
                Map<String, Object> params = new HashMap<>();
                params.put("touser", user.wechatUserId);
                params.put("msgtype", "textcard");
                params.put("agentid", emailServerById.data.wechatAgentId.trim());
                Map<String, Object> textcard = new HashMap<>();
                textcard.put("title", "管道告警通知");
                textcard.put("description", content);
                textcard.put("url", dispatchEmail.url);
                textcard.put("btntxt", "更多");
                params.put("textcard", textcard);
                params.put("enable_id_trans", 0);
                params.put("enable_duplicate_check", 0);
                params.put("duplicate_check_interval", 1800);

                try {
                    //发送企业微信
                    String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;
                    String send = HttpPost(url,JSON.toJSONString(params));
                    JSONObject jsonSend = JSONObject.parseObject(send);
                } catch (Exception e) {
                    throw new FkException(ResultEnum.ERROR,e.getMessage());
                }
            }
        }

        return null;
    }

    @Override
    public ResultEnum setNotification(RecipientsDtO dto)
    {
        List<DispatchEmailPO> dispatchList = new ArrayList<>();
        List<RecipientsPO> recipientsList = new ArrayList<>();
        if (dto != null)
        {
            //删除已配置的告警通知重新添加
            this.removeById(dto.dispatchEmailId);
            //查出所有已配置的收件人 然后进行删除重新添加
            List<RecipientsPO> poList = recipients.query().eq("dispatch_email_id",dto.dispatchEmailId).list();
            for (RecipientsPO r: poList) {
                recipients.removeById(r.id);
            }
            //添加
            DispatchEmailPO emailPO = new DispatchEmailPO();
            emailPO.emailserverConfigId = dto.emailserverConfigId;
            emailPO.nifiCustomWorkflowId = dto.nifiCustomWorkflowId;
            emailPO.type = dto.type;
            emailPO.sendMode = dto.sendMode;
            dispatchList.add(emailPO);
            this.saveBatch(dispatchList);
            //添加收件人(收件人单独一张表)
            for (UserInfoDTO user:dto.userInfo) {
                RecipientsPO po = new RecipientsPO();
                po.dispatchEmailId = (int) emailPO.id;
                po.wechatUserId = user.wechatUserId;
                po.wechatUserName =user.wechatUserName;
                po.userId = user.userId;
                po.userName  = user.usetName;
                recipientsList.add(po);
            }
            recipients.saveBatch(recipientsList);
        }
        return ResultEnum.SUCCESS;
    }

}
