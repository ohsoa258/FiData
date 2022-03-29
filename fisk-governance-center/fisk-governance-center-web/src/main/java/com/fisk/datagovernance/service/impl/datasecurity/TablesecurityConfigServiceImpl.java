package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.TablesecurityConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.PermissionManagementPO;
import com.fisk.datagovernance.entity.datasecurity.TablesecurityConfigPO;
import com.fisk.datagovernance.enums.datasecurity.SecurityTableTypeEnum;
import com.fisk.datagovernance.map.datasecurity.TablesecurityConfigMap;
import com.fisk.datagovernance.mapper.datasecurity.PermissionManagementMapper;
import com.fisk.datagovernance.mapper.datasecurity.TablesecurityConfigMapper;
import com.fisk.datagovernance.service.datasecurity.TablesecurityConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class TablesecurityConfigServiceImpl extends ServiceImpl<TablesecurityConfigMapper, TablesecurityConfigPO> implements TablesecurityConfigService {

    @Resource
    private PermissionManagementServiceImpl permissionManagementServiceImpl;

    @Resource
    private PermissionManagementMapper permissionManagementMapper;

    @Override
    public TablesecurityConfigDTO getData(long id) {

        TablesecurityConfigPO po = this.getById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // po -> dto
        return TablesecurityConfigMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum addData(TablesecurityConfigDTO dto) {

        try {
            // dto -> po
            TablesecurityConfigPO model = TablesecurityConfigMap.INSTANCES.dtoToPo(dto);
            // 参数校验
            if (model == null) {
                return ResultEnum.PARAMTER_NOTNULL;
            }

            // 保存主表数据
            this.save(model);

            // 保存访问权限
            dto.accessPermissionList.forEach(e -> {
                PermissionManagementPO permissionManagementPo = new PermissionManagementPO();
                permissionManagementPo.tableType = SecurityTableTypeEnum.TABLE_SECURITY.getValue();
                permissionManagementPo.tableId = model.id;
                permissionManagementPo.accessType = model.accessType;
                permissionManagementPo.accessPermission = e;
                permissionManagementServiceImpl.addData(permissionManagementPo);
            });

            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(TablesecurityConfigDTO dto) {

        // 修改的时候,访问权限直接走对应controller的单表添加或删除,没有修改

        // 参数校验
        TablesecurityConfigPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        // 执行修改
        return this.updateById(TablesecurityConfigMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(long id) {
        try {
            // 参数校验
            TablesecurityConfigPO model = this.getById(id);
            if (model == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }

            // 删除主表数据
            baseMapper.deleteByIdWithFill(model);

            // 查询当前用户or用户组的权限
            List<PermissionManagementPO> list = permissionManagementServiceImpl.query()
                    .eq("table_type", 1)
                    .eq("table_id", id)
                    .eq("access_type", 0)
                    .list();
            if (CollectionUtils.isNotEmpty(list)) {
                // 删除权限
                list.forEach(e -> permissionManagementMapper.deleteByIdWithFill(e));
            }

            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    @Override
    public List<TablesecurityConfigDTO> getList() {
        // 根据创建时间倒叙
        List<TablesecurityConfigPO> listPo = this.query().orderByDesc("create_time").list();
        return TablesecurityConfigMap.INSTANCES.listPoToDto(listPo);
    }
}