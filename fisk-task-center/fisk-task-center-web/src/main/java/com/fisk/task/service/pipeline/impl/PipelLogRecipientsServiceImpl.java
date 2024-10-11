package com.fisk.task.service.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.emailwarnlevel.EmailWarnLevelEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.vo.tableservice.WechatUserVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.task.dto.statistics.PipelLogRecipientsDTO;
import com.fisk.task.entity.PipelLogRecipientsPO;
import com.fisk.task.entity.ScheduleSettingPO;
import com.fisk.task.mapper.PipelLogRecipientsMapper;
import com.fisk.task.scheduled.CronTaskRegistrar;
import com.fisk.task.scheduled.SchedulingRunnable;
import com.fisk.task.service.pipeline.PipelLogRecipientsService;
import com.fisk.task.vo.statistics.PipelLogRecipientsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fisk.task.utils.HttpUtils.HttpGet;
import static com.fisk.task.utils.HttpUtils.HttpPost;

@Slf4j
@Service("pipelLogRecipientsService")
public class PipelLogRecipientsServiceImpl extends ServiceImpl<PipelLogRecipientsMapper, PipelLogRecipientsPO> implements PipelLogRecipientsService {
    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;
    @Resource
    UserClient userClient;

    @Override
    public PipelLogRecipientsVO getPipelLogAlarmNotice() {
        ScheduleSettingPO scheduleSettingPO = new ScheduleSettingPO();
        ScheduleSettingPO scheduleSetting = scheduleSettingPO.selectOne(new QueryWrapper<ScheduleSettingPO>().eq("bean_name", "SubscribeEmailTask"));

        PipelLogRecipientsVO pipelLogRecipientsVO = new PipelLogRecipientsVO();
        List<PipelLogRecipientsPO> list = this.list();
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
            pipelLogRecipientsVO.setNoticeServerId(list.get(0).getNoticeServerId());
            pipelLogRecipientsVO.setUserEmails(list.get(0).getUserEmails());
            pipelLogRecipientsVO.setEnable(list.get(0).getEnable());
            pipelLogRecipientsVO.setWechatUserList(wechatUserList);
            pipelLogRecipientsVO.setWarnLevel(list.get(0).getWarnLevel());
        }
        if (scheduleSetting != null) {
            pipelLogRecipientsVO.setCronExpression(scheduleSetting.getCronExpression());
        }
        return pipelLogRecipientsVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum savePipelLogAlarmNotice(PipelLogRecipientsDTO dto) {
        List<PipelLogRecipientsPO> pipelLogRecipientsPOS = new ArrayList<>();
        if (dto.getNoticeServerType() == 1) {
            PipelLogRecipientsPO pipelLogRecipientsPO = new PipelLogRecipientsPO();
            pipelLogRecipientsPO.setNoticeServerId(dto.getNoticeServerId());
            pipelLogRecipientsPO.setUserEmails(dto.getUserEmails());
            pipelLogRecipientsPO.setType(dto.getNoticeServerType());
            pipelLogRecipientsPO.setEnable(dto.enable);
            pipelLogRecipientsPO.setWarnLevel(dto.warnLevel);
            pipelLogRecipientsPOS.add(pipelLogRecipientsPO);
        } else if (dto.getNoticeServerType() == 2 && CollectionUtils.isNotEmpty(dto.getWechatUserList())) {
            dto.getWechatUserList().forEach(t -> {
                PipelLogRecipientsPO pipelLogRecipientsPO = new PipelLogRecipientsPO();
                pipelLogRecipientsPO.setNoticeServerId(dto.getNoticeServerId());
                pipelLogRecipientsPO.setWechatUserId(t.getWechatUserId());
                pipelLogRecipientsPO.setWechatUserName(t.getWechatUserName());
                pipelLogRecipientsPO.setEnable(dto.enable);
                pipelLogRecipientsPO.setType(dto.getNoticeServerType());
                pipelLogRecipientsPO.setWarnLevel(dto.warnLevel);
                pipelLogRecipientsPOS.add(pipelLogRecipientsPO);
            });
        }
        if (CollectionUtils.isNotEmpty(pipelLogRecipientsPOS)) {
            // 先删再插

            List<PipelLogRecipientsPO> recipientsPOList = this.list();
            if (CollectionUtils.isNotEmpty(recipientsPOList)) {
                List<Long> idList = recipientsPOList.stream().map(PipelLogRecipientsPO::getId).collect(Collectors.toList());
                // 修改的是del_flag状态
                this.removeByIds(idList);
            }
            ResultEnum resultEnum = updateCronTask(dto.cronExpression);
            if (resultEnum == ResultEnum.SUCCESS) {
                return this.saveBatch(pipelLogRecipientsPOS) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
            }else {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        return ResultEnum.SAVE_DATA_ERROR;
    }

    public ResultEnum updateCronTask(String cron) {
        ScheduleSettingPO scheduleSettingPO = new ScheduleSettingPO();
        ScheduleSettingPO scheduleSetting = scheduleSettingPO.selectOne(new QueryWrapper<ScheduleSettingPO>().eq("bean_name", "SubscribeEmailTask"));
        if (scheduleSetting == null) {
            scheduleSetting = new ScheduleSettingPO();
            scheduleSetting.setBeanName("SubscribeEmailTask");
            scheduleSetting.setRemark("管道监控邮箱定时任务");
            scheduleSetting.setMethodName("pipelLogSubscribeEmail");
            scheduleSetting.setJobStatus(1);
            scheduleSetting.setCronExpression(cron);
            boolean insert = scheduleSetting.insert();
            if (!insert) {
                return ResultEnum.ERROR;
            } else {
                if (scheduleSetting.getJobStatus().equals(1)) {// 添加成功,并且状态是1，直接放入任务器
                    SchedulingRunnable task = new SchedulingRunnable(scheduleSetting.getBeanName(), scheduleSetting.getMethodName(), scheduleSetting.getMethodParams());
                    cronTaskRegistrar.addCronTask(task, scheduleSetting.getCronExpression());
                }
            }
            return ResultEnum.SUCCESS;
        } else {
            scheduleSetting.setCronExpression(cron);
            boolean update = scheduleSetting.update(new UpdateWrapper<ScheduleSettingPO>().eq("job_id", scheduleSetting.getJobId()));
            if (!update) {
                return ResultEnum.ERROR;
            } else {
                // 修改成功,则先删除任务器中的任务,并重新添加
                SchedulingRunnable task = new SchedulingRunnable(scheduleSetting.getBeanName(), scheduleSetting.getMethodName(), scheduleSetting.getMethodParams());
                cronTaskRegistrar.removeCronTask(task);
                if (scheduleSetting.getJobStatus().equals(1)) {// 如果修改后的任务状态是1就加入任务器
                    SchedulingRunnable task1 = new SchedulingRunnable(scheduleSetting.getBeanName(), scheduleSetting.getMethodName(), scheduleSetting.getMethodParams());
                    cronTaskRegistrar.addCronTask(task1, scheduleSetting.getCronExpression());
                }
            }
            return ResultEnum.SUCCESS;
        }
    }

    @Override
    public ResultEnum deletePipelLogAlarmNotice() {
        List<PipelLogRecipientsPO> recipientsPOList = this.list();
        if (CollectionUtils.isNotEmpty(recipientsPOList)) {
            List<Long> idList = recipientsPOList.stream().map(PipelLogRecipientsPO::getId).collect(Collectors.toList());
            // 修改的是del_flag状态
            this.removeByIds(idList);
        }
        ScheduleSettingPO scheduleSettingPO = new ScheduleSettingPO();
        boolean delete = scheduleSettingPO.delete(new QueryWrapper<ScheduleSettingPO>().eq("bean_name","SubscribeEmailTask"));
        if (delete) {
            // 修改成功,则删除任务器中的任务
            SchedulingRunnable task = new SchedulingRunnable("SubscribeEmailTask", "pipelLogSubscribeEmail");
            cronTaskRegistrar.removeCronTask(task);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum sendPipelLogSendEmails(String content) {
        // 发邮件
        List<PipelLogRecipientsPO> email = this.list();
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
            Integer warnLevel = email.get(0).getWarnLevel();
            EmailWarnLevelEnum anEnum = EmailWarnLevelEnum.getEnum(warnLevel);
            mailSenderDTO.setSubject(String.format("【"+anEnum.getName()+"】管道执行情况"));

            mailSenderDTO.setBody(content);
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
            //取出body的key和value值并将key和value拼接成一段 HTML 格式的字符串

            for (PipelLogRecipientsPO user : email) {
                //构造卡片消息内容
                Map<String, Object> params = new HashMap<>();
                params.put("touser", user.getWechatUserId());
                params.put("msgtype", "textcard");
                params.put("agentid", emailServerById.data.wechatAgentId.trim());
                Map<String, Object> textcard = new HashMap<>();
                textcard.put("title", "管道执行情况");
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
                    log.debug("【sendPipelLogSendEmails】 e：" + e);
                    throw new FkException(ResultEnum.ERROR, e.getMessage());
                }
            }
        }

        return ResultEnum.SUCCESS;
    }
}
