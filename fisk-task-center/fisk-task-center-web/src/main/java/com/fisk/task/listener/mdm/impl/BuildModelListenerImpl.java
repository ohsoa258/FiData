package com.fisk.task.listener.mdm.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.mapstruct.EnumTypeConversionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.mdm.client.MdmClient;
import com.fisk.mdm.dto.attribute.AttributeDomainDTO;
import com.fisk.mdm.dto.attribute.AttributeFactDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.attribute.AttributeStatusDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.enums.DataTypeEnum;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.task.dto.model.EntityDTO;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.task.listener.mdm.BuildModelListener;
import com.fisk.mdm.utils.mdmBEBuild.BuildFactoryHelper;
import com.fisk.mdm.utils.mdmBEBuild.IBuildSqlCommand;
import com.fisk.mdm.utils.mdmBEBuild.impl.BuildPgCommandImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ch.qos.logback.core.db.DBHelper.closeConnection;
import static com.fisk.common.service.mdmBEBuild.AbstractDbHelper.rollbackConnection;
import static com.fisk.mdm.enums.AttributeStatusEnum.*;
import static com.fisk.mdm.enums.MdmStatusTypeEnum.*;
import static com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils.*;
import static com.fisk.mdm.utils.mdmBEBuild.impl.BuildPgCommandImpl.*;

/**
 * Description: 创建模型日志表
 *
 * @author wangyan
 */
@Component
@Slf4j
public class BuildModelListenerImpl implements BuildModelListener {

    @Value("${pgsql-mdm.type}")
    DataSourceTypeEnum type;
    @Value("${pgsql-mdm.url}")
    String connectionStr;
    @Value("${pgsql-mdm.username}")
    String acc;
    @Value("${pgsql-mdm.password}")
    String pwd;

    @Resource
    MdmClient mdmClient;

