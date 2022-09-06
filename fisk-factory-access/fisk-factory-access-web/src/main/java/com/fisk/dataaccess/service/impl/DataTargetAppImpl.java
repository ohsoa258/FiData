package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.dataaccess.dto.datatargetapp.DataTargetAppDTO;
import com.fisk.dataaccess.dto.datatargetapp.DataTargetAppQueryDTO;
import com.fisk.dataaccess.entity.DataTargetAppPO;
import com.fisk.dataaccess.map.DataTargetAppMap;
import com.fisk.dataaccess.mapper.DataTargetAppMapper;
import com.fisk.dataaccess.service.IDataTargetApp;
import com.fisk.dataaccess.utils.filterfield.FilterFieldUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class DataTargetAppImpl implements IDataTargetApp {

    @Resource
    GenerateCondition generateCondition;
    @Resource
    DataTargetAppMapper mapper;
    @Resource
    DataTargetImpl dataTarget;

    @Resource
    FilterFieldUtils utils;

    @Override
    public Page<DataTargetAppDTO> getDataTargetAppList(DataTargetAppQueryDTO dto) {
        String query = null;
        if (!CollectionUtils.isEmpty(dto.queryDTOList)) {
            query = generateCondition.getCondition(dto.queryDTOList);
        }
        return mapper.queryList(dto.page, query);
    }

    @Override
    public ResultEnum addDataTargetApp(DataTargetAppDTO dto) {
        QueryWrapper<DataTargetAppPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataTargetAppPO::getName, dto.name);
        DataTargetAppPO po = mapper.selectOne(queryWrapper);
        if (po != null) {
            return ResultEnum.NAME_EXISTS;
        }
        return mapper.insert(DataTargetAppMap.INSTANCES.dtoToPo(dto)) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DataTargetAppDTO getDataTargetApp(long id) {
        DataTargetAppPO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return DataTargetAppMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum updateDataTargetApp(DataTargetAppDTO dto) {
        DataTargetAppPO po = mapper.selectById(dto.id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //判断名称是否已存在
        QueryWrapper<DataTargetAppPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataTargetAppPO::getName, dto.name);
        DataTargetAppPO appPo = mapper.selectOne(queryWrapper);
        if (appPo != null && appPo.id != dto.id) {
            return ResultEnum.NAME_EXISTS;
        }
        return mapper.updateById(DataTargetAppMap.INSTANCES.dtoToPo(dto)) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteDataTargetApp(long id) {
        DataTargetAppPO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        if (mapper.deleteByIdWithFill(po) > 0) {
            //删除应用下的数据目标配置
            return dataTarget.deleteBatchByAppId(id);
        }
        return ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<FilterFieldDTO> getDataTargetAppColumn() {
        return utils.getDataTargetColumn("tb_data_target_app", FilterSqlConstants.DATA_TARGET_APP);
    }


}
