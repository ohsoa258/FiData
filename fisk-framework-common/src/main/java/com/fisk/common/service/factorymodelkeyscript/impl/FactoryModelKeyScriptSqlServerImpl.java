package com.fisk.common.service.factorymodelkeyscript.impl;

import com.fisk.common.core.utils.StringBuildUtils;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.factorymodelkeyscript.IBuildFactoryModelKeyScript;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author lishiji
 */
public class FactoryModelKeyScriptSqlServerImpl implements IBuildFactoryModelKeyScript {

    /**
     * 拼接数仓建模构建维度key脚本
     *
     * @return
     */
    @Override
    public String buildKeyScript(List<TableSourceRelationsDTO> dto) {

        StringBuilder str = new StringBuilder();

        String tName = "[temp_" + dto.get(0).sourceTable + "].";

        for (TableSourceRelationsDTO item : dto) {
            str.append("update ")
                    .append("[temp_");
            str.append(item.sourceTable)
                    .append("]");
            str.append(" set ")
                    .append("[temp_");
            str.append(item.sourceTable)
                    .append("]")
                    .append(".")
                    .append("[")
                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
                    .append("]");
            str.append(" = ");
            str.append("[")
                    .append(item.targetTable)
                    .append("]")
                    .append(".")
                    .append("[")
                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
                    .append("]");
            str.append(" from ")
                    .append("[temp_");
            str.append(item.sourceTable)
                    .append("]");
            if (!StringUtils.isEmpty(item.joinType)) {
                str.append(" ").append(item.joinType);
                str.append(" ")
                        .append("[")
                        .append(item.targetTable)
                        .append("]");
            }
            str.append(" on ")
                    .append("[temp_");

            str.append(item.sourceTable)
                    .append("]")
                    .append(".")
                    .append("[")
                    .append(item.sourceColumn)
                    .append("]");
            str.append(" = ");
            str.append("[")
                    .append(item.targetTable)
                    .append("]")
                    .append(".")
                    .append("[")
                    .append(item.targetColumn)
                    .append("]")
                    .append(" WHERE ")
                    .append(tName)
                    .append("fidata_batch_code=")
                    .append("'${fidata_batch_code}' AND ")
                    .append(tName)
                    .append("fidata_flow_batch_code=")
                    .append("'${fragment.index}'");
            str.append(";");
        }

        return str.toString();
    }

}
