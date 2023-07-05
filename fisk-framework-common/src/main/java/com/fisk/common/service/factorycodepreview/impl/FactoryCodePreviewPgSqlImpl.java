package com.fisk.common.service.factorycodepreview.impl;

import com.fisk.common.service.factorycodepreview.IBuildFactoryCodePreview;
import com.fisk.common.service.factorycodepreview.factorycodepreviewdto.PreviewTableBusinessDTO;
import com.fisk.common.service.factorycodepreview.factorycodepreviewdto.PublishFieldDTO;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lishiji
 * @describe 该工具类用于数仓建模和数据接入模块的pg-sql预览
 * @createtime 2023-06-07
 */
public class FactoryCodePreviewPgSqlImpl implements IBuildFactoryCodePreview {

    /**
     * 追加覆盖方式拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     * @return
     */
    @Override
    public String insertAndSelectSql(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList) {
        //拼接insert into...
        StringBuilder prefix = new StringBuilder("INSERT INTO " + tableName + " (");
        //遍历字段集合
        for (PublishFieldDTO f : fieldList) {
            prefix.append("\"")
                    .append(f.fieldEnName)
                    .append("\"")
                    .append(",");

        }
        prefix.append("fi_createtime,")
                .append("fi_updatetime,")
                .append("fidata_batch_code)");
        //拼接insert into完毕

        //拼接select...
        StringBuilder suffix = new StringBuilder("SELECT ");
        //遍历字段集合
        for (PublishFieldDTO f : fieldList) {
            if ("DATE".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" CASE WHEN CAST(")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append(" AS numeric) <=0 THEN TO_DATE(")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append(",'YYYY-MM-DD') ELSE TO_DATE(TO_CHAR(TIMESTAMP 'epoch' + ")
                        .append("CAST(")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append("AS bigint) * INTERVAL '1 millisecond', 'YYYY-MM-DD'),'YYYY-MM-DD') END AS ")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append(",");
            } else if ("TIME".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" CASE WHEN CAST(")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append(" AS numeric) <=0 THEN CAST(")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append("AS TIME) ELSE CAST(TO_CHAR(TIMESTAMP 'epoch' + ")
                        .append("CAST(")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append("AS bigint) * INTERVAL '1 millisecond', 'HH24:MI:SS') AS TIME) END AS ")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append(",");
            } else if ("TIMESTAMP".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" CASE WHEN CAST(")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append(" AS numeric) <=0 THEN TO_TIMESTAMP(")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append(",'YYYY-MM-DD HH24:MI:SS') ELSE TO_TIMESTAMP(")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append("::bigint / 1000) AT TIME ZONE 'Asia/Shanghai' END AS ")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append(",");
            }
