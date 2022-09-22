package com.fisk.task.extend.actuators;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.utils.StackTraceHelper;
import com.fisk.task.utils.nifi.INiFiHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @author cfk
 */
@Component
@Order(value = 2)
@Slf4j
public class DefiningCommonVariablesImpl implements ApplicationRunner {
    @Resource
    INiFiHelper iNiFiHelper;
    @Value("${pgsql-datamodel.url}")
    private String pgsqlDatamodelUrl;
    @Value("${pgsql-datamodel.username}")
    private String pgsqlDatamodelUsername;
    @Value("${pgsql-datamodel.password}")
    private String pgsqlDatamodelPassword;
    @Value("${pgsql-datainput.url}")
    private String pgsqlDatainputUrl;
    @Value("${pgsql-datainput.username}")
    private String pgsqlDatainputUsername;
    @Value("${pgsql-datainput.password}")
    private String pgsqlDatainputPassword;
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String KafkaBrokers;
    @Value("${spring.datasource.dynamic.datasource.taskdb.url}")
    private String jdbcStr;
    @Value("${spring.datasource.dynamic.datasource.taskdb.username}")
    private String user;
    @Value("${spring.datasource.dynamic.datasource.taskdb.password}")
    private String password;
    @Value("${spring.datasource.dynamic.datasource.taskdb.driver-class-name}")
    private String driverClassName;
    @Value("${datamodeldorisconstr.url}")
    private String dorisUrl;
    @Value("${datamodeldorisconstr.username}")
    private String dorisUser;
    @Value("${datamodeldorisconstr.password}")
    private String dorisPwd;
    @Value("${datamodeldorisconstr.driver_class_name}")
    private String dorisDriver;
    @Resource
    UserClient userClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("第一次启动项目创建常用变量,没有就创建");
        try {
            HashMap<String, String> configMap = new HashMap<>();
            //卡夫卡
            configMap.put(ComponentIdTypeEnum.KAFKA_BROKERS.getName(), KafkaBrokers);
            //查询类型,配置库
            configMap.put(ComponentIdTypeEnum.CFG_DB_POOL_PASSWORD.getName(), password);
            configMap.put(ComponentIdTypeEnum.CFG_DB_POOL_USERNAME.getName(), user);
            configMap.put(ComponentIdTypeEnum.CFG_DB_POOL_URL.getName(), jdbcStr);
            //pg-ods
            ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(5);
            configMap.put(ComponentIdTypeEnum.PG_ODS_DB_POOL_PASSWORD.getName(), pgsqlDatainputPassword);
            configMap.put(ComponentIdTypeEnum.PG_ODS_DB_POOL_USERNAME.getName(), pgsqlDatainputUsername);
            configMap.put(ComponentIdTypeEnum.PG_ODS_DB_POOL_URL.getName(), pgsqlDatainputUrl);
            if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO data = fiDataDataSource.data;
                configMap.put(ComponentIdTypeEnum.PG_ODS_DB_POOL_PASSWORD.getName(), data.conPassword);
                configMap.put(ComponentIdTypeEnum.PG_ODS_DB_POOL_USERNAME.getName(), data.conAccount);
                configMap.put(ComponentIdTypeEnum.PG_ODS_DB_POOL_URL.getName(), data.conStr);
            }


            //pg-dw
            configMap.put(ComponentIdTypeEnum.PG_DW_DB_POOL_PASSWORD.getName(), pgsqlDatamodelPassword);
            configMap.put(ComponentIdTypeEnum.PG_DW_DB_POOL_USERNAME.getName(), pgsqlDatamodelUsername);
            configMap.put(ComponentIdTypeEnum.PG_DW_DB_POOL_URL.getName(), pgsqlDatamodelUrl);
            //doris-olap
            configMap.put(ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_PASSWORD.getName(), dorisPwd);
            configMap.put(ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_USERNAME.getName(), dorisUser);
            configMap.put(ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_URL.getName(), dorisUrl);
            iNiFiHelper.buildNifiGlobalVariable(configMap);
            log.info("创建变量完成");
        } catch (Exception e) {
            log.error("创建常量报错:" + StackTraceHelper.getStackTraceInfo(e));
        }
    }
}
