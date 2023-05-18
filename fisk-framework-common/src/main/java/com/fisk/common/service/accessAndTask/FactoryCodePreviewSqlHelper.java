package com.fisk.common.service.accessAndTask;

import com.fisk.common.service.accessAndTask.factorycodepreviewdto.PreviewTableBusinessDTO;
import com.fisk.common.service.accessAndTask.factorycodepreviewdto.PublishFieldDTO;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lishiji
 * @describe 该工具类用于数仓建模和数据接入模块的sql预览
 * @createtime 2023-04-21
 */
public class FactoryCodePreviewSqlHelper {

    /**
     * 追加覆盖方式拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     * @return
     */
    public static String insertAndSelectSql(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList) {
        //拼接insert into...
        StringBuilder prefix = new StringBuilder("INSERT INTO " + tableName + " (");
//        //主键字段剔除
//        List<PublishFieldDTO> fieldListWithoutPk = fieldList.stream().filter(f -> f.isPrimaryKey != 1).collect(Collectors.toList());
        //遍历字段集合
        for (PublishFieldDTO f : fieldList) {
            if (f.sourceFieldName != null && f.attributeType == 0) {
                prefix.append("[")
                        .append(f.sourceFieldName)
                        .append("]")
                        .append(",");
            } else {
                prefix.append("[")
                        .append(f.fieldEnName)
                        .append("]")
                        .append(",");
            }

        }
        prefix.append("fi_createtime,")
                .append("fi_updatetime,")
                .append("fidata_batch_code)");
        //拼接insert into完毕

        //拼接select...
        StringBuilder suffix = new StringBuilder("SELECT ");
        //遍历字段集合
        for (PublishFieldDTO f : fieldList) {
            if (f.sourceFieldName != null && f.attributeType == 0) {
                //主键不需要
                if (f.fieldType.equalsIgnoreCase("DATE")) {
                    suffix.append(" CASE WHEN CAST(isnumeric(")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(")")
                            .append(" AS int) <=0 THEN ")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(" ELSE convert(datetime,DATEADD(DATE,CAST(LEFT(")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(",10) AS bigint)/60,'1970-1-1'),112) END, ");
                } else if (f.fieldType.equalsIgnoreCase("TIME")) {
                    suffix.append(" CASE WHEN CAST(isnumeric(")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(")")
                            .append(" AS int) <=0 THEN ")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(" ELSE convert(datetime,DATEADD(MINUTE,CAST(LEFT(")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(",10) AS bigint)/60,'08:00:00'),112) END, ");
                } else if (f.fieldType.equalsIgnoreCase("TIMESTAMP")) {
                    suffix.append(" CASE WHEN CAST(isnumeric(")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(")")
                            .append(" AS int) <=0 THEN ")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(" ELSE convert(datetime,DATEADD(MINUTE,CAST(left(")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(",10) AS bigint)/60,'1970-01-01 08:00:00'),112) END, ");
                } else if (f.fieldType.equalsIgnoreCase("DATETIME")) {
                    suffix.append(" CASE WHEN CAST(isnumeric(")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(")")
                            .append(" AS int) <=0 THEN ")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(" ELSE convert(datetime,DATEADD(MINUTE,CAST(left(")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(",10) AS bigint)/60,'1970-01-01 08:00:00'),112) END, ");
                } else {
                    suffix.append("CAST(")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(" AS ")
                            .append(f.fieldType);
                    if ("NVARCHAR".equalsIgnoreCase(f.fieldType) || "VARCHAR".equalsIgnoreCase(f.fieldType)) {
                        suffix.append("(")
                                .append(f.fieldLength)
                                .append("))");
                    } else {
                        suffix.append(")");
                    }
                    suffix.append(" AS ")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(",");
                }
            } else {
                //主键不需要
                if (f.fieldType.equalsIgnoreCase("DATE")) {
                    suffix.append(" CASE WHEN CAST(isnumeric(")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(")")
                            .append(" AS int) <=0 THEN ")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(" ELSE convert(datetime,DATEADD(DATE,CAST(LEFT(")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(",10) AS bigint)/60,'1970-1-1'),112) END, ");
                } else if (f.fieldType.equalsIgnoreCase("TIME")) {
                    suffix.append(" CASE WHEN CAST(isnumeric(")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(")")
                            .append(" AS int) <=0 THEN ")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(" ELSE convert(datetime,DATEADD(MINUTE,CAST(LEFT(")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(",10) AS bigint)/60,'08:00:00'),112) END, ");
                } else if (f.fieldType.equalsIgnoreCase("TIMESTAMP")) {
                    suffix.append(" CASE WHEN CAST(isnumeric(")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(")")
                            .append(" AS int) <=0 THEN ")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(" ELSE convert(datetime,DATEADD(MINUTE,CAST(left(")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(",10) AS bigint)/60,'1970-01-01 08:00:00'),112) END, ");
                } else if (f.fieldType.equalsIgnoreCase("DATETIME")) {
                    suffix.append(" CASE WHEN CAST(isnumeric(")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(")")
                            .append(" AS int) <=0 THEN ")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(" ELSE convert(datetime,DATEADD(MINUTE,CAST(left(")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(",10) AS bigint)/60,'1970-01-01 08:00:00'),112) END, ");
                } else {
                    suffix.append("CAST(")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(" AS ")
                            .append(f.fieldType);
                    if ("NVARCHAR".equalsIgnoreCase(f.fieldType) || "VARCHAR".equalsIgnoreCase(f.fieldType)) {
                        suffix.append("(")
                                .append(f.fieldLength)
                                .append("))");
                    } else {
                        suffix.append(")");
                    }
                    suffix.append(" AS ")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(",");
                }
            }

        }
        suffix.append("getdate(),")
                .append("getdate(),")
                .append("fidata_batch_code")
                .append(" FROM ")
                .append(sourceTableName)
                .append(" SOURCE WITH(nolock) WHERE fidata_batch_code='${fidata_batch_code}' AND fidata_flow_batch_code='${fragment.index}'");
        //拼接select完毕

        //返回拼接完成的追加覆盖方式拼接的sql
        return prefix + "   " + suffix;
    }