    @Override
    public ResultEnum msg(String dataInfo, Acknowledgment acke) {

        String tableName = null;
        String sql = null;
        Connection connection = null;
        try {
            // 获取需要创建的表名
            ModelDTO model = JSON.parseObject(dataInfo, ModelDTO.class);
            tableName = model.getAttributeLogName();

            // 工厂
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);

            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            connection = abstractDbHelper.connection(connectionStr, acc, pwd, type);
            sql = sqlBuilder.buildAttributeLogTable(tableName);
            // 执行sql
            abstractDbHelper.executeSql(sql, connection);
            log.info("【创建属性日志表名】:" + tableName + "创建属性日志表名SQL:" + sql);
            return ResultEnum.SUCCESS;
        } catch (Exception ex) {
            log.error("【创建属性日志表名】:" + tableName + "【创建属性日志表名SQL】:" + sql
                    + "【创建属性日志表名失败,异常信息】:" + ex);
            ex.printStackTrace();
            return ResultEnum.ERROR;
        } finally {
            closeConnection(connection);
            acke.acknowledge();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum backgroundCreateTasks(String dataInfo, Acknowledgment acke) {
        Connection connection = null;
        try {
            EntityDTO dto = JSON.parseObject(dataInfo, EntityDTO.class);

            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            // 获取连接信息
            connection = abstractDbHelper.connection(connectionStr, acc, pwd, type);

            // 获取实体信息
            EntityInfoVO entityInfoVo = mdmClient.getAttributeById(dto.getEntityId(),null).getData();
            String status = entityInfoVo.getStatus();
            if (status.equals(NOT_CREATED.getName()) || status.equals(CREATED_FAIL.getName())) {
                // 1.执行创建表任务
                this.createTable(abstractDbHelper, sqlBuilder, connection, entityInfoVo);
            } else if (status.equals(CREATED_SUCCESSFULLY.getName())) {
                // 2.执行修改表任务
                this.updateTable(abstractDbHelper, connection, sqlBuilder, entityInfoVo, dto.getEntityId());
            }
            return ResultEnum.SUCCESS;
        } catch (Exception ex) {
            log.error("创建后台任务表失败,异常信息:" + ex);
            ex.printStackTrace();
            return ResultEnum.ERROR;
        } finally {
            closeConnection(connection);
            acke.acknowledge();
        }
    }

    /**
     * 执行表后台修改任务
     * @param abstractDbHelper
     * @param connection
     * @param sqlBuilder
     * @param entityInfoVo
     * @param entityId
     * @throws Exception
     */
    public void updateTable(AbstractDbHelper abstractDbHelper, Connection connection,
                            IBuildSqlCommand sqlBuilder, EntityInfoVO entityInfoVo,
                            Integer entityId) {

        // 筛选属性出去发布
        List<AttributeInfoDTO> noSubmitAttributeList = entityInfoVo.getAttributeList().stream().filter(e -> !e.getStatus().equals(AttributeStatusEnum.SUBMITTED.getName()))
                .collect(Collectors.toList());
        try{
            // a.开启事务
            connection.getAutoCommit();
            connection.setAutoCommit(false);

            // 1.stg表删了重新生成
            this.updateStgTable(abstractDbHelper, connection, sqlBuilder, entityInfoVo, noSubmitAttributeList);
            EntityInfoVO data = mdmClient.getAttributeById(entityId,null).getData();
            // 2.mdm表更新、log表更新
            List<AttributeStatusDTO> dtoList = this.updateMdmTable(abstractDbHelper, connection, sqlBuilder, data.getAttributeList());
            // 3.viw视图重新生成
            this.createViwTable(abstractDbHelper, sqlBuilder, connection, entityInfoVo);
            // 3.1更新事实属性表
            this.updateFactTable(sqlBuilder, connection, entityInfoVo.getAttributeList());

            // e.提交事务
            connection.commit();

            // 4.清空错误信息
            entityInfoVo.getAttributeList().stream().filter(Objects::nonNull)
                    .forEach(e -> {
                        AttributeStatusDTO dto1 = new AttributeStatusDTO();
                        dto1.setId(e.getId());
                        dto1.setErrorMsg(" ");
                        mdmClient.updateStatus(dto1);
                    });

            // 5.回写属性成功状态
            dtoList.stream().filter(Objects::nonNull).forEach(e -> {
                mdmClient.updateStatus(e);
            });
        }catch (Exception ex){
            // a.回滚事务
            rollbackConnection(connection);
        }
    }

    /**
     * 更新事实属性表
     * @param sqlBuilder
     * @param connection
     * @param attributeList
     */
    public void updateFactTable(IBuildSqlCommand sqlBuilder,Connection connection,List<AttributeInfoDTO> attributeList){

        try {
            // 1.删除状态为删除和修改得属性
            List<Integer> deleteAttributeIds = attributeList.stream()
                    .filter(e -> e.getStatus().equals(DELETE.getName()) ||
                            e.getStatus().equals(UPDATE.getName()))
                    .map(e -> e.getId())
                    .collect(Collectors.toList());

            String deleteSql = sqlBuilder.deleteDataByAttributeId("tb_fact_attribute", "attribute_id", deleteAttributeIds);

            // 2.插入状态为修改和新增得属性
            String insertSql = this.buildAttributeSql(sqlBuilder, attributeList);

            PreparedStatement stateDelete = connection.prepareStatement(deleteSql);
            PreparedStatement stateInsert = connection.prepareStatement(insertSql);
            stateDelete.execute();
            stateInsert.execute();
        }catch (Exception ex){
            // 回滚事务
            rollbackConnection(connection);

            // 记录日志
            log.error(ResultEnum.FACT_ATTRIBUTE_FAILD.getMsg() + "【原因:】" + ex.getMessage());

            throw new FkException(ResultEnum.FACT_ATTRIBUTE_FAILD);
        }
    }

    /**
     * stg表删了重新生成
     * @param abstractDbHelper
     * @param connection
     * @param sqlBuilder
     * @param entityInfoVo
     * @param noSubmitAttributeList
     */
    public void updateStgTable(AbstractDbHelper abstractDbHelper, Connection connection,
                               IBuildSqlCommand sqlBuilder, EntityInfoVO entityInfoVo,
                               List<AttributeInfoDTO> noSubmitAttributeList){

        String sql = null;
        try{
            // 1.删除视图
            AttributeInfoDTO dto = noSubmitAttributeList.get(0);
            String viwTableName = generateViwTableName(dto.getModelId(), dto.getEntityId());
            sql = this.dropViwTable(abstractDbHelper, connection, sqlBuilder, noSubmitAttributeList,viwTableName);
            if (StringUtils.isNotBlank(sql)){
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.execute();
            }

            // 2.删除stg
            String stgTableName = generateStgTableName(dto.getModelId(), dto.getEntityId());
            sql = this.dropStgTable(abstractDbHelper, connection, sqlBuilder, noSubmitAttributeList,stgTableName);
            if (StringUtils.isNotBlank(sql)){
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.execute();
            }

            // 3.创建Stg表
            sql = this.createStgTable(sqlBuilder, entityInfoVo,stgTableName);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();

            // 4.回写columnName
            this.writableColumnName(entityInfoVo.getAttributeList());
        }catch (SQLException ex){
            // a.回滚事务
            rollbackConnection(connection);

            // 回写失败属性信息
            this.exceptionAttributeProcess(noSubmitAttributeList, ResultEnum.CREATE_STG_TABLE.getMsg()
                    + "【执行SQL】" + sql + "【原因】:" + ex.getMessage());
            throw new FkException(ResultEnum.CREATE_STG_TABLE);
        }
    }

    /**
     * 判断表是否存在
     *
     * @param sqlBuilder
     * @param tableName
     * @return
     */
    public boolean isExits(IBuildSqlCommand sqlBuilder, AbstractDbHelper abstractDbHelper,
                           Connection connection, String tableName) {
        try {
            // 1.查询表是否存在
            String querySql = sqlBuilder.queryData(tableName);
            abstractDbHelper.executeSql(querySql, connection);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * 删除stg表
     *
     * @param abstractDbHelper
     * @param connection
     * @param sqlBuilder
     * @param attributeList
     */
    public String dropStgTable(AbstractDbHelper abstractDbHelper, Connection connection,
                             IBuildSqlCommand sqlBuilder, List<AttributeInfoDTO> attributeList
                             ,String stgTableName) {

        // 判断视图是否存在
        boolean exits = this.isExits(sqlBuilder, abstractDbHelper, connection, stgTableName);
        if (exits == true) {
            // 1.创建Sql
            String dropTableSql = sqlBuilder.dropTable(stgTableName);
            return dropTableSql;
        }

        return null;
    }


    /**
     * 删除Viw视图
     *
     * @param abstractDbHelper
     * @param connection
     * @param sqlBuilder
     * @param attributeList
     */
    public String dropViwTable(AbstractDbHelper abstractDbHelper, Connection connection,
                             IBuildSqlCommand sqlBuilder, List<AttributeInfoDTO> attributeList
                             ,String viwTableName) {

        // 判断视图是否存在
        boolean exits = this.isExits(sqlBuilder, abstractDbHelper, connection, viwTableName);
        if (exits == true) {
            // 1.创建Sql
            String dropTableSql = sqlBuilder.dropViw(viwTableName);
            return dropTableSql;
        }

        return null;
    }

    /**
     * 更新mdm表
     *
     * @param sqlBuilder
     * @param attributeList
     */
    public List<AttributeStatusDTO> updateMdmTable(AbstractDbHelper abstractDbHelper, Connection connection,
                               IBuildSqlCommand sqlBuilder, List<AttributeInfoDTO> attributeList) {
        // 表名
        AttributeInfoDTO dto = attributeList.get(0);
        String tableName = generateMdmTableName(dto.getModelId(),dto.getEntityId());
        String logTableName = generateLogTableName(dto.getModelId(),dto.getEntityId());

        List<AttributeStatusDTO> dtoList = new ArrayList<>();
        for (AttributeInfoDTO infoDto : attributeList) {

            String sql = null;
            String logTableSql = null;
            AttributeStatusDTO status = new AttributeStatusDTO();
            status.setId(infoDto.getId());
            status.setColumnName("column_" + infoDto.getEntityId() + "_" + infoDto.getId());

            try {

                if (infoDto.getStatus().equals(INSERT.getName())) {
                    // 新增字段
                    String filedType = this.getDataType(infoDto.getDataType(), infoDto.getDataTypeLength(),infoDto.getDataTypeDecimalLength());

                    // 判断是否必填
                    String required = null;
                    Boolean enableRequired = infoDto.getEnableRequired();
                    if (enableRequired == true) {
                        required = " NOT NULL ";
                    } else {
                        required = " NULL ";
                    }

                    // 1.构建Sql
                    sql = sqlBuilder.addColumn(tableName, infoDto.getColumnName(), filedType + required);
                    logTableSql = sqlBuilder.addColumn(logTableName, infoDto.getName(), filedType + required);
                    // 2.执行Sql
                    PreparedStatement statement = connection.prepareStatement(sql);
                    PreparedStatement statementLog = connection.prepareStatement(logTableSql);
                    statement.execute();
                    statementLog.execute();
                } else if (infoDto.getStatus().equals(UPDATE.getName())) {
                    // 修改字段
                    String filedType = this.getDataType(infoDto.getDataType(), infoDto.getDataTypeLength(),infoDto.getDataTypeDecimalLength());

                    // 1.修改字段类型
                    sql = sqlBuilder.modifyFieldType(tableName, infoDto.getColumnName(), filedType);
                    logTableSql = sqlBuilder.modifyFieldType(logTableName, infoDto.getName(), filedType);
                    // 2.执行Sql
                    PreparedStatement statement = connection.prepareStatement(sql);
                    PreparedStatement statementLog = connection.prepareStatement(logTableSql);
                    statement.execute();
                    statementLog.execute();

                    // 2.修改字段长度
                    if (infoDto.getDataType().equals("文本")) {
                        sql = sqlBuilder.modifyFieldLength(tableName, infoDto.getColumnName(), filedType);
                        logTableSql = sqlBuilder.modifyFieldLength(logTableName, infoDto.getName(), filedType);
                        // 2.执行Sql
                        PreparedStatement statement1 = connection.prepareStatement(sql);
                        PreparedStatement statementLog1 = connection.prepareStatement(logTableSql);
                        statement1.execute();
                        statementLog1.execute();
                    }

                    // 3.修改字段是否必填
                    PreparedStatement preparedStatement = null;
                    Boolean enableRequired = infoDto.getEnableRequired();
                    if (enableRequired == true) {
                        sql = sqlBuilder.notNullable(tableName, infoDto.getColumnName());
                        logTableSql = sqlBuilder.notNullable(logTableName, infoDto.getName());
                    } else {
                        sql = sqlBuilder.nullable(tableName, infoDto.getColumnName());
                        logTableSql = sqlBuilder.notNullable(logTableName, infoDto.getName());
                    }
                    // 2.执行Sql
                    preparedStatement = connection.prepareStatement(sql);
                    PreparedStatement statementLog1 = connection.prepareStatement(logTableSql);
                    preparedStatement.execute();
                    statementLog1.execute();
                } else if (infoDto.getStatus().equals(DELETE.getName())) {
                    // 删除字段
                    sql = sqlBuilder.deleteFiled(tableName, infoDto.getColumnName());
                    logTableSql = sqlBuilder.deleteFiled(logTableName, infoDto.getName());
                    PreparedStatement statement = connection.prepareStatement(sql);
                    PreparedStatement statementLog = connection.prepareStatement(logTableSql);
                    statement.execute();
                    statementLog.execute();
                    mdmClient.delete(infoDto.getId());
                }

                // 3.回写成功状态
                status.setStatus(2);
                status.setSyncStatus(1);
                dtoList.add(status);
            } catch (SQLException ex) {
                // a.回滚事务
                rollbackConnection(connection);

                // 回写失败状态
                status.setStatus(this.stringToStatusInt(infoDto.getStatus()));
                status.setSyncStatus(0);
                status.setErrorMsg(ResultEnum.UPDATE_MDM_TABLE.getMsg()
                        + "【执行SQL】" + sql + "【原因】:" + ex);
                // 回写属性状态
                mdmClient.updateStatus(status);

                log.error("修改Mdm表失败,异常信息:" + ex);
                throw new FkException(ResultEnum.UPDATE_MDM_TABLE);
            }
        }

        return dtoList;
    }

    /**
     * 返回字段类型
     *
     * @param dataType
     * @param dataTypeLength
     * @return
     */
    public String getDataType(String dataType, Integer dataTypeLength,Integer precision) {
        if (dataType != null) {
            String filedType = null;
            switch (dataType) {
                case "文件":
                case "经纬度坐标":
                    filedType = "VARCHAR ( " + "50" + " )";
                    break;
                case "域字段":
                case "数值":
                    filedType = "int4";
                    break;
                case "时间":
                    filedType = "TIME";
                    break;
                case "日期":
                    filedType = "date";
                    break;
                case "日期时间":
                    filedType = "timestamp";
                    break;
                case "浮点型":
                    filedType = "numeric(" + dataTypeLength + "," + precision + ")";
                    break;
                case "布尔型":
                    filedType = "bool";
                    break;
                case "货币":
                    filedType = "money";
                    break;
                case "文本":
                default:
                    filedType = "VARCHAR ( " + dataTypeLength + " )";
            }

            return filedType;
        }

        return null;
    }

    /**
     * 执行创建表任务
     *
     * @param entityInfoVo
     */
    public void createTable(AbstractDbHelper abstractDbHelper, IBuildSqlCommand sqlBuilder,
                            Connection connection, EntityInfoVO entityInfoVo) throws SQLException {

        String sql = null;
        try {
            // a.开启事务
            connection.getAutoCommit();
            connection.setAutoCommit(false);

            // 1.创建Stg表
            String stgTableName = generateStgTableName(entityInfoVo.getModelId(), entityInfoVo.getId());
            sql = this.createStgTable(sqlBuilder, entityInfoVo,stgTableName);
            PreparedStatement stemStg = connection.prepareStatement(sql);
            stemStg.execute();

            // 2.创建mdm表
            String mdmTableName = generateMdmTableName(entityInfoVo.getModelId(), entityInfoVo.getId());
            sql = this.createMdmTable(sqlBuilder, entityInfoVo,mdmTableName);
            PreparedStatement stemMdm = connection.prepareStatement(sql);
            stemMdm.execute();

            // 3.创建日志表
            String logTableName = generateLogTableName(entityInfoVo.getModelId(), entityInfoVo.getId());
            sql = this.createLogTable(sqlBuilder,entityInfoVo,logTableName);
            PreparedStatement stemLog = connection.prepareStatement(sql);
            stemLog.execute();

            // 3.回写columnName
            this.writableColumnName(entityInfoVo.getAttributeList());

            // 4.创建view视图
            sql = this.buildViewTable(entityInfoVo,sqlBuilder);
            PreparedStatement stemViw = connection.prepareStatement(sql);
            stemViw.execute();

            // 4.1 提交最新属性表
            sql = this.buildAttributeSql(sqlBuilder, entityInfoVo.getAttributeList());
            PreparedStatement stemAttribute = connection.prepareStatement(sql);
            stemAttribute.execute();
            
            // e.提交事务
            connection.commit();

            // 5.回写成功属性状态
            this.writableAttributeStatus(entityInfoVo.getAttributeList());
            // 6.回写实体状态
            this.writableEntityStatus(entityInfoVo,mdmTableName);

            // 7.清空错误信息
            entityInfoVo.getAttributeList().stream().filter(Objects::nonNull)
                    .forEach(e -> {
                        AttributeStatusDTO dto1 = new AttributeStatusDTO();
                        dto1.setId(e.getId());
                        dto1.setErrorMsg(" ");
                        mdmClient.updateStatus(dto1);
                    });
        } catch (Exception ex) {
            // a.回滚事务
            rollbackConnection(connection);
            // b.回写失败状态
            this.exceptionProcess(entityInfoVo, ex, ResultEnum.CREATE_TABLE_ERROR.getMsg() + "【原因】:" + ex.getMessage());
            log.error(ResultEnum.CREATE_TABLE_ERROR.getMsg() + "【执行Sql】:" + sql);
        }
    }

    /**
     * 生成插入属性事实表的Sql
     * @param sqlBuilder
     * @param attributeList
     * @return
     */
    public String buildAttributeSql(IBuildSqlCommand sqlBuilder,List<AttributeInfoDTO> attributeList){

        // 1.数据转换
        List<AttributeFactDTO> dtoList = attributeList.stream().filter(e -> e.getStatus().equals(INSERT.getName())).map(e -> {
            AttributeFactDTO dto = new AttributeFactDTO();
            dto.setName(e.getName());
            dto.setDataType(DataTypeEnum.getValue(e.getDataType()).getValue());
            dto.setDataTypeLength(e.getDataTypeLength());
            dto.setDataTypeDecimalLength(e.getDataTypeDecimalLength());

            // bool值转换
            EnumTypeConversionUtils conversionUtils = new EnumTypeConversionUtils();
            dto.setEnableRequired(conversionUtils.boolToInt(e.getEnableRequired()));
            dto.setAttribute_id(e.getId());
            return dto;
        }).collect(Collectors.toList());

        // 2.创建Sql
        String sql = sqlBuilder.insertAttributeFact(dtoList);
        return sql;
    }

    /**
     * 回写实体失败信息
     *
     * @param entityInfoVo
     */
    public void exceptionEntityProcess(EntityInfoVO entityInfoVo, Exception ex) {
        UpdateEntityDTO dto = new UpdateEntityDTO();
        dto.setId(entityInfoVo.getId());
        dto.setStatus(2);
        mdmClient.update(dto);
        log.error("创建实体失败,异常信息:" + ex);
    }

    /**
     * 回写实体成功状态
     *
     * @param entityInfoVo
     */
    public void writableEntityStatus(EntityInfoVO entityInfoVo,String mdmTableName) {
        UpdateEntityDTO dto = new UpdateEntityDTO();
        dto.setId(entityInfoVo.getId());
        dto.setStatus(1);
        dto.setTableName(mdmTableName);
        mdmClient.update(dto);
    }

    /**
     * 回写成功属性状态
     */
    public void writableAttributeStatus(List<AttributeInfoDTO> attributeList) {
        List<AttributeStatusDTO> dtoList = attributeList.stream().filter(Objects::nonNull)
                .map(e -> {
                    AttributeStatusDTO dto = new AttributeStatusDTO();
                    dto.setId(e.getId());
                    dto.setColumnName("column_" + e.getEntityId() + "_" + e.getId());
                    dto.setStatus(2);
                    dto.setSyncStatus(1);

                    return dto;
                }).collect(Collectors.toList());

        dtoList.stream().forEach(e -> {
            ResultEntity<ResultEnum> result = mdmClient.updateStatus(e);
            if (result.getData() == ResultEnum.UPDATE_DATA_ERROR) {
                // todo
                throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
            }
        });
    }

    /**
     * 回写columnName
     */
    public void writableColumnName(List<AttributeInfoDTO> attributeList) {
        List<AttributeStatusDTO> dtoList = attributeList.stream().filter(Objects::nonNull)
                .map(e -> {
                    AttributeStatusDTO dto = new AttributeStatusDTO();
                    dto.setId(e.getId());
                    dto.setColumnName("column_" + e.getEntityId() + "_" + e.getId());

                    return dto;
                }).collect(Collectors.toList());

        dtoList.stream().forEach(e -> {
            ResultEntity<ResultEnum> result = mdmClient.updateStatus(e);
            if (result.getData() == ResultEnum.UPDATE_DATA_ERROR) {
                // todo
                throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
            }
        });
    }

    /**
     * 创建Stg表
     * @param sqlBuilder
     * @param entityInfoVo
     * @param stgTableName
     * @return
     */
    public String createStgTable(IBuildSqlCommand sqlBuilder, EntityInfoVO entityInfoVo,String stgTableName) {

        // 1.生成Sql
        String buildStgTableSql = sqlBuilder.buildStgTable(entityInfoVo,stgTableName);
        // 2.执行sql
        return buildStgTableSql;
    }

    /**
     * 创建日志表
     * @param sqlBuilder
     * @param entityInfoVo
     * @param stgTableName
     * @return
     */
    public String createLogTable(IBuildSqlCommand sqlBuilder,EntityInfoVO entityInfoVo,String stgTableName) {

        // 1.生成Sql
        List<String> code = entityInfoVo.getAttributeList().stream().filter(e -> e.getName().equals("code")).map(e -> e.getName())
                .collect(Collectors.toList());
        String buildLogTableSql = sqlBuilder.buildLogTable(entityInfoVo,stgTableName,code.get(0));
        // 2.执行sql
        return buildLogTableSql;
    }

    /**
     * 创建mdm表
     * @param sqlBuilder
     * @param entityInfoVo
     * @param mdmTableName
     * @return
     */
    public String createMdmTable(IBuildSqlCommand sqlBuilder, EntityInfoVO entityInfoVo,String mdmTableName) {

        // 1.生成Sql
        List<String> code = entityInfoVo.getAttributeList().stream().filter(e -> e.getName().equals("code")).map(e -> {
            return "column_" + e.getEntityId() + "_" + e.getId();
        }).collect(Collectors.toList());
        String buildStgTableSql = sqlBuilder.buildMdmTable(entityInfoVo,mdmTableName,code.get(0));
        return buildStgTableSql;
    }

    /**
     * 回写失败信息
     *
     * @param entityInfoVo
     * @param ex
     */
    public void exceptionProcess(EntityInfoVO entityInfoVo, Exception ex, String message) {
        // 1.回写属性失败状态
        this.exceptionAttributeProcess(entityInfoVo.getAttributeList(), message);
        // 2.回写实体失败状态
        this.exceptionEntityProcess(entityInfoVo, ex);
    }

    /**
     * 回写属性失败状态
     *
     * @param dtoList
     * @param message
     */
    public void exceptionAttributeProcess(List<AttributeInfoDTO> dtoList, String message) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return;
        }

        dtoList.stream().filter(Objects::nonNull)
                .forEach(e -> {
                    AttributeStatusDTO dto = new AttributeStatusDTO();
                    dto.setId(e.getId());
                    dto.setColumnName("column_" + e.getEntityId() + "_" + e.getId());
                    dto.setStatus(this.stringToStatusInt(e.getStatus()));
                    dto.setSyncStatus(0);
                    dto.setErrorMsg(message);
                    mdmClient.updateStatus(dto);
                });
    }

    public void exceptionAttributeProcess(List<AttributeStatusDTO> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return;
        }

        dtoList.stream().filter(Objects::nonNull)
                .forEach(e -> {
                    mdmClient.updateStatus(e);
                });
    }

