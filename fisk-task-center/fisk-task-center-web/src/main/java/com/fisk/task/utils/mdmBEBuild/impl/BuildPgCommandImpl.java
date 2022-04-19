package com.fisk.task.utils.mdmBEBuild.impl;

import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.task.utils.mdmBEBuild.IBuildSqlCommand;

import java.util.stream.Collectors;

import static com.fisk.mdm.enums.AttributeStatusEnum.INSERT;

/**
 * @author WangYan
 * @date 2022/4/13 18:06
 */
public class BuildPgCommandImpl implements IBuildSqlCommand {

    public static final String PUBLIC = "PUBLIC";

    @Override
    public String buildAttributeLogTable(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE public." + tableName).append("(");
        str.append("ID int4 NOT NULL,");
        str.append("model_id int4 NULL,");
        str.append("entity_id int4 NULL,");
        str.append("attribute_id int4 NULL,");
        str.append("member_id int4 NULL,");
        str.append("batch_id int4 NULL,");
        str.append("version_id int4 NULL,");
        str.append("old_code VARCHAR ( 200 ) NULL,");
        str.append("old_value VARCHAR ( 200 ) NULL,");
        str.append("new_code VARCHAR ( 200 ) NULL,");
        str.append("new_value VARCHAR ( 200 ) NULL");
        str.append(");");
        return str.toString();
    }

    @Override
    public String buildStgTable(EntityVO entityVo) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE " + PUBLIC + ".");
        str.append("tb_" + "stg_" + entityVo.getModelId() + "_" + entityVo.getId()).append("(");

        // 拼接Stg表基础字段
        str.append(this.splicingStgTable());

        // 字段sql
        String fieldSql = entityVo.getAttributeList().stream().filter(e -> e.getStatus().equals(INSERT))
                .map(e -> {

                    // 判断数据类型
                    switch (e.getDataType()) {
                        case TEXT:
                            str.append(e.getName() + " VARCHAR(" + e.getDataTypeLength() + ")" + "NULL");
                            break;
                        default:
                            str.append(e.getName() + " VARCHAR( 200 )" + "NULL");
                            break;
                    }

                    return str;
                }).collect(Collectors.joining(","));

        str.append(fieldSql);
        str.append(");");
        return str.toString();
    }

    @Override
    public String buildMdmTable(EntityVO entityVo) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE " + PUBLIC + ".");
        str.append("tb_" + "mdm_" + entityVo.getModelId() + "_" + entityVo.getId()).append("(");

        // 拼接mdm表基础字段拼接
        str.append(this.splicingMdmTable());

        // 字段sql
        String fieldSql = entityVo.getAttributeList().stream().filter(e -> e.getStatus().equals(INSERT))
                .map(e -> {

                    String name = "column_" + entityVo.getId() + "_" + e.getId();
                    // 判断数据类型
                    switch (e.getDataType()) {
                        case NUMERICAL:
                        case DOMAIN:
                            str.append(name + " int4 " + "NULL");
                        case DATE:
                            str.append(name + " date " + "NULL");
                        case FLOAT:
                            str.append(name + " float4 " + "NULL");
                        case TEXT:
                        default:
                            str.append(name + " VARCHAR(" + e.getDataTypeLength() + ")" + "NULL");
                    }

                    return str;
                }).collect(Collectors.joining(","));

        str.append(fieldSql);
        str.append(");");
        return str.toString();
    }

    @Override
    public String buildViewTable(EntityVO entityVo) {
        StringBuilder str = new StringBuilder();
        String vSTableName = "tb_" + "mdm_" + entityVo.getModelId() + "_" + entityVo.getId();
        str.append("CREATE VIEW " + PUBLIC + ".");
        str.append("tb_" + "viw_" + entityVo.getModelId() + "_" + entityVo.getId());
        str.append(" AS (").append("SELECT ");

        // 拼接mdm表基础字段拼接
        str.append(this.splicingViewTable());

        // 字段Sql
        String collect = entityVo.getAttributeList().stream().filter(e -> e.getDomainId() == null)
                .map(e -> {

                    String name = e.getColumnName() + " AS " + e.getName();
                    return name;
                }).collect(Collectors.joining(","));

        str.append(collect);
        str.append(" FROM " + vSTableName);
        str.append(")");
        return null;
    }

    /**
     * stg表基础字段拼接
     * @return
     */
    public String splicingStgTable(){
        StringBuilder str = new StringBuilder();
        str.append("id int4 NOT NULL").append(",");
        str.append("import_type int4 NULL").append(",");
        str.append("batch_id int4 NULL").append(",");
        str.append("version_id int4 NULL").append(",");
        str.append("error_id int4 NULL").append(",");
        str.append("new_code VARCHAR ( 100 ) NULL").append(",");
        return str.toString();
    }

    /**
     * mdm表基础字段拼接
     * @return
     */
    public String splicingMdmTable(){
        StringBuilder str = new StringBuilder();
        str.append("id int4 NOT NULL").append(",");
        str.append("version_id int4 NULL").append(",");
        str.append("lock_tag int4 NULL").append(",");
        return str.toString();
    }

    /**
     * View 视图表基础字段拼接
     * @return
     */
    public String splicingViewTable(){
        StringBuilder str = new StringBuilder();
        str.append("id").append(",");
        str.append("version_id").append(",");
        return str.toString();
    }
}
