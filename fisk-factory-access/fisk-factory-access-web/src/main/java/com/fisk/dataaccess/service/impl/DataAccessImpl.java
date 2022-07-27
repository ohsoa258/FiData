package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataReqDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.map.DataAccessMap;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.mapper.TableFieldsMapper;
import com.fisk.dataaccess.service.IDataAccess;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    TableAccessImpl tableAccessImpl;
    @Resource
    TableFieldsMapper tableFieldsMapper;
    @Resource
    TableFieldsImpl tableFieldsImpl;

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

    @Override
    public List<FiDataTableMetaDataDTO> buildFiDataTableMetaData(FiDataTableMetaDataReqDTO dto) {

        List<FiDataTableMetaDataDTO> fiDataTableMetaDataDtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dto.tableUniques)) {
            List<TableAccessPO> tableAccessPoList = new ArrayList<>();
            // SELECT table_name,id FROM tb_table_access WHERE del_flag=1 AND (id = ?)
            // dto.tableUniques.forEach(tableId -> tableAccessPoList.add(tableAccessImpl.query().eq("id", tableId).select("table_name", "id").one()));

            tableAccessPoList.forEach(po -> {
                // table: po => dto
                FiDataTableMetaDataDTO tableMeta = DataAccessMap.INSTANCES.tablePoToFiDataTableMetaData(po);
                if (tableMeta != null) {
                    // SELECT field_name,id FROM tb_table_fields WHERE del_flag=1 AND (table_access_id = ?)
                    List<TableFieldsPO> tableFieldsPoList = tableFieldsImpl.query().eq("table_access_id", po.id).select("field_name", "id").list();
                    // listField: po -> dto
                    tableMeta.setFieldList(DataAccessMap.INSTANCES.fieldListPoToFiDataTableMetaData(tableFieldsPoList));
                    fiDataTableMetaDataDtoList.add(tableMeta);
                }
            });
        }
        return fiDataTableMetaDataDtoList;
    }
}
