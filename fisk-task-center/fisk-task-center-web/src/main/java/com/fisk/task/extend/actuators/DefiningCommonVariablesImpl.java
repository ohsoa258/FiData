package com.fisk.task.extend.actuators;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.davis.client.model.ControllerServiceEntity;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.nifi.BuildKeytabCredentialsServiceDTO;
import com.fisk.task.po.app.NifiConfigPO;
import com.fisk.task.service.pipeline.impl.NifiConfigServiceImpl;
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
import java.util.List;
import java.util.Objects;

/**
 * @author cfk
 */
@Component
@Order(value = 2)
@Slf4j
public class DefiningCommonVariablesImpl implements ApplicationRunner {
    @Resource
    INiFiHelper iNiFiHelper;
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
    @Value("${fiData-data-ods-source}")
    private String dataSourceOdsId;
    @Value("${fiData-data-dw-source}")
    private String dataSourceDwId;
    @Value("${nifi.Enable-Authentication}")
    public String enableAuthentication;
    @Value("${nifi.kerberos.Keytab}")
    public String kerberosKeytab;
    @Value("${nifi.kerberosprincipal}")
    public String kerberosprincipal;
    @Resource
    NifiConfigServiceImpl nifiConfigService;

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
            ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
            log.info("查询数据源:" + JSON.toJSONString(fiDataDataSource));
            if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO data = fiDataDataSource.data;
                configMap.put(ComponentIdTypeEnum.PG_ODS_DB_POOL_PASSWORD.getName(), data.conPassword);
                configMap.put(ComponentIdTypeEnum.PG_ODS_DB_POOL_USERNAME.getName(), data.conAccount);
                configMap.put(ComponentIdTypeEnum.PG_ODS_DB_POOL_URL.getName(), data.conStr);
            } else {
                log.error("userclient无法查询到ods库的连接信息");
            }

            //pg-dw
            ResultEntity<DataSourceDTO> fiDataDataDwSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
            log.info("查询数据源:" + JSON.toJSONString(fiDataDataSource));
            if (fiDataDataDwSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO data = fiDataDataDwSource.data;
                configMap.put(ComponentIdTypeEnum.PG_DW_DB_POOL_PASSWORD.getName(), data.conPassword);
                configMap.put(ComponentIdTypeEnum.PG_DW_DB_POOL_USERNAME.getName(), data.conAccount);
                configMap.put(ComponentIdTypeEnum.PG_DW_DB_POOL_URL.getName(), data.conStr);
            } else {
                log.error("userclient无法查询到dw库的连接信息");
            }


            //doris-olap
            configMap.put(ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_PASSWORD.getName(), dorisPwd);
            configMap.put(ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_USERNAME.getName(), dorisUser);
            configMap.put(ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_URL.getName(), dorisUrl);
            if (Objects.equals(enableAuthentication, NifiConstants.enableAuthentication.ENABLE)) {
                NifiConfigPO one = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.KEYTAB_CREDENTIALS_SERVICE_ID.getName()).eq("del_flag", 1).one();
                if (Objects.isNull(one)) {
                    NifiConfigPO nifiConfig = new NifiConfigPO();
                    nifiConfig.componentId = getKeytabCredentialsServiceId();
                    nifiConfig.componentKey = ComponentIdTypeEnum.KEYTAB_CREDENTIALS_SERVICE_ID.getName();
                    nifiConfig.delFlag = 1;
                    nifiConfigService.save(nifiConfig);
                }
            }

            // 设置数据源
            setDatasourceInfo(configMap);

            iNiFiHelper.buildNifiGlobalVariable(configMap);
            log.info("创建变量完成");
        } catch (Exception e) {
            log.error("创建常量报错:" + StackTraceHelper.getStackTraceInfo(e));
        }
    }

    /**
     * 添加datasource数据源变量信息（用户名、密码、字符串）
     *
     * @param configMap 常用变量map集合
     */
    private void setDatasourceInfo(HashMap<String, String> configMap){
        // 获取数据源信息
        ResultEntity<List<DataSourceDTO>> result = null;
        try{
            result = userClient.getAll();
        }catch (Exception e){
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED,e.getMessage());
        }

        // 添加数据源变量
        List<DataSourceDTO> list = result.getData();
        if (CollectionUtils.isNotEmpty(list)){
            for (DataSourceDTO item : list) {
                configMap.put(ComponentIdTypeEnum.DB_URL.getName() + item.id, item.conStr);
                configMap.put(ComponentIdTypeEnum.DB_USERNAME.getName() + item.id, item.conAccount);
                configMap.put(ComponentIdTypeEnum.DB_PASSWORD.getName() + item.id, item.conPassword);
            }
        }else{
            log.error("userclient未查询到所有的数据源信息!");
        }
    }


    public String getKeytabCredentialsServiceId() {
        BuildKeytabCredentialsServiceDTO buildKeytabCredentialsService = new BuildKeytabCredentialsServiceDTO();
        buildKeytabCredentialsService.groupId = "root";
        buildKeytabCredentialsService.kerberosKeytab = kerberosKeytab;
        buildKeytabCredentialsService.kerberosprincipal = kerberosprincipal;
        buildKeytabCredentialsService.name = "KeytabCredentialsService";
        buildKeytabCredentialsService.details = "KeytabCredentialsService";
        BusinessResult<ControllerServiceEntity> controllerServiceEntity =
                iNiFiHelper.buildKeytabCredentialsService(buildKeytabCredentialsService);
        if (controllerServiceEntity.success == true) {
            return controllerServiceEntity.data.getId();
        } else {
            log.error("创建broker认证控制器服务报错");
            return "";
        }


    }
}
