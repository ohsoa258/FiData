package com.fisk.task.utils.mdmBEBuild.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.mdm.client.MdmClient;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.task.utils.mdmBEBuild.IBuildSqlCommand;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fisk.mdm.enums.AttributeStatusEnum.*;

/**
 * @author WangYan
 * @date 2022/4/13 18:06
 */
@Component
public class BuildPgCommandImpl implements IBuildSqlCommand {

    @Resource
    MdmClient mdmClient;

    public static final String PUBLIC = "PUBLIC";
    public static final String PRIMARY_TABLE = "a";

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
        str.append("new_value VARCHAR ( 200 ) NULL,");
        str.append(this.commonBaseField());
        str.deleteCharAt(str.length()-1);
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
        String fieldSql = entityInfoVo.getAttributeList().stream()
                .filter(e -> e.getStatus().equals(INSERT.getName()) || e.getStatus().equals(UPDATE.getName()))
                .map(e -> {

                    String str1 = null;

                    // 判断数据类型
                    switch (e.getDataType()) {
                        case "文本":
                            str1 = e.getName() + " VARCHAR(" + e.getDataTypeLength() + ")" + "NULL";
                            break;
                        default:
                            str1 = e.getName() + " VARCHAR( 200 )" + "NULL";
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
    public String buildMdmTable(EntityInfoVO entityInfoVo) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE " + PUBLIC + ".");
        str.append("mdm_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId()).append("(");

        // 拼接mdm表基础字段拼接
        str.append(this.splicingMdmTable());

        // 字段sql
        String fieldSql = entityInfoVo.getAttributeList().stream().filter(e -> e.getStatus().equals(INSERT.getName()))
                .map(e -> {

                    String str1 = null;
                    String name = "column_" + entityInfoVo.getId() + "_" + e.getId();
                    // 判断数据类型
                    switch (e.getDataType()) {
                        case "数值":
                        case "域字段":
                            str1 = name + " int4 " + "NULL";
                            break;
                        case "时间":
                            str1 = name + " date " + "NULL";
                            break;
                        case "浮点型":
                            str1 = name + " float4 " + "NULL";
                            break;
                        case "文本":
                        default:
                            str1 = name + " VARCHAR(" + e.getDataTypeLength() + ")" + "NULL";
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
    public String buildViewTable(EntityInfoVO entityInfoVo) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE VIEW " + PUBLIC + ".");
        str.append("viw_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId());
        str.append(" AS ").append("SELECT ");

        List<Integer> ids = entityInfoVo.getAttributeList().stream().filter(e -> e.getId() != null).map(e -> e.getId()).collect(Collectors.toList());
        List<AttributeInfoDTO> attributeList = mdmClient.getByIds(ids).getData();
        // 存在外键数据
        List<AttributeInfoDTO> foreignList = attributeList.stream().filter(e -> e.getDomainId() != null).collect(Collectors.toList());
        // 不存在外键数据
        List<AttributeInfoDTO> noForeignList = attributeList.stream().filter(e -> e.getDomainId() == null).collect(Collectors.toList());

        // 先去判断属性有没有外键
        if (CollectionUtils.isEmpty(foreignList)){
            // 不存在外键
            str.append(this.noDomainSplicing(noForeignList));
        }else {
            // 存在外键
            str.append(this.domainSplicing(foreignList,noForeignList));
        }

        return str.toString();
    }

    @Override
    public String modifyFieldType(String tableName, String filedName, String type) {
        StringBuilder str = new StringBuilder();
        str.append("alter table ").append(tableName).append(" alter column ").append(filedName);
        str.append(" type ").append(type).append(" using ").append(filedName).append("::").append(type);
        return str.toString();
    }

    @Override
    public String modifyFieldLength(String tableName, String filedName, String type) {
        StringBuilder str = new StringBuilder();
        str.append("alter table ").append(tableName).append(" alter column ").append(filedName).append(" type ").append(type);
        return str.toString();
    }

    @Override
    public String dropTable(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("DROP TABLE ").append(PUBLIC + ".").append(tableName);
        return str.toString();
    }

    @Override
    public String dropViw(String viwName) {
        StringBuilder str = new StringBuilder();
        str.append("DROP VIEW ").append(PUBLIC + ".").append(viwName);
        return str.toString();
    }

    /**
     * 存在域字段
     * @param foreignList
     * @param noForeignList
     */
    public String domainSplicing(List<AttributeInfoDTO> foreignList,List<AttributeInfoDTO> noForeignList){
        StringBuilder str = new StringBuilder();

        // 不存在域字段的属性
        String noForeign = noForeignList.stream().filter(e -> e.getDomainId() == null).map(e -> {
            String str1 = PRIMARY_TABLE + "." + e.getColumnName() + " AS " + e.getName();
            return str1;
        }).collect(Collectors.joining(","));

        // 存在域字段的属性
        List<Integer> domainIds = foreignList.stream().filter(e -> e.getDomainId() != null).map(e -> e.getDomainId() + 1).collect(Collectors.toList());
        List<AttributeInfoDTO> list = mdmClient.getByIds(domainIds).getData();

        AtomicInteger amount = new AtomicInteger(1);
        String foreign = list.stream().filter(e -> e.getName() != null).map(e -> {
            String str1 = PRIMARY_TABLE + amount.incrementAndGet() + "." + e.getColumnName() + " AS " + e.getName();
            return str1;
        }).collect(Collectors.joining(","));

        // 获取主表表名
        AttributeInfoDTO dto = noForeignList.get(1);
        str.append(this.splicingViewTable(true));
        str.append(noForeign);
        str.append(foreign);
        // 主表表名
        str.append("mdm_" + dto.getModelId() + "_" + dto.getEntityId() + PRIMARY_TABLE);

        AtomicInteger amount1 = new AtomicInteger(1);
        String leftJoin = foreignList.stream().filter(Objects::nonNull)
                .map(e -> {
                    String tableName = "mdm_" + e.getModelId() + "_" + e.getEntityId();
                    String alias = PRIMARY_TABLE + amount1.incrementAndGet() + ".";
                    String on = " ON " + PRIMARY_TABLE + "." + "version_id" + "=" + alias + "version_id" +
                            " AND " + PRIMARY_TABLE + "." + e.getColumnName() + "=" + alias + ".id";
                    String str1 = tableName + alias + on;
                    return str1;
                }).collect(Collectors.joining(" LEFT JOIN "));

        str.append(" LEFT JOIN " + leftJoin);
        return str.toString();
    }

    /**
     * 不存在域字段拼接
     * @param noForeignList
     * @return
     */
    public String noDomainSplicing(List<AttributeInfoDTO> noForeignList){
        StringBuilder str = new StringBuilder();
        // 视图基础字段
        str.append(this.splicingViewTable(false));

        String collect = noForeignList.stream().filter(Objects::nonNull).map(e -> {
            String str1 = e.getColumnName() + " AS " + e.getName();
            return str1;
        }).collect(Collectors.joining(","));

        AttributeInfoDTO infoDto = noForeignList.get(1);
        // 业务字段
        str.append(collect);
        str.append(" FROM " + "mdm_" + infoDto.getModelId() + "_" + infoDto.getEntityId());
        return str.toString();
    }

    /**
     * stg表基础字段拼接
     * @return
     */
    public String splicingStgTable(){
        StringBuilder str = new StringBuilder();
        str.append("fidata_id serial NOT NULL").append(",");
        str.append("fidata_import_type int4 NULL").append(",");
        str.append("fidata_batch_code VARCHAR ( 100 ) NULL").append(",");
        str.append("fidata_version_id int4 NULL").append(",");
        str.append("fidata_error_id int4 NULL").append(",");
        str.append("fidata_new_code VARCHAR ( 100 ) NULL").append(",");
        str.append("fidata_status int4 NULL").append(",");
        str.append(this.commonBaseField());

        return str.toString();
    }

    /**
     * mdm表基础字段拼接
     * @return
     */
    public String splicingMdmTable(){
        StringBuilder str = new StringBuilder();
        str.append("fidata_id serial NOT NULL").append(",");
        str.append("fidata_version_id int4 NULL").append(",");
        str.append("fidata_lock_tag int4 NULL").append(",");
        str.append(this.commonBaseField());

        return str.toString();
    }

    /**
     * View 视图表基础字段拼接
     * @return
     */
    public String splicingViewTable(boolean isDomain){
        StringBuilder str = new StringBuilder();
        if (isDomain == false){
            str.append("fidata_id").append(",");
            str.append("fidata_version_id").append(",");
            str.append(this.commonBaseField());
        }else{
            str.append(PRIMARY_TABLE + "." + "fidata_id").append(",");
            str.append(PRIMARY_TABLE + "." + "fidata_version_id").append(",");
            str.append(PRIMARY_TABLE + "." + "fidata_create_time timestamp(6) NULL").append(",");
            str.append(PRIMARY_TABLE + "." + "fidata_create_user varchar(50) NULL").append(",");
            str.append(PRIMARY_TABLE + "." + "fidata_update_time timestamp(6) NULL").append(",");
            str.append(PRIMARY_TABLE + "." + "fidata_update_user varchar(50) NULL").append(",");
            str.append(PRIMARY_TABLE + "." + "fidata_del_flag int2 NULL").append(",");
        }

        return str.toString();
    }

    /**
     * 公用基础字段
     * @return
     */
    public String commonBaseField(){
        StringBuilder str = new StringBuilder();
        str.append("fidata_create_time timestamp(6) NULL").append(",");
        str.append("fidata_create_user varchar(50) NULL").append(",");
        str.append("fidata_update_time timestamp(6) NULL").append(",");
        str.append("fidata_update_user varchar(50) NULL").append(",");
        str.append("fidata_del_flag int2 NULL").append(",");
        return str.toString();
    }
}