    /**
     * 全量覆盖方式拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     * @return
     */
    public static String fullVolumeSql(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList) {
        //全量和追加的区别在于：多了一段truncate table tableName...
        //调用封装的追加方式拼接sql方法
        StringBuilder suffixSql =
                new StringBuilder(FactoryCodePreviewSqlHelper.insertAndSelectSql(tableName, sourceTableName, fieldList));

        //返回的sql前加上需要的前缀truncate table tableName,并隔开两段sql
        StringBuilder fullVolumeSql = suffixSql.insert(0, "DELETE FROM " + tableName + " WHERE fidata_batch_code<>'${fidata_batch_code}';   ");
        //返回拼接完成的全量覆盖方式拼接的sql
        return String.valueOf(fullVolumeSql);
    }

    /**
     * 业务标识覆盖方式--删除插入--拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     * @return
     */
    public static String delAndInsert(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList) {
        //业务标识覆盖方式--删除插入和追加的区别在于：多了一段delete TARGET...
        StringBuilder suffixSql =
                new StringBuilder(FactoryCodePreviewSqlHelper.insertAndSelectSql(tableName, sourceTableName, fieldList));
        //获取业务标识覆盖方式标识的字段
        List<PublishFieldDTO> pkFields = fieldList.stream().filter(f -> f.isBusinessKey == 1).collect(Collectors.toList());

        //开始拼接前缀：delete TARGET...  拼接到SOURCE.fidata_batch_code
        StringBuilder suffix = new StringBuilder();
        suffix.append("DELETE TARGET FROM ")
                .append(tableName)
                .append(" TARGET JOIN (SELECT fidata_batch_code,")
                .append(" ? ")
                .append("FROM ")
                .append(sourceTableName)
                .append(" WHERE fidata_batch_code='${fidata_batch_code}' AND fidata_flow_batch_code='${fragment.index}'")
                .append(" GROUP BY fidata_batch_code,")
                .append(" ? ")
                .append(") ")
                .append("SOURCE ON TARGET.fidata_batch_code <> SOURCE.fidata_batch_code ");

        //新建业务覆盖标识字段字符串，预装载所有业务覆盖标识字段字符串，格式为:  字段a,字段b,字段c,字段end     为了替换suffix前缀中预留的占位符  ?
        StringBuilder pkFieldNames = new StringBuilder();
        if (!CollectionUtils.isEmpty(pkFields)) {
            //此循环是为了拼出所有业务覆盖标识字段名称的字符串 格式为:  字段a,字段b,字段c,字段,
            for (PublishFieldDTO pkField : pkFields) {
                if (pkField.sourceFieldName != null && pkField.attributeType == 0) {
                    pkFieldNames.append("[")
                            .append(pkField.sourceFieldName)
                            .append("]")
                            .append(",");
                } else {
                    pkFieldNames.append("[")
                            .append(pkField.fieldEnName)
                            .append("]")
                            .append(",");
                }
            }
            //删除最后一个多余的逗号
            pkFieldNames.deleteCharAt(pkFieldNames.lastIndexOf(","));
        }

        //替换规则
        String regex = "\\?";
        //将所有的占位符 ? 替换成我们拼接完成的业务覆盖标识字段字符串
        String halfSql = String.valueOf(suffix).replaceAll(regex, String.valueOf(pkFieldNames));

        //String halfSql转为StringBulider,准备拼接
        StringBuilder matchAgain = new StringBuilder(halfSql);
        //第二次拼接开始：AND TARGET.'业务主键标识的字段' = SOURCE.'业务主键标识的字段' ...
        for (PublishFieldDTO pkField : pkFields) {
            if (pkField.sourceFieldName != null && pkField.attributeType == 0) {
                matchAgain.append("AND TARGET.")
                        .append("[")
                        .append(pkField.sourceFieldName)
                        .append("]")
                        .append(" = SOURCE.")
                        .append("[")
                        .append(pkField.sourceFieldName)
                        .append("]")
                        .append(" ");
            } else {
                matchAgain.append("AND TARGET.")
                        .append("[")
                        .append(pkField.fieldEnName)
                        .append("]")
                        .append(" = SOURCE.")
                        .append("[")
                        .append(pkField.fieldEnName)
                        .append("]")
                        .append(" ");
            }
        }
        //拼接分号，拼成最终的sql
        String finalSql = String.valueOf(matchAgain.append("   "));

        //返回的sql前加上需要的前缀finalSql
        StringBuilder delInsertSql = suffixSql.insert(0, finalSql);
        //返回拼接后完整的删除插入sql
        return String.valueOf(delInsertSql);
    }

