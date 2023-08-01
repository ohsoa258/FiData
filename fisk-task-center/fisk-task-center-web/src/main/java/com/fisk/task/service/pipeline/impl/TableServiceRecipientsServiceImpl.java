package com.fisk.task.service.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.vo.tableservice.WechatUserVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.task.dto.tableservice.TableServiceRecipientsDTO;
import com.fisk.task.entity.ScheduleSettingPO;
import com.fisk.task.entity.TableServiceRecipientsPO;
import com.fisk.task.mapper.TableServiceRecipientsMapper;
import com.fisk.task.scheduled.CronTaskRegistrar;
import com.fisk.task.scheduled.SchedulingRunnable;
import com.fisk.task.service.pipeline.TableServiceRecipientsService;
import com.fisk.task.vo.tableservice.TableServiceRecipientsVO;
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
@Service("tableServiceRecipientsService")
public class TableServiceRecipientsServiceImpl extends ServiceImpl<TableServiceRecipientsMapper, TableServiceRecipientsPO> implements TableServiceRecipientsService {
    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;
    @Resource
    UserClient userClient;

    @Override
    public TableServiceRecipientsVO getTableServiceAlarmNotice() {
        ScheduleSettingPO scheduleSettingPO = new ScheduleSettingPO();
        ScheduleSettingPO scheduleSetting = scheduleSettingPO.selectOne(new QueryWrapper<ScheduleSettingPO>().eq("bean_name", "SubscribeEmailTask"));

        TableServiceRecipientsVO pipelLogRecipientsVO = new TableServiceRecipientsVO();
        List<TableServiceRecipientsPO> list = this.list();
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
        }
        if (scheduleSetting != null) {
            pipelLogRecipientsVO.setCronExpression(scheduleSetting.getCronExpression());
        }
        return pipelLogRecipientsVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveTableServiceAlarmNotice(TableServiceRecipientsDTO dto) {
        List<TableServiceRecipientsPO> tableServiceRecipientsPOS = new ArrayList<>();
        if (dto.getNoticeServerType() == 1) {
            TableServiceRecipientsPO tableServiceRecipientsPO = new TableServiceRecipientsPO();
            tableServiceRecipientsPO.setNoticeServerId(dto.getNoticeServerId());
            tableServiceRecipientsPO.setUserEmails(dto.getUserEmails());
            tableServiceRecipientsPO.setType(dto.getNoticeServerType());
            tableServiceRecipientsPO.setEnable(dto.enable);
            tableServiceRecipientsPOS.add(tableServiceRecipientsPO);
        } else if (dto.getNoticeServerType() == 2 && CollectionUtils.isNotEmpty(dto.getWechatUserList())) {
            dto.getWechatUserList().forEach(t -> {
                TableServiceRecipientsPO tableServiceRecipientsPO = new TableServiceRecipientsPO();
                tableServiceRecipientsPO.setNoticeServerId(dto.getNoticeServerId());
                tableServiceRecipientsPO.setWechatUserId(t.getWechatUserId());
                tableServiceRecipientsPO.setWechatUserName(t.getWechatUserName());
                tableServiceRecipientsPO.setEnable(dto.enable);
                tableServiceRecipientsPO.setType(dto.getNoticeServerType());
                tableServiceRecipientsPOS.add(tableServiceRecipientsPO);
            });
        }
        if (CollectionUtils.isNotEmpty(tableServiceRecipientsPOS)) {
            // 先删再插

            List<TableServiceRecipientsPO> recipientsPOList = this.list();
            if (CollectionUtils.isNotEmpty(recipientsPOList)) {
                List<Long> idList = recipientsPOList.stream().map(TableServiceRecipientsPO::getId).collect(Collectors.toList());
                // 修改的是del_flag状态
                this.removeByIds(idList);
            }
            ResultEnum resultEnum = updateCronTask(dto.cronExpression);
            if (resultEnum == ResultEnum.SUCCESS) {
                return this.saveBatch(tableServiceRecipientsPOS) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
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
            scheduleSetting.setMethodName("tableServiceSubscribeEmail");
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
    public ResultEnum deleteTableServiceAlarmNotice() {
        List<TableServiceRecipientsPO> recipientsPOList = this.list();
        if (CollectionUtils.isNotEmpty(recipientsPOList)) {
            List<Long> idList = recipientsPOList.stream().map(TableServiceRecipientsPO::getId).collect(Collectors.toList());
            // 修改的是del_flag状态
            this.removeByIds(idList);
        }
        ScheduleSettingPO scheduleSettingPO = new ScheduleSettingPO();
        boolean delete = scheduleSettingPO.delete(new QueryWrapper<ScheduleSettingPO>().eq("bean_name","SubscribeEmailTask"));
        if (delete) {
            // 修改成功,则删除任务器中的任务
            SchedulingRunnable task = new SchedulingRunnable("SubscribeEmailTask", "tableServiceSubscribeEmail");
            cronTaskRegistrar.removeCronTask(task);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum sendTableServiceSendEmails(String content) {
        // 发邮件
        List<TableServiceRecipientsPO> email = this.list();
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
            mailSenderDTO.setSubject(String.format("表服务执行情况"));

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

            for (TableServiceRecipientsPO user : email) {
                //构造卡片消息内容
                Map<String, Object> params = new HashMap<>();
                params.put("touser", user.getWechatUserId());
                params.put("msgtype", "textcard");
                params.put("agentid", emailServerById.data.wechatAgentId.trim());
                Map<String, Object> textcard = new HashMap<>();
                textcard.put("title", "表服务执行情况");
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
                    log.debug("【sendTableServiceSendEmails】 e：" + e);
                    throw new FkException(ResultEnum.ERROR, e.getMessage());
                }
            }
        }

        return ResultEnum.SUCCESS;
    }
}
