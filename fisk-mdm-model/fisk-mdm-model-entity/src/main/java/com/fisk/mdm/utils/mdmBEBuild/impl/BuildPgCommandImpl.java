package com.fisk.mdm.utils.mdmBEBuild.impl;

import com.fisk.mdm.dto.attribute.AttributeFactDTO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.utils.mdmBEBuild.IBuildSqlCommand;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.fisk.mdm.enums.AttributeStatusEnum.*;

/**
 * @author WangYan
 * @date 2022/4/13 18:06
 */
@Component
public class BuildPgCommandImpl implements IBuildSqlCommand {

    public static final String PUBLIC = "PUBLIC";
    public static final String PRIMARY_TABLE = "a";
    public static final String MARK ="fidata_";

    @Override
    public String buildAttributeLogTable(String tableName) {
        int pk = (int)(Math.random()*8999)+1000+1;

        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE public.\"" + tableName+"\"").append("(");
        str.append("id serial NOT NULL,");
        str.append("constraint pk_" + tableName + "_id_" + pk + " primary key(id),");
        str.append("model_id int4 NULL,");
        str.append("entity_id int4 NULL,");
        str.append("attribute_id int4 NULL,");
        str.append("member_id int4 NULL,");
        str.append("batch_code VARCHAR ( 100 ) NULL").append(",");
        str.append("version_id int4 NULL,");
        str.append("old_code VARCHAR ( 200 ) NULL,");
        str.append("old_value VARCHAR ( 200 ) NULL,");
        str.append("new_code VARCHAR ( 200 ) NULL,");
        str.append("new_value VARCHAR ( 200 ) NULL,");
        str.append("create_time timestamp(6) NULL").append(",");
        str.append("create_user varchar(50) NULL").append(",");
        str.append("del_flag int2 NULL");
        str.append(");");
        return str.toString();
    }

    @Override
    public String buildStgTable(EntityInfoVO entityInfoVo,String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE " + PUBLIC + ".");
        str.append("\""+tableName+"\"").append("(");

        // 拼接Stg表基础字段
        str.append(this.splicingStgTable(tableName));

        // 字段sql
        String fieldSql = entityInfoVo.getAttributeList().stream()
                .filter(e -> !e.getStatus().equals("删除待发布"))
                .map(e -> {

                    String str1 = null;

                    // 判断是否必填
                    String required = " NULL ";

                    // 判断数据类型
                    switch (e.getDataType()) {
                        case "文本":
                            str1 = e.getName() + " VARCHAR(" + e.getDataTypeLength() + ")" + required;
                            break;
                        default:
                            str1 = e.getName() + " VARCHAR( 200 )" + required;
                            break;
                    }

                    return str1;
                }).collect(Collectors.joining(","));

        if (StringUtils.isNotBlank(fieldSql)){
            str.append(fieldSql);
        }else {
            str.deleteCharAt(str.length()-1);
        }

        str.append(");");
        return str.toString();
    }

    @Override
    public String buildLogTable(EntityInfoVO entityInfoVo, String tableName,String code) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE " + PUBLIC + ".");
        str.append("\""+tableName+"\"").append("(");

        // 拼接日志表基础字段
        str.append(this.splicingLogTable(tableName));

        // 字段sql
        String fieldSql = entityInfoVo.getAttributeList().stream().filter(e -> e.getStatus().equals(INSERT.getName()))
                .map(e -> {

                    String str1 = null;

                    // 判断是否必填
                    String required = null;
                    if (e.getEnableRequired() == true){
                        required = " NOT NULL ";
                    }else {
                        required = " NULL ";
                    }

                    String name = e.getName();

                    // 判断数据类型
                    switch (e.getDataType()) {
                        case "文件":
                        case "经纬度坐标":
                            str1 = name + " VARCHAR(" + "50" + ")" + required;
                            break;
                        case "数值":
                        case "域字段":
                            str1 = name + " int4 " + required;
                            break;
                        case "时间":
                            str1 = name + " TIME " + required;
                            break;
                        case "日期":
                            str1 = name + " date " + required;
                            break;
                        case "日期时间":
                            str1 = name + " timestamp " + required;
                            break;
                        case "浮点型":
                            str1 = name + " numeric(12,2) " + required;
                            break;
                        case "布尔型":
                            str1 = name + " bool " + required;
                            break;
                        case "货币":
                            str1 = name + " money " + required;
                            break;
                        case "文本":
                        default:
                            str1 = name + " VARCHAR(" + e.getDataTypeLength() + ")" + required;
                            break;
                    }

                    return str1;
                }).collect(Collectors.joining(","));

        if (StringUtils.isNotBlank(fieldSql)){
            str.append(fieldSql);
        }else {
            str.deleteCharAt(str.length()-1);
        }

        str.append(");");
        return str.toString();
    }

    @Override
    public String buildMdmTable(EntityInfoVO entityInfoVo,String tableName,String code) {

        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE " + PUBLIC + ".");
        str.append("\""+tableName+"\"").append("(");

        // 拼接mdm表基础字段拼接
        str.append(this.splicingMdmTable(tableName,code));

        // 字段sql
        String fieldSql = entityInfoVo.getAttributeList().stream().filter(e -> e.getStatus().equals(INSERT.getName()))
                .map(e -> {

                    String str1 = null;
                    String name = "column_" + e.getName();

                    // 判断是否必填
                    String required = null;
                    if (e.getEnableRequired() == true){
                        required = " NOT NULL ";
                    }else {
                        required = " NULL ";
                    }

                    // 判断数据类型
                    switch (e.getDataType()) {
                        case "文件":
                        case "经纬度坐标":
                            str1 = name + " VARCHAR(" + "50" + ")" + required;
                            break;
                        case "数值":
                        case "域字段":
                            str1 = name + " int4 " + required;
                            break;
                        case "时间":
                            str1 = name + " TIME " + required;
                            break;
                        case "日期":
                            str1 = name + " date " + required;
                            break;
                        case "日期时间":
                            str1 = name + " timestamp " + required;
                            break;
                        case "浮点型":
                            str1 = name + " numeric(" + e.getDataTypeLength() + "," + e.getDataTypeDecimalLength() + ")";
                            break;
                        case "布尔型":
                            str1 = name + " bool " + required;
                            break;
                        case "货币":
                            str1 = name + " money " + required;
                            break;
                        case "文本域":
                            str1=  name + " text " + required;
                        case "文本":
                        default:
                            str1 = name + " VARCHAR(" + e.getDataTypeLength() + ")" + required;
                            break;
                    }

                    return str1;
                }).collect(Collectors.joining(","));

        if (StringUtils.isNotBlank(fieldSql)){
            str.append(fieldSql);
        }else {
            str.deleteCharAt(str.length()-1);
        }

        str.append(");");
        return str.toString();
    }

    @Override
    public String modifyFieldType(String tableName, String filedName, String type) {
        StringBuilder str = new StringBuilder();
        str.append("ALTER TABLE " + PUBLIC + ".\"" + tableName+"\"");
        str.append(" alter column \"" + filedName +"\"");
        str.append(" type " + type + " using " + filedName + "::" + type);
        return str.toString();
    }

    @Override
    public String modifyFieldLength(String tableName, String filedName, String type) {
        StringBuilder str = new StringBuilder();
        str.append("ALTER TABLE " + PUBLIC + ".\"" + tableName+"\"");
        str.append(" alter column \"" + filedName + "\"");
        str.append(" type " + type);
        return str.toString();
    }

    @Override
    public String dropTable(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("DROP TABLE ").append(PUBLIC + ".").append("\""+tableName+"\"");
        return str.toString();
    }

    @Override
    public String dropViw(String viwName) {
        StringBuilder str = new StringBuilder();
        str.append("DROP VIEW ").append(PUBLIC + ".").append("\""+viwName+"\"");
        return str.toString();
    }

    @Override
    public String addColumn(String tableName, String filedName, String filedType) {
        StringBuilder str = new StringBuilder();
        str.append("ALTER TABLE ");
        str.append(PUBLIC + ".\"" + tableName+"\"");
        str.append(" ADD COLUMN \"" + filedName + "\" " + filedType);
        return str.toString();
    }

    @Override
    public String notNullable(String tableName, String filedName) {
        StringBuilder str = new StringBuilder();
        str.append("ALTER TABLE ");
        str.append(PUBLIC + ".\"" + tableName+"\"");
        str.append(" ALTER \"" + filedName + "\"  set not null ");
        return str.toString();
    }

    @Override
    public String nullable(String tableName, String filedName) {
        StringBuilder str = new StringBuilder();
        str.append("ALTER TABLE ");
        str.append(PUBLIC + ".\"" + tableName+"\"");
        str.append(" ALTER \"" + filedName + "\"  drop not null ");
        return str.toString();
    }

    @Override
    public String deleteFiled(String tableName, String filedName) {
        StringBuilder str = new StringBuilder();
        str.append("ALTER TABLE ");
        str.append(PUBLIC + ".\"" + tableName+"\"");
        str.append(" drop column if exists ").append("\""+filedName+"\"");
        return str.toString();
    }

    @Override
    public String queryData(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT * FROM " + PUBLIC + ".\"" + tableName + "\" LIMIT 1 ");
        return str.toString();
    }

    @Override
    public String insertAttributeFact(List<AttributeFactDTO> dtoList) {
        StringBuilder str = new StringBuilder();
        str.append("INSERT INTO ").append(" tb_fact_attribute(");
        str.append("\"name\", data_type, data_type_length, data_type_decimal_length, enable_required, attribute_id");
        str.append(")VALUES");

        String value = dtoList.stream().map(e -> {
            StringBuilder str1 = new StringBuilder();
            str1.append("('" + e.getName() + "'").append(",");
            str1.append(e.getDataType()).append(",");
            str1.append(e.getDataTypeLength()).append(",");
            str1.append(e.getDataTypeDecimalLength()).append(",");
            str1.append(e.getEnableRequired()).append(",");
            str1.append(e.getAttribute_id()).append(")");
            return str1;
        }).collect(Collectors.joining(","));

        if (StringUtils.isNotBlank(value)){
            str.append(value);
        }

        return str.toString();
    }

    @Override
    public String deleteDataByAttributeId(String tableName, String deleteFiled, List<Integer> attributeIds) {
        String attributeId = attributeIds.stream().map(e -> "'" + e.toString() + "'").collect(Collectors.joining(","));
        if (StringUtils.isBlank(attributeId)){
            return null;
        }

        StringBuilder str = new StringBuilder();
        str.append("DELETE FROM ").append("\""+tableName+"\"");
        str.append(" WHERE ");
        str.append(deleteFiled + " IN(");
        str.append(attributeId).append(")");
        return str.toString();
    }

    /**
     * stg表基础字段拼接
     * @return
     */
    public String splicingStgTable(String tableName){
        int pk = (int)(Math.random()*8999)+1000+1;

        StringBuilder str = new StringBuilder();
        str.append(MARK + "id serial NOT NULL").append(",");
        str.append("constraint pk_" +tableName + "_id_" + pk + " primary key(" + MARK + "id)").append(",");
        str.append(MARK + "import_type int4 NULL").append(",");
        str.append(MARK + "batch_code VARCHAR ( 100 ) NULL").append(",");
        str.append(MARK + "version_id int4 NULL").append(",");
        str.append(MARK + "error_msg VARCHAR ( 1000 ) NULL").append(",");
        str.append(MARK + "new_code VARCHAR ( 100 ) NULL").append(",");
        str.append(MARK + "status int4 NULL").append(",");
        str.append(MARK + "syncy_type int4 NULL").append(",");
        str.append(this.commonBaseField());

        return str.toString();
    }

    /**
     * mdm表基础字段拼接
     * @return
     */
    public String splicingMdmTable(String tableName,String code){
        int pk = (int)(Math.random()*8999)+1000+1;

        StringBuilder str = new StringBuilder();
        str.append(MARK + "id serial NOT NULL").append(",");
        str.append("constraint pk_"+ tableName + "_id_" + pk +" primary key(" + MARK + "id)").append(",");
        str.append(MARK + "version_id int4 NULL").append(",");
        str.append(MARK + "lock_tag int4 NULL").append(",");
        str.append(MARK + "new_code varchar(100) NULL").append(",");
        str.append("constraint pk_"+ tableName + "_code_" + pk +" unique(" + code + "," + MARK + "version_id" +")").append(",");
        str.append(this.commonBaseField());

        return str.toString();
    }

    /**
     * 日志表基础字段
     * @param tableName
     * @return
     */
    public String splicingLogTable(String tableName){
        int pk = (int)(Math.random()*8999)+1000+1;

        StringBuilder str = new StringBuilder();
        str.append(MARK + "id serial NOT NULL").append(",");
        str.append("constraint pk_"+ tableName + "_id_" + pk +" primary key(" + MARK + "id)").append(",");
        str.append(MARK + "version_id int4 NULL").append(",");
        str.append(MARK + "lock_tag int4 NULL").append(",");
        str.append(MARK + "new_code varchar(100) NULL").append(",");
        str.append(this.commonLogBaseField());
        str.append(MARK + "old_name varchar(100) NULL").append(",");

        return str.toString();
    }

    /**
     * View 视图表基础字段拼接
     * @return
     */
    public String splicingViewTable(boolean isDomain){
        StringBuilder str = new StringBuilder();
        if (isDomain == false){
            str.append(MARK + "id").append(",");
            str.append(MARK + "version_id").append(",");
            str.append(this.commonBaseField());
        }else{
            str.append(PRIMARY_TABLE + "." + MARK + "create_time").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "create_user").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "update_time").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "update_user").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "del_flag");
        }

        return str.toString();
    }

    /**
     * 成员日志公用基础字段
     * @return
     */
    public String commonLogBaseField(){
        StringBuilder str = new StringBuilder();
        str.append(MARK + "create_time timestamp(6) NULL").append(",");
        str.append(MARK + "create_user varchar(50) NULL").append(",");
        str.append(MARK + "del_flag int2 NULL").append(",");
        str.append(MARK + "mdm_fidata_id int2 NULL").append(",");
        return str.toString();
    }

    /**
     * 公用基础字段
     * @return
     */
    public String commonBaseField(){
        StringBuilder str = new StringBuilder();
        str.append(MARK + "create_time timestamp(6) NULL").append(",");
        str.append(MARK + "create_user varchar(50) NULL").append(",");
        str.append(MARK + "update_time timestamp(6) NULL").append(",");
        str.append(MARK + "update_user varchar(50) NULL").append(",");
        str.append(MARK + "del_flag int2 NULL").append(",");
        return str.toString();
    }

    /**
     * 创建视图基础字段
     * @param isDomain
     * @return
     */
    public String createViw(boolean isDomain){
        StringBuilder str = new StringBuilder();
        if (isDomain == true){
            str.append(PRIMARY_TABLE + "." + MARK + "create_time").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "create_user").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "update_time").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "update_user").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "del_flag").append(",");
        }else {
            str.append(MARK + "create_time").append(",");
            str.append(MARK + "create_user").append(",");
            str.append(MARK + "update_time").append(",");
            str.append(MARK + "update_user").append(",");
            str.append(MARK + "del_flag").append(",");
        }

        return str.toString();
    }
}