//            else if (f.fieldType.equalsIgnoreCase("INT") || f.fieldType.equalsIgnoreCase("BIGINT")) {
//                suffix.append("CAST(")
//                        .append("\"")
//                        .append(f.fieldEnName)
//                        .append("\"")
//                        .append("::")
//                        .append(f.fieldType)
//                        .append(" AS ")
//                        .append("\"")
//                        .append(f.fieldEnName)
//                        .append("\"")
//                        .append(",");
//            }
            else {
                suffix.append("CAST(")
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
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
                        .append("\"")
                        .append(f.fieldEnName)
                        .append("\"")
                        .append(",");
            }
        }

        suffix.append("now(),")
                .append("now(),")
                .append("fidata_batch_code")
                .append(" FROM ")
                .append(sourceTableName)
                .append(" SOURCE WHERE fidata_batch_code='${fidata_batch_code}' AND fidata_flow_batch_code='${fragment.index}'");
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
    @Override
    public String fullVolumeSql(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList) {
        //全量和追加的区别在于：多了一段DELETE FROM tableName...
        //调用封装的追加方式拼接sql方法
        StringBuilder suffixSql =
                new StringBuilder(insertAndSelectSql(tableName, sourceTableName, fieldList));

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
    @Override
    public String delAndInsert(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList) {
        //业务标识覆盖方式--删除插入和追加的区别在于：多了一段delete TARGET...
        StringBuilder suffixSql =
                new StringBuilder(insertAndSelectSql(tableName, sourceTableName, fieldList));
        //获取业务标识覆盖方式标识的字段
        List<PublishFieldDTO> pkFields = fieldList.stream().filter(f -> f.isBusinessKey == 1).collect(Collectors.toList());

        //开始拼接前缀：delete TARGET...  拼接到SOURCE.fidata_batch_code
        StringBuilder suffix = new StringBuilder();
        suffix.append("DELETE FROM ")
                .append(tableName)
                .append(" USING(SELECT fidata_batch_code,")
                .append("?")
                .append("FROM")
                .append(sourceTableName)
                .append("WHERE fidata_batch_code = '${fidata_batch_code}' AND fidata_flow_batch_code = '${fragment.index}' ")
                .append("GROUP BY fidata_batch_code,")
                .append("<?>")
                .append(") SOURCE WHERE ")
                .append(tableName)
                .append(".fidata_batch_code <> SOURCE.fidata_batch_code");

        //新建业务覆盖标识字段字符串，预装载所有业务覆盖标识字段字符串，格式为:  字段a,字段b,字段c,字段end     为了替换suffix前缀中预留的占位符  ?
        StringBuilder pkFieldNames = new StringBuilder();
        //新建业务覆盖标识字段字符串，预装载所有业务覆盖标识字段字符串，格式为:  字段a,字段b,字段c,字段end     为了替换suffix前缀中预留的占位符  <?>
        StringBuilder pkFieldNames1 = new StringBuilder();
        if (!CollectionUtils.isEmpty(pkFields)) {
            //此循环是为了拼出所有业务覆盖标识字段名称的字符串 格式为:  字段a,字段b,字段c,字段,
            for (PublishFieldDTO pkField : pkFields) {
                if ("DATE".equalsIgnoreCase(pkField.fieldType)) {
                    pkFieldNames.append(" CASE WHEN CAST(")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append(" AS numeric) <=0 THEN TO_DATE(")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append(",'YYYY-MM-DD') ELSE TO_DATE(TO_CHAR(TIMESTAMP 'epoch' + ")
                            .append("CAST(")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append("AS bigint) * INTERVAL '1 millisecond', 'YYYY-MM-DD'),'YYYY-MM-DD') END AS ")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append(",");
                } else if ("TIME".equalsIgnoreCase(pkField.fieldType)) {
                    pkFieldNames.append(" CASE WHEN CAST(")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append(" AS numeric) <=0 THEN CAST(")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append("AS TIME) ELSE CAST(TO_CHAR(TIMESTAMP 'epoch' + ")
                            .append("CAST(")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append("AS bigint) * INTERVAL '1 millisecond', 'HH24:MI:SS') AS TIME) END AS ")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append(",");
                } else if ("TIMESTAMP".equalsIgnoreCase(pkField.fieldType)) {
                    pkFieldNames.append(" CASE WHEN CAST(")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append(" AS numeric) <=0 THEN TO_TIMESTAMP(")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append(",'YYYY-MM-DD HH24:MI:SS') ELSE TO_TIMESTAMP(")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append("::bigint / 1000) AT TIME ZONE 'Asia/Shanghai' END AS ")
                            .append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append(",");
                } else {
                    pkFieldNames.append("\"")
                            .append(pkField.fieldEnName)
                            .append("\"")
                            .append(",");
                }
            }
            //删除最后一个多余的逗号
            pkFieldNames.deleteCharAt(pkFieldNames.lastIndexOf(","));
        }

        //此循环是为了拼出所有业务覆盖标识字段名称的字符串 格式为:  字段a,字段b,字段c,字段,
        // 去掉上一个循环的 AS ")
        //                            .append("[")
        //                            .append(pkField.fieldEnName)
        //                            .append("] ");
        //替换第二个占位符 <?>
        for (PublishFieldDTO pkField : pkFields) {

            if ("DATE".equalsIgnoreCase(pkField.fieldType)) {
                pkFieldNames1.append(" CASE WHEN CAST(")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append(" AS numeric) <=0 THEN TO_DATE(")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append(",'YYYY-MM-DD') ELSE TO_DATE(TO_CHAR(TIMESTAMP 'epoch' + ")
                        .append("CAST(")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append("AS bigint) * INTERVAL '1 millisecond', 'YYYY-MM-DD'),'YYYY-MM-DD') END")
                        .append(",");
            } else if ("TIME".equalsIgnoreCase(pkField.fieldType)) {
                pkFieldNames1.append(" CASE WHEN CAST(")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append(" AS numeric) <=0 THEN CAST(")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append("AS TIME) ELSE CAST(TO_CHAR(TIMESTAMP 'epoch' + ")
                        .append("CAST(")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append("AS bigint) * INTERVAL '1 millisecond', 'HH24:MI:SS') AS TIME) END")
                        .append(",");
            } else if ("TIMESTAMP".equalsIgnoreCase(pkField.fieldType)) {
                pkFieldNames1.append(" CASE WHEN CAST(")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append(" AS numeric) <=0 THEN TO_TIMESTAMP(")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append(",'YYYY-MM-DD HH24:MI:SS') ELSE TO_TIMESTAMP(")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append("::bigint / 1000) AT TIME ZONE 'Asia/Shanghai' END")
                        .append(",");
            } else {
                pkFieldNames1.append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append(",");
            }
        }
        //删除最后一个多余的逗号
        pkFieldNames1.deleteCharAt(pkFieldNames1.lastIndexOf(","));

        //替换规则
        String regex = "\\?";
        String regex1 = "<\\?>";
        //将所有的占位符 ? 替换成我们拼接完成的业务覆盖标识字段字符串
        String halfSql = String.valueOf(suffix).replaceFirst(regex, String.valueOf(pkFieldNames));
        //将所有的占位符 <?> 替换成我们拼接完成的业务覆盖标识字段字符串
        halfSql = halfSql.replaceFirst(regex1, String.valueOf(pkFieldNames1));

        //String halfSql转为StringBulider,准备拼接
        StringBuilder matchAgain = new StringBuilder(halfSql);
        //第二次拼接开始：AND TARGET.'业务主键标识的字段' = SOURCE.'业务主键标识的字段' ...
        for (PublishFieldDTO pkField : pkFields) {
            matchAgain.append(" AND ")
                    .append(tableName)
                    .append(".")
                    .append("\"")
                    .append(pkField.fieldEnName)
                    .append("\"")
                    .append(" = ");

//            if ("DATE".equalsIgnoreCase(pkField.fieldType)) {
//                matchAgain.append(" CASE WHEN CAST(SOURCE.")
//                        .append("\"")
//                        .append(pkField.fieldEnName)
//                        .append("\"")
//                        .append(" AS numeric) <=0 THEN TO_DATE(SOURCE.")
//                        .append("\"")
//                        .append(pkField.fieldEnName)
//                        .append("\"")
//                        .append(",'YYYY-MM-DD') ELSE TO_DATE(TO_CHAR(TIMESTAMP 'epoch' + ")
//                        .append("CAST(SOURCE.")
//                        .append("\"")
//                        .append(pkField.fieldEnName)
//                        .append("\"")
//                        .append("AS bigint) * INTERVAL '1 millisecond', 'YYYY-MM-DD'),'YYYY-MM-DD') END")
//                        .append(" ");
//            } else if ("TIME".equalsIgnoreCase(pkField.fieldType)) {
//                matchAgain.append(" CASE WHEN CAST(SOURCE.")
//                        .append("\"")
//                        .append(pkField.fieldEnName)
//                        .append("\"")
//                        .append(" AS numeric) <=0 THEN CAST(SOURCE.")
//                        .append("\"")
//                        .append(pkField.fieldEnName)
//                        .append("\"")
//                        .append("AS TIME) ELSE CAST(TO_CHAR(TIMESTAMP 'epoch' + ")
//                        .append("CAST(SOURCE.")
//                        .append("\"")
//                        .append(pkField.fieldEnName)
//                        .append("\"")
//                        .append("AS bigint) * INTERVAL '1 millisecond', 'HH24:MI:SS') AS TIME) END")
//                        .append(" ");
//            } else if ("TIMESTAMP".equalsIgnoreCase(pkField.fieldType)) {
//                matchAgain.append(" CASE WHEN CAST(SOURCE.")
//                        .append("\"")
//                        .append(pkField.fieldEnName)
//                        .append("\"")
//                        .append(" AS numeric) <=0 THEN TO_TIMESTAMP(SOURCE.")
//                        .append("\"")
//                        .append(pkField.fieldEnName)
//                        .append("\"")
//                        .append(",'YYYY-MM-DD HH24:MI:SS') ELSE TO_TIMESTAMP(")
//                        .append("SOURCE.")
//                        .append("\"")
//                        .append(pkField.fieldEnName)
//                        .append("\"")
//                        .append("::bigint / 1000) AT TIME ZONE 'Asia/Shanghai' END")
//                        .append(" ");
//            }
            if ("INT4".equalsIgnoreCase(pkField.fieldType) || "INT8".equalsIgnoreCase(pkField.fieldType) || "FLOAT4".equalsIgnoreCase(pkField.fieldType)) {
                matchAgain.append("CAST(SOURCE.")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append(" AS ")
                        .append(pkField.fieldType)
                        .append(") ");
            } else {
                matchAgain.append("SOURCE.")
                        .append("\"")
                        .append(pkField.fieldEnName)
                        .append("\"")
                        .append(" ");
            }
        }
        //拼接分号，拼成最终的sql
        String finalSql = String.valueOf(matchAgain.append(";   "));

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
    @Override
    public String merge(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList) {
        //获取业务标识覆盖方式标识的字段
        List<PublishFieldDTO> pkFields = fieldList.stream().filter(f -> f.isBusinessKey == 1).collect(Collectors.toList());
        String startSql = insertAndSelectSql(tableName, sourceTableName, fieldList);
        startSql = startSql.substring(0, startSql.lastIndexOf(sourceTableName));
        StringBuilder firstSql = new StringBuilder(startSql);
        firstSql.append(sourceTableName)
                .append(" AS source WHERE source.fidata_batch_code = '${fidata_batch_code}' AND source.fidata_flow_batch_code = '${fragment.index}' ")
                .append("ON CONFLICT (");

        //遍历业务标识覆盖方式标识的字段集合
        for (PublishFieldDTO f : pkFields) {
            firstSql.append("\"")
                    .append(f.fieldEnName)
                    .append("\"")
                    .append(",");
        }
        //删除最后一个多余的逗号
        firstSql.deleteCharAt(firstSql.lastIndexOf(","));
        //补上最后一个括号
        firstSql.append(") ");

        //继续拼接
        //开始拼接后半段
        StringBuilder endSql = firstSql;
        endSql.append("DO UPDATE ")
                .append("SET ");

        //遍历字段集合--不包含主键
        for (PublishFieldDTO f : fieldList) {
            endSql.append("\"")
                    .append(f.fieldEnName)
                    .append("\"")
                    .append(" = EXCLUDED.")
                    .append("\"")
                    .append(f.fieldEnName)
                    .append("\"")
                    .append(",");
        }
        endSql.append("\"fi_updatetime\"=NOW();");

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
    @Override
    public String businessTimeOverLay(String tableName, String sourceTableName,
                                      List<PublishFieldDTO> fieldList, PreviewTableBusinessDTO previewTableBusinessDTO) {
//        //主键字段剔除
//        List<PublishFieldDTO> fieldListWithoutPk = fieldList.stream().filter(f -> f.isPrimaryKey != 1).collect(Collectors.toList());
        //获取页面选择的逻辑类型：1普通模式    2高级模式
        Integer otherLogic = previewTableBusinessDTO.otherLogic;
        if (otherLogic == 0) {
            return "'请选择业务时间覆盖方式!'";
        }

        //业务覆盖单位,Year,Month,Day,Hour
        String rangeDateUnit = previewTableBusinessDTO.rangeDateUnit;
        //业务覆盖时间范围
        Long businessRange = previewTableBusinessDTO.businessRange;
        if (businessRange != 0) {
            businessRange = Long.valueOf(businessRange.toString().split("-")[1]);
        }
        //业务时间覆盖字段
        String businessTimeField = previewTableBusinessDTO.businessTimeField;
        if (StringUtils.isEmpty(businessTimeField)) {
            return "'请选择业务时间覆盖字段!'";
        }
        //>,>=,=,<=,<  (条件符号)
        String businessOperator = previewTableBusinessDTO.businessOperator;
        StringBuilder startSQL = new StringBuilder("DELETE FROM ");
        startSQL.append(tableName)
                .append(" WHERE fidata_batch_code<>'${fidata_batch_code}' AND ");

        //最后加上这个tailSql
        StringBuilder tailSql = new StringBuilder(" AND ");

        PublishFieldDTO field = new PublishFieldDTO();

        for (PublishFieldDTO publishFieldDTO : fieldList) {
            if (publishFieldDTO.fieldEnName.equals(businessTimeField)) {
                field = publishFieldDTO;
                break;
            }
        }

        if (otherLogic == 1) {
            if ("TIME".equalsIgnoreCase(field.fieldType)) {
                //拼接所选择的字段，条件运算符，时间单位，运算时间范围...
                startSQL.append("\"")
                        .append(businessTimeField)
                        .append("\"")
                        .append(businessOperator)
                        .append("(CURRENT_TIME - INTERVAL '")
                        .append(businessRange)
                        .append(" ")
                        .append(rangeDateUnit.toLowerCase())
                        .append("');   ");

                tailSql.append("CAST(TO_CHAR(TIMESTAMP 'epoch' + ")
                        .append("CAST(")
                        .append("\"")
                        .append(businessTimeField)
                        .append("\"")
                        .append("AS bigint) * INTERVAL '1 millisecond', 'HH24:MI:SS') AS TIME)")
                        .append(businessOperator)
                        .append("(CURRENT_TIME - INTERVAL '")
                        .append(businessRange)
                        .append(" ")
                        .append(rangeDateUnit.toLowerCase())
                        .append("')")
                        .append(";   ");
            } else {
                //拼接所选择的字段，条件运算符，时间单位，运算时间范围...
                startSQL.append("\"")
                        .append(businessTimeField)
                        .append("\"")
                        .append(businessOperator)
                        .append("(NOW() - INTERVAL '")
                        .append(businessRange)
                        .append(" ")
                        .append(rangeDateUnit.toLowerCase())
                        .append("');   ");

                tailSql.append("TO_TIMESTAMP(CAST(left(")
                        .append("\"")
                        .append(businessTimeField)
                        .append("\"")
                        .append(", 10) AS bigint))")
                        .append(businessOperator)
                        .append("(NOW() - INTERVAL '")
                        .append(businessRange)
                        .append(" ")
                        .append(rangeDateUnit.toLowerCase())
                        .append("');   ");
            }
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

            if (businessRangeStandby != 0) {
                businessRangeStandby = Long.valueOf(businessRangeStandby.toString().split("-")[1]);
            }


            if ("TIME".equalsIgnoreCase(field.fieldType)) {
                startSQL.append("\"")
                        .append(businessTimeField)
                        .append("\"")
                        .append(businessOperator)
                        .append("(")
                        .append("CASE WHEN EXTRACT(")
                        .append(businessTimeFlag)
                        .append(" FROM CURRENT_TIMESTAMP)<")
                        .append(businessDate)
                        .append(" THEN ")
                        .append("(CURRENT_TIME - INTERVAL '")
                        .append(businessRange)
                        .append(" ")
                        .append(rangeDateUnit.toLowerCase())
                        .append("')")
                        .append(" ELSE ")
                        .append("(CURRENT_TIME - INTERVAL '")
                        .append(businessRangeStandby)
                        .append(" ")
                        .append(rangeDateUnitStandby.toLowerCase())
                        .append("') END)")
                        .append(";   ");

                tailSql.append("CAST(TO_CHAR(TIMESTAMP 'epoch' + ")
                        .append("CAST(")
                        .append("\"")
                        .append(businessTimeField)
                        .append("\"")
                        .append("AS bigint) * INTERVAL '1 millisecond', 'HH24:MI:SS') AS TIME)")
                        .append(businessOperator)
                        .append("(")
                        .append("CASE WHEN EXTRACT(")
                        .append(businessTimeFlag)
                        .append(" FROM CURRENT_TIMESTAMP)<")
                        .append(businessDate)
                        .append(" THEN ")
                        .append("(CURRENT_TIME - INTERVAL '")
                        .append(businessRange)
                        .append(" ")
                        .append(rangeDateUnit.toLowerCase())
                        .append("')")
                        .append(" ELSE ")
                        .append("(CURRENT_TIME - INTERVAL '")
                        .append(businessRangeStandby)
                        .append(" ")
                        .append(rangeDateUnitStandby.toLowerCase())
                        .append("') END)")
                        .append(";   ");
            } else {
                startSQL.append("\"")
                        .append(businessTimeField)
                        .append("\"")
                        .append(businessOperator)
                        .append("(")
                        .append("CASE WHEN EXTRACT(")
                        .append(businessTimeFlag)
                        .append(" FROM CURRENT_TIMESTAMP)<")
                        .append(businessDate)
                        .append(" THEN ")
                        .append("(NOW() - INTERVAL '")
                        .append(businessRange)
                        .append(" ")
                        .append(rangeDateUnit.toLowerCase())
                        .append("')")
                        .append(" ELSE ")
                        .append("(NOW() - INTERVAL '")
                        .append(businessRangeStandby)
                        .append(" ")
                        .append(rangeDateUnitStandby.toLowerCase())
                        .append("') END)")
                        .append(";   ");

                tailSql.append("TO_TIMESTAMP(CAST(left(")
                        .append("\"")
                        .append(businessTimeField)
                        .append("\"")
                        .append(", 10) AS bigint))")
                        .append(businessOperator)
                        .append("(")
                        .append("CASE WHEN EXTRACT(")
                        .append(businessTimeFlag)
                        .append(" FROM CURRENT_TIMESTAMP)<")
                        .append(businessDate)
                        .append(" THEN ")
                        .append("(NOW() - INTERVAL '")
                        .append(businessRange)
                        .append(" ")
                        .append(rangeDateUnit.toLowerCase())
                        .append("')")
                        .append(" ELSE ")
                        .append("(NOW() - INTERVAL '")
                        .append(businessRangeStandby)
                        .append(" ")
                        .append(rangeDateUnitStandby.toLowerCase())
                        .append("') END)")
                        .append(";   ");
            }
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
