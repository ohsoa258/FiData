package com.fisk.dataaccess.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.dataops.TableInfoDTO;
import com.fisk.dataaccess.dto.dataops.TableQueryDTO;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.service.IDataOps;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class DataOpsImpl implements IDataOps {

    @Resource
    TableAccessMapper tableAccessMapper;

    @Override
    public TableInfoDTO getTableInfo(String tableName) {
        TableQueryDTO tableInfo = tableAccessMapper.getTableInfo(tableName);
        if (tableInfo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        TableInfoDTO dto = new TableInfoDTO();
        dto.appId = tableInfo.appId;
        dto.olapTable = 3;
        dto.tableAccessId = tableInfo.id;
        dto.tableName = tableInfo.odsTableName;
        return dto;
    }

}
