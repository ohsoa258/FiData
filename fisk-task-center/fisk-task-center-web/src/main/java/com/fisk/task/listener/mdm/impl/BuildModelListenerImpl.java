package com.fisk.task.listener.mdm.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.mdm.client.MdmClient;
import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.enums.MdmStatusTypeEnum;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.task.dto.model.EntityDTO;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.task.listener.mdm.BuildModelListener;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.BuildFactoryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import static com.fisk.mdm.enums.AttributeStatusEnum.INSERT;
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

    @Override
    public void backgroundCreateTasks(String dataInfo, Acknowledgment acke) {

        try {
            EntityDTO dto = JSON.parseObject(dataInfo, EntityDTO.class);
            // 获取实体信息
            EntityVO entityVo = mdmClient.getAttributeById(dto.getEntityId()).getData();
            List<AttributeDTO> attributeList = entityVo.getAttributeList();
            MdmStatusTypeEnum status = entityVo.getStatus();
            if (status.equals(NOT_CREATED)){
                // 实体未创建
                // 工厂
                IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);

                // todo 保证事务一致性
                // 创建stg表
                String buildStgTableSql = sqlBuilder.buildStgTable(entityVo);
                // 创建mdm表
                String buildMdmTableSql = sqlBuilder.buildMdmTable(entityVo);
                // 创建view视图
                String buildViewTableSql = sqlBuilder.buildViewTable(entityVo);
            }

        } catch (Exception ex) {
            log.error("创建后台任务表失败,异常信息:" + ex);
            ex.printStackTrace();
        } finally {
            acke.acknowledge();
        }
    }
}