    /**
     * 属性状态转换: status
     *
     * @param status
     * @return
     */
    public Integer stringToStatusInt(String status) {
        switch (status) {
            case "新增待发布":
                return AttributeStatusEnum.INSERT.getValue();
            case "修改待发布":
                return UPDATE.getValue();
            case "删除待发布":
                return AttributeStatusEnum.DELETE.getValue();
            case "已发布":
                return AttributeStatusEnum.SUBMITTED.getValue();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * 创建view视图
     *
     * @param abstractDbHelper
     * @param sqlBuilder
     * @param connection
     * @param entityInfoVo
     */
    public void createViwTable(AbstractDbHelper abstractDbHelper, IBuildSqlCommand sqlBuilder,
                                 Connection connection, EntityInfoVO entityInfoVo) {

        String buildStgTableSql = null;
        try {
            // 1.生成Sql
            buildStgTableSql = this.buildViewTable(entityInfoVo,sqlBuilder);
            // 2.执行sql
            PreparedStatement statement = connection.prepareStatement(buildStgTableSql);
            statement.execute();
        } catch (Exception ex) {

            // 筛选属性出去发布
            List<AttributeInfoDTO> noSubmitAttributeList = entityInfoVo.getAttributeList().stream().filter(e -> !e.getStatus().equals(AttributeStatusEnum.SUBMITTED.getName()))
                    .collect(Collectors.toList());

            // 回写失败属性信息
            this.exceptionAttributeProcess(noSubmitAttributeList, ResultEnum.CREATE_VIW_TABLE.getMsg()
                    + "【执行SQL】" + buildStgTableSql + "【原因】:" + ex);
            log.error("创建Stg表失败,异常信息:" + ex);
            throw new FkException(ResultEnum.CREATE_VIW_TABLE);
        }
    }

    /**
     * 创建视图
     *
     * @param entityInfoVo
     * @return
     */
    public String buildViewTable(EntityInfoVO entityInfoVo,IBuildSqlCommand sqlBuilder) {
        String viwTableName = generateViwTableName(entityInfoVo.getModelId(), entityInfoVo.getId());
        String mdmTableName = generateMdmTableName(entityInfoVo.getModelId(), entityInfoVo.getId());

        StringBuilder str = new StringBuilder();
        str.append("CREATE VIEW " + PUBLIC + ".");
        str.append(viwTableName);
        str.append(" AS ").append("SELECT ");

        List<Integer> ids = entityInfoVo.getAttributeList().stream().filter(e -> e.getId() != null).map(e -> e.getId()).collect(Collectors.toList());
        List<AttributeInfoDTO> attributeList = mdmClient.getByIds(ids).getData();

        // 存在外键数据
        List<AttributeInfoDTO> foreignList = attributeList.stream().filter(e -> e.getDomainId() != null).collect(Collectors.toList());
        // 不存在外键数据
        List<AttributeInfoDTO> noForeignList = attributeList.stream().filter(e -> e.getDomainId() == null).collect(Collectors.toList());

        // 先去判断属性有没有外键
        if (CollectionUtils.isEmpty(foreignList)) {
            // 不存在外键
            str.append(this.noDomainSplicing(noForeignList));
        } else {
            // 存在外键
            str.append(this.domainSplicing(foreignList, noForeignList));
        }

        return str.toString();
    }

    /**
     * 存在域字段
     *
     * @param foreignList
     * @param noForeignList
     */
    public String domainSplicing(List<AttributeInfoDTO> foreignList, List<AttributeInfoDTO> noForeignList) {
        StringBuilder str = new StringBuilder();

        // 复杂数据类型
        List<AttributeInfoDTO> complexType = noForeignList.stream().filter(e -> e.getDataType().equals("文件")
                || e.getDataType().equals("经纬度坐标")).collect(Collectors.toList());

        // 文件类型
        List<AttributeInfoDTO> fileList = noForeignList.stream().filter(e -> e.getDataType().equals("文件")).collect(Collectors.toList());
        // 经纬度类型
        List<AttributeInfoDTO> longitudeList = noForeignList.stream().filter(e -> e.getDataType().equals("经纬度坐标")).collect(Collectors.toList());

        // 不存在域字段的属性
        String noForeign = noForeignList.stream().filter(e -> e.getDomainId() == null).map(e -> {
            String str1 = PRIMARY_TABLE + "." + e.getColumnName() + " AS " + e.getName();
            return str1;
        }).collect(Collectors.joining(","));

        // 存在域字段的属性
        List<Integer> domainIds = foreignList.stream().filter(e -> e.getDomainId() != null).map(e -> e.getDomainId() + 1).collect(Collectors.toList());
        List<AttributeInfoDTO> list = mdmClient.getByIds(domainIds).getData();

        AtomicInteger amount = new AtomicInteger(0);
        String foreign = list.stream().filter(e -> e.getName() != null).map(e -> {
            int incrementAndGet = amount.incrementAndGet();
            // 获取域字段名称
            AttributeVO dataCode = mdmClient.get(e.getId() - 1).getData();
            AttributeInfoDTO data = this.getDomainName(foreignList, e.getId());
            StringBuilder stringBuilder = new StringBuilder();
            if (dataCode != null && data != null) {
                stringBuilder.append(PRIMARY_TABLE + incrementAndGet  + "." + dataCode.getColumnName() + " AS " + data.getName() + "_code");
                stringBuilder.append(",");
                stringBuilder.append(PRIMARY_TABLE + incrementAndGet + "." + e.getColumnName() + " AS " + data.getName() + "_name");
            }

            return stringBuilder;
        }).collect(Collectors.joining(","));


        // 复杂数据类型
        // 文件类型
        StringBuilder complexTypeField = new StringBuilder();
        if (CollectionUtils.isNotEmpty(fileList)){

            // 存在文件类型
            String collect = fileList.stream().filter(Objects::nonNull)
                    .map(e -> {

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(PRIMARY_TABLE + amount.incrementAndGet() + "." + "file_name"
                                + " AS " + e.getName() + "_file_name").append(",");
                        stringBuilder.append(PRIMARY_TABLE + amount + "." + "file_path"
                                + " AS " + e.getName() + "_file_path");
                        return stringBuilder.toString();
                    }).collect(Collectors.joining(","));
            complexTypeField.append(collect);
        }

        if (CollectionUtils.isNotEmpty(fileList) && CollectionUtils.isNotEmpty(longitudeList)){
            complexTypeField.append(",");
        }

        // 地图类型
        if (CollectionUtils.isNotEmpty(longitudeList)){

            // 存在经纬度类型
            String collect = longitudeList.stream().filter(Objects::nonNull)
                    .map(e -> {

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(PRIMARY_TABLE + amount.incrementAndGet() + "." + "lng"
                                + " AS " + e.getName() + "_lng").append(",");
                        stringBuilder.append(PRIMARY_TABLE + amount + "." + "lat"
                                + " AS " + e.getName() + "_lat").append(",");
                        stringBuilder.append(PRIMARY_TABLE + amount + "." + "map_type"
                                + " AS " + e.getName() + "_map_type");
                        return stringBuilder;
                    }).collect(Collectors.joining(","));
            complexTypeField.append(collect);
        }

        // 获取主表表名
        AttributeInfoDTO dto = noForeignList.get(1);
        str.append(this.splicingViewTable(true));
        str.append(noForeign).append(",");
        str.append(foreign).append(",");
        if (CollectionUtils.isNotEmpty(complexType)){
            str.append(complexTypeField).append(",");
        }

        // 追加系统字段
        BuildPgCommandImpl buildPgCommand = new BuildPgCommandImpl();
        str.append(buildPgCommand.splicingViewTable(true));

        // 主表表名
        str.append(" FROM " + "mdm_" + dto.getModelId() + "_" + dto.getEntityId() + " " + PRIMARY_TABLE);

        AtomicInteger amount1 = new AtomicInteger(0);
        String leftJoin = list.stream().filter(Objects::nonNull)
                .map(e -> {

                    // 获取域字段名称
                    AttributeInfoDTO data = this.getDomainName(foreignList, e.getId());

                    String alias = PRIMARY_TABLE + amount1.incrementAndGet();
                    String tableName = "mdm_" + e.getModelId() + "_" + e.getEntityId() + " " + alias;
                    String on = " ON " + PRIMARY_TABLE + "." + MARK + "version_id" + " = " + alias + "." + MARK + "version_id" +
                            " AND " + PRIMARY_TABLE + "." + data.getColumnName() + " = " + alias + "." + MARK + "id";
                    String str1 = tableName + " " + on;
                    return str1;
                }).collect(Collectors.joining(" LEFT JOIN "));

        str.append(" LEFT JOIN " + leftJoin);

        // 复杂数据类型 left join
        String typeJoin = this.complexTypeJoin(amount1, fileList, longitudeList);

        // 追加复杂类型表名 join
        if (StringUtils.isNotBlank(typeJoin)){
            str.append(typeJoin);
        }

        return str.toString();
    }

    /**
     * 复杂数据类型 left join
     * @param amount1
     * @param fileList
     * @param longitudeList
     */
    public String complexTypeJoin(AtomicInteger amount1,List<AttributeInfoDTO> fileList,List<AttributeInfoDTO> longitudeList){
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(fileList)){
            // 文件类型

            // 文件left join字段
            String fileField = fileList.stream().map(e -> {
                String alias = PRIMARY_TABLE + amount1.incrementAndGet();

                StringBuilder str = new StringBuilder();
                str.append(" LEFT JOIN ");
                str.append(" tb_file ").append(alias);
                str.append(" ON ");
                str.append(PRIMARY_TABLE + "." + MARK + "version_id" + " = " + alias + "." + MARK + "version_id");
                str.append(" AND ").append(PRIMARY_TABLE + "." + e.getColumnName() + " = " + alias + ".\"code\"");

                return str;
            }).collect(Collectors.joining(" "));
            stringBuilder.append(fileField);
        }

        if (CollectionUtils.isNotEmpty(longitudeList)){
            // 经纬度坐标

            // 经纬度left join字段
            String longitudes = longitudeList.stream().map(e -> {
                String alias = PRIMARY_TABLE + amount1.incrementAndGet();

                StringBuilder str = new StringBuilder();
                str.append(" LEFT JOIN ");
                str.append(" tb_geography ").append(alias);
                str.append(" ON ");
                str.append(PRIMARY_TABLE + "." + MARK + "version_id" + " = " + alias + "." + MARK + "version_id");
                str.append(" AND ").append(PRIMARY_TABLE + "." + e.getColumnName() + " = " + alias + ".\"code\"");

                return str;
            }).collect(Collectors.joining(" "));
            stringBuilder.append(longitudes);
        }

        return stringBuilder.toString();
    }

    /**
     * 获取域字段名称
     *
     * @param foreignList
     * @param id
     * @return
     */
    public AttributeInfoDTO getDomainName(List<AttributeInfoDTO> foreignList, Integer id) {
        AttributeDomainDTO dto1 = new AttributeDomainDTO();
        dto1.setEntityId(foreignList.get(0).getEntityId());
        dto1.setDomainId(id - 1);
        AttributeInfoDTO data = mdmClient.getByDomainId(dto1).getData();
        return data;
    }

    /**
     * 不存在域字段拼接
     *
     * @param noForeignList
     * @return
     */
    public String noDomainSplicing(List<AttributeInfoDTO> noForeignList) {
        StringBuilder str = new StringBuilder();

        // 复杂数据类型
        List<AttributeInfoDTO> complexType = noForeignList.stream().filter(e -> e.getDataType().equals("文件")
                || e.getDataType().equals("经纬度坐标")).collect(Collectors.toList());

        // 文件类型
        List<AttributeInfoDTO> fileList = noForeignList.stream().filter(e -> e.getDataType().equals("文件")).collect(Collectors.toList());
        // 经纬度类型
        List<AttributeInfoDTO> longitudeList = noForeignList.stream().filter(e -> e.getDataType().equals("经纬度坐标")).collect(Collectors.toList());

        AtomicInteger count = new AtomicInteger();
        StringBuilder complexTypeField = new StringBuilder();
        // 文件类型
        if (CollectionUtils.isNotEmpty(fileList)){

            // 存在文件类型
            String collect = fileList.stream().filter(Objects::nonNull)
                    .map(e -> {
                        String name = mdmClient.getDataById(e.getEntityId()).getData().getName();

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(PRIMARY_TABLE + count.incrementAndGet() + "." + "file_name"
                                + " AS " + e.getName() + "_file_name").append(",");
                        stringBuilder.append(PRIMARY_TABLE + count + "." + "file_path"
                                + " AS " + e.getName() + "_file_path");
                        return stringBuilder.toString();
                    }).collect(Collectors.joining(","));
            complexTypeField.append(collect);
        }

        if (CollectionUtils.isNotEmpty(fileList) && CollectionUtils.isNotEmpty(longitudeList)){
            complexTypeField.append(",");
        }

        // 地图类型
        if (CollectionUtils.isNotEmpty(longitudeList)){

            // 存在经纬度类型
            String collect = longitudeList.stream().filter(Objects::nonNull)
                    .map(e -> {
                        StringBuilder stringBuilder = new StringBuilder();

                        stringBuilder.append(PRIMARY_TABLE + count.incrementAndGet() + "." + "lng"
                                + " AS " + e.getName() + "_lng").append(",");
                        stringBuilder.append(PRIMARY_TABLE + count + "." + "lat"
                                + " AS " + e.getName() + "_lat").append(",");
                        stringBuilder.append(PRIMARY_TABLE + count + "." + "map_type"
                                + " AS " + e.getName() + "_map_type");
                        return stringBuilder;
                    }).collect(Collectors.joining(","));
            complexTypeField.append(collect);
        }

        // 如果存在复杂数据类型就需要字段加上别名
        String splicingViewFiled = null;
        if (CollectionUtils.isEmpty(complexType)){
            splicingViewFiled = this.splicingViewTable(false);
        }else {
            splicingViewFiled = this.splicingViewTable(true);
        }

        // 视图基础字段
        str.append(splicingViewFiled);

        // 业务字段
        String collect = noForeignList.stream().filter(e -> !e.getStatus().equals(AttributeStatusEnum.DELETE.getName())).map(e -> {
            String str1 = null;
            if (CollectionUtils.isEmpty(complexType)){
                str1 = e.getColumnName() + " AS " + e.getName();
            }else {
                str1 = PRIMARY_TABLE + "." + e.getColumnName() + " AS " + e.getName();
            }

            return str1;
        }).collect(Collectors.joining(","));

        BuildPgCommandImpl buildPgCommand = new BuildPgCommandImpl();
        AttributeInfoDTO infoDto = noForeignList.get(1);
        // 追加业务字段
        str.append(collect).append(",");

        if (StringUtils.isNotBlank(complexTypeField)){
            // 追加基础字段
            str.append(buildPgCommand.createViw(true));
            // 追加复杂类型字段
            str.append(complexTypeField);
        }else{
            str.append(buildPgCommand.createViw(false));
            str.deleteCharAt(str.length()-1);
        }

        str.append(" FROM " + "mdm_" + infoDto.getModelId() + "_" + infoDto.getEntityId());

        // 复杂数据类型 left join
        AtomicInteger amount1 = new AtomicInteger(0);
        String typeJoin = this.complexTypeJoin(amount1, fileList, longitudeList);

        // 追加复杂类型表名 join
        if (StringUtils.isNotBlank(typeJoin)){
            str.append(" ").append(PRIMARY_TABLE).append(" ");
            str.append(typeJoin);
        }
        return str.toString();
    }

    /**
     * View 视图表基础字段拼接
     *
     * @return
     */
    public String splicingViewTable(boolean isDomain) {
        StringBuilder str = new StringBuilder();
        if (isDomain == false) {
            str.append(MARK + "id").append(",");
            str.append(MARK + "version_id").append(",");
            str.append(MARK + "lock_tag").append(",");
        } else {
            str.append(PRIMARY_TABLE + "." + MARK + "id").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "version_id").append(",");
            str.append(PRIMARY_TABLE + "." + MARK + "lock_tag").append(",");
        }
        return str.toString();
    }
}
