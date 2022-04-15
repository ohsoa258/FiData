package com.fisk.task.listener.mdm.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.task.listener.mdm.BuildModelListener;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.BuildFactoryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.sql.Connection;

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
            log.error("创建属性日志表名");
            e.printStackTrace();
        } finally {
            acke.acknowledge();
        }
    }
}
