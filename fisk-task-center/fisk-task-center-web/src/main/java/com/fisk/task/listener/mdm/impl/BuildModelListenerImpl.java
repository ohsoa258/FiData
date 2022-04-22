package com.fisk.task.listener.mdm.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.mdm.client.MdmClient;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
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
import java.util.stream.Collectors;

import static ch.qos.logback.core.db.DBHelper.closeConnection;
import static com.fisk.mdm.enums.MdmStatusTypeEnum.NOT_CREATED;

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
            // 获取实体信息
            EntityInfoVO entityInfoVo = mdmClient.getAttributeById(dto.getEntityId()).getData();
            String status = entityInfoVo.getStatus();
            if (status.equals(NOT_CREATED.getName())){
                // 实体未创建
                // 执行创建表任务
                this.createTable(entityInfoVo);
            }

        } catch (Exception ex) {
            log.error("创建后台任务表失败,异常信息:" + ex);
            ex.printStackTrace();
        } finally {
            acke.acknowledge();
        }
    }

    /**
     * 执行创建表任务
     * @param entityInfoVo
     */
    public void createTable(EntityInfoVO entityInfoVo){
        try{
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = abstractDbHelper.connection(connectionStr, acc, pwd, type);
            // 1.创建Stg表
            this.createStgTable(abstractDbHelper,sqlBuilder, connection, entityInfoVo);
            // 2.创建mdm表
            this.createMdmTable(abstractDbHelper,sqlBuilder, connection, entityInfoVo);
            // 3.回写成功属性状态
            this.writableAttributeStatus(entityInfoVo.getAttributeList());
            // 4.创建view视图
            this.createViwTable(abstractDbHelper,sqlBuilder, connection, entityInfoVo);
            // 5.回写实体状态
            this.writableEntityStatus(entityInfoVo);
        }catch (Exception ex){
            UpdateEntityDTO dto = new UpdateEntityDTO();
            dto.setId(entityInfoVo.getId());
            dto.setStatus(2);
            mdmClient.update(dto);

            log.error("创建实体失败,异常信息:" + ex);
            ex.printStackTrace();
        }
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
        List<AttributeUpdateDTO> dtoList = attributeList.stream().filter(Objects::nonNull)
                .map(e -> {
                    AttributeUpdateDTO dto = new AttributeUpdateDTO();
                    dto.setId(e.getId());
                    dto.setColumnName("column_" + e.getEntityId() + "_" + e.getId());
                    dto.setStatus(2);
                    dto.setSyncStatus(1);

                    return dto;
                }).collect(Collectors.toList());

        dtoList.stream().forEach(e -> {
            ResultEntity<ResultEnum> result = mdmClient.update(e);
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
            log.error("创建Stg表失败,异常信息:" + ex);
            ex.printStackTrace();
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
            // 回写属性失败状态
            entityInfoVo.getAttributeList().stream().filter(Objects::nonNull)
                    .forEach(e -> {
                        AttributeUpdateDTO dto = new AttributeUpdateDTO();
                        dto.setId(e.getId());
                        dto.setColumnName("column_" + e.getEntityId() + "_" + e.getId());
                        dto.setStatus(this.stringToStatusInt(e.getStatus()));
                        dto.setSyncStatus(1);
                        dto.setErrorMsg(ex.getMessage());
                        mdmClient.update(dto);
                    });

            log.error("创建Mdm表失败,异常信息:" + ex);
            ex.printStackTrace();
        }
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
                return AttributeStatusEnum.UPDATE.getValue();
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
            String buildStgTableSql = sqlBuilder.buildViewTable(entityInfoVo);
            // 2.执行sql
            abstractDbHelper.executeSql(buildStgTableSql, connection);
        }catch (Exception ex){
            closeConnection(connection);
            log.error("创建Viw视图表失败,异常信息:" + ex);
            ex.printStackTrace();
        }
    }
}
