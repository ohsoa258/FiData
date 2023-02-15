package com.fisk.datamodel.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamodel.dto.dataops.DataModelQueryDTO;
import com.fisk.datamodel.dto.dataops.DataModelTableInfoDTO;
import com.fisk.datamodel.entity.dimension.DimensionAttributePO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.service.IDataOps;
import com.fisk.datamodel.service.impl.dimension.DimensionAttributeImpl;
import com.fisk.datamodel.service.impl.fact.FactAttributeImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class DataOpsImpl implements IDataOps {

    @Resource
    BusinessAreaMapper businessAreaMapper;

    @Resource
    DimensionAttributeImpl dimensionAttribute;
    @Resource
    FactAttributeImpl factAttribute;

    @Override
    public DataModelTableInfoDTO getTableInfo(String tableName) {
        tableName = tableName.replace("dbo.", "");
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

    @Override
    public List<String[]> getTableColumnDisplay(String tableName) {
        DataModelTableInfoDTO tableInfo = getTableInfo(tableName);
        List<String[]> data = new ArrayList<>();
        //1:维度
        if (tableInfo.olapTable == 1) {
            List<DimensionAttributePO> list = dimensionAttribute.query()
                    .select("dimension_field_en_name", "dimension_field_cn_name")
                    .eq("dimension_id", tableInfo.tableId)
                    .list();

            for (DimensionAttributePO item : list) {
                String[] dto = new String[2];
                dto[0] = item.dimensionFieldEnName;
                dto[1] = item.dimensionFieldCnName;
                data.add(dto);
            }
            return data;
        }

        List<FactAttributePO> list = factAttribute.query()
                .select("fact_field_en_name", "fact_field_cn_name")
                .eq("fact_id", tableInfo.tableId)
                .list();
        for (FactAttributePO item : list) {
            String[] dto = new String[2];
            dto[0] = item.factFieldEnName;
            dto[1] = item.factFieldCnName;
            data.add(dto);
        }

        return data;
    }

}
