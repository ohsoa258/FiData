package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.dto.emailserver.EmailServerDTO;
import com.fisk.system.dto.emailserver.EmailServerEditDTO;
import com.fisk.system.dto.emailserver.EmailServerQueryDTO;
import com.fisk.system.entity.EmailServerPO;
import com.fisk.system.enums.EmailServerTypeEnum;
import com.fisk.system.map.EmailServerMap;
import com.fisk.system.mapper.EmailServerMapper;
import com.fisk.system.service.IEmailServerManageService;
import com.fisk.system.vo.emailserver.EmailServerVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 邮件配置实现类
 * @date 2022/3/23 12:56
 */
@Service
public class EmailServerManageImpl extends ServiceImpl<EmailServerMapper, EmailServerPO> implements IEmailServerManageService {

    @Override
    public Page<EmailServerVO> getAll(EmailServerQueryDTO query) {
        Page<EmailServerVO> pageAll = baseMapper.getPageAll(query.page, query.keyword);
        if (pageAll != null && !CollectionUtils.isEmpty(pageAll.getOrders())) {
            pageAll.getRecords().forEach(t -> {
                t.setEmailServerType(EmailServerTypeEnum.getEnum(t.getEmailServerTypeValue()));
            });
        }
        return pageAll;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(EmailServerDTO dto) {
        //第一步：转换DTO对象为PO对象
        EmailServerPO emailServerPO = EmailServerMap.INSTANCES.dtoToPo(dto);
        if (emailServerPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        int insert = baseMapper.insert(emailServerPO);
        if (insert <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editData(EmailServerEditDTO dto) {
        EmailServerPO emailServerPO = baseMapper.selectById(dto.id);
        if (emailServerPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //第一步：转换DTO对象为PO对象
        emailServerPO = EmailServerMap.INSTANCES.dtoToPo_Edit(dto);
        if (emailServerPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        int insert = baseMapper.updateById(emailServerPO);
        if (insert <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(int id) {
        EmailServerPO emailServerPO = baseMapper.selectById(id);
        if (emailServerPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return baseMapper.deleteByIdWithFill(emailServerPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<EmailServerVO> getEmailServerList() {
        List<EmailServerVO> all = baseMapper.getAll();
        if (!CollectionUtils.isEmpty(all)) {
            all.forEach(t -> {
                t.setEmailServerType(EmailServerTypeEnum.getEnum(t.getEmailServerTypeValue()));
            });
        }
        return all;
    }

    @Override
    public EmailServerVO getEmailServerById(int id) {
        EmailServerVO byId = baseMapper.getById(id);
        if (byId != null) {
            byId.setEmailServerType(EmailServerTypeEnum.getEnum(byId.getEmailServerTypeValue()));
        }
        return byId;
    }

    @Override
    public EmailServerVO getDefaultEmailServer() {
        EmailServerVO emailServerVO = baseMapper.getDefaultEmailServer();
        if (emailServerVO != null) {
            emailServerVO.setEmailServerType(EmailServerTypeEnum.getEnum(emailServerVO.getEmailServerTypeValue()));
        }
        return emailServerVO;
    }
}