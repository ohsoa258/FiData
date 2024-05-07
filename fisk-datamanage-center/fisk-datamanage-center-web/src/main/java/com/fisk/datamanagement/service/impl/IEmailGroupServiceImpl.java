package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.email.*;
import com.fisk.datamanagement.entity.EmailGroupPO;
import com.fisk.datamanagement.entity.EmailGroupUserMapPO;
import com.fisk.datamanagement.entity.EmailUserPO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.map.EmailGroupMap;
import com.fisk.datamanagement.map.EmailUserMap;
import com.fisk.datamanagement.mapper.EmailGroupPOMapper;
import com.fisk.datamanagement.service.IEmailGroupService;
import com.fisk.system.client.UserClient;
import com.fisk.system.enums.EmailServerTypeEnum;
import com.fisk.system.vo.emailserver.EmailServerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 56263
 * @description 针对表【tb_email_group】的数据库操作Service实现
 * @createDate 2024-04-22 10:00:43
 */
@Service
@Slf4j
public class IEmailGroupServiceImpl extends ServiceImpl<EmailGroupPOMapper, EmailGroupPO>
        implements IEmailGroupService {

    @Resource
    private UserClient userClient;

    @Resource
    private EmailUserPOServiceImpl emailUserPOService;

    @Resource
    private IEmailGroupUserMapServiceImpl emailGroupUserMapService;

    @Resource
    private MetadataEntityImpl metadataEntityImpl;


    /**
     * 添加或编辑邮件组
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object editGroup(EmailGroupDTO dto) {
        EmailGroupPO po = EmailGroupMap.INSTANCES.dtoToPo(dto);
        return this.saveOrUpdate(po);
    }

    /**
     * 添加或编辑邮件组
     *
     * @param currentPage 当前页
     * @param size        页大小
     * @return
     */
    @Override
    public Page<EmailGroupDTO> pageFilterE(Integer currentPage, Integer size) {
        Page<EmailGroupPO> page = new Page<>(currentPage, size);
        page = this.page(page);
        List<EmailGroupPO> records = page.getRecords();
        List<EmailGroupDTO> emailGroupDTOS = EmailGroupMap.INSTANCES.poListToDtoList(records);
        Page<EmailGroupDTO> emailGroupDTOPage = new Page<>(currentPage, size);
        emailGroupDTOPage.setTotal(page.getTotal());
        emailGroupDTOPage.setRecords(emailGroupDTOS);
        return emailGroupDTOPage;
    }

    /**
     * 获取系统模块的邮件服务器信息 id+邮件服务器名称
     *
     * @return
     */
    @Override
    public List<EmailServerDTO> getSystemEmailServer() {
        ResultEntity<List<EmailServerVO>> resultEntity = userClient.getEmailServerList();
        if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.GET_SYSTEM_EMAIL_SERVER_ERROR);
        }

        List<EmailServerVO> data = resultEntity.getData();

        List<EmailServerDTO> list = new ArrayList<>();
        //筛选所需信息回显给前端
        for (EmailServerVO d : data) {
            EmailServerDTO emailServerDTO = new EmailServerDTO();
            emailServerDTO.setId(d.id);
            emailServerDTO.setName(d.name);
            emailServerDTO.setEmailServer(d.emailServer);
            list.add(emailServerDTO);
        }
        return list;
    }

    /**
     * 删除邮件组
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object deleteGroupById(EmailGroupDTO dto) {
        //删除邮件组
        boolean b = this.removeById(dto.getId());

        //删除关联关系
        emailGroupUserMapService.remove(new LambdaQueryWrapper<EmailGroupUserMapPO>().eq(EmailGroupUserMapPO::getGroupId, dto.getId()));

        if (!b) {
            throw new FkException(ResultEnum.DELETE_ERROR);
        } else {
            return true;
        }

    }

    /**
     * 获取所有邮件组
     *
     * @return
     */
    @Override
    public List<EmailGroupDTO> getAllEGroups() {
        List<EmailGroupPO> list = this.list(new LambdaQueryWrapper<EmailGroupPO>().select(EmailGroupPO::getId, EmailGroupPO::getGroupName));

        return EmailGroupMap.INSTANCES.poListToDtoList(list);
    }

    /**
     * 获取单个邮件组详情
     *
     * @param groupId 组id
     * @return
     */
    @Override
    public EmailGroupDetailDTO getEmailGroupDetailById(Integer groupId) {
        EmailGroupPO one = this.getOne(new LambdaQueryWrapper<EmailGroupPO>().eq(EmailGroupPO::getId, groupId));

        EmailGroupDetailDTO emailGroupDetailDTO = new EmailGroupDetailDTO();
        emailGroupDetailDTO.setId(one.getId());
        emailGroupDetailDTO.setGroupName(one.getGroupName());
        emailGroupDetailDTO.setGroupDesc(one.getGroupDesc());
        emailGroupDetailDTO.setEmailServerId(one.getEmailServerId());
        emailGroupDetailDTO.setEmailServerName(one.getEmailServerName());
        emailGroupDetailDTO.setCreateTime(one.getCreateTime());

        List<EmailUserDTO> users = new ArrayList<>();

        //获取当前组所拥有的关联关系
        List<EmailGroupUserMapPO> list = emailGroupUserMapService.list(new LambdaQueryWrapper<EmailGroupUserMapPO>().eq(EmailGroupUserMapPO::getGroupId, groupId));

        for (EmailGroupUserMapPO emailGroupUserMapPO : list) {
            EmailUserPO userPO = emailUserPOService.getOne(new LambdaQueryWrapper<EmailUserPO>().eq(EmailUserPO::getId, emailGroupUserMapPO.getUserId()));

            EmailUserDTO emailUserDTO = EmailUserMap.INSTANCES.poToDto(userPO);
            users.add(emailUserDTO);
        }

        emailGroupDetailDTO.setUsers(users);
        return emailGroupDetailDTO;
    }

    /**
     * 设置邮箱组和用户的关联关系
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object mapGroupWithUser(EmailGroupUserMapAddDTO dto) {
        //邮箱组id
        int groupId = dto.getGroupId();

        //先删除原有关联关系
        emailGroupUserMapService.remove(new LambdaQueryWrapper<EmailGroupUserMapPO>().eq(EmailGroupUserMapPO::getGroupId, groupId));

        //邮箱组要关联的用户id
        List<Integer> userIds = dto.getUserIds();

        List<EmailGroupUserMapPO> pos = new ArrayList<>();

        for (Integer userId : userIds) {
            EmailGroupUserMapPO po = new EmailGroupUserMapPO();
            po.setGroupId(groupId);
            po.setUserId(userId);
            pos.add(po);
        }
        return emailGroupUserMapService.saveBatch(pos);
    }

    /**
     * 定时任务 校验元数据实体的生命周期时间 如果过期则往对应的邮件组发邮件
     */
    @Scheduled(cron = "0 0 23 * * ? ")//每晚 23：00：00 执行
    public void sendEmail() {
        log.debug("*****数据资产 元数据生命周期邮件发送校验定时任务开始执行*****" + LocalDateTime.now());
        //获取所有类型为表和字段的元数据实体
        //为缩小查询范围 要求过期时间和邮箱组id不为空
        LambdaQueryWrapper<MetadataEntityPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(MetadataEntityPO::getTypeId, EntityTypeEnum.RDBMS_TABLE.getValue(), EntityTypeEnum.RDBMS_COLUMN.getValue())
                .isNotNull(MetadataEntityPO::getExpiresTime)
                .isNotNull(MetadataEntityPO::getEmailGroupId);
        List<MetadataEntityPO> list = metadataEntityImpl.list(wrapper);
        List<MetadataEntityPO> expiresEntities = new ArrayList<>();
        if (!CollectionUtils.isEmpty(list)) {
            for (MetadataEntityPO po : list) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expiresTime = po.getExpiresTime();
                //如果实体的生命周期时间已过期 则记录当前实体信息 随后统一发送邮件
                if (expiresTime.isBefore(now)) {
                    expiresEntities.add(po);
                }
            }
            if (!CollectionUtils.isEmpty(expiresEntities)) {
                //找到过期实体们都配置了哪些邮件组
                Set<Integer> collect = expiresEntities.stream().map(MetadataEntityPO::getEmailGroupId).collect(Collectors.toSet());
                //邮箱组id : 待发送信息
                Map<Integer, String> map = new HashMap<>();
                for (Integer integer : collect) {
                    String msg = "以下是数据中台元数据实体生命周期已过期实体名单[ip_dbName_tblName]：<br/>";
                    StringBuilder entityName = new StringBuilder();
                    for (MetadataEntityPO entity : expiresEntities) {
                        if (integer.equals(entity.getEmailGroupId())) {
                            entityName.append("【")
                                    .append(entity.getQualifiedName())
                                    .append("】")
                                    .append("<br/>");
                        }
                    }
                    msg = msg + entityName;
                    map.put(integer, msg);
                }
                //发送邮件
                sendEmail(map);
            }
        }
        log.debug("*****数据资产 元数据生命周期邮件发送校验定时任务执行完毕*****" + LocalDateTime.now());
    }

    /**
     * 发送邮件
     *
     * @return
     */
    private void sendEmail(Map<Integer, String> map) {
        //获取邮件组id
        Set<Integer> groupIds = map.keySet();
        //获取邮件组po list
        List<EmailGroupPO> groupPOS = this.listByIds(groupIds);

        for (EmailGroupPO groupPO : groupPOS) {
            int id = (int) groupPO.getId();
            //获取当前邮件组跟用户的关联信息
            List<EmailGroupUserMapPO> list = emailGroupUserMapService.list(new LambdaQueryWrapper<EmailGroupUserMapPO>().eq(EmailGroupUserMapPO::getGroupId, id));
            //通过关联信息获取用户id list
            List<Integer> collect = list.stream().map(EmailGroupUserMapPO::getUserId).collect(Collectors.toList());
            //获取当前邮件组下的用户邮件信息
            List<EmailUserPO> emailUserPOS = emailUserPOService.listByIds(collect);

            //获取system 邮件服务器id
            Integer emailServerId = groupPO.getEmailServerId();

            //第一步：查询邮件服务器设置
            ResultEntity<EmailServerVO> resultEntity = userClient.getEmailServerById(emailServerId);
            if (resultEntity == null || resultEntity.getCode() != ResultEnum.SUCCESS.getCode() ||
                    resultEntity.getData() == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }

            EmailServerVO data = resultEntity.getData();
            if (data.getEmailServerType().equals(EmailServerTypeEnum.SMTP)) {
                MailServeiceDTO mailServeiceDTO = new MailServeiceDTO();
                mailServeiceDTO.setOpenAuth(true);
                mailServeiceDTO.setOpenDebug(true);
                mailServeiceDTO.setHost(data.getEmailServer());
                mailServeiceDTO.setProtocol(data.getEmailServerType().getName());
                mailServeiceDTO.setUser(data.getEmailServerAccount());
                mailServeiceDTO.setPassword(data.getEmailServerPwd());
                mailServeiceDTO.setPort(data.getEmailServerPort());

                for (EmailUserPO emailUserPO : emailUserPOS) {
                    MailSenderDTO mailSenderDTO = new MailSenderDTO();
                    mailSenderDTO.setUser(data.getEmailServerAccount());
                    //邮件标题
                    mailSenderDTO.setSubject("FiData 【数据资产】【元数据生命周期】校验结果通知");
                    //邮件正文
                    mailSenderDTO.setBody(map.get(id));
                    mailSenderDTO.setToAddress(emailUserPO.getEmailAddress());
                    try {
                        //第二步：调用邮件发送方法
                        log.info("元数据生命周期邮件校验-mailServeiceDTO：" + JSON.toJSONString(mailServeiceDTO));
                        log.info("元数据生命周期邮件校验-mailSenderDTO：" + JSON.toJSONString(mailSenderDTO));
                        log.info("FiData 【数据资产】【元数据生命周期】开始发送邮件");
                        MailSenderUtils.send(mailServeiceDTO, mailSenderDTO);
                    } catch (Exception e) {
                        log.error("FiData 【数据资产】【元数据生命周期】发送邮件失败" + e);
                        throw new FkException(ResultEnum.EMAIL_NOT_SEND, e.getMessage());
                    }
                }

            }
        }
    }


}




