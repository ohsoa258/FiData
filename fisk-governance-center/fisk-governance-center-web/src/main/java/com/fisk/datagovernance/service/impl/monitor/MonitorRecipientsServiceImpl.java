package com.fisk.datagovernance.service.impl.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.datagovernance.dto.monitor.MonitorRecipientsDTO;
import com.fisk.datagovernance.entity.monitor.MonitorRecipientsPO;
import com.fisk.datagovernance.mapper.monitor.MonitorRecipientsMapper;
import com.fisk.datagovernance.service.monitor.MonitorRecipientsService;
import com.fisk.datagovernance.vo.monitor.MonitorRecipientsVO;
import com.fisk.datagovernance.vo.monitor.WechatUserVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.vo.emailserver.EmailServerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fisk.datagovernance.util.HttpUtils.HttpGet;
import static com.fisk.datagovernance.util.HttpUtils.HttpPost;

@Service("monitorRecipientsService")
@Slf4j
public class MonitorRecipientsServiceImpl extends ServiceImpl<MonitorRecipientsMapper, MonitorRecipientsPO> implements MonitorRecipientsService {

    @Resource
    private UserClient userClient;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public MonitorRecipientsVO getSystemMonitorAlarmNotice() {

        MonitorRecipientsVO systemRecipientsVO = null;
        List<MonitorRecipientsPO> list = this.list();
        if (CollectionUtils.isNotEmpty(list)) {
            ResultEntity<EmailServerVO> emailServerById = userClient.getEmailServerById(list.get(0).getNoticeServerId());
            if (emailServerById == null || emailServerById.getCode() != ResultEnum.SUCCESS.getCode() || emailServerById.getData() == null) {
                throw new FkException(ResultEnum.DS_THE_MESSAGE_NOTIFICATION_METHOD_DOES_NOT_EXIST);
            }

            int noticeServerType = emailServerById.getData().getServerConfigType();
            List<WechatUserVO> wechatUserList = new ArrayList<>();
            if (noticeServerType == 2) {
                list.forEach(t -> {
                    if (org.apache.commons.lang.StringUtils.isNotEmpty(t.getWechatUserId())) {
                        WechatUserVO wechatUserVO = new WechatUserVO();
                        wechatUserVO.setWechatUserId(t.getWechatUserId());
                        wechatUserVO.setWechatUserName(t.getWechatUserName());
                        wechatUserList.add(wechatUserVO);
                    }
                });
            }
            systemRecipientsVO = new MonitorRecipientsVO();
            systemRecipientsVO.setNoticeServerId(list.get(0).getNoticeServerId());
            systemRecipientsVO.setUserEmails(list.get(0).getUserEmails());
            systemRecipientsVO.setEnable(list.get(0).getEnable());
            systemRecipientsVO.setWechatUserList(wechatUserList);
        }
        return systemRecipientsVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveSystemMonitorAlarmNotice(MonitorRecipientsDTO dto) {
        List<MonitorRecipientsPO> monitorRecipientsPOS = new ArrayList<>();
        if (dto.getNoticeServerType() == 1) {
            MonitorRecipientsPO monitorRecipientsPO = new MonitorRecipientsPO();
            monitorRecipientsPO.setNoticeServerId(dto.getNoticeServerId());
            monitorRecipientsPO.setUserEmails(dto.getUserEmails());
            monitorRecipientsPO.setType(dto.getNoticeServerType());
            monitorRecipientsPO.setEnable(dto.enable);
            monitorRecipientsPOS.add(monitorRecipientsPO);
        } else if (dto.getNoticeServerType() == 2 && CollectionUtils.isNotEmpty(dto.getWechatUserList())) {
            dto.getWechatUserList().forEach(t -> {
                MonitorRecipientsPO monitorRecipientsPO = new MonitorRecipientsPO();
                monitorRecipientsPO.setNoticeServerId(dto.getNoticeServerId());
                monitorRecipientsPO.setWechatUserId(t.getWechatUserId());
                monitorRecipientsPO.setWechatUserName(t.getWechatUserName());
                monitorRecipientsPO.setEnable(dto.enable);
                monitorRecipientsPO.setType(dto.getNoticeServerType());
                monitorRecipientsPOS.add(monitorRecipientsPO);
            });
        }
        if (CollectionUtils.isNotEmpty(monitorRecipientsPOS)) {
            // 先删再插

            List<MonitorRecipientsPO> tableRecipientsPOList = this.list();
            if (CollectionUtils.isNotEmpty(tableRecipientsPOList)) {
                List<Long> idList = tableRecipientsPOList.stream().map(MonitorRecipientsPO::getId).collect(Collectors.toList());
                // 修改的是del_flag状态
                this.removeByIds(idList);
            }
            return this.saveBatch(monitorRecipientsPOS) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteSystemMonitorAlarmNotice() {
        List<MonitorRecipientsPO> tableRecipientsPOList = this.list();
        if (CollectionUtils.isNotEmpty(tableRecipientsPOList)) {
            List<Long> idList = tableRecipientsPOList.stream().map(MonitorRecipientsPO::getId).collect(Collectors.toList());
            // 修改的是del_flag状态
            this.removeByIds(idList);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum sendSystemMonitorSendEmails(Map<String, String> body) {
        String name = body.get("服务");
        Object o = redisUtil.get(RedisKeyEnum.EMAIL_SEND_STATUS.getName() + ":" + name);
        if (ObjectUtils.isNotEmpty(o)){
            return ResultEnum.SUCCESS;
        }else {
            redisUtil.set(RedisKeyEnum.EMAIL_SEND_STATUS.getName() + ":" + name,"邮件已发送");
        }
        // 发邮件
        List<MonitorRecipientsPO> email = this.list();
        //第一步：查询邮件服务器设置
        if (!CollectionUtils.isNotEmpty(email)) {
            return ResultEnum.ERROR;
        }
        if (email.get(0).getEnable() == 2) {
            return ResultEnum.SUCCESS;
        }
        if (email.get(0).getEnable() != 1) {
            return ResultEnum.ERROR;
        }
        ResultEntity<EmailServerVO> emailServerById = userClient.getEmailServerById(email.get(0).getNoticeServerId());
        if (emailServerById == null || emailServerById.getCode() != ResultEnum.SUCCESS.getCode() ||
                emailServerById.getData() == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        if (email.get(0).getType() == 1) {
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
            mailSenderDTO.setSubject(String.format("系统监控告警通知"));

            mailSenderDTO.setBody(JSON.toJSONString(body));
            //邮件收件人
            mailSenderDTO.setToAddress(email.get(0).getUserEmails());
            //mailSenderDTO.setToCc("邮件抄送人");
            //mailSenderDTO.setSendAttachment("是否发送附件");
            //mailSenderDTO.setAttachmentName("附件名称");
            //mailSenderDTO.setAttachmentPath("附件地址");
            //mailSenderDTO.setAttachmentActualName("附件实际名称");
            //mailSenderDTO.setCompanyLogoPath("公司logo地址");
            try {
                //第二步：调用邮件发送方法
                log.info("sendSystemMonitorSendEmails-mailServeiceDTO：" + JSON.toJSONString(mailServeiceDTO));
                log.info("sendSystemMonitorSendEmails-mailSenderDTO：" + JSON.toJSONString(mailSenderDTO));
                MailSenderUtils.send(mailServeiceDTO, mailSenderDTO);
            } catch (Exception ex) {
                throw new FkException(ResultEnum.ERROR, ex.getMessage());
            }
        } else if (email.get(0).getType() == 2) //发送企业微信
        {
            //获取企业微信token
            String accessTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + emailServerById.data.wechatCorpId + "&corpsecret=" + emailServerById.data.wechatAppSecret + "";
            String stringAccessToken = HttpGet(accessTokenUrl);
            JSONObject json = JSONObject.parseObject(stringAccessToken);
            String accessToken = json.getString("access_token");
            //取出dispatchEmail.body的key和value值并将key和value拼接成一段 HTML 格式的字符串
            StringBuffer sb = new StringBuffer();
            int i = 0;
            for (Map.Entry<String, String> entry : body.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key).append(": ").append(value);
                //如果是最后一条数据不加br
                if (i != body.size() - 1) {
                    sb.append("<br>");
                }
                i++;
            }
            String content = sb.toString();

            for (MonitorRecipientsPO user : email) {
                //构造卡片消息内容
                Map<String, Object> params = new HashMap<>();
                params.put("touser", user.getWechatUserId());
                params.put("msgtype", "textcard");
                params.put("agentid", emailServerById.data.wechatAgentId.trim());
                Map<String, Object> textcard = new HashMap<>();
                textcard.put("title", "系统监控告警通知");
                textcard.put("description", content);
                textcard.put("btntxt", "更多");
                params.put("textcard", textcard);
                params.put("enable_id_trans", 0);
                params.put("enable_duplicate_check", 0);
                params.put("duplicate_check_interval", 1800);

                try {
                    //发送企业微信
                    String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;
                    String send = HttpPost(url, JSON.toJSONString(params));
                    JSONObject jsonSend = JSONObject.parseObject(send);
                } catch (Exception e) {
                    log.debug("【sendSystemMonitorSendEmails】 e：" + e);
                    throw new FkException(ResultEnum.ERROR, e.getMessage());
                }
            }
        }
        return ResultEnum.SUCCESS;
    }
}
