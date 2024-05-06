package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.dto.email.EmailUserDTO;
import com.fisk.datamanagement.entity.EmailUserPO;

import java.util.List;

/**
 * @author 56263
 * @description 针对表【tb_email_user】的数据库操作Service
 * @createDate 2024-04-24 10:01:51
 */
public interface EmailUserPOService extends IService<EmailUserPO> {

    /**
     * 获取邮箱用户配置的所有用户信息
     *
     * @return
     */
    List<EmailUserDTO> getUserInfo();

    /**
     * 添加或编辑邮件组
     *
     * @param dto
     * @return
     */
    Object editUser(EmailUserDTO dto);


    /**
     * 批量或单一删除用户信息
     * @param dtos
     * @return
     */
    Object delUser(List<EmailUserDTO> dtos);

}
