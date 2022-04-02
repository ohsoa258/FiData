package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.datasecurity.TablesecurityConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.datamasking.DataSourceIdDTO;
import com.fisk.datagovernance.entity.datasecurity.TablesecurityConfigPO;
import com.fisk.datagovernance.map.datasecurity.TableSecurityConfigMap;
import com.fisk.datagovernance.mapper.datasecurity.PermissionManagementMapper;
import com.fisk.datagovernance.mapper.datasecurity.TablesecurityConfigMapper;
import com.fisk.datagovernance.service.datasecurity.TableSecurityConfigService;
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
public class TablesecurityConfigServiceImpl extends ServiceImpl<TablesecurityConfigMapper, TablesecurityConfigPO> implements TableSecurityConfigService {

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
        TablesecurityConfigDTO dto = TableSecurityConfigMap.INSTANCES.poToDto(po);
        // TODO 根据访问类型和用户(组)id,查询用户(组)名称
        return dto;
    }

    @Override
    public ResultEnum addData(TablesecurityConfigDTO dto) {

        // dto -> po
        TablesecurityConfigPO model = TableSecurityConfigMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 保存主表数据
        return this.save(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(TablesecurityConfigDTO dto) {

        // 参数校验
        TablesecurityConfigPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        // 执行修改
        return this.updateById(TableSecurityConfigMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(long id) {
        // 参数校验
        TablesecurityConfigPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 执行删除
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<TablesecurityConfigDTO> getList(DataSourceIdDTO dto) {
        // 根据创建时间倒叙
        List<TablesecurityConfigPO> listPo = this.query()
                .eq("datasource_id", dto.datasourceId)
                .eq("table_id", dto.tableId)
                .orderByDesc("create_time").list();
        List<TablesecurityConfigDTO> list = TableSecurityConfigMap.INSTANCES.listPoToDto(listPo);

        for (TablesecurityConfigDTO e : list) {
            // TODO 根据访问类型和用户(组)id,查询用户(组)名称
        }

        return list;
    }

    @Override
    public ResultEnum editDefaultConfig(long defaultConfig) {

        UpdateWrapper updateWrapper = new UpdateWrapper();
        // 修改表中default_config这一列的数据
        updateWrapper.set("default_config", defaultConfig);
        return baseMapper.update(null, updateWrapper) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}