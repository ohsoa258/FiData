package com.fisk.common.service.factorymodelkeyscript.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.StringBuildUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.factorymodelkeyscript.IBuildFactoryModelKeyScript;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author lishiji
 */
public class FactoryModelKeyScriptDorisSqlImpl implements IBuildFactoryModelKeyScript {

    //todo:以下是原来的update语句 勿删
//    /**
//     * 拼接数仓建模构建维度key脚本
//     *
//     * @return
//     */
//    @Override
//    public String buildKeyScript(List<TableSourceRelationsDTO> dto) {
//
//        StringBuilder str = new StringBuilder();
//
//        String tName = "temp_" + dto.get(0).sourceTable + ".";
//
//        for (TableSourceRelationsDTO item : dto) {
//            str.append("update ")
//                    .append("`temp_");
//            str.append(item.sourceTable)
//                    .append("`");
//
//            str.append("")
//                    .append(" set ")
//                    .append("`temp_")
//                    .append(item.sourceTable)
//                    .append("`")
//                    .append(".")
//                    .append("")
//                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
//                    .append("")
//                    .append(" = ")
//                    .append("`")
//                    .append(item.targetTable)
//                    .append("`")
//                    .append(".")
//                    .append("`")
//                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
//                    .append("` FROM `")
//                    .append(item.targetTable)
//                    .append("`")
//                    .append(" WHERE `temp_")
//                    .append(item.sourceTable)
//                    .append("`.`")
//                    .append(item.sourceColumn)
//                    .append("` = `")
//                    .append(item.targetTable)
//                    .append("`.`")
//                    .append(item.targetColumn)
//                    .append("` AND ")
//                    .append(tName)
//                    .append("fidata_batch_code=")
//                    .append("'${fidata_batch_code}' AND ")
//                    .append(tName)
//                    .append("fidata_flow_batch_code=")
//                    .append("'${fragment.index}'");
//            str.append(";");
//        }
//        return str.toString();
//    }

    /**
     * 拼接数仓建模构建维度key脚本
     *
     * @return
     */
    @Override
    public String buildKeyScript(List<TableSourceRelationsDTO> dto) {

        StringBuilder str = new StringBuilder();

        String tName = "`temp_" + dto.get(0).sourceTable + "`.";

        for (TableSourceRelationsDTO item : dto) {
            List<String> keyNameList = item.getKeyNameList();
            if (CollectionUtils.isEmpty(keyNameList)) {
                throw new FkException(ResultEnum.PARAMTER_ERROR);
            }
            String pkSql = "";
            String insertColumn = "";
            for (String keyName : keyNameList) {
                keyName = tName + "`" + keyName + "`,";
                pkSql = pkSql + keyName;
                insertColumn = "`" + keyName + "`,";
            }

            str.append("set enable_unique_key_partial_update=true;");
            str.append("INSERT INTO ")
                    .append("`temp_");
            str.append(item.sourceTable)
                    .append("` ")
                    .append("(")
                    .append(insertColumn)
                    .append("`")
                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
                    .append("`")
                    .append(") ");

            str.append("SELECT ")
                    .append(pkSql)
                    .append("`")
                    .append(item.targetTable)
                    .append("`")
                    .append(".`")
                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
                    .append("` ")
                    .append(" FROM ")
                    .append("`temp_")
                    .append(item.sourceTable)
                    .append("` LEFT JOIN `")
                    .append(item.targetTable)
                    .append("` ON ")
                    .append("`temp_")
                    .append(item.sourceTable)
                    .append("`")
                    .append(".`")
                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
                    .append("`")
                    .append(" = ")
                    .append("`")
                    .append(item.targetTable)
                    .append("`")
                    .append(".`")
                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
                    .append("` WHERE ")
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
