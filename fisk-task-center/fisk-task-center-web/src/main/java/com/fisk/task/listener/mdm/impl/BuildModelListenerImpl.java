package com.fisk.task.listener.mdm.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.mdm.client.MdmClient;
import com.fisk.mdm.dto.attribute.AttributeDomainDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.attribute.AttributeStatusDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.task.dto.model.EntityDTO;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.task.listener.mdm.BuildModelListener;
import com.fisk.task.utils.mdmBEBuild.BuildFactoryHelper;
import com.fisk.task.utils.mdmBEBuild.IBuildSqlCommand;
import com.fisk.task.utils.mdmBEBuild.impl.BuildPgCommandImpl;
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
import static com.fisk.task.utils.mdmBEBuild.impl.BuildPgCommandImpl.*;

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
        try {
            // 获取需要创建的表名
            ModelDTO model = JSON.parseObject(dataInfo, ModelDTO.class);
            tableName = model.getAttributeLogName();

            // 工厂
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);

            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = abstractDbHelper.connection(connectionStr, acc, pwd, type);
            sql = sqlBuilder.buildAttributeLogTable(tableName);
            // 执行sql
            abstractDbHelper.executeSql(sql, connection);
            log.info("【创建属性日志表名】:" + tableName + "创建属性日志表名SQL:" + sql);
        } catch (Exception ex) {
            log.error("【创建属性日志表名】:" + tableName + "【创建属性日志表名SQL】:" + sql
                    + "【创建属性日志表名失败,异常信息】:" + ex);
            ex.printStackTrace();
            return ResultEnum.SUCCESS;
        } finally {
            acke.acknowledge();
            return ResultEnum.ERROR;
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
            EntityInfoVO entityInfoVo = mdmClient.getAttributeById(dto.getEntityId()).getData();
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

        // 筛选属性出去已提交
        List<AttributeInfoDTO> noSubmitAttributeList = entityInfoVo.getAttributeList().stream().filter(e -> !e.getStatus().equals(AttributeStatusEnum.SUBMITTED.getName()))
                .collect(Collectors.toList());
        try{
            // a.开启事务
            connection.getAutoCommit();
            connection.setAutoCommit(false);

            // 1.stg表删了重新生成
            this.updateStgTable(abstractDbHelper, connection, sqlBuilder, entityInfoVo, noSubmitAttributeList);
            EntityInfoVO data = mdmClient.getAttributeById(entityId).getData();
            // 2.mdm表更新
            this.updateMdmTable(abstractDbHelper, connection, sqlBuilder, data.getAttributeList());
            // 3.viw视图重新生成
            this.createViwTable(abstractDbHelper, sqlBuilder, connection, entityInfoVo);

            // e.提交事务
            connection.commit();

        }catch (Exception ex){
            // a.回滚事务
            rollbackConnection(connection);
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
            sql = this.dropViwTable(abstractDbHelper, connection, sqlBuilder, noSubmitAttributeList);
            if (StringUtils.isNotBlank(sql)){
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.execute();
            }

            // 2.删除stg
            sql = this.dropStgTable(abstractDbHelper, connection, sqlBuilder, noSubmitAttributeList);
            if (StringUtils.isNotBlank(sql)){
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.execute();
            }

            // 3.创建Stg表
            sql = this.createStgTable(abstractDbHelper, sqlBuilder, connection, entityInfoVo);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();

            // 4.回写columnName
            this.writableColumnName(entityInfoVo.getAttributeList());
        }catch (SQLException ex){
            // a.回滚事务
            rollbackConnection(connection);

            // 回写失败属性信息
            this.exceptionAttributeProcess(noSubmitAttributeList, ResultEnum.CREATE_STG_TABLE.getMsg()
                    + "【执行SQL】" + sql + "【原因】:" + ex);
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
                             IBuildSqlCommand sqlBuilder, List<AttributeInfoDTO> attributeList) {

        // 表名
        AttributeInfoDTO dto = attributeList.get(0);
        String tableName = "stg_" + dto.getModelId() + "_" + dto.getEntityId();

        // 判断视图是否存在
        boolean exits = this.isExits(sqlBuilder, abstractDbHelper, connection, tableName);
        if (exits == true) {
            // 1.创建Sql
            String dropTableSql = sqlBuilder.dropTable(tableName);
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
                             IBuildSqlCommand sqlBuilder, List<AttributeInfoDTO> attributeList) {

        // 表名
        AttributeInfoDTO dto = attributeList.get(0);
        String viwName = "viw_" + dto.getModelId() + "_" + dto.getEntityId();

        // 判断视图是否存在
        boolean exits = this.isExits(sqlBuilder, abstractDbHelper, connection, viwName);
        if (exits == true) {
            // 1.创建Sql
            String dropTableSql = sqlBuilder.dropViw(viwName);
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
    public void updateMdmTable(AbstractDbHelper abstractDbHelper, Connection connection,
                               IBuildSqlCommand sqlBuilder, List<AttributeInfoDTO> attributeList) {
        // 表名
        AttributeInfoDTO dto = attributeList.get(0);
        String tableName = "mdm_" + dto.getModelId() + "_" + dto.getEntityId();

        List<AttributeStatusDTO> dtoList = new ArrayList<>();
        for (AttributeInfoDTO infoDto : attributeList) {

            String sql = null;
            AttributeStatusDTO status = new AttributeStatusDTO();
            status.setId(infoDto.getId());
            status.setColumnName("column_" + infoDto.getEntityId() + "_" + infoDto.getId());

            try {

                if (infoDto.getStatus().equals(INSERT.getName())) {
                    // 新增字段
                    String filedType = this.getDataType(infoDto.getDataType(), infoDto.getDataTypeLength());

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
                    // 2.执行Sql
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.execute();
                } else if (infoDto.getStatus().equals(UPDATE.getName())) {
                    // 修改字段
                    String filedType = this.getDataType(infoDto.getDataType(), infoDto.getDataTypeLength());

                    // 1.修改字段类型
                    sql = sqlBuilder.modifyFieldType(tableName, infoDto.getColumnName(), filedType);
                    // 2.执行Sql
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.execute();
                    // 2.修改字段长度
                    if (infoDto.getDataType().equals("文本")) {
                        sql = sqlBuilder.modifyFieldLength(tableName, infoDto.getColumnName(), filedType);
                        // 2.执行Sql
                        PreparedStatement statement1 = connection.prepareStatement(sql);
                        statement1.execute();
                    }

                    // 3.修改字段是否必填
                    PreparedStatement preparedStatement = null;
                    Boolean enableRequired = infoDto.getEnableRequired();
                    if (enableRequired == true) {
                        sql = sqlBuilder.notNullable(tableName, infoDto.getColumnName());
                    } else {
                        sql = sqlBuilder.nullable(tableName, infoDto.getColumnName());
                    }
                    // 2.执行Sql
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                } else if (infoDto.getStatus().equals(DELETE.getName())) {
                    // 删除字段
                    sql = sqlBuilder.deleteFiled(tableName, infoDto.getColumnName());
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.execute();
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
                dtoList.add(status);
                log.error("修改Mdm表失败,异常信息:" + ex);
                throw new FkException(ResultEnum.UPDATE_MDM_TABLE);
            }
        }

        // 回写属性状态
        this.exceptionAttributeProcess(dtoList);
    }

    /**
     * 返回字段类型
     *
     * @param dataType
     * @param dataTypeLength
     * @return
     */
    public String getDataType(String dataType, Integer dataTypeLength) {
        if (dataType != null) {
            String filedType = null;
            switch (dataType) {
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
                    filedType = "numeric(12,2)";
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
            sql = this.createStgTable(abstractDbHelper, sqlBuilder, connection, entityInfoVo);
            PreparedStatement stemStg = connection.prepareStatement(sql);
            stemStg.execute();

            // 2.创建mdm表
            sql = this.createMdmTable(abstractDbHelper, sqlBuilder, connection, entityInfoVo);
            PreparedStatement stemMdm = connection.prepareStatement(sql);
            stemMdm.execute();

            // 3.回写columnName
            this.writableColumnName(entityInfoVo.getAttributeList());

            // 4.创建view视图
            sql = this.buildViewTable(entityInfoVo);
            PreparedStatement stemViw = connection.prepareStatement(sql);
            stemViw.execute();

            // e.提交事务
            connection.commit();

            // 5.回写成功属性状态
            this.writableAttributeStatus(entityInfoVo.getAttributeList());
            // 6.回写实体状态
            this.writableEntityStatus(entityInfoVo);
        } catch (Exception ex) {
            // a.回滚事务
            rollbackConnection(connection);
            // b.回写失败状态
            this.exceptionProcess(entityInfoVo, ex, ResultEnum.CREATE_TABLE_ERROR.getMsg() + "【执行Sql】:" + sql
                    + "【原因】:" + ex.getMessage());
        }
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
    public void writableEntityStatus(EntityInfoVO entityInfoVo) {
        UpdateEntityDTO dto = new UpdateEntityDTO();
        dto.setId(entityInfoVo.getId());
        dto.setStatus(1);
        dto.setTableName("mdm_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId());
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
     *
     * @param abstractDbHelper
     * @param sqlBuilder
     * @param connection
     * @param entityInfoVo
     */
    public String createStgTable(AbstractDbHelper abstractDbHelper, IBuildSqlCommand sqlBuilder,
                                 Connection connection, EntityInfoVO entityInfoVo) {

        String buildStgTableSql = null;
        try {
            // 1.生成Sql
            buildStgTableSql = sqlBuilder.buildStgTable(entityInfoVo);
            // 2.执行sql
            return buildStgTableSql;
        } catch (Exception ex) {

            // 筛选属性出去已提交
            List<AttributeInfoDTO> noSubmitAttributeList = entityInfoVo.getAttributeList().stream().filter(e -> !e.getStatus().equals(AttributeStatusEnum.SUBMITTED.getName()))
                    .collect(Collectors.toList());

            // 回写失败属性信息
            this.exceptionAttributeProcess(noSubmitAttributeList, ResultEnum.CREATE_STG_TABLE.getMsg()
                    + "【执行SQL】" + buildStgTableSql + "【原因】:" + ex);
            log.error("创建Stg表失败,异常信息:" + ex);
            throw new FkException(ResultEnum.CREATE_STG_TABLE);
        }
    }

    /**
     * 创建mdm表
     *
     * @param abstractDbHelper
     * @param sqlBuilder
     * @param connection
     * @param entityInfoVo
     */
    public String createMdmTable(AbstractDbHelper abstractDbHelper, IBuildSqlCommand sqlBuilder,
                                 Connection connection, EntityInfoVO entityInfoVo) {

        String buildStgTableSql = null;
        try {
            // 1.生成Sql
            buildStgTableSql = sqlBuilder.buildMdmTable(entityInfoVo);
            // 2.执行sql
            return buildStgTableSql;
        } catch (Exception ex) {
            closeConnection(connection);

            // 回写失败信息
            this.exceptionProcess(entityInfoVo, ex, ResultEnum.CREATE_MDM_TABLE.getMsg()
                    + "【执行的SQL】" + buildStgTableSql + "【原因】:" + ex);
            log.error("创建Mdm表失败,异常信息:" + ex);
            throw new FkException(ResultEnum.CREATE_MDM_TABLE);
        }
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
            case "新增待提交":
                return AttributeStatusEnum.INSERT.getValue();
            case "修改待提交":
                return UPDATE.getValue();
            case "删除待提交":
                return AttributeStatusEnum.DELETE.getValue();
            case "已提交":
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
            buildStgTableSql = this.buildViewTable(entityInfoVo);
            // 2.执行sql
            PreparedStatement statement = connection.prepareStatement(buildStgTableSql);
            statement.execute();
        } catch (Exception ex) {

            // 筛选属性出去已提交
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
    public String buildViewTable(EntityInfoVO entityInfoVo) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE VIEW " + PUBLIC + ".");
        str.append("viw_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId());
        str.append(" AS ").append("SELECT ");

        List<AttributeInfoDTO> attributeList = null;
        if (entityInfoVo.getStatus().equals(NOT_CREATED.getName())) {
            List<Integer> ids = entityInfoVo.getAttributeList().stream().filter(e -> e.getId() != null).map(e -> e.getId()).collect(Collectors.toList());
            attributeList = mdmClient.getByIds(ids).getData();
        } else {
            attributeList = entityInfoVo.getAttributeList();
        }

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

        // 获取主表表名
        AttributeInfoDTO dto = noForeignList.get(1);
        str.append(this.splicingViewTable(true));
        str.append(noForeign).append(",");
        str.append(foreign).append(",");

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
        return str.toString();
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
        List<Integer> ids = noForeignList.stream().filter(e -> e.getId() != null).map(e -> e.getId()).collect(Collectors.toList());
        List<AttributeInfoDTO> list = mdmClient.getByIds(ids).getData();

        StringBuilder str = new StringBuilder();
        // 视图基础字段
        str.append(this.splicingViewTable(false));

        String collect = list.stream().filter(e -> !e.getStatus().equals("删除待提交")).map(e -> {
            String str1 = e.getColumnName() + " AS " + e.getName();
            return str1;
        }).collect(Collectors.joining(","));

        BuildPgCommandImpl buildPgCommand = new BuildPgCommandImpl();
        AttributeInfoDTO infoDto = noForeignList.get(1);
        // 业务字段
        str.append(collect).append(",");
        // 追加基础字段
        str.append(buildPgCommand.createViw());
        str.deleteCharAt(str.length() - 1);
        str.append(" FROM " + "mdm_" + infoDto.getModelId() + "_" + infoDto.getEntityId());
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
