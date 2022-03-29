package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.entity.datasecurity.PermissionManagementPO;
import com.fisk.datagovernance.mapper.datasecurity.PermissionManagementMapper;
import com.fisk.datagovernance.service.datasecurity.PermissionManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-29 17:59:34
 */
@Service
public class PermissionManagementServiceImpl extends ServiceImpl<PermissionManagementMapper, PermissionManagementPO> implements PermissionManagementService {
    @Override
    public PermissionManagementPO getData(long id) {

        PermissionManagementPO po = this.getById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return po;
    }

    @Override
    public ResultEnum addData(PermissionManagementPO po) {
        // 参数校验
        if (po == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        //保存
        return this.save(po) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(PermissionManagementPO po) {

        // 参数校验
        PermissionManagementPO model = this.getById(po.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        // 执行修改
        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(long id) {
        // 参数校验
        PermissionManagementPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 执行删除
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}