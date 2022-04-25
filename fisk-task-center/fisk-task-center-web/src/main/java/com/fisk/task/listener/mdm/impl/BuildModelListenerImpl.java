package com.fisk.task.listener.mdm.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
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
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.task.dto.model.EntityDTO;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.task.listener.mdm.BuildModelListener;
import com.fisk.task.utils.mdmBEBuild.BuildFactoryHelper;
import com.fisk.task.utils.mdmBEBuild.IBuildSqlCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ch.qos.logback.core.db.DBHelper.closeConnection;
import static com.fisk.mdm.enums.AttributeStatusEnum.UPDATE;
import static com.fisk.mdm.enums.MdmStatusTypeEnum.*;
import static com.fisk.task.utils.mdmBEBuild.impl.BuildPgCommandImpl.PUBLIC;
import static com.fisk.task.utils.mdmBEBuild.impl.BuildPgCommandImpl.PRIMARY_TABLE;

/**
 * Description: 创建模型日志表
 *
 * @author wangyan
 */
@Component
@Slf4j
public class BuildModelListenerImpl implements BuildModelListener {

    DataSourceTypeEnum type = DataSourceTypeEnum.PG;
    String connectionStr = "jdbc:postgresql://192.168.1.250:5432/dmp_mdm?stringtype=unspecified";
    String acc = "postgres";
    String pwd = "Password01!";

    @Resource
    MdmClient mdmClient;

