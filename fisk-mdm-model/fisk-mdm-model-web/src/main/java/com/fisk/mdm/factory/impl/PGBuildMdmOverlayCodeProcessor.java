package com.fisk.mdm.factory.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.mdm.factory.BuildMdmOverlayCodeProcessor;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-12-26
 * @Description:
 */
public class PGBuildMdmOverlayCodeProcessor  implements BuildMdmOverlayCodeProcessor {
    @Override
    public String buildMeargeByPrimaryKey(List<AccessAttributeDTO> attributeList, String souceTableName, String targetTableName, Integer versionId) {

        List<String> sourceNames = new ArrayList<>();
        //处理类型转换
        for (AccessAttributeDTO accessAttributeDTO : attributeList) {
            String sourceName;
            switch (accessAttributeDTO.getDataType()) {
                case "文件":
                case "经纬度坐标":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "数值":
                case "域字段":
                    sourceName =  "CAST(NULLIF(\""+accessAttributeDTO.getFieldName()+"\",\'\'\'\') AS int4) AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "时间":
                    sourceName = "TO_TIMESTAMP(\""+accessAttributeDTO.getFieldName()+"\",\'\'HH24:MI:SS\'\') AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "日期":
                    sourceName = "TO_TIMESTAMP(\""+accessAttributeDTO.getFieldName()+"\",\'\'YYYY-MM-DD\'\') AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "日期时间":
                    sourceName = "TO_TIMESTAMP(\""+accessAttributeDTO.getFieldName()+"\",\'\'YYYY-MM-DD HH24:MI:SS\'\') AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "浮点型":
                    sourceName = "CAST(NULLIF(\""+accessAttributeDTO.getFieldName()+"\",\'\'\'\') AS DECIMAL("+accessAttributeDTO.getDataTypeLength()+","+accessAttributeDTO.getDataTypeDecimalLength()+")) AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "布尔型":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "货币":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "POI":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "文本":
                default:
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
            }
            sourceNames.add(sourceName);
        }


        //获取stg表列名
        List<String> sourceColumnNames = attributeList.stream()
                .map(i->"\""+i.getFieldName() +"\"")
                .collect(Collectors.toList());
        //获取mdm表列名
        List<String> targetColumnNames = attributeList.stream()
                .map(i->"\""+i.getMdmFieldName()+"\"")
                .collect(Collectors.toList());
        //获取主键
        List<AccessAttributeDTO> businessKeys = attributeList.stream().filter(i -> i.getBusinessKey() == 1).collect(Collectors.toList());
        //如果code是主键就正常同步
        List<AccessAttributeDTO> code = businessKeys.stream().filter(i -> i.getFieldName().equals("code")).collect(Collectors.toList());
        boolean flag = false;
        if (CollectionUtils.isEmpty(code)){
            flag = true;
        }
        StringBuilder str = new StringBuilder();
        //调用存储过程并拼接同步sql
        str.append("call \"public\".\"sync_mdm_table\"('DECLARE\nsource_row RECORD;\n");
        str.append("BEGIN\n");
        str.append("    FOR source_row IN SELECT\n");
        str.append("    fidata_version_id,\n");
        str.append("    fidata_create_time,\n");
        str.append("    fidata_create_user,\n");
        str.append("    fidata_del_flag,\n");
        str.append("    fidata_batch_code,\n");
        if(flag){
            str.append("    \"code\",\n");
        }
        str.append("    "+org.apache.commons.lang.StringUtils.join(sourceNames, ",\n    ")+"\n");

        str.append("  FROM\n");
        str.append("    \""+souceTableName+"\"\n");
        str.append("  WHERE\n");
        str.append("    fidata_batch_code = ''${fidata_batch_code}''\n");
        str.append("    AND fidata_flow_batch_code = ''${fragment.index}''\n");
        str.append("    AND fidata_version_id = ''"+versionId+"''\n");
        str.append("  LOOP\n");
        str.append("    BEGIN\n");
        str.append("        UPDATE \""+targetTableName+"\"\n");
        str.append("        SET\n");
        str.append("        fidata_version_id = source_row.fidata_version_id,\n");
        str.append("        fidata_create_time = source_row.fidata_create_time,\n");
        str.append("        fidata_create_user =  source_row.fidata_create_user,\n");
        str.append("        fidata_update_time = DATE_TRUNC(''second'', CURRENT_TIMESTAMP),\n");
        str.append("        fidata_update_user = source_row.fidata_create_user,\n");
        str.append("        fidata_del_flag = source_row.fidata_del_flag,\n");
        str.append("        fidata_batch_code = source_row.fidata_batch_code,\n");
        if(flag){
            str.append("        column_code = uuid_generate_v4(),\n");
        }
        str.append(attributeList.stream().map(i->
                "       \""+i.getMdmFieldName()+"\" = source_row.\""+i.getFieldName()+"\""
        ).collect(Collectors.joining(",\n")));
        str.append("\n      WHERE\n");
        str.append("        fidata_version_id = ''"+versionId+"''\n");
        str.append(businessKeys.stream().map(i->
                "        AND \""+i.getMdmFieldName()+"\" = source_row.\""+i.getFieldName()+"\"").collect(Collectors.joining(",\n")));
        str.append(";\n");
        str.append("      IF\n");
        str.append("        NOT FOUND THEN\n");

        str.append("          insert into \"" + targetTableName+"\" (");


        str.append("fidata_version_id, fidata_create_time, fidata_create_user, fidata_del_flag, fidata_batch_code,");
        if(flag){
            str.append("\"column_code\",");
        }
        str.append(org.apache.commons.lang.StringUtils.join(targetColumnNames, ","));
        str.append(" )\n");
        str.append("        VALUES\n");
        str.append("          (\n");
        String columnNames = sourceColumnNames.stream().map(i ->
                "           source_row." + i).collect(Collectors.joining(",\n"));
        str.append("            source_row.fidata_version_id,\n");
        str.append("            source_row.fidata_create_time,\n");
        str.append("            source_row.fidata_create_user,\n");
        str.append("            source_row.fidata_del_flag,\n");
        str.append("            source_row.fidata_batch_code,\n");
        if(flag){
            str.append("            uuid_generate_v4(),\n");
        }
        str.append(columnNames);
        str.append("\n          );\n");
        str.append("      END IF;\n");
        str.append("      EXCEPTION\n");
        str.append("      WHEN unique_violation THEN\n");
        String collect = businessKeys.stream().map(i -> {
            StringBuilder str1 = new StringBuilder();
            str1.append("%");
            return str1;
        }).collect(Collectors.joining(","));
        if(flag){
            collect+=",%";
        }
        str.append("      RAISE NOTICE''Duplicate key value ("+collect+") found. Skipping.'',");
        if(flag){
            str.append("source_row.\"code\",");
        }
        str.append(businessKeys.stream().map(i ->
                "source_row.\"" + i.getFieldName()+"\"").collect(Collectors.joining(",")));
        str.append(";\n    END;\n");
        str.append("  END LOOP;')\n");
        return str.toString();

    }

