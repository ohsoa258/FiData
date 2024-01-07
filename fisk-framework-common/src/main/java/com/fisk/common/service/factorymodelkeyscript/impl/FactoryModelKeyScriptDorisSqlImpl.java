package com.fisk.common.service.factorymodelkeyscript.impl;

import com.fisk.common.core.utils.StringBuildUtils;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.factorymodelkeyscript.IBuildFactoryModelKeyScript;

import java.util.ArrayList;
import java.util.HashMap;
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
//                    .append(item.targetColumn)cccc
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

    //todo：以下是update换为select语句  并放入到sql预览的from后面

    /**
     * 拼接数仓建模构建维度key脚本
     *
     * @return
     */
    @Override
    public String buildKeyScript(List<TableSourceRelationsDTO> dto) {
        //获取要关联的外键表名 获取关联的key名称 以及要关联的字段 （left join tbl on 目标字段）
        //目标表名
        List<String> targetTables = new ArrayList<>();
        //外键字段名
        List<String> fns = new ArrayList<>();
        //外键:外键目标表
        HashMap<String, String> fnMap = new HashMap<>();

        //join on条件字段名称
        List<String> targetColumns = new ArrayList<>();

        for (TableSourceRelationsDTO item : dto) {
            //目标表名
            String targetTable = item.getTargetTable();
            //外键字段名
            String fn = StringBuildUtils.dimensionKeyName(targetTable);
            //join on条件字段名称
            String targetColumn = item.getTargetColumn();
            targetTables.add(targetTable);
            fns.add(fn);
            fnMap.put(fn, targetTable);
            targetColumns.add(targetColumn);
        }


        StringBuilder str = new StringBuilder();
        String tName = "temp_" + dto.get(0).sourceTable + ".";
        str.append("SELECT ");
        //获取此次发布的字段
        List<String> fieldNameList = dto.get(0).getFieldNameList();
        for (String fieldName : fieldNameList) {
            for (String fn : fns) {
                if (fn.equals(fieldName)) {
                    String targetTBlName = fnMap.get(fn);
                    fieldName = "`" + targetTBlName + "`" + ".`" + fieldName + "`,";
                } else {
                    fieldName = tName + "`" + fieldName + "`,";
                }
            }
            str.append(fieldName);
        }

        str.append(tName)
                .append("fidata_batch_code,")
                .append(tName)
                .append("fi_createtime, ")
                .append(tName)
                .append("fi_updatetime")
                .append(" FROM `temp_")
                .append(dto.get(0).getSourceTable())
                .append("` ");


        //开始拼接left join
        for (TableSourceRelationsDTO item : dto) {
            str.append(" LEFT JOIN `")
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
                    .append("` ");
        }

        str.append(" WHERE ")
                .append(tName).append("fidata_batch_code = '${fidata_batch_code}' AND ")
                .append(tName).append("fidata_flow_batch_code = '${fragment.index}'");

        return String.valueOf(str);
    }

    //todo：以下是update换为insert语句
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
//        String tName = "`temp_" + dto.get(0).sourceTable + "`.";
//
//        for (TableSourceRelationsDTO item : dto) {
//            List<String> keyNameList = item.getKeyNameList();
//            if (CollectionUtils.isEmpty(keyNameList)) {
//                throw new FkException(ResultEnum.PARAMTER_ERROR);
//            }
//            String pkSql = "";
//            String insertColumn = "";
//            for (String keyName : keyNameList) {
//                insertColumn = "`" + keyName + "`,";
//                keyName = tName + "`" + keyName + "`,";
//                pkSql = pkSql + keyName;
//            }
//
//            str.append("set enable_unique_key_partial_update=true;");
//            str.append("INSERT INTO ")
//                    .append("`temp_");
//            str.append(item.sourceTable)
//                    .append("` ")
//                    .append("(")
//                    .append(insertColumn)
//                    .append("`")
//                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
//                    .append("`")
//                    .append(") ");
//
//            str.append("SELECT ")
//                    .append(pkSql)
//                    .append("`")
//                    .append(item.targetTable)
//                    .append("`")
//                    .append(".`")
//                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
//                    .append("` ")
//                    .append(" FROM ")
//                    .append("`temp_")
//                    .append(item.sourceTable)
//                    .append("` LEFT JOIN `")
//                    .append(item.targetTable)
//                    .append("` ON ")
//                    .append("`temp_")
//                    .append(item.sourceTable)
//                    .append("`")
//                    .append(".`")
//                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
//                    .append("`")
//                    .append(" = ")
//                    .append("`")
//                    .append(item.targetTable)
//                    .append("`")
//                    .append(".`")
//                    .append(StringBuildUtils.dimensionKeyName(item.targetTable))
//                    .append("` WHERE ")
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

}