    @Override
    public void msg(String dataInfo, Acknowledgment acke) {

        try {
            // 获取需要创建的表名
            ModelDTO model = JSON.parseObject(dataInfo, ModelDTO.class);
            String tableName = model.getAttributeLogName();

            // 工厂
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);

            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = abstractDbHelper.connection(connectionStr, acc, pwd, type);
            // 执行sql
            abstractDbHelper.executeSql(sqlBuilder.buildAttributeLogTable(tableName), connection);
            log.info("创建属性日志表名:" + tableName);
        } catch (Exception e) {
            log.error("创建属性日志表名失败,异常信息:" + e);
            e.printStackTrace();
        } finally {
            acke.acknowledge();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void backgroundCreateTasks(String dataInfo, Acknowledgment acke) {

        try {
            EntityDTO dto = JSON.parseObject(dataInfo, EntityDTO.class);

            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            // 获取连接信息
            Connection connection = abstractDbHelper.connection(connectionStr, acc, pwd, type);

            // 获取实体信息
            EntityInfoVO entityInfoVo = mdmClient.getAttributeById(dto.getEntityId()).getData();
            String status = entityInfoVo.getStatus();
            if (status.equals(NOT_CREATED.getName())){
                // 1.执行创建表任务
                this.createTable(abstractDbHelper,sqlBuilder,connection,entityInfoVo);
                // 2.回写成功属性状态
                this.writableAttributeStatus(entityInfoVo.getAttributeList());
                // 3.创建view视图
                this.createViwTable(abstractDbHelper,sqlBuilder, connection, entityInfoVo);
                // 4.回写实体状态
                this.writableEntityStatus(entityInfoVo);
            }else if (status.equals(CREATED_SUCCESSFULLY.getName())){
                // 1.stg表删了重新生成
                this.updateStgTable(abstractDbHelper,connection, sqlBuilder,entityInfoVo);
                // 2.mdm表更新
                this.updateMdmTable(abstractDbHelper,connection, sqlBuilder,entityInfoVo.getAttributeList());
                // 3.viw视图重新生成
                this.updateViwTable(abstractDbHelper, sqlBuilder, connection,entityInfoVo);
                // 4.回写成功属性状态
                this.writableAttributeStatus(entityInfoVo.getAttributeList());
            }

        } catch (Exception ex) {
            log.error("创建后台任务表失败,异常信息:" + ex);
            ex.printStackTrace();
        } finally {
            acke.acknowledge();
        }
    }

    /**
     * Viw 视图重新生成
     * @param abstractDbHelper
     * @param sqlBuilder
     * @param connection
     * @param entityInfoVo
     */
    public void updateViwTable(AbstractDbHelper abstractDbHelper, IBuildSqlCommand sqlBuilder,
                               Connection connection, EntityInfoVO entityInfoVo) throws Exception {
        // 1.创建view视图
        this.createViwTable(abstractDbHelper,sqlBuilder, connection, entityInfoVo);
    }

    /**
     * stg表删除重新生成
     * @param abstractDbHelper
     * @param connection
     * @param sqlBuilder
     * @param entityInfoVo
     */
    public void updateStgTable(AbstractDbHelper abstractDbHelper,Connection connection,
                      IBuildSqlCommand sqlBuilder,EntityInfoVO entityInfoVo) throws Exception{
        // 1.删除视图
        this.dropViwTable(abstractDbHelper,connection, sqlBuilder,entityInfoVo.getAttributeList());
        // 2.删除stg表
        this.dropStgTable(abstractDbHelper, connection, sqlBuilder, entityInfoVo.getAttributeList());
        // 3.创建Stg表
        this.createStgTable(abstractDbHelper,sqlBuilder, connection, entityInfoVo);
    }

    /**
     * 删除stg表
     * @param abstractDbHelper
     * @param connection
     * @param sqlBuilder
     * @param attributeList
     */
    public void dropStgTable(AbstractDbHelper abstractDbHelper,Connection connection,
                      IBuildSqlCommand sqlBuilder,List<AttributeInfoDTO> attributeList){
        try{
            // 表名
            AttributeInfoDTO dto = attributeList.get(0);
            String tableName = "stg_" + dto.getModelId() + "_" + dto.getEntityId();

            // 1.创建Sql
            String dropTableSql = sqlBuilder.dropTable(tableName);
            // 2.执行Sql
            abstractDbHelper.executeSql(dropTableSql, connection);
        }catch (Exception ex){
            closeConnection(connection);

            // 回写失败属性信息
            this.exceptionAttributeProcess(attributeList, ResultEnum.DROP_STG_TABLE.getMsg() + "原因:" + ex);
            log.error("删除Stg表失败,异常信息:" + ex);
            throw new FkException(ResultEnum.DROP_STG_TABLE);
        }
    }


    /**
     * 删除Viw视图
     * @param abstractDbHelper
     * @param connection
     * @param sqlBuilder
     * @param attributeList
     */
    public void dropViwTable(AbstractDbHelper abstractDbHelper,Connection connection,
                             IBuildSqlCommand sqlBuilder,List<AttributeInfoDTO> attributeList){
        try{
            // 表名
            AttributeInfoDTO dto = attributeList.get(0);
            String viwName = "viw_" + dto.getModelId() + "_" + dto.getEntityId();

            // 1.创建Sql
            String dropTableSql = sqlBuilder.dropViw(viwName);
            // 2.执行Sql
            abstractDbHelper.executeSql(dropTableSql, connection);
        }catch (Exception ex){
            closeConnection(connection);

            // 回写失败属性信息
            this.exceptionAttributeProcess(attributeList, ResultEnum.DROP_VIW_TABLE.getMsg() + "原因:" + ex);
            log.error("删除Viw视图失败,异常信息:" + ex);
            throw new FkException(ResultEnum.DROP_VIW_TABLE);
        }
    }

    /**
     * 更新mdm表
     * @param sqlBuilder
     * @param attributeList
     */
    public void updateMdmTable(AbstractDbHelper abstractDbHelper,Connection connection,
                            IBuildSqlCommand sqlBuilder,List<AttributeInfoDTO> attributeList){
        try{
            // 表名
            AttributeInfoDTO dto = attributeList.get(0);
            String tableName = "mdm_" + dto.getModelId() + "_" + dto.getEntityId();

            attributeList.stream().filter(e -> e.getStatus().equals(UPDATE.getName()))
                    .forEach(e -> {

                        String filedType = null;
                        String filedTypeLength = null;
                        switch (e.getDataType()){
                            case "数值":
                                filedType = "int4";
                                filedTypeLength = "int4";
                                break;
                            default:
                                filedType = "varchar";
                                filedTypeLength = "varchar(" + e.getDataTypeLength() + ")";
                        }

                        // 1.修改字段类型
                        String modifyFieldType = sqlBuilder.modifyFieldType(tableName, e.getColumnName(), filedType);
                        abstractDbHelper.executeSql(modifyFieldType, connection);
                        // 2.修改字段长度
                        String fieldLength = sqlBuilder.modifyFieldLength(tableName, e.getColumnName(), filedTypeLength);
                        abstractDbHelper.executeSql(fieldLength, connection);
                    });
        }catch (Exception ex){
            closeConnection(connection);

            // 回写失败属性信息
            this.exceptionAttributeProcess(attributeList, ResultEnum.UPDATE_MDM_TABLE.getMsg() + "原因:" + ex);
            log.error("修改Mdm表失败,异常信息:" + ex);
            throw new FkException(ResultEnum.UPDATE_MDM_TABLE);
        }
    }

    /**
     * 执行创建表任务
     * @param entityInfoVo
     */
    public void createTable(AbstractDbHelper abstractDbHelper, IBuildSqlCommand sqlBuilder,
                            Connection connection,EntityInfoVO entityInfoVo) throws Exception {

        // 1.创建Stg表
        this.createStgTable(abstractDbHelper,sqlBuilder, connection, entityInfoVo);
        // 2.创建mdm表
        this.createMdmTable(abstractDbHelper,sqlBuilder, connection, entityInfoVo);
    }

    /**
     * 回写实体失败信息
     * @param entityInfoVo
     */
    public void exceptionEntityProcess(EntityInfoVO entityInfoVo,Exception ex){
        UpdateEntityDTO dto = new UpdateEntityDTO();
        dto.setId(entityInfoVo.getId());
        dto.setStatus(2);
        mdmClient.update(dto);
        log.error("创建实体失败,异常信息:" + ex);
    }

    /**
     * 回写实体状态
     * @param entityInfoVo
     */
    public void writableEntityStatus(EntityInfoVO entityInfoVo){
        UpdateEntityDTO dto = new UpdateEntityDTO();
        dto.setId(entityInfoVo.getId());
        dto.setStatus(1);
        mdmClient.update(dto);
    }

    /**
     * 回写成功属性状态
     */
    public void writableAttributeStatus(List<AttributeInfoDTO> attributeList){
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
            if (result.getData() == ResultEnum.UPDATE_DATA_ERROR){
                // todo
                throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
            }
        });
    }

    /**
     * 创建Stg表
     * @param abstractDbHelper
     * @param sqlBuilder
     * @param connection
     * @param entityInfoVo
     */
    public void createStgTable(AbstractDbHelper abstractDbHelper, IBuildSqlCommand sqlBuilder,
                               Connection connection, EntityInfoVO entityInfoVo){
        try{
            // 1.生成Sql
            String buildStgTableSql = sqlBuilder.buildStgTable(entityInfoVo);
            // 2.执行sql
            abstractDbHelper.executeSql(buildStgTableSql, connection);
        }catch (Exception ex){
            closeConnection(connection);

            // 回写失败信息
            this.exceptionProcess(entityInfoVo,ex,ResultEnum.CREATE_STG_TABLE.getMsg() + "原因:" + ex);
            log.error("创建Stg表失败,异常信息:" + ex);
            throw new FkException(ResultEnum.CREATE_STG_TABLE);
        }
    }

    /**
     * 创建mdm表
     * @param abstractDbHelper
     * @param sqlBuilder
     * @param connection
     * @param entityInfoVo
     */
    public void createMdmTable(AbstractDbHelper abstractDbHelper, IBuildSqlCommand sqlBuilder,
                               Connection connection, EntityInfoVO entityInfoVo){
        try{
            // 1.生成Sql
            String buildStgTableSql = sqlBuilder.buildMdmTable(entityInfoVo);
            // 2.执行sql
            abstractDbHelper.executeSql(buildStgTableSql, connection);
        }catch (Exception ex){
            closeConnection(connection);

            // 回写失败信息
            this.exceptionProcess(entityInfoVo,ex,ResultEnum.CREATE_MDM_TABLE.getMsg() + "原因:" + ex);
            log.error("创建Mdm表失败,异常信息:" + ex);
            throw new FkException(ResultEnum.CREATE_MDM_TABLE);
        }
    }

    /**
     * 回写失败信息
     * @param entityInfoVo
     * @param ex
     */
    public void exceptionProcess(EntityInfoVO entityInfoVo,Exception ex,String message){
        // 1.回写属性失败状态
        this.exceptionAttributeProcess(entityInfoVo.getAttributeList(), message);
        // 2.回写实体失败状态
        this.exceptionEntityProcess(entityInfoVo,ex);
    }

    /**
     * 回写属性失败状态
     * @param dtoList
     * @param message
     */
    public void exceptionAttributeProcess(List<AttributeInfoDTO> dtoList, String message){
        if (CollectionUtils.isEmpty(dtoList)){
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

    /**
     * 属性状态转换: status
     * @param status
     * @return
     */
    public Integer stringToStatusInt(String status){
        switch (status){
            case "待新增":
                return AttributeStatusEnum.INSERT.getValue();
            case "待修改":
                return UPDATE.getValue();
            case "已提交":
                return AttributeStatusEnum.SUBMITTED.getValue();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * 创建view视图
     * @param abstractDbHelper
     * @param sqlBuilder
     * @param connection
     * @param entityInfoVo
     */
    public void createViwTable(AbstractDbHelper abstractDbHelper, IBuildSqlCommand sqlBuilder,
                               Connection connection, EntityInfoVO entityInfoVo){
        try{
            // 1.生成Sql
            String buildStgTableSql = this.buildViewTable(entityInfoVo);
            // 2.执行sql
            abstractDbHelper.executeSql(buildStgTableSql, connection);
        }catch (Exception ex){
            closeConnection(connection);

            // 回写失败信息
            this.exceptionProcess(entityInfoVo,ex,ResultEnum.CREATE_VIW_TABLE.getMsg() + "原因:" + ex);
            log.error("创建Stg表失败,异常信息:" + ex);
            throw new FkException(ResultEnum.CREATE_VIW_TABLE);
        }
    }

    /**
     * 创建视图
     * @param entityInfoVo
     * @return
     */
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

        AtomicInteger amount = new AtomicInteger(0);
        String foreign = list.stream().filter(e -> e.getName() != null).map(e -> {
            // 获取域字段名称
            AttributeInfoDTO data = this.getDomainName(foreignList, e.getId());
            String str1 = null;
            if (data != null){
                str1 = PRIMARY_TABLE + amount.incrementAndGet() + "." + e.getColumnName() + " AS " + data.getName();
            }
            return str1;
        }).collect(Collectors.joining(","));

        // 获取主表表名
        AttributeInfoDTO dto = noForeignList.get(1);
        str.append(this.splicingViewTable(true));
        str.append(noForeign).append(",");
        str.append(foreign);
        // 主表表名
        str.append(" FROM " + "mdm_" + dto.getModelId() + "_" + dto.getEntityId() + " " + PRIMARY_TABLE);

        AtomicInteger amount1 = new AtomicInteger(0);
        String leftJoin = list.stream().filter(Objects::nonNull)
                .map(e -> {

                    // 获取域字段名称
                    AttributeInfoDTO data = this.getDomainName(foreignList, e.getId());

                    String alias = PRIMARY_TABLE + amount1.incrementAndGet();
                    String tableName = "mdm_" + e.getModelId() + "_" + e.getEntityId() + " " + alias;
                    String on = " ON " + PRIMARY_TABLE + "." + "version_id" + " = " + alias + ".version_id" +
                            " AND " + PRIMARY_TABLE + "." + data.getColumnName() + " = " + alias + ".id";
                    String str1 = tableName + " " + on;
                    return str1;
                }).collect(Collectors.joining(" LEFT JOIN "));

        str.append(" LEFT JOIN " + leftJoin);
        return str.toString();
    }

    /**
     * 获取域字段名称
     * @param foreignList
     * @param id
     * @return
     */
    public AttributeInfoDTO getDomainName(List<AttributeInfoDTO> foreignList,Integer id){
        AttributeDomainDTO dto1 = new AttributeDomainDTO();
        dto1.setEntityId(foreignList.get(0).getEntityId());
        dto1.setDomainId(id-1);
        AttributeInfoDTO data = mdmClient.getByDomainId(dto1).getData();
        return data;
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
     * View 视图表基础字段拼接
     * @return
     */
    public String splicingViewTable(boolean isDomain){
        StringBuilder str = new StringBuilder();
        if (isDomain == false){
            str.append("id").append(",");
            str.append("version_id").append(",");
        }else{
            str.append(PRIMARY_TABLE + "." + "id").append(",");
            str.append(PRIMARY_TABLE + "." + "version_id").append(",");
        }
        return str.toString();
    }
}
