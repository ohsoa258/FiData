package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataaccess.dto.GetConfigDTO;
import com.fisk.dataaccess.dto.datatargetapp.DataTargetAppDTO;
import com.fisk.dataaccess.dto.datatargetapp.DataTargetAppQueryDTO;
import com.fisk.dataaccess.entity.DataTargetAppPO;
import com.fisk.dataaccess.map.DataTargetAppMap;
import com.fisk.dataaccess.mapper.DataTargetAppMapper;
import com.fisk.dataaccess.service.IDataTargetApp;
import org.springframework.stereotype.Service;
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
    GetMetadata getMetadata;
    @Resource
    GetConfigDTO getConfig;

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
    public ResultEnum deleteDataTargetApp(long id) {
        DataTargetAppPO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return mapper.deleteByIdWithFill(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<FilterFieldDTO> getDataTargetAppColumn() {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.driver = getConfig.driver;
        dto.tableName = "tb_data_target_app";
        dto.filterSql = FilterSqlConstants.DATA_TARGET_APP;
        return getMetadata.getMetadataList(dto);
    }


}
