package com.fisk.mdm.utlis;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.dto.DataSourceConDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.stgbatch.MdmDTO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.enums.SyncStatusTypeEnum;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.fisk.common.service.mdmBEBuild.AbstractDbHelper.execQueryResultList;
import static com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils.generateMdmTableName;
import static com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils.generateStgTableName;

/**
 * @Author WangYan
 * @Date 2022/5/6 14:19
 * @Version 1.0
 */
@Slf4j
@Component
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
    @Resource
    AttributeService attributeService;

    public static final String MARK ="fidata_";
    public static final String SPECIAL_CHARACTERS_NULL = "`fidata_null`";

    /**
     * stg数据同步
     * @param entityId
     * @param batchCode
     */
    public ResultEnum stgDataSynchronize(Integer entityId,String batchCode){

        // 1.查询属性配置信息
        EntityInfoVO entityInfoVo = entityService.getAttributeById(entityId);
        List<AttributeInfoDTO> attributeList = entityInfoVo.getAttributeList();
        String mdmTableName = entityInfoVo.getTableName();

        // 获取stg表名
        String stgTableName = generateStgTableName(entityInfoVo.getModelId(), entityInfoVo.getId());

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

        // stg表的主键id转换成mdm表的id
        StringBuilder stringBuilder = new StringBuilder();

        List<String> codeColumnName = attributeList.stream().filter(e -> e.getName().equals("code"))
                .map(e -> {
                    return e.getColumnName();
                }).collect(Collectors.toList());

        String columnName = attributeList.stream().filter(Objects::nonNull)
                .map(e -> {
                    return e.getColumnName() + " AS " + e.getName();
                }).collect(Collectors.joining(","));

        String codes = resultList.stream().filter(Objects::nonNull).map(e -> {
            Object code = e.get("code");
            String code1 = "'" + code + "'";
            return code1;
        }).collect(Collectors.joining(","));

        stringBuilder.append("SELECT fidata_id, " + columnName  + "  FROM " + mdmTableName);
        stringBuilder.append(" WHERE fidata_del_flag = 1 AND " + codeColumnName.get(0));
        stringBuilder.append(" IN(" + codes + ")");

        // 查询数据
        List<Map<String, Object>> mdmResultList = execQueryResultList(stringBuilder.toString(), dto);

        // 域字段转换成id
        List<AttributeInfoDTO> domains = attributeList.stream().filter(e -> e.getDomainId() != null).collect(Collectors.toList());
        domains.stream().forEach(e -> {
            resultList.stream().forEach(item -> {
                for (String key : item.keySet()) {
                    if (e.getName().equals(key)){
                        // 查询出域字段信息
                        AttributeVO data = attributeService.getById(e.getDomainId()).getData();

                        // 域字段的表名称
                        String mdmTableName1 = generateMdmTableName(data.getModelId(), data.getEntityId());

                        StringBuilder str = new StringBuilder();
                        str.append("SELECT fidata_id FROM " + mdmTableName1);
                        str.append(" WHERE " + data.getColumnName() + " = '" + item.get(key)  +"'");
                        // 查询域字段数据
                        List<MdmDTO> ids = execQueryResultList(str.toString(), connection, MdmDTO.class);

                        if (CollectionUtils.isNotEmpty(ids)){
                            item.put(key,ids.get(0).getFidata_id());
                        }
                    }
                }
            });
        });


        // 处理需要插入和更新的数据(关键点)
        List<Map<String, Object>> dateList = this.dataProcessing(mdmResultList, resultList, attributeList);

        // 4.数据导入
        return this.dataImport(mdmTableName,stgTableName,dto,attributeList,dateList,batchCode);
    }

    /**
     * 数据导入
     */
    public ResultEnum dataImport(String mdmTableName,String stgTableName
            ,DataSourceConDTO dto
            ,List<AttributeInfoDTO> attributeList
            ,List<Map<String, Object>> listMap
            ,String batchCode){
        AbstractDbHelper dbHelper = new AbstractDbHelper();
        Connection connection = dbHelper.connection(dto.conStr, dto.conAccount,
                dto.conPassword,dto.conType);

        StringBuilder str = new StringBuilder();
        str.append("INSERT INTO " + mdmTableName);
        str.append("(");
        // 系统字段
        str.append(MARK + "new_code").append(",");
        str.append(MARK + "version_id").append(",");
        str.append(MARK + "lock_tag").append(",");
        // 表基础字段
        str.append(MARK + "create_time").append(",");
        str.append(MARK + "create_user").append(",");
        str.append(MARK + "update_time").append(",");
        str.append(MARK + "update_user").append(",");
        str.append(MARK + "del_flag").append(",");

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

        List<AttributeInfoDTO> codeAssociationCondition = attributeList.stream().filter(e -> e.getStatus().equals(AttributeStatusEnum.SUBMITTED.getName())
                && e.getName().equals("code")).collect(Collectors.toList());

        // 获取唯一键
        String columnName = codeAssociationCondition.get(0).getColumnName();
        str.append(" ON CONFLICT ( " + columnName + "," + MARK + "version_id" +") DO UPDATE ");
        str.append(" SET ");
        str.append(MARK + "new_code = " + "excluded." + MARK + "new_code").append(",");
        str.append(MARK + "version_id = " + "excluded." + MARK + "version_id").append(",");
        str.append(MARK + "lock_tag = " + "excluded." + MARK + "lock_tag").append(",");
        str.append(MARK + "del_flag = " + "excluded." + MARK + "del_flag").append(",");

        String code1 = columnName + " = " + "excluded." + MARK + "new_code" + ",";

        // 业务字段
        String collect1 = attributeList.stream().filter(e -> e.getStatus().equals(AttributeStatusEnum.SUBMITTED.getName())
                && !e.getName().equals("code"))
                .map(e -> {
                    StringBuilder str1 = new StringBuilder();
                    str1.append(e.getColumnName() + " = ");
                    str1.append("excluded." + e.getColumnName());
                    return str1;
                }).collect(Collectors.joining(","));
        str.append(code1);
        str.append(collect1);

        PreparedStatement stmt = null;
        try {
            // 系统字段和表基础字段
            stmt = connection.prepareStatement(str.toString());
            stmt.setArray(1, connection.createArrayOf(JDBCType.VARCHAR.getName(), this.getParameter(listMap,MARK + "new_code").toArray()));
            stmt.setArray(2, connection.createArrayOf(JDBCType.INTEGER.getName(),this.getParameter(listMap,MARK + "version_id").toArray()));
            stmt.setArray(3, connection.createArrayOf(JDBCType.INTEGER.getName(), this.getParameter(listMap,MARK + "lock_tag").toArray()));
            stmt.setArray(4, connection.createArrayOf(JDBCType.TIMESTAMP.getName(), this.getParameter(listMap,MARK + "create_time").toArray()));
            stmt.setArray(5, connection.createArrayOf(JDBCType.VARCHAR.getName(), this.getParameter(listMap,MARK + "create_user").toArray()));
            stmt.setArray(6, connection.createArrayOf(JDBCType.TIMESTAMP.getName(), this.getParameter(listMap,MARK + "update_time").toArray()));
            stmt.setArray(7, connection.createArrayOf(JDBCType.VARCHAR.getName(), this.getParameter(listMap,MARK + "update_user").toArray()));
            stmt.setArray(8, connection.createArrayOf(JDBCType.INTEGER.getName(), this.getParameter(listMap,MARK + "del_flag").toArray()));

            // 业务字段
            int index = 8;
            for (AttributeInfoDTO infoDto : attributeList) {
                stmt.setArray(++index, connection.createArrayOf(this.getFieldType(infoDto.getDataType()), this.getParameter(listMap,infoDto.getColumnName()).toArray()));
            }

            // 影响记录条数
            int res = stmt.executeUpdate();
            System.out.println("成功条数！:" + res);
            log.info(ResultEnum.DATA_SYNCHRONIZATION_SUCCESS.getMsg() + "【成功条数】:" + res
                       + "【批次号】:" + batchCode);
            // 回调成功同步状态
            this.callbackSuccessStatus(stgTableName,batchCode,connection);

            return ResultEnum.DATA_SYNCHRONIZATION_SUCCESS;
        } catch (SQLException ex) {
            log.error("stg表数据同步失败,异常信息:" + ex);
            String errorMessage = ResultEnum.DATA_SYNCHRONIZATION_FAILED.getMsg() + "【原因】:" + ex.getMessage();
            this.errorMessageProcess(stgTableName,errorMessage,batchCode,connection);

            // 回调失败同步状态
            this.callbackFailedStatus(stgTableName,batchCode,connection);

            return ResultEnum.DATA_SYNCHRONIZATION_FAILED;
        }
    }

    /**
     * 根据指定字段获取值
     * @param listMap
     * @param filed
     */
    public List<Object> getParameter(List<Map<String, Object>> listMap,String filed){
        List<Object> list = new ArrayList<>();
        listMap.stream().forEach(e -> {
            e.forEach((k,v) -> {
                if (k.equals(filed)){
                    if (SPECIAL_CHARACTERS_NULL.equals(v)){
                        list.add(null);
                    }else {
                        list.add(v);
                    }
                }
            });
        });

        return list;
    }

    /**
     * stg表插入失败信息
     * @param stgTableName
     * @param errorMessage
     * @param batchCode
     * @param connection
     */
    public void errorMessageProcess(String stgTableName,String errorMessage,String batchCode
                      ,Connection connection){
        StringBuilder str = new StringBuilder();
        str.append("UPDATE " + stgTableName);
        str.append(" SET fidata_error_msg = '" + errorMessage).append("'");
        str.append(" WHERE fidata_batch_code ='" + batchCode + "'");

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(str.toString());
            statement.execute();
        } catch (SQLException ex) {
            log.error("stg表数据失败信息插入失败!,【执行SQL】:" + str
                   + "【原因】:" + ex.getMessage());
        }
    }

    /**
     * 字段类型匹配
     * @param dataType
     * @return
     */
    public String getFieldType(String dataType) {
        if (dataType != null) {
            String filedType = null;
            switch (dataType) {
                case "域字段":
                case "数值":
                    filedType = JDBCType.INTEGER.getName();
                    break;
                case "时间":
                    filedType = JDBCType.TIME.getName();
                    break;
                case "日期":
                    filedType = JDBCType.DATE.getName();
                    break;
                case "日期时间":
                    filedType = JDBCType.TIMESTAMP.getName();
                    break;
                case "浮点型":
                    filedType = JDBCType.NUMERIC.getName();
                    break;
                case "布尔型":
                    filedType = JDBCType.BOOLEAN.getName();
                    break;
                case "货币":
                    filedType = JDBCType.DECIMAL.getName();
                    break;
                case "文本":
                default:
                    filedType = JDBCType.VARCHAR.getName();
            }

            return filedType;
        }

        return null;
    }

    /**
     * 回调成功同步状态
     * @param batchCode
     */
    public void callbackSuccessStatus(String stgTableName,String batchCode,Connection connection){
        StringBuilder str = new StringBuilder();
        str.append("UPDATE " + stgTableName);
        str.append(" SET fidata_status = '" + SyncStatusTypeEnum.SUBMITTED_SUCCESSFULLY.getValue()).append("'");
        str.append(",fidata_error_msg = null");
        str.append(" WHERE fidata_batch_code ='" + batchCode + "'");

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(str.toString());
            statement.execute();
        } catch (SQLException ex) {
            log.error("mdm表数据同步回调成功状态失败!,【执行SQL】:" + str
                    + "【原因】:" + ex.getMessage());
        }
    }

    /**
     * 回调成功失败状态
     * @param stgTableName
     * @param batchCode
     * @param connection
     */
    public void callbackFailedStatus(String stgTableName,String batchCode,Connection connection){
        StringBuilder str = new StringBuilder();
        str.append("UPDATE " + stgTableName);
        str.append(" SET fidata_status = '" + SyncStatusTypeEnum.SUBMISSION_FAILED.getValue()).append("'");
        str.append(" WHERE fidata_batch_code ='" + batchCode + "'");
        str.append(" AND fidata_del_flag = 1 ");

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(str.toString());
            statement.execute();
        } catch (SQLException ex) {
            log.error("mdm表数据同步回调失败状态失败!,【执行SQL】:" + str
                    + "【原因】:" + ex.getMessage());
        }
    }

    /**
     * 处理需要插入和更新的数据
     * @param mdmResultList
     * @param resultList
     * @param attributeList
     * @return
     */
    public List<Map<String, Object>> dataProcessing(List<Map<String, Object>> mdmResultList,List<Map<String, Object>> resultList
                                            ,List<AttributeInfoDTO> attributeList){
        List<Map<String, Object>> updateList = new ArrayList<>();
        List<Map<String, Object>> insertList = new ArrayList<>();
        // 需要更新的数据
        mdmResultList.stream().forEach(item -> {
            resultList.stream().filter(e -> e.get("code").equals(item.get("code")))
                    .forEach(e -> {
                        for (String key : e.keySet()) {
                            Object newCodeValue = e.get("fidata_new_code");
                            Object codeValue = e.get("code");
                            if (ObjectUtils.isNotEmpty(newCodeValue)){
                                if (key.equals("fidata_new_code")){
                                    e.put("fidata_new_code", newCodeValue);
                                    updateList.add(e);
                                }
                            }else {
                                if (key.equals("fidata_new_code")){
                                    e.put("fidata_new_code", codeValue);
                                    updateList.remove(e);
                                    updateList.add(e);
                                }
                            }
                        }
                    });
        });

        // 更新的数据存在编码,进行赋值
        mdmResultList.stream().forEach(item -> {
            updateList.stream().filter(e -> e.get("code").equals(item.get("code")))
                    .forEach(e -> {
                        for (String mdmKey : item.keySet()) {
                            for (String stgKey : e.keySet()) {
                                Object stgValue = e.get(stgKey);
                                if (ObjectUtils.isEmpty(stgValue) && mdmKey.equals(stgKey)){
                                    e.put(stgKey,item.get(mdmKey));
                                }
                            }
                        }
                    });
        });

        // 需要插入的数据
        if (CollectionUtils.isNotEmpty(updateList)){
            List<Map<String, Object>> list = resultList.stream().filter(
                    (mapItem) -> !updateList.stream().map(item -> item.get("code")
                    ).collect(Collectors.toList()).contains(mapItem.get("code"))
            ).collect(Collectors.toList());
            insertList.addAll(list);
        }else {
            insertList.addAll(resultList);
        }


        // 新增数据new_code赋值code
        List<Map<String, Object>> insertDates = new ArrayList<>();
        insertList.stream().filter(Objects::nonNull).forEach(e -> {
            for (String key : e.keySet()) {
                if (key.equals("fidata_new_code")) {
                    e.put("fidata_new_code", e.get("code"));
                    insertDates.add(e);
                }
            }
        });

        List<Map<String, Object>> dateList = new ArrayList<>();
        // 名称转换(name转换成ColumnName)
        updateList.stream().filter(e -> CollectionUtils.isNotEmpty(e)).forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            e.forEach((k,v) -> {
                map.put(k,v);
                for (AttributeInfoDTO infoDto : attributeList) {
                    if (k.equals(infoDto.getName())){
                        map.remove(k);
                        map.put(infoDto.getColumnName(),v);
                    }
                }
            });
            dateList.add(map);
        });

        insertDates.stream().filter(e -> CollectionUtils.isNotEmpty(e)).forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            e.forEach((k,v) -> {
                map.put(k,v);
                for (AttributeInfoDTO infoDto : attributeList) {
                    if (k.equals(infoDto.getName())){
                        map.remove(k);
                        map.put(infoDto.getColumnName(),v);
                    }
                }
            });
            dateList.add(map);
        });

        return dateList;
    }
}
