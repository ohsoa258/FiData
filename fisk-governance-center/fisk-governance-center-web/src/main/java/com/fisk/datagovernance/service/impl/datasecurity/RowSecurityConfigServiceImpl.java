package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.RowSecurityConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.RowUserAssignmentDTO;
import com.fisk.datagovernance.dto.datasecurity.RowfilterConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.RowSecurityConfigPO;
import com.fisk.datagovernance.entity.datasecurity.RowUserAssignmentPO;
import com.fisk.datagovernance.entity.datasecurity.RowfilterConfigPO;
import com.fisk.datagovernance.map.datasecurity.RowFilterConfigMap;
import com.fisk.datagovernance.map.datasecurity.RowSecurityConfigMap;
import com.fisk.datagovernance.map.datasecurity.RowUserAssignmentMap;
import com.fisk.datagovernance.mapper.datasecurity.RowSecurityConfigMapper;
import com.fisk.datagovernance.mapper.datasecurity.RowUserAssignmentMapper;
import com.fisk.datagovernance.mapper.datasecurity.RowfilterConfigMapper;
import com.fisk.datagovernance.service.datasecurity.RowSecurityConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class RowSecurityConfigServiceImpl extends ServiceImpl<RowSecurityConfigMapper, RowSecurityConfigPO> implements RowSecurityConfigService {

    @Resource
    private RowfilterConfigServiceImpl rowfilterConfigServiceImpl;

    @Resource
    private RowfilterConfigMapper rowfilterConfigMapper;

    @Resource
    private RowUserAssignmentServiceImpl rowUserAssignmentServiceImpl;

    @Resource
    private RowUserAssignmentMapper rowUserAssignmentMapper;

    @Override
    public RowSecurityConfigDTO getData(long id) {

        // 查询tb_rowsecurity_config表数据
        RowSecurityConfigPO model = this.getById(id);
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        // po -> dto
        RowSecurityConfigDTO rowSecurityConfigDTO = RowSecurityConfigMap.INSTANCES.poToDto(model);

        // 查询tb_rowfilter_config表数据集合
        List<RowfilterConfigPO> rowfilterConfigPoList = rowfilterConfigServiceImpl.query().eq("rowsecurity_id", id).list();
        rowSecurityConfigDTO.filterConditionDtoList = RowFilterConfigMap.INSTANCES.listPoToDto(rowfilterConfigPoList);

        // 查询tb_row_user_assignment表数据集合
        List<RowUserAssignmentPO> rowUserAssignmentPoList = rowUserAssignmentServiceImpl.query().eq("rowsecurity_id", id).list();
        rowSecurityConfigDTO.rowUserAssignmentDTOList = RowUserAssignmentMap.INSTANCES.listPoToDto(rowUserAssignmentPoList);

        return rowSecurityConfigDTO;
    }

    @Override
    public ResultEnum addData(RowSecurityConfigDTO dto) {

        // 过滤条件对象集合
        List<RowfilterConfigDTO> filterConditionDtoList = dto.filterConditionDtoList;
        // 行级权限对象集合
        List<RowUserAssignmentDTO> rowUserAssignmentDtoList = dto.rowUserAssignmentDTOList;

        // 保存tb_rowsecurity_config表数据
        // 当前字段名不可重复
        List<String> list = this.list().stream().map(e -> e.permissionsName).collect(Collectors.toList());
        if (list.contains(dto.permissionsName)) {
            return ResultEnum.ROW_SECURITYNAME_EXISTS;
        }
        // dto -> po
        RowSecurityConfigPO model = RowSecurityConfigMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null || filterConditionDtoList.isEmpty() || rowUserAssignmentDtoList.isEmpty()) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        boolean save = this.save(model);
        if (!save) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 批量保存tb_rowfilter_config表数据
        List<RowfilterConfigPO> rowfilterConfigPoList = RowFilterConfigMap.INSTANCES.listDtoToPo(filterConditionDtoList);
        rowfilterConfigPoList.forEach(e -> e.rowsecurityId = model.id);
        save = rowfilterConfigServiceImpl.saveBatch(rowfilterConfigPoList);
        if (!save) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 批量保存tb_row_user_assignment表数据
        List<RowUserAssignmentPO> rowUserAssignmentPoList = RowUserAssignmentMap.INSTANCES.listDtoToPo(rowUserAssignmentDtoList);
        // tb_rowsecurity_config表id 作为 tb_row_user_assignment表rowsecurity_id
        rowUserAssignmentPoList.forEach(e -> e.rowsecurityId = model.id);

        save = rowUserAssignmentServiceImpl.saveBatch(rowUserAssignmentPoList);
        if (!save) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(RowSecurityConfigDTO dto) {

        // 过滤条件对象集合
        List<RowfilterConfigDTO> filterConditionDtoList = dto.filterConditionDtoList;
        // 行级权限对象集合
        List<RowUserAssignmentDTO> rowUserAssignmentDtoList = dto.rowUserAssignmentDTOList;

        // 修改tb_rowsecurity_config表数据
        RowSecurityConfigPO rowSecurityConfigPo = RowSecurityConfigMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        RowSecurityConfigPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        if (rowSecurityConfigPo == null || filterConditionDtoList.isEmpty() || rowUserAssignmentDtoList.isEmpty()) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        // 判断名称是否重复
        QueryWrapper<RowSecurityConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RowSecurityConfigPO::getPermissionsName, dto.permissionsName);
        RowSecurityConfigPO po = baseMapper.selectOne(queryWrapper);
        if (po != null && po.id != dto.id) {
            return ResultEnum.ROW_SECURITYNAME_EXISTS;
        }
        boolean update = this.updateById(rowSecurityConfigPo);
        if (!update) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 批量修改tb_rowfilter_config表数据
        update = rowfilterConfigServiceImpl.updateBatchById(RowFilterConfigMap.INSTANCES.listDtoToPo(filterConditionDtoList));
        if (!update) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 批量修改tb_row_user_assignment表数据
        update = rowUserAssignmentServiceImpl.updateBatchById(RowUserAssignmentMap.INSTANCES.listDtoToPo(rowUserAssignmentDtoList));
        if (!update) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(long id) {

        // 删除tb_rowsecurity_config表数据
        RowSecurityConfigPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        int deleteResult = baseMapper.deleteByIdWithFill(model);
        if (deleteResult <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 删除tb_rowfilter_config表数据
        List<RowfilterConfigPO> rowfilterConfigPoList = rowfilterConfigServiceImpl.query().eq("rowsecurity_id", id).list();
        if (CollectionUtils.isNotEmpty(rowfilterConfigPoList)) {
            rowfilterConfigPoList.forEach(e -> rowfilterConfigMapper.deleteByIdWithFill(e));
        }
        // 删除tb_row_user_assignment表数据
        List<RowUserAssignmentPO> rowUserAssignmentPoList = rowUserAssignmentServiceImpl.query().eq("rowsecurity_id", id).list();
        if (CollectionUtils.isNotEmpty(rowUserAssignmentPoList)) {
            rowUserAssignmentPoList.forEach(e -> rowUserAssignmentMapper.deleteByIdWithFill(e));
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editDefaultConfig(long defaultConfig) {

        UpdateWrapper updateWrapper = new UpdateWrapper();
        // 修改表中default_config这一列的数据
        updateWrapper.set("default_config", defaultConfig);
        return baseMapper.update(null, updateWrapper) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}