package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.email.EmailUserDTO;
import com.fisk.datamanagement.entity.EmailGroupUserMapPO;
import com.fisk.datamanagement.entity.EmailUserPO;
import com.fisk.datamanagement.map.EmailUserMap;
import com.fisk.datamanagement.mapper.EmailUserPOMapper;
import com.fisk.datamanagement.service.EmailUserPOService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 56263
 * @description 针对表【tb_email_user】的数据库操作Service实现
 * @createDate 2024-04-24 10:01:51
 */
@Service
public class EmailUserPOServiceImpl extends ServiceImpl<EmailUserPOMapper, EmailUserPO>
        implements EmailUserPOService {

    @Resource
    private IEmailGroupUserMapServiceImpl emailGroupUserMapService;

    /**
     * 获取邮箱用户配置的所有用户信息
     *
     * @return
     */
    @Override
    public List<EmailUserDTO> getUserInfo() {
        List<EmailUserPO> list = this.list();
        return EmailUserMap.INSTANCES.poListToDtoList(list);
    }

    /**
     * 添加或编辑邮件组
     *
     * @param dto
     * @return
     */
    @Override
    public Object editUser(EmailUserDTO dto) {
        EmailUserPO userPO = EmailUserMap.INSTANCES.dtoToPo(dto);
        return this.saveOrUpdate(userPO);
    }

    /**
     * 批量或单一删除用户信息
     *
     * @param dtos
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object delUser(List<EmailUserDTO> dtos) {
        List<Integer> ids = dtos.stream().map(EmailUserDTO::getId).collect(Collectors.toList());
        //删除用户
        boolean b = removeByIds(ids);

        //删除用户和邮件组的关联关系
        emailGroupUserMapService.remove(new LambdaQueryWrapper<EmailGroupUserMapPO>().in(EmailGroupUserMapPO::getUserId, ids));

        if (b) {
            return true;
        } else {
            log.error("删除用户及用户-邮箱组关联关系失败");
            throw new FkException(ResultEnum.DELETE_ERROR);
        }
    }


}




