package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.dto.email.EmailGroupDTO;
import com.fisk.datamanagement.dto.email.EmailGroupDetailDTO;
import com.fisk.datamanagement.dto.email.EmailGroupUserMapAddDTO;
import com.fisk.datamanagement.dto.email.EmailServerDTO;
import com.fisk.datamanagement.entity.EmailGroupPO;
import com.fisk.system.dto.userinfo.UserDTO;

import java.util.List;

/**
 * @author 56263
 * @description 针对表【tb_email_group】的数据库操作Service
 * @createDate 2024-04-22 10:00:43
 */
public interface IEmailGroupService extends IService<EmailGroupPO> {

    /**
     * 添加或编辑邮件组
     *
     * @param dto
     * @return
     */
    Object editGroup(EmailGroupDTO dto);

    /**
     * 添加或编辑邮件组
     *
     * @param currentPage 当前页
     * @param size        页大小
     * @return
     */
    Page<EmailGroupDTO> pageFilterE(Integer currentPage, Integer size);

    /**
     * 获取系统模块的邮件服务器信息 id+邮件服务器名称
     *
     * @return
     */
    List<EmailServerDTO> getSystemEmailServer();

    /**
     * 删除邮件组
     *
     * @param dto
     * @return
     */
    Object deleteGroupById(EmailGroupDTO dto);

    /**
     * 获取所有邮件组
     *
     * @return
     */
    List<EmailGroupDTO> getAllEGroups();

    /**
     * 获取单个邮件组详情
     *
     * @param groupId 组id
     * @return
     */
    EmailGroupDetailDTO getEmailGroupDetailById(Integer groupId);

    /**
     * 设置邮箱组和用户的关联关系
     *
     * @param dto
     * @return
     */
    Object mapGroupWithUser(EmailGroupUserMapAddDTO dto);

}
