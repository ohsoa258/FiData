package com.fisk.task.listener.mdm.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.mdm.client.MdmClient;
import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.enums.AttributeSyncStatusEnum;
import com.fisk.mdm.enums.MdmStatusTypeEnum;
import com.fisk.mdm.vo.entity.EntityVO;
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

import static com.fisk.mdm.enums.AttributeStatusEnum.SUBMITTED;
import static com.fisk.mdm.enums.AttributeSyncStatusEnum.SUCCESS;
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
    String connectionStr = "jdbc:postgresql://192.168.1.250:5432/dmp_mdm_attributeLog?stringtype=unspecified";
    String connectionStr1 = "jdbc:postgresql://192.168.1.250:5432/dmp_mdm_backstageTable?stringtype=unspecified";
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
            EntityVO entityVo = mdmClient.getAttributeById(dto.getEntityId()).getData();
            MdmStatusTypeEnum status = entityVo.getStatus();
            if (status.equals(NOT_CREATED)){
                // 实体未创建
                // 执行创建表任务
                this.createTable(entityVo);
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
     * @param entityVo
     */
    public void createTable(EntityVO entityVo){
        try{
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = abstractDbHelper.connection(connectionStr1, acc, pwd, type);
            // 1.创建Stg表
            this.createStgTable(abstractDbHelper,sqlBuilder, connection,entityVo);
            // 2.创建mdm表
            this.createMdmTable(abstractDbHelper,sqlBuilder, connection,entityVo);
            // 3.回写属性状态
            this.writableAttributeStatus(entityVo.getAttributeList());
            // 4.创建view视图
            this.createViwTable(abstractDbHelper,sqlBuilder, connection,entityVo);
            // 5.回写实体状态
            this.writableEntityStatus(entityVo);
        }catch (Exception ex){
            UpdateEntityDTO dto = new UpdateEntityDTO();
            dto.setId(entityVo.getId());
            dto.setStatus(MdmStatusTypeEnum.CREATED_FAIL);
            // todo
            mdmClient.updateData(dto);

            log.error("创建实体失败,异常信息:" + ex);
            ex.printStackTrace();
        }
    }

    /**
     * 回写实体状态
     * @param entityVo
     */
    public void writableEntityStatus(EntityVO entityVo){
        UpdateEntityDTO dto = new UpdateEntityDTO();
        dto.setId(entityVo.getId());
        dto.setStatus(MdmStatusTypeEnum.CREATED_SUCCESSFULLY);
        mdmClient.updateData(dto);
    }

    /**
     * 回写属性状态
     */
    public void writableAttributeStatus(List<AttributeDTO> attributeList){
        List<AttributeUpdateDTO> dtoList = attributeList.stream().filter(Objects::nonNull)
                .map(e -> {
                    AttributeUpdateDTO dto = new AttributeUpdateDTO();
                    dto.setId(e.getId());
                    dto.setColumnName("column_" + e.getEntityId() + "_" + e.getId());
                    dto.setStatus(SUBMITTED);
                    dto.setSyncStatus(SUCCESS);

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
     * @param entityVo
     */
    public void createStgTable(AbstractDbHelper abstractDbHelper,IBuildSqlCommand sqlBuilder,
                      Connection connection,EntityVO entityVo){
        try{
            // 1.生成Sql
            String buildStgTableSql = sqlBuilder.buildStgTable(entityVo);
            // 2.执行sql
            abstractDbHelper.executeSql(buildStgTableSql, connection);
        }catch (Exception ex){
            log.error("创建Stg表失败,异常信息:" + ex);
            ex.printStackTrace();
        }
    }

    /**
     * 创建mdm表
     * @param abstractDbHelper
     * @param sqlBuilder
     * @param connection
     * @param entityVo
     */
    public void createMdmTable(AbstractDbHelper abstractDbHelper,IBuildSqlCommand sqlBuilder,
                               Connection connection,EntityVO entityVo){
        try{
            // 1.生成Sql
            String buildStgTableSql = sqlBuilder.buildMdmTable(entityVo);
            // 2.执行sql
            abstractDbHelper.executeSql(buildStgTableSql, connection);
        }catch (Exception ex){
            // 回写属性失败状态
            entityVo.getAttributeList().stream().filter(Objects::nonNull)
                    .forEach(e -> {
                        AttributeUpdateDTO dto = new AttributeUpdateDTO();
                        dto.setId(e.getId());
                        dto.setColumnName("column_" + e.getEntityId() + "_" + e.getId());
                        dto.setStatus(e.getStatus());
                        dto.setSyncStatus(AttributeSyncStatusEnum.ERROR);
                        dto.setErrorMsg(ex.getMessage());
                    });

            log.error("创建Mdm表失败,异常信息:" + ex);
            ex.printStackTrace();
        }
    }

    /**
     * 创建view视图
     * @param abstractDbHelper
     * @param sqlBuilder
     * @param connection
     * @param entityVo
     */
    public void createViwTable(AbstractDbHelper abstractDbHelper,IBuildSqlCommand sqlBuilder,
                               Connection connection,EntityVO entityVo){
        try{
            // 1.生成Sql
            String buildStgTableSql = sqlBuilder.buildViewTable(entityVo);
            // 2.执行sql
            abstractDbHelper.executeSql(buildStgTableSql, connection);
        }catch (Exception ex){
            log.error("创建Viw视图表失败,异常信息:" + ex);
            ex.printStackTrace();
        }
    }
}
