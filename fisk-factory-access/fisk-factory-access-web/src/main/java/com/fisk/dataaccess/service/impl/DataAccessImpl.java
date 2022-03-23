package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.map.DataAccessMap;
import com.fisk.dataaccess.mapper.AppRegistrationMapper;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.mapper.TableFieldsMapper;
import com.fisk.dataaccess.service.IDataAccess;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 * @version 2.0
 * @description
 * @date 2022/1/6 15:16
 */
@Service
public class DataAccessImpl implements IDataAccess {

    @Resource
    TableAccessMapper tableAccessMapper;
    @Resource
    TableFieldsMapper tableFieldsMapper;
    @Resource
    AppRegistrationMapper appRegistrationMapper;

    @Override
    public ResultEntity<List<DataAccessSourceTableDTO>> getDataAccessMetaData() {

        List<DataAccessSourceTableDTO> tableDtoList = tableAccessMapper.listTableMetaData();

        // 获取物理表下的字段信息
        tableDtoList.forEach(e -> {
            QueryWrapper<TableFieldsPO> fieldsQueryWrapper = new QueryWrapper<>();
            fieldsQueryWrapper.lambda().eq(TableFieldsPO::getTableAccessId, e.id);
            List<TableFieldsPO> fieldsList = tableFieldsMapper.selectList(fieldsQueryWrapper);
            e.list = DataAccessMap.INSTANCES.fieldListPoToDto(fieldsList);
        });

        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableDtoList);
    }
}
