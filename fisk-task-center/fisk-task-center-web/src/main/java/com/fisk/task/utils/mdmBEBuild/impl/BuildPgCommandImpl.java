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
    public static final String MARK ="fidata_";

    @Override
    public String buildAttributeLogTable(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE public." + tableName).append("(");
        str.append("id serial NOT NULL,");
        str.append("constraint age primary key(id),");
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
    public String buildStgTable(EntityInfoVO entityInfoVo) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE " + PUBLIC + ".");
        str.append("stg_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId()).append("(");

        // 拼接Stg表基础字段
        str.append(this.splicingStgTable());

        // 字段sql
        String fieldSql = entityInfoVo.getAttributeList().stream()
                .filter(Objects::nonNull)
                .map(e -> {

                    String str1 = null;

                    // 判断是否必填
                    String required = null;
                    if (e.getEnableRequired() == true){
                        required = " NOT NULL ";
                    }else {
                        required = " NULL ";
                    }

                    // 判断数据类型
                    switch (e.getDataType()) {
                        case "文本":
                            str1 = e.getName() + " VARCHAR(" + e.getDataTypeLength() + ")" + required;
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

                    // 判断是否必填
                    String required = null;
                    if (e.getEnableRequired() == true){
                        required = " NOT NULL ";
                    }else {
                        required = " NULL ";
                    }

                    // 判断数据类型
                    switch (e.getDataType()) {
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
                            str1 = name + " numeic(12,2) " + required;
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
        str.append("ALTER TABLE " + PUBLIC + "." + tableName);
        str.append(" alter column " + filedName);
        str.append(" type " + type + " using " + filedName + "::" + type);
        return str.toString();
    }

    @Override
    public String modifyFieldLength(String tableName, String filedName, String type) {
        StringBuilder str = new StringBuilder();
        str.append("ALTER TABLE " + PUBLIC + "." + tableName);
        str.append(" alter column " + filedName);
        str.append(" type " + type);
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

    @Override
    public String addColumn(String tableName, String filedName, String filedType) {
        StringBuilder str = new StringBuilder();
        str.append("ALTER TABLE ");
        str.append(PUBLIC + "." + tableName);
        str.append(" ADD COLUMN " + filedName + " " + filedType);
        return str.toString();
    }

    @Override
    public String notNullable(String tableName, String filedName) {
        StringBuilder str = new StringBuilder();
        str.append("ALTER TABLE ");
        str.append(PUBLIC + "." + tableName);
        str.append(" ALTER " + filedName + "  set not null ");
        return null;
    }

    @Override
    public String nullable(String tableName, String filedName) {
        StringBuilder str = new StringBuilder();
        str.append("ALTER TABLE ");
        str.append(PUBLIC + "." + tableName);
        str.append(" ALTER " + filedName + "  drop not null ");
        return null;
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
        str.append(MARK + "id serial NOT NULL").append(",");
        str.append("constraint age primary key(" + MARK + "id)").append(",");
        str.append(MARK + "import_type int4 NULL").append(",");
        str.append(MARK + "batch_code VARCHAR ( 100 ) NULL").append(",");
        str.append(MARK + "version_id int4 NULL").append(",");
        str.append(MARK + "error_id int4 NULL").append(",");
        str.append(MARK + "new_code VARCHAR ( 100 ) NULL").append(",");
        str.append(MARK + "status int4 NULL").append(",");
        str.append(this.commonBaseField());

        return str.toString();
    }

    /**
     * mdm表基础字段拼接
     * @return
     */
    public String splicingMdmTable(){
        StringBuilder str = new StringBuilder();
        str.append(MARK + "id serial NOT NULL").append(",");
        str.append("constraint age primary key(" + MARK + "id)").append(",");
        str.append(MARK + "version_id int4 NULL").append(",");
        str.append(MARK + "lock_tag int4 NULL").append(",");
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
            str.append(MARK + "id").append(",");
            str.append(MARK + "version_id").append(",");
            str.append(this.commonBaseField());
        }else{
            str.append(PRIMARY_TABLE + "." + MARK + "id").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "version_id").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "create_time timestamp(6) NULL").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "create_user varchar(50) NULL").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "update_time timestamp(6) NULL").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "update_user varchar(50) NULL").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "del_flag int2 NULL").append(",");
        }

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
     * @return
     */
    public String createViw(){
        StringBuilder str = new StringBuilder();
        str.append(MARK + "create_time").append(",");
        str.append(MARK + "create_user").append(",");
        str.append(MARK + "update_time").append(",");
        str.append(MARK + "update_user").append(",");
        str.append(MARK + "del_flag").append(",");
        return str.toString();
    }
}
