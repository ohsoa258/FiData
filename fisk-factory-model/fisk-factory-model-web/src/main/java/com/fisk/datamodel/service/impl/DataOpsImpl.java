package com.fisk.datamodel.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamodel.dto.dataops.DataModelQueryDTO;
import com.fisk.datamodel.dto.dataops.DataModelTableInfoDTO;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.service.IDataOps;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class DataOpsImpl implements IDataOps {

    @Resource
    BusinessAreaMapper businessAreaMapper;

    @Override
    public DataModelTableInfoDTO getTableInfo(String tableName) {
        DataModelQueryDTO tableInfo = businessAreaMapper.getTableInfo(tableName);
        if (tableInfo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        DataModelTableInfoDTO data = new DataModelTableInfoDTO();
        data.businessAreaId = tableInfo.businessAreaId;
        data.tableId = tableInfo.id;
        data.tableName = tableInfo.odsTableName;
        data.olapTable = tableInfo.tableType;
        return data;
    }

}
