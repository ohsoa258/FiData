package com.fisk.task.utils.mdmBEBuild.impl;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.mdm.client.MdmClient;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.task.utils.mdmBEBuild.IBuildSqlCommand;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fisk.mdm.enums.AttributeStatusEnum.INSERT;

/**
 * @author WangYan
 * @date 2022/4/13 18:06
 */
public class BuildPgCommandImpl implements IBuildSqlCommand {

    @Resource
    MdmClient mdmClient;

    public static final String PUBLIC = "PUBLIC";
    public static final String PRIMARY_TABLE = "a";
    public static final String ACCESSORY_TABLE = "b";

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
    public String buildStgTable(EntityInfoVO entityInfoVo) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE " + PUBLIC + ".");
        str.append("stg_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId()).append("(");

        // 拼接Stg表基础字段
        str.append(this.splicingStgTable());

        // 字段sql
        String fieldSql = entityInfoVo.getAttributeList().stream().filter(e -> e.getStatus().equals(INSERT))
                .map(e -> {

                    // 判断数据类型
                    switch (e.getDataType()) {
                        case "文本":
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
    public String buildMdmTable(EntityInfoVO entityInfoVo) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE " + PUBLIC + ".");
        str.append("mdm_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId()).append("(");

        // 拼接mdm表基础字段拼接
        str.append(this.splicingMdmTable());

        // 字段sql
        String fieldSql = entityInfoVo.getAttributeList().stream().filter(e -> e.getStatus().equals(INSERT))
                .map(e -> {

                    String name = "column_" + entityInfoVo.getId() + "_" + e.getId();
                    // 判断数据类型
                    switch (e.getDataType()) {
                        case "数值":
                        case "域字段":
                            str.append(name + " int4 " + "NULL");
                        case "时间":
                            str.append(name + " date " + "NULL");
                        case "浮点型":
                            str.append(name + " float4 " + "NULL");
                        case "文本":
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
    public String buildViewTable(EntityInfoVO entityInfoVo) {
        StringBuilder str = new StringBuilder();
        String vSTableName = "mdm_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId();
        str.append("CREATE VIEW " + PUBLIC + ".");
        str.append("viw_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId());
        str.append(" AS (").append("SELECT ");

        List<AttributeInfoDTO> attributeList = entityInfoVo.getAttributeList();
        // 属性外键数据
        List<AttributeVO> foreignList = attributeList.stream().filter(e -> e.getDomainId() != null).map(e -> {
            ResultEntity<AttributeVO> result = mdmClient.get(e.getDomainId());
            return result.data;
        }).collect(Collectors.toList());

        // 属性外键: select b.column_6_9 AS Class
        String foreignFiled = attributeList.stream().filter(e -> e.getDomainId() != null)
                .map(e -> {
                    ResultEntity<AttributeVO> result = mdmClient.get(e.getDomainId());
                    if (result != null) {
                        AttributeVO data = result.getData();
                        String fieldName = ACCESSORY_TABLE + "." + "column_" + data.getEntityId() + "_"
                                + data.getId()+1 + " AS " + e.getName();
                        return fieldName;
                    }

                    return null;
                }).collect(Collectors.joining(","));

        if (StringUtils.isBlank(foreignFiled)){
            // 没有关联关系
            // 拼接mdm表基础字段拼接
            str.append(this.splicingViewTable(foreignFiled));

            // 字段Sql
            String collect = entityInfoVo.getAttributeList().stream().filter(e -> e.getDomainId() == null)
                    .map(e -> {

                        String name = e.getColumnName() + " AS " + e.getName();
                        return name;
                    }).collect(Collectors.joining(","));

            str.append(collect);
        }else{
            // 存在关联关系
            // 拼接mdm表基础字段拼接
            str.append(this.splicingViewTable(foreignFiled));

            // 没有外键的字段Sql
            String collect = entityInfoVo.getAttributeList().stream().filter(e -> e.getDomainId() == null)
                    .map(e -> {

                        String name = PRIMARY_TABLE + "." + e.getColumnName() + " AS " + e.getName();
                        return name;
                    }).collect(Collectors.joining(","));

            str.append(collect);
            str.append(foreignFiled);
        }


        str.append(" FROM " + vSTableName);

        if (StringUtils.isNotBlank(foreignFiled)){
            // 存在外键关系,LEFT JOIN 从表
            String leftJoin = this.appendLeftJoin(foreignFiled, foreignList);
            str.append(PRIMARY_TABLE + leftJoin);
        }
        str.append(")");
        return str.toString();
    }

    /**
     * 追加Left join
     * @param foreignFiled
     * @param foreignList
     * @return
     */
    public String appendLeftJoin(String foreignFiled,List<AttributeVO> foreignList){
        if (StringUtils.isNotBlank(foreignFiled)){
            String collect = foreignList.stream().filter(Objects::nonNull)
                    .map(e -> {
                        StringBuilder str = new StringBuilder();
                        str.append(" LEFT JOIN ");
                        str.append("mdm_" + e.getModelId() + e.getEntityId() + ACCESSORY_TABLE);
                        str.append(" ON " + PRIMARY_TABLE + ".version_id=").append(ACCESSORY_TABLE + ".version_id");
                        str.append(" AND " + PRIMARY_TABLE + "." + e.getColumnName()).append(ACCESSORY_TABLE + ".id");

                        return str.toString();
                    }).collect(Collectors.joining(" LEFT JOIN "));
            return collect;
        }

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
    public String splicingViewTable(String foreignFiled){
        StringBuilder str = new StringBuilder();
        if (StringUtils.isBlank(foreignFiled)){
            str.append("id").append(",");
            str.append("version_id").append(",");
        }else{
            str.append(PRIMARY_TABLE + "." + "id").append(",");
            str.append(PRIMARY_TABLE + "." + "version_id").append(",");
        }

        return str.toString();
    }
}
