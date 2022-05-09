package com.fisk.mdm.utlis;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.dto.DataSourceConDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.fisk.common.service.mdmBEBuild.AbstractDbHelper.execQueryResultList;

/**
 * @Author WangYan
 * @Date 2022/5/6 14:19
 * @Version 1.0
 */
public class DataSynchronizationUtils {

    @Value("${pgsql-mdm.type}")
    DataSourceTypeEnum type;
    @Value("${pgsql-mdm.url}")
    String connectionStr;
    @Value("${pgsql-mdm.username}")
    String acc;
    @Value("${pgsql-mdm.password}")
    String pwd;

    @Resource
    EntityService entityService;

    public static final String MARK ="fidata_";


    /**
     * stg数据同步
     * @param entityId
     * @param batchCode
     */
    public void stgDataSynchronize(Integer entityId,String batchCode){

        // 1.查询属性配置信息
        EntityInfoVO entityInfoVo = entityService.getAttributeById(entityId);
        List<AttributeInfoDTO> attributeList = entityInfoVo.getAttributeList();
        String mdmTableName = entityInfoVo.getTableName();
        String stgTableName = "stg_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId();

        // 2.查询需要同步的数据
        String sql = "SELECT * FROM " + stgTableName + " WHERE fidata_batch_code = '" + batchCode +"'";

        DataSourceConDTO dto = new DataSourceConDTO();
        dto.setConStr(connectionStr);
        dto.setConAccount(acc);
        dto.setConPassword(pwd);
        dto.setConType(type);

        // 连接对象
        AbstractDbHelper dbHelper = new AbstractDbHelper();
        Connection connection = dbHelper.connection(dto.conStr, dto.conAccount,
                dto.conPassword,dto.conType);


        // 3.结果集转换
        List<Map<String, Object>> resultList = execQueryResultList(sql, dto);
        Map<String, List<Object>> listMap = new HashMap<>();

        resultList.stream().filter(Objects::nonNull)
                .forEach(e -> {

                    // key值
                    AtomicReference<String> key = null;
                    List<Object> valueList = new ArrayList<>();
                    e.forEach((k,v) -> {
                        key.set(k);
                        valueList.add(v);
                    });

                    listMap.put(key.toString(),valueList);
                });

        // stg表的主键id转换成mdm表的id
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT fidata_id FROM " + mdmTableName);

        List<Object> objectList = listMap.get("code");
        List<String> codeColumnName = attributeList.stream().filter(e -> e.getName().equals("code"))
                .map(e -> {
                    return e.getColumnName();
                }).collect(Collectors.toList());
        String inParameter = objectList.stream().filter(Objects::nonNull)
                .map(e -> {
                    String str = "'" + e + "'";
                    return str;
                }).collect(Collectors.joining(","));

        stringBuilder.append(" WHERE " + codeColumnName.get(0));
        stringBuilder.append(" IN(" + inParameter + ")");

        // 查询数据
        List<Object> ids = execQueryResultList(stringBuilder.toString(), connection, Object.class);
        listMap.put("fidata_id",ids);

        // 名称转换
        listMap.forEach((k,v) -> {
            attributeList.stream().filter(e -> k.equals(e.getName()))
                    .forEach(e -> {
                        listMap.put(e.getColumnName(),v);
                    });
        });

        // 4.校验数据类型

        // 5.数据导入
        this.dataImport(mdmTableName,dto,attributeList,listMap);
    }

    /**
     * 数据导入
     */
    public void dataImport(String mdmTableName,DataSourceConDTO dto
            ,List<AttributeInfoDTO> attributeList
            ,Map<String, List<Object>> listMap){
        AbstractDbHelper dbHelper = new AbstractDbHelper();
        Connection connection = dbHelper.connection(dto.conStr, dto.conAccount,
                dto.conPassword,dto.conType);

        StringBuilder str = new StringBuilder();
        str.append("INSERT INTO " + mdmTableName);
        str.append("(");
        // 系统字段
        str.append(MARK + "id ").append(",");
        str.append(MARK + "import_type").append(",");
        str.append(MARK + "batch_code").append(",");
        str.append(MARK + "version_id").append(",");
        str.append(MARK + "error_id").append(",");
        str.append(MARK + "new_code").append(",");
        str.append(MARK + "status").append(",");
        // 表基础字段
        str.append(MARK + "create_time timestamp(6) NULL").append(",");
        str.append(MARK + "create_user varchar(50) NULL").append(",");
        str.append(MARK + "update_time timestamp(6) NULL").append(",");
        str.append(MARK + "update_user varchar(50) NULL").append(",");
        str.append(MARK + "del_flag int2 NULL").append(",");

        // 业务字段
        String businessFields = attributeList.stream().filter(e -> e.getStatus().equals(AttributeStatusEnum.SUBMITTED.getName()))
                .map(e -> {
                    StringBuilder str1 = new StringBuilder();
                    str1.append(e.getColumnName());
                    return str1;
                }).collect(Collectors.joining(","));

        // 占位符
        String placeholders = attributeList.stream().filter(e -> e.getStatus().equals(AttributeStatusEnum.SUBMITTED.getName()))
                .map(e -> {
                    StringBuilder str1 = new StringBuilder();
                    str1.append("unnest(?)");
                    return str1;
                }).collect(Collectors.joining(","));

        str.append(businessFields).append(")");
        str.append(" VALUES (" + placeholders + ",unnest(?),unnest(?),unnest(?),unnest(?)" +
                ",unnest(?),unnest(?),unnest(?),unnest(?)" +") ");
        str.append(" ON CONFLICT ( " + MARK +"id) DO UPDATE ");
        str.append(" SET ");
        str.append(MARK + "id = " + "excluded." + MARK + "id").append(",");
        str.append(MARK + "import_type = " +  "excluded." + MARK + "import_type").append(",");
        str.append(MARK + "batch_code = " + "excluded." + MARK + "batch_code").append(",");
        str.append(MARK + "version_id = " + "excluded." + MARK + "version_id").append(",");
        str.append(MARK + "error_id = " + "excluded." + MARK + "error_id").append(",");
        str.append(MARK + "new_code = " + "excluded." + MARK + "new_code").append(",");
        str.append(MARK + "status = " + "excluded." + MARK + "status").append(",");

        // 业务字段
        String collect1 = attributeList.stream().filter(e -> e.getStatus().equals(AttributeStatusEnum.SUBMITTED.getName()))
                .map(e -> {
                    StringBuilder str1 = new StringBuilder();
                    str1.append(e.getColumnName() + " = ");
                    str1.append("excluded." + e.getColumnName());
                    return str1;
                }).collect(Collectors.joining(","));
        str.append(collect1);

        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(str.toString());
            // todo
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
