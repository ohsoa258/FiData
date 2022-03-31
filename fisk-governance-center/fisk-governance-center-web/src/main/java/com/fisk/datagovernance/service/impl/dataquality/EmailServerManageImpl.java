package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.emailserver.EmailServerDTO;
import com.fisk.datagovernance.dto.dataquality.emailserver.EmailServerEditDTO;
import com.fisk.datagovernance.dto.dataquality.emailserver.EmailServerQueryDTO;
import com.fisk.datagovernance.entity.dataquality.EmailServerPO;
import com.fisk.datagovernance.map.dataquality.EmailServerMap;
import com.fisk.datagovernance.mapper.dataquality.EmailServerMapper;
import com.fisk.datagovernance.service.dataquality.IEmailServerManageService;
import com.fisk.datagovernance.vo.dataquality.emailserver.EmailServerVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return baseMapper.getAll(query.page, query.keyword);
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
}