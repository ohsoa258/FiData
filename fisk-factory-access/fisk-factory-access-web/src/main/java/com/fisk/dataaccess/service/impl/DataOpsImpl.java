package com.fisk.dataaccess.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.dataops.TableInfoDTO;
import com.fisk.dataaccess.dto.dataops.TableQueryDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.service.IDataOps;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class DataOpsImpl implements IDataOps {

    @Resource
    TableAccessMapper tableAccessMapper;
    @Resource
    TableFieldsImpl tableFields;

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

    @Override
    public List<String[]> getTableColumnDisplay(String tableName) {
        TableInfoDTO tableInfo = getTableInfo(tableName);
        List<TableFieldsPO> tableFieldsPoList = tableFields.query()
                .select("field_name", "display_name")
                .eq("table_access_id", tableInfo.tableAccessId)
                .list();
        if (CollectionUtils.isEmpty(tableFieldsPoList)) {
            return new ArrayList<>();
        }
        List<String[]> list = new ArrayList<>();
        for (TableFieldsPO item : tableFieldsPoList) {
            String[] data = new String[2];
            data[0] = item.fieldName;
            data[1] = item.displayName;
            list.add(data);
        }
        return list;
    }

}
