package com.fisk.common.service.factorycodepreview.impl;

import com.fisk.common.service.factorycodepreview.IBuildFactoryCodePreview;
import com.fisk.common.service.factorycodepreview.factorycodepreviewdto.PreviewTableBusinessDTO;
import com.fisk.common.service.factorycodepreview.factorycodepreviewdto.PublishFieldDTO;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lishiji
 * @describe 该工具类用于数仓建模和数据接入模块的pg-sql预览
 * @createtime 2023-06-07
 */
public class FactoryCodePreviewMysqlApiSqlImpl implements IBuildFactoryCodePreview {

    /**
     * 追加覆盖方式拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     */
    @Override
    public String insertAndSelectSql(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList, String updateSql) {
        //去掉dim_ fact_ 类似前缀，用于系统主键key赋值  例如mr01key
        String tabNameWithoutPre = tableName.substring(tableName.indexOf("_") + 1);
        tabNameWithoutPre = tabNameWithoutPre.replace("`", "");

        //拼接insert into...
        StringBuilder prefix = new StringBuilder("INSERT INTO " + tableName + " (");
        //遍历字段集合
        for (PublishFieldDTO f : fieldList) {
            prefix.append("`")
                    .append(f.fieldEnName)
                    .append("`")
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
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else if ("TIME".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else if ("TIMESTAMP".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else if ("DATETIME".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else {
                suffix.append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            }
        }

        if (!StringUtils.isEmpty(updateSql)) {
            sourceTableName = "(" + updateSql + ") as FNK";
        }
        suffix.append("now(),")
                .append("now(),")
                .append("fidata_batch_code")
                .append(" FROM ")
                .append(sourceTableName)
                .append(" WHERE fidata_batch_code='${fidata_batch_code}' AND fidata_flow_batch_code='${fragment.index}' AND fi_verify_type<>'2'")
        ;
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
     */
    @Override
    public String fullVolumeSql(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList, String updateSql) {
        //全量和追加的区别在于：多了一段DELETE FROM tableName...
        //调用封装的追加方式拼接sql方法

        //返回的sql前加上需要的前缀truncate table tableName,并隔开两段sql
//        StringBuilder fullVolumeSql = suffixSql.insert(0, "DELETE FROM " + tableName + " WHERE fidata_batch_code<>'${fidata_batch_code}';   ");
        //返回拼接完成的全量覆盖方式拼接的sql
        return String.valueOf(insertAndSelectSql(tableName, sourceTableName, fieldList, updateSql));
    }

    /**
     * 业务标识覆盖方式--删除插入--拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     */
    @Override
    public String delAndInsert(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList, Integer type, String updateSql) {
        //获取业务标识覆盖方式标识的字段
        List<PublishFieldDTO> pkFields = new ArrayList<>();
        if (type != null) {
            //维度表  业务标识覆盖字段用的是isBusinessKey = 1
            if (type == 1) {
                pkFields = fieldList.stream().filter(f -> f.isBusinessKey == 1).collect(Collectors.toList());
            } else {
                //fact那些表  业务标识覆盖字段用的是isPrimaryKey = 1
                pkFields = fieldList.stream().filter(f -> f.isPrimaryKey == 1).collect(Collectors.toList());
            }
        } else {
            pkFields = fieldList.stream().filter(f -> f.isPrimaryKey == 1).collect(Collectors.toList());
        }

        //获取业务主键字段
        List<PublishFieldDTO> pkFields1 = new ArrayList<>();
        if (type != null) {
            //维度表  主键标识用的是isPrimaryKey = 1
            if (type == 1) {
                pkFields1 = fieldList.stream().filter(f -> f.isPrimaryKey == 1).collect(Collectors.toList());
            } else {
                //fact那些表  主键标识用的是isBusinessKey = 1
                pkFields1 = fieldList.stream().filter(f -> f.isBusinessKey == 1).collect(Collectors.toList());
            }
        } else {
            pkFields = fieldList.stream().filter(f -> f.isPrimaryKey == 1).collect(Collectors.toList());
        }
        //新建业务覆盖标识字段字符串，预装载所有业务覆盖标识字段字符串  为了替换delete前缀中预留的占位符 lishiji
        StringBuilder pkFieldNames = new StringBuilder();

        //新建业务覆盖标识字段字符串，预装载所有业务覆盖标识字段字符串  为了替换delete前缀中预留的占位符 lishiji1
        StringBuilder pkFieldNames1 = new StringBuilder();
        StringBuilder pkFieldNames2 = new StringBuilder();
        //开始拼接前缀：delete TARGET...  拼接到SOURCE.fidata_batch_code
        StringBuilder delete = new StringBuilder();
        delete.append("DELETE FROM ")
                .append(tableName)
                .append(" WHERE fidata_batch_code <> '${fidata_batch_code}' ")
                .append(" AND ")
                .append("EXISTS( ")
                .append("SELECT ")
                .append("lishiji")
                .append(" FROM ")
                .append("(SELECT ")
                .append("lishiji2")
                .append(" FROM ")
                .append(sourceTableName)
                .append(" WHERE fidata_batch_code = '${fidata_batch_code}' AND fidata_flow_batch_code = '${fragment.index}') AS `tempTbl` " +
                        "WHERE ")
                .append("lishiji1");

        //将所有的占位符 ? 替换成我们拼接完成的业务覆盖标识字段字符串
        for (PublishFieldDTO pkField : pkFields) {
            pkFieldNames.append("`")
                    .append(pkField.fieldEnName)
                    .append("`,");

            pkFieldNames1.append(tableName)
                    .append(".`")
                    .append(pkField.fieldEnName)
                    .append("`")
                    .append(" = ")
                    .append("`tempTbl`")
                    .append(".`")
                    .append(pkField.fieldEnName)
                    .append("`")
                    .append(" AND ");

            pkFieldNames2.append(" DISTINCT `")
                    .append(pkField.fieldEnName)
                    .append("`,");
        }
        //删除多余逗号
        pkFieldNames.deleteCharAt(pkFieldNames.lastIndexOf(","));
        //删除多余AND
        pkFieldNames1.delete(pkFieldNames1.lastIndexOf("AND"), pkFieldNames1.lastIndexOf("AND") + 3);
        pkFieldNames2.deleteCharAt(pkFieldNames2.lastIndexOf(","));

        String pksql = delete.toString();
        String halfSql = pksql.replaceFirst("lishiji", String.valueOf(pkFieldNames));
        halfSql = halfSql.replaceFirst("lishiji1", String.valueOf(pkFieldNames1));
        halfSql = halfSql.replaceFirst("lishiji2", String.valueOf(pkFieldNames2));
        halfSql = halfSql + ");   ";

        //去掉dim_ fact_ 类似前缀，用于系统主键key赋值  例如mr01key
        String tabNameWithoutPre = tableName.substring(tableName.indexOf("_") + 1);
        tabNameWithoutPre = tabNameWithoutPre.replace("`", "");

        //拼接insert into...
        StringBuilder prefix = new StringBuilder("INSERT INTO " + tableName + " (");
        //遍历字段集合
        for (PublishFieldDTO f : fieldList) {
            prefix.append("`")
                    .append(f.fieldEnName)
                    .append("`")
                    .append(",");

        }
        prefix.append("fi_createtime,")
                .append("fi_updatetime,")
                .append("fidata_batch_code,`")
                .append(tabNameWithoutPre)
                .append("key`)");
        //拼接insert into完毕

        //拼接select...
        StringBuilder suffix = new StringBuilder("SELECT ");
        //遍历字段集合
        for (PublishFieldDTO f : fieldList) {
            if ("DATE".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else if ("TIME".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else if ("TIMESTAMP".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else if ("DATETIME".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else {
                suffix.append("CAST(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
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
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            }
        }

        if (!StringUtils.isEmpty(updateSql)) {
            sourceTableName = "(" + updateSql + ") as FNK";
        }
        suffix.append("now(),")
                .append("now(),")
                .append("fidata_batch_code,")
                .append("md5(concat(\"\"lishiji))")
                .append(" FROM ")
                .append(sourceTableName);
        if (StringUtils.isEmpty(updateSql)) {
            suffix.append(" WHERE fidata_batch_code='${fidata_batch_code}' AND fidata_flow_batch_code='${fragment.index}' AND fi_verify_type<>'2'");
        }
        //替换lishiji为主键字段
        String regex = "lishiji";
        StringBuilder pkSql = new StringBuilder();
        for (PublishFieldDTO pkField : pkFields1) {
            pkSql.append(",`")
                    .append(pkField.fieldEnName)
                    .append("`");
        }
        suffix = new StringBuilder(suffix.toString().replaceAll(regex, String.valueOf(pkSql)));

        //返回拼接完成的追加覆盖方式拼接的sql
        return halfSql + prefix + "   " + suffix;
    }

    /**
     * 业务标识覆盖方式--merge覆盖（业务标识可以作为业务主键）--拼接的sql代码
     *
     * @param tableName       真实表名
     * @param sourceTableName 来源表名（临时表名）
     * @param fieldList       前端传递的源表字段属性集合
     */
    @Override
    public String merge(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList, Integer type, String updateSql) {
        //获取业务主键字段
        List<PublishFieldDTO> pkFields = new ArrayList<>();
        if (type != null) {
            //维度表  主键标识用的是isPrimaryKey = 1
            if (type == 1) {
                pkFields = fieldList.stream().filter(f -> f.isPrimaryKey == 1).collect(Collectors.toList());
            } else {
                //fact那些表  主键标识用的是isBusinessKey = 1
                pkFields = fieldList.stream().filter(f -> f.isBusinessKey == 1).collect(Collectors.toList());
            }
        } else {
            pkFields = fieldList.stream().filter(f -> f.isPrimaryKey == 1).collect(Collectors.toList());
        }

        //去掉dim_ fact_ 类似前缀，用于系统主键key赋值  例如mr01key
        String tabNameWithoutPre = tableName.substring(tableName.indexOf("_") + 1);
        tabNameWithoutPre = tabNameWithoutPre.replace("`", "");

        //拼接insert into...
        StringBuilder prefix = new StringBuilder("INSERT INTO " + tableName + " (");
        //遍历字段集合
        for (PublishFieldDTO f : fieldList) {
            prefix.append("`")
                    .append(f.fieldEnName)
                    .append("`")
                    .append(",");

        }
        prefix.append("fi_createtime,")
                .append("fi_updatetime")
//                .append("fidata_batch_code")
//                .append("`")
//                .append(tabNameWithoutPre)
//                .append("key`)");
                .append(") ");
        //拼接insert into完毕

        //拼接select...
        StringBuilder suffix = new StringBuilder("SELECT ");
        //遍历字段集合
        for (PublishFieldDTO f : fieldList) {
            if ("DATE".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append(".`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else if ("TIME".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else if ("TIMESTAMP".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else if ("DATETIME".equalsIgnoreCase(f.fieldType)) {
                suffix.append(" FROM_UNIXTIME(")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`)")
                        .append(" AS ")
                        .append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");
            } else {
                suffix.append("`")
                        .append(f.fieldEnName)
                        .append("`")
                        .append(",");

            }
        }
        if (!StringUtils.isEmpty(updateSql)) {
            sourceTableName = "(" + updateSql + ") as FNK";
        }
        suffix.append("now() AS fi_createtime,")
                .append("now() AS fi_updatetime")
//                .append("fidata_batch_code")
//                .append("md5(concat(\"\"lishiji))")
                .append(" FROM ")
                .append(sourceTableName)
                .append(" SOURCE ON DUPLICATE KEY UPDATE ");

        for (PublishFieldDTO publishFieldDTO : fieldList) {
            suffix.append(publishFieldDTO.fieldEnName)
                    .append(" = ")
                    .append("SOURCE.")
                    .append(publishFieldDTO.fieldEnName)
                    .append(",")
            ;
        }
        suffix.append("fi_updatetime = NOW();");

        if (StringUtils.isEmpty(updateSql)) {
            suffix.append(" WHERE fidata_batch_code='${fidata_batch_code}' AND fidata_flow_batch_code='${fragment.index}' AND fi_verify_type<>'2'");
        }
        //返回拼接完成的追加覆盖方式拼接的sql
        String sql = prefix + "   " + suffix;
        return String.valueOf(sql);
    }

    /**
     * 业务时间覆盖方式拼接的sql代码
     *
     * @param tableName               真实表名
     * @param sourceTableName         来源表名（临时表名）
     * @param fieldList               前端传递的源表字段属性集合
     * @param previewTableBusinessDTO 业务时间覆盖方式页面选择的逻辑
     */
    @Override
    public String businessTimeOverLay(String tableName, String sourceTableName,
                                      List<PublishFieldDTO> fieldList, PreviewTableBusinessDTO previewTableBusinessDTO, String updateSql) {
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
        String sql = insertAndSelectSql(tableName, sourceTableName, fieldList, updateSql);
        //拼接最终sql
        StringBuilder endSql = startSQL.append(sql)
                .append(tailSql);

        //返回拼接完整的业务时间覆盖的sql
        return String.valueOf(endSql);
    }

    @Override
    public String mergeWithMark(String tableName, String sourceTableName, List<PublishFieldDTO> fieldList, Integer type, String updateSql) {
        return "mysql 标识覆盖sql暂未开发";
    }

}