    @Override
    public String buildMeargeByAllData(List<AccessAttributeDTO> attributeList, String souceTableName, String targetTableName, Integer versionId) {
        List<String> sourceNames = new ArrayList<>();
        //处理类型转换
        for (AccessAttributeDTO accessAttributeDTO : attributeList) {
            String sourceName;
            switch (accessAttributeDTO.getDataType()) {
                case "文件":
                case "经纬度坐标":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "数值":
                case "域字段":
                    sourceName =  "CAST(NULLIF(\""+accessAttributeDTO.getFieldName()+"\",\'\') AS int4) AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "时间":
                    sourceName = "TO_TIMESTAMP(\""+accessAttributeDTO.getFieldName()+"\",\'HH24:MI:SS\') AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "日期":
                    sourceName = "TO_TIMESTAMP(\""+accessAttributeDTO.getFieldName()+"\",\'YYYY-MM-DD\') AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "日期时间":
                    sourceName = "TO_TIMESTAMP(\""+accessAttributeDTO.getFieldName()+"\",\'YYYY-MM-DD HH24:MI:SS\') AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "浮点型":
                    sourceName = "CAST(NULLIF(\""+accessAttributeDTO.getFieldName()+"\",\'\') AS DECIMAL("+accessAttributeDTO.getDataTypeLength()+","+accessAttributeDTO.getDataTypeDecimalLength()+")) AS \"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "布尔型":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "货币":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "POI":
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
                case "文本":
                default:
                    sourceName = "\"" + accessAttributeDTO.getFieldName() + "\"";
                    break;
            }
            sourceNames.add(sourceName);
        }


        //获取stg表列名
        List<String> sourceColumnNames = attributeList.stream()
                .map(i->"\""+i.getFieldName() +"\"")
                .collect(Collectors.toList());
        //获取mdm表列名
        List<String> targetColumnNames = attributeList.stream()
                .map(i->"\""+i.getMdmFieldName()+"\"")
                .collect(Collectors.toList());
        //获取主键
        List<AccessAttributeDTO> businessKeys = attributeList.stream().filter(i -> i.getBusinessKey() == 1).collect(Collectors.toList());
        //如果code是主键就正常同步
        List<AccessAttributeDTO> code = businessKeys.stream().filter(i -> i.getFieldName().equals("code")).collect(Collectors.toList());
        boolean flag = false;
        if (CollectionUtils.isEmpty(code)){
            flag = true;
        }
        StringBuilder str = new StringBuilder();
//        str.append("DELETE\n");
//        str.append("  FROM\n");
//        str.append("\""+targetTableName+"\"\n");
//        str.append("WHERE\n");
//        str.append("  fidata_version_id = '"+versionId+"';\n");
        str.append("INSERT INTO \""+targetTableName+"\" (");
        str.append("fidata_version_id, fidata_create_time, fidata_create_user, fidata_update_time, fidata_update_user, fidata_del_flag, fidata_batch_code,");
        if(flag){
            str.append("\"column_code\",");
        }
        str.append(org.apache.commons.lang.StringUtils.join(targetColumnNames, ","));
        str.append(" ) SELECT\n");
        str.append("fidata_version_id,\n");
        str.append("fidata_create_time,\n");
        str.append("fidata_create_user,\n");
        str.append("DATE_TRUNC('second', CURRENT_TIMESTAMP) as fidata_update_time,\n");
        str.append("fidata_create_user,\n");
        str.append("fidata_del_flag,\n");
        str.append("fidata_batch_code,\n");
        if(flag){
            str.append("uuid_generate_v4() as \"code\",");
        }
        str.append(org.apache.commons.lang.StringUtils.join(sourceNames, ",\n")+"\n");

        str.append("FROM\n");
        str.append("  \""+souceTableName+"\"\n");
        str.append("WHERE\n");
        str.append("  fidata_batch_code = '${fidata_batch_code}'\n");
        str.append("  AND fidata_flow_batch_code = '${fragment.index}'\n");
        str.append("  AND fidata_version_id = '"+versionId+"'\n");
        return str.toString();
    }
}