    /**
     * 业务标识覆盖方式--merge覆盖（业务标识可以作为业务主键）--拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     * @return
     */
    public static String merge(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList) {
//        //主键字段剔除
//        List<PublishFieldDTO> fieldListWithoutPk = fieldList.stream().filter(f -> f.isPrimaryKey != 1).collect(Collectors.toList());

        //拼接第一段...  前段
        StringBuilder startSql = new StringBuilder("MERGE ");
        startSql.append(tableName)
                .append(" AS TARGET USING (SELECT ");
        //遍历字段集合--不包含主键
        for (PublishFieldDTO f : fieldList) {
            if (f.sourceFieldName != null && f.attributeType == 0) {
                startSql.append("[")
                        .append(f.sourceFieldName)
                        .append("]")
                        .append(",");
            } else {
                startSql.append("[")
                        .append(f.fieldEnName)
                        .append("]")
                        .append(",");
            }
        }
        //删除最后一个多余的逗号
        startSql.deleteCharAt(startSql.lastIndexOf(","));
        //继续拼接
        startSql.append(" FROM ")
                .append(sourceTableName)
                .append(" WITH(nolock) WHERE fidata_batch_code='${fidata_batch_code}'AND fidata_flow_batch_code='${fragment.index}') AS SOURCE ON ");
        //获取业务标识覆盖方式标识的字段
        List<PublishFieldDTO> pkFields = fieldList.stream().filter(f -> f.isBusinessKey == 1).collect(Collectors.toList());
        //拼接开始：TARGET.'业务主键标识的字段' = SOURCE.'业务主键标识的字段' ...
        if (!CollectionUtils.isEmpty(pkFields)) {
            //遍历前端传递的字段集合--只包含主键
            for (PublishFieldDTO pkField : pkFields) {
                if (pkField.sourceFieldName != null && pkField.attributeType == 0) {
                    startSql.append("TARGET.")
                            .append("[")
                            .append(pkField.sourceFieldName)
                            .append("]")
                            .append(" = SOURCE.")
                            .append("[")
                            .append(pkField.sourceFieldName)
                            .append("]")
                            .append(" AND ");
                } else {
                    startSql.append("TARGET.")
                            .append("[")
                            .append(pkField.fieldEnName)
                            .append("]")
                            .append(" = SOURCE.")
                            .append("[")
                            .append(pkField.fieldEnName)
                            .append("]")
                            .append(" AND ");
                }
            }
        }
        //删除多余的AND
        startSql.delete(startSql.lastIndexOf("A"), startSql.lastIndexOf("D") + 1);
        //第一段sql与第二段sql隔开
        startSql.append("   ");

        //开始拼接第二段  中段
        StringBuilder middleSql = startSql;
        middleSql.append("WHEN MATCHED THEN UPDATE SET ");

        //遍历字段集合--不包含主键
        for (PublishFieldDTO f : fieldList) {
            if (f.sourceFieldName != null && f.attributeType == 0) {
                if (f.fieldType.equalsIgnoreCase("DATE")) {
                    middleSql.append("TARGET.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(" = convert(datetime,DATEADD(DATE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("TIME")) {
                    middleSql.append("TARGET.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(" = convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("TIMESTAMP")) {
                    middleSql.append("TARGET.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(" = convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("DATETIME")) {
                    middleSql.append("TARGET.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(" = convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else {
                    middleSql.append("TARGET.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(" = SOURCE.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(",");
                }
            } else {
                if (f.fieldType.equalsIgnoreCase("DATE")) {
                    middleSql.append("TARGET.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(" = convert(datetime,DATEADD(DATE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("TIME")) {
                    middleSql.append("TARGET.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(" = convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("TIMESTAMP")) {
                    middleSql.append("TARGET.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(" = convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("DATETIME")) {
                    middleSql.append("TARGET.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(" = convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else {
                    middleSql.append("TARGET.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(" = SOURCE.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(",");
                }
            }


        }
        middleSql.append("TARGET.fi_updatetime=GETDATE()");
        //第二段sql与第三段sql隔开
        middleSql.append("   ");

        //开始拼接第三段
        StringBuilder endSql = middleSql;
        endSql.append("WHEN NOT MATCHED THEN insert")
                .append("(");

        //遍历字段集合,拼接 insert(.....)
        for (PublishFieldDTO f : fieldList) {
            if (f.sourceFieldName != null && f.attributeType == 0) {
                endSql.append("[")
                        .append(f.sourceFieldName)
                        .append("]")
                        .append(",");
            } else {
                endSql.append("[")
                        .append(f.fieldEnName)
                        .append("]")
                        .append(",");
            }

        }
        endSql.append("fi_createtime, fi_updatetime, fidata_batch_code) Values(");

        //遍历字段集合,拼接values...
        for (PublishFieldDTO f : fieldList) {
            if (f.sourceFieldName != null && f.attributeType == 0) {
                if (f.fieldType.equalsIgnoreCase("DATE")) {
                    endSql.append("convert(datetime,DATEADD(DATE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("TIME")) {
                    endSql.append("convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("TIMESTAMP")) {
                    endSql.append("convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("DATETIME")) {
                    endSql.append("convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else {
                    endSql.append("SOURCE.")
                            .append("[")
                            .append(f.sourceFieldName)
                            .append("]")
                            .append(",");
                }
            } else {
                if (f.fieldType.equalsIgnoreCase("DATE")) {
                    endSql.append("convert(datetime,DATEADD(DATE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("TIME")) {
                    endSql.append("convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("TIMESTAMP")) {
                    endSql.append("convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else if (f.fieldType.equalsIgnoreCase("DATETIME")) {
                    endSql.append("convert(datetime,DATEADD(MINUTE,CAST(left(SOURCE.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(", 10) AS bigint) / 60,'1970-01-01 08:00:00'),112)")
                            .append(",");
                } else {
                    endSql.append("SOURCE.")
                            .append("[")
                            .append(f.fieldEnName)
                            .append("]")
                            .append(",");
                }
            }

        }
        endSql.append("GETDATE(),")
                .append("GETDATE(),")
                .append("'${fidata_batch_code}'")
                .append(");");
        //返回返回拼接完整的merge覆盖sql
        return String.valueOf(endSql);
    }

    /**
     * 业务时间覆盖方式拼接的sql代码
     *
     * @param tableName               真实表名
     * @param sourceTableName         来源表名（临时表名）
     * @param fieldList               前端传递的源表字段属性集合
     * @param previewTableBusinessDTO 业务时间覆盖方式页面选择的逻辑
     * @return
     */
    public static String businessTimeOverLay(String tableName, String sourceTableName,
                                             List<PublishFieldDTO> fieldList, PreviewTableBusinessDTO previewTableBusinessDTO) {
//        //主键字段剔除
//        List<PublishFieldDTO> fieldListWithoutPk = fieldList.stream().filter(f -> f.isPrimaryKey != 1).collect(Collectors.toList());
        //获取页面选择的逻辑类型：1普通模式    2高级模式
        Integer otherLogic = previewTableBusinessDTO.otherLogic;

        //业务覆盖单位,Year,Month,Day,Hour
        String rangeDateUnit = previewTableBusinessDTO.rangeDateUnit;
        //业务覆盖时间范围
        Long businessRange = previewTableBusinessDTO.businessRange;
        //业务时间覆盖字段
        String businessTimeField = previewTableBusinessDTO.businessTimeField;
        //>,>=,=,<=,<  (条件符号)
        String businessOperator = previewTableBusinessDTO.businessOperator;
        StringBuilder startSQL = new StringBuilder("DELETE FROM ");
        startSQL.append(tableName)
                .append(" WHERE fidata_batch_code<>'${fidata_batch_code}' AND ");

        //最后加上这个tailSql
        StringBuilder tailSql = new StringBuilder(" AND ");
        if (otherLogic == 1) {
            //拼接所选择的字段，条件运算符，时间单位，运算时间范围...
            startSQL.append(businessTimeField)
                    .append(businessOperator)
                    .append("DATEADD(")
                    .append(rangeDateUnit)
                    .append(",")
                    .append(businessRange)
                    .append(",")
                    .append("getdate())")
                    .append("   ");

            //拼接tailSql
            tailSql.append("DATEADD(MINUTE, CAST(left(")
                    .append(businessTimeField)
                    .append(", 10) AS bigint)/60, '1970-01-01 08:00:00')")
                    .append(businessOperator)
                    .append("DATEADD(")
                    .append(rangeDateUnit)
                    .append(",")
                    .append(businessRange)
                    .append(",")
                    .append("getdate())")
                    .append(";   ");
        } else {
            //具体日期
            Long businessDate = previewTableBusinessDTO.businessDate;
            //每年 每月 每天
            String businessTimeFlag = previewTableBusinessDTO.businessTimeFlag;
            if ("每年".equals(businessTimeFlag)) {
                businessTimeFlag = "MONTH";
            } else if ("每月".equals(businessTimeFlag)) {
                businessTimeFlag = "DAY";
            } else {
                businessTimeFlag = "HOUR";
            }
            //>,>=,=,<=,<  (条件符号预备值) 目前已经不需要了，前端不传这个参数
//            String businessOperatorStandby = previewTableBusinessDTO.businessOperatorStandby;
            //业务覆盖时间范围预备值
            Long businessRangeStandby = previewTableBusinessDTO.businessRangeStandby;
            //业务覆盖单位,Year,Month,Day,Hour预备值
            String rangeDateUnitStandby = previewTableBusinessDTO.rangeDateUnitStandby;
            startSQL.append(businessTimeField)
                    .append(businessOperator)
                    .append("(")
                    .append("CASE WHEN ")
                    .append(businessTimeFlag)
                    .append("(Getdate())<")
                    .append(businessDate)
                    .append(" THEN ")
                    .append("DATEADD(")
                    .append(rangeDateUnit)
                    .append(",")
                    .append(businessRange)
                    .append(",")
                    .append("getdate())")
                    .append(" ELSE ")
                    .append("DATEADD(")
                    .append(rangeDateUnitStandby)
                    .append(",")
                    .append(businessRangeStandby)
                    .append(",")
                    .append("getdate()) END)")
                    .append("   ");

            //拼接tailSql
            tailSql.append("DATEADD(MINUTE, CAST(left(")
                    .append(businessTimeField)
                    .append(", 10) AS bigint)/60, '1970-01-01 08:00:00')")
                    .append(businessOperator)
                    .append("(CASE WHEN ")
                    .append(businessTimeFlag)
                    .append("(Getdate())<")
                    .append(businessDate)
                    .append(" THEN ")
                    .append("DATEADD(")
                    .append(rangeDateUnit)
                    .append(",")
                    .append(businessRange)
                    .append(",")
                    .append("getdate())")
                    .append(" ELSE ")
                    .append("DATEADD(")
                    .append(rangeDateUnitStandby)
                    .append(",")
                    .append(businessRangeStandby)
                    .append(",")
                    .append("getdate()) END)")
                    .append(";   ");

//            tailSql.append("(")
//                    .append("CASE WHEN ")
//                    .append(businessTimeFlag)
//                    .append("(Getdate())<")
//                    .append(businessDate)
//                    .append(" THEN ")
//                    .append(businessTimeField)
//                    .append(businessOperator)
//                    .append("DATEADD(")
//                    .append(rangeDateUnit)
//                    .append(",")
//                    .append(businessRange)
//                    .append(",")
//                    .append("getdate())")
//                    .append(" ELSE ")
//                    .append(businessTimeField)
//                    .append(businessOperatorStandby)
//                    .append("DATEADD(")
//                    .append(rangeDateUnitStandby)
//                    .append(",")
//                    .append(businessRangeStandby)
//                    .append(",")
//                    .append("getdate()) END)")
//                    .append(";   ");
        }
        //调用追加的sql方法，用于拼接
        String sql = insertAndSelectSql(tableName, sourceTableName, fieldList);
        //拼接最终sql
        StringBuilder endSql = startSQL.append(sql)
                .append(tailSql);

        //返回拼接完整的业务时间覆盖的sql
        return String.valueOf(endSql);
    }

}
