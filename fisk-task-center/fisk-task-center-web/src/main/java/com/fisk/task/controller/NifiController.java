package com.fisk.task.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessGroupStatusDTO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.accessAndModel.LogPageQueryDTO;
import com.fisk.common.service.accessAndModel.NifiLogResultDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.datafactory.dto.dataaccess.DataAccessIdDTO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.dataservice.dto.tableapi.TableApiServiceDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.dto.DwLogQueryDTO;
import com.fisk.task.dto.DwLogResultDTO;
import com.fisk.task.dto.accessmdm.LogResultDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;
import com.fisk.task.listener.nifi.*;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.service.pipeline.IEtlLog;
import com.fisk.task.service.pipeline.INifiSchedulingComponentService;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.nifi.INiFiHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
@Api(tags = {SwaggerConfig.Nifi})
@RestController
@RequestMapping("/nifi")
@Slf4j
public class NifiController {
    @Resource
    INiFiHelper iNiFiHelper;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @Resource
    INifiCustomWorkFlow iNifiCustomWorkFlow;
    @Resource
    UserClient userClient;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    ISftpDataUploadListener iSftpDataUploadListener;
    @Resource
    INifiSchedulingComponentService iNifiSchedulingComponentService;
    @Resource
    private ISapBwListener sapBwListener;
    @Resource
    private IpowerBiListener powerBiListener;
    @Resource
    private IApiListener apiListener;
    @Resource
    private ITableNifiSettingService iTableNifiSettingService;

    @Resource
    IEtlLog etlLog;

    @ApiOperation("修改调度")
    @PostMapping("/modifyScheduling")
    public ResultEntity<Object> modifyScheduling(@RequestParam("groupId") String groupId, @RequestParam("ProcessorId") String ProcessorId, @RequestParam("schedulingStrategy") String schedulingStrategy, @RequestParam("schedulingPeriod") String schedulingPeriod) {
        return ResultEntityBuild.build(iNiFiHelper.modifyScheduling(groupId, ProcessorId, schedulingStrategy, schedulingPeriod));

    }

    @ApiOperation("删除Nifi流")
    @PostMapping("/deleteNifiFlow")
    public ResultEntity<Object> deleteNifiFlow(@RequestBody DataModelVO dataModelVO) {
        return ResultEntityBuild.build(iNiFiHelper.deleteNifiFlow(dataModelVO));
    }

    @ApiOperation("获取NIFI表设置")
    @PostMapping("/getTableNifiSetting")
    public ResultEntity<TableNifiSettingPO> getTableNifiSetting(@RequestBody DataAccessIdDTO dto) {
        ResultEntity<TableNifiSettingPO> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = tableNifiSettingService.query().eq("app_id", dto.appId).eq("table_access_id", dto.tableId).eq("type", dto.olapTableEnum.getValue()).one();
        objectResultEntity.code = 0;
        return objectResultEntity;

    }

    @ApiOperation("获取Pg Ods的Sql")
    @PostMapping("/getSqlForPgOds")
    public ResultEntity<List<String>> getSqlForPgOds(@RequestBody DataAccessConfigDTO configDTO) {
        ResultEntity<List<String>> SqlForPgOds = new ResultEntity<>();
        SqlForPgOds.data = iNiFiHelper.getSqlForPgOds(configDTO);
        SqlForPgOds.code = 0;
        return SqlForPgOds;
    }

    @ApiOperation("获取DORIS Ods的Sql")
    @PostMapping("/getSqlForDorisOds")
    public ResultEntity<List<String>> getSqlForDorisOds(@RequestBody DataAccessConfigDTO configDTO) {
        ResultEntity<List<String>> SqlForPgOds = new ResultEntity<>();
        SqlForPgOds.data = iNiFiHelper.getSqlForDorisOds(configDTO);
        SqlForPgOds.code = 0;
        return SqlForPgOds;
    }

    @ApiOperation("获取Pg Ods的Sql API&WEBSERVICE")
    @PostMapping("/getSqlForPgOdsV2")
    public ResultEntity<List<String>> getSqlForPgOdsV2(@RequestBody DataAccessConfigDTO configDTO) {
        ResultEntity<List<String>> SqlForPgOds = new ResultEntity<>();
        SqlForPgOds.data = iNiFiHelper.getSqlForPgOdsV2(configDTO);
        SqlForPgOds.code = 0;
        return SqlForPgOds;
    }

    @ApiOperation("删除自定义工作Nifi流程")
    @PostMapping("/deleteCustomWorkNifiFlow")
    public void deleteCustomWorkNifiFlow(@RequestBody NifiCustomWorkListDTO nifiCustomWorkListDTO) {
        iNifiCustomWorkFlow.deleteCustomWorkNifiFlow(nifiCustomWorkListDTO);
    }

    @ApiOperation("暂停自定义工作Nifi流程")
    @PostMapping("/suspendCustomWorkNifiFlow")
    public ResultEntity<Object> suspendCustomWorkNifiFlow(@RequestParam("nifiCustomWorkflowId") String nifiCustomWorkflowId, @RequestParam("ifFire") boolean ifFire) {
        return ResultEntityBuild.build(iNifiCustomWorkFlow.suspendCustomWorkNifiFlow(nifiCustomWorkflowId, ifFire));
    }

    /**
     * 添加系统数据源时调用设置nifi参数
     *
     * @param dto
     */
    @ApiOperation("添加系统数据源时调用设置nifi参数")
    @PostMapping("/add")
    public ResultEntity<Object> addDataSetParams(@RequestBody DataSourceSaveDTO dto) {
        // 添加系统数据源
        ResultEntity<Object> resultEntity;
        try {
            resultEntity = userClient.addData(dto);
        } catch (Exception e) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

        //RestfulApi类型的数据源的账号不允许重复
        if (resultEntity.getCode() == ResultEnum.DATA_SOURCE_ACCOUNT_ALREADY_EXISTS.getCode()) {
            log.error("system服务添加数据源失败，[{}]", resultEntity.getMsg());
            return ResultEntityBuild.build(ResultEnum.DATA_SOURCE_ACCOUNT_ALREADY_EXISTS);
        }

        //新增数据源时，不允许数据源名称重复
        if (resultEntity.getCode() == ResultEnum.DATA_SOURCE_NAME_ALREADY_EXISTS.getCode()) {
            log.error("system服务添加数据源失败，[{}]", resultEntity.getMsg());
            return ResultEntityBuild.build(ResultEnum.DATA_SOURCE_NAME_ALREADY_EXISTS, resultEntity.getData());
        }

        //新增数据源时,相同数据库类型,相同ip,相同库名不允许重复添加
        if (resultEntity.getCode() == ResultEnum.DATA_SOURCE_ALREADY_EXISTS.getCode()) {
            log.error("system服务添加数据源失败，[{}]", resultEntity.getMsg());
            return ResultEntityBuild.build(ResultEnum.DATA_SOURCE_ALREADY_EXISTS, resultEntity.getData());
        }

        if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            log.error("system服务添加数据源失败，[{}]", resultEntity.getMsg());
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        //RestfulApi无需在nifi创建全局变量，它不走nifi
        //WEBSERVICE和RestfulApi同理  都不走nifi
        //SAPBW同理 源--临时表的流程不走nifi    临时表-目标表的流程才走nifi
        //DORIS_CATALOG同理
        //hudi同理
        if (dto.conType == DataSourceTypeEnum.RESTFULAPI
                || dto.conType == DataSourceTypeEnum.SAPBW
                || dto.conType == DataSourceTypeEnum.WEBSERVICE
                || dto.conType == DataSourceTypeEnum.DORIS_CATALOG
                || dto.conType == DataSourceTypeEnum.HUDI
                || dto.conType == DataSourceTypeEnum.API
                || dto.conType == DataSourceTypeEnum.POWERBI_DATASETS
        ) {
            return resultEntity;
        }

        Integer id = (Integer) resultEntity.getData();
        log.info("开始向nifi中添加参数，数据源[{}],[{}]", id, dto);
        Map<String, String> map = new HashMap<>();
        map.put(ComponentIdTypeEnum.DB_URL.getName() + id, dto.getConStr());
        map.put(ComponentIdTypeEnum.DB_USERNAME.getName() + id, dto.getConAccount());
        map.put(ComponentIdTypeEnum.DB_PASSWORD.getName() + id, dto.getConPassword());
        iNiFiHelper.buildNifiGlobalVariable(map);
        log.info("结束向nifi中添加参数");
        return resultEntity;
    }

    /**
     * 修改系统数据源时调用设置nifi参数
     *
     * @param dto
     * @return
     */
    @ApiOperation("修改系统数据源时调用设置nifi参数")
    @PutMapping("/edit")
    public ResultEntity<Object> editDataSetParams(@RequestBody DataSourceSaveDTO dto) {
        // 修改数据源
        ResultEntity<Object> resultEntity;
        ResultEntity<DataSourceDTO> modelResult;
        try {
            // 获取系统数据源历史数据
            modelResult = userClient.getFiDataDataSourceById(dto.id);
            if (modelResult.getCode() != ResultEnum.SUCCESS.getCode() || modelResult.getData() == null) {
                log.error("task模块调用system服务查询数据源失败，[{}]", modelResult.getMsg());
                return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
            }

            resultEntity = userClient.editData(dto);

            if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
                log.error("system服务修改数据源失败，[{}]", resultEntity.getMsg());
                return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
            }

            //在修改数据源的同时，连带修改数据接入引用了平台配置数据源的app应用的数据源信息
            //远程调用数据接入的方法
            ResultEntity<List<AppDataSourceDTO>> sources = dataAccessClient.getDataSourcesBySystemDataSourceId(dto.id);
            if (!sources.getData().isEmpty()) {
                ResultEntity<Boolean> booleanResultEntity = dataAccessClient.editDataSourceByTask(dto);
                if (booleanResultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
                    log.error("数据接入服务修改数据源失败，[{}]", booleanResultEntity.getMsg());
                    return ResultEntityBuild.build(ResultEnum.SAVE_ACCESS_DATA_SOURCE_ERROR);
                }
            }

        } catch (Exception e) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

        //RestfulApi无需在nifi创建全局变量，它不走nifi
        //SAPBW同理 源--临时表的流程不走nifi
        //HIVE同理
        if (dto.conType == DataSourceTypeEnum.RESTFULAPI
                || dto.conType == DataSourceTypeEnum.SAPBW
                || dto.conType == DataSourceTypeEnum.WEBSERVICE
                || dto.conType == DataSourceTypeEnum.DORIS_CATALOG
                || dto.conType == DataSourceTypeEnum.API
                || dto.conType == DataSourceTypeEnum.HUDI
                || dto.conType == DataSourceTypeEnum.MONGODB
                || dto.conType == DataSourceTypeEnum.POWERBI_DATASETS
        ) {
            return resultEntity;
        }

        // 查询修改后的连接信息
        DataSourceDTO model = modelResult.getData();
        log.info("历史数据源：【{}】", JSON.toJSONString(model));
        Map<String, String> map = new HashMap<>();
        if (!StringUtils.isEmpty(dto.getConStr()) && !model.getConStr().equals(dto.getConStr())) {
            map.put(ComponentIdTypeEnum.DB_URL.getName() + model.getId(), dto.getConStr());
        }
        if (!StringUtils.isEmpty(dto.getConAccount()) && !model.getConAccount().equals(dto.getConAccount())) {
            map.put(ComponentIdTypeEnum.DB_USERNAME.getName() + model.getId(), dto.getConAccount());
        }
        if (!StringUtils.isEmpty(dto.getConPassword()) && !model.getConPassword().equals(dto.getConPassword())) {
            map.put(ComponentIdTypeEnum.DB_PASSWORD.getName() + model.getId(), dto.getConPassword());
        }
        if (!map.isEmpty()) {
            log.info("开始更新nifi变量数据：【{}】", JSON.toJSONString(map));
            iNiFiHelper.updateNifiGlobalVariable(map);
            log.info("更新nifi变量结束");
        }
        return resultEntity;
    }

    /**
     * sftp或ftp-Java代码同步
     *
     * @param kafkaReceive
     * @return
     */
    @ApiOperation("sftp或ftp-Java代码同步")
    @PostMapping("/sftpDataUploadListener")
    public ResultEntity<Object> sftpDataUploadListener(@RequestBody KafkaReceiveDTO kafkaReceive) {
        return ResultEntityBuild.build(iSftpDataUploadListener.buildSftpDataUploadListener(JSON.toJSONString(kafkaReceive)));
    }

    @GetMapping("/runOnce")
    @ApiOperation(value = "执行一次管道")
    public ResultEntity<Object> runOnce(@RequestParam("id") Long id) {
        return ResultEntityBuild.build(iNifiSchedulingComponentService.runOnce(id));
    }

    @ApiOperation(value = "表服务启用或禁用")
    @PostMapping("enableOrDisable")
    public ResultEntity<TableServiceDTO> enableOrDisable(@RequestBody TableServiceDTO tableServiceDTO) {
        return tableNifiSettingService.enableOrDisable(tableServiceDTO);
    }

    /**
     * sapbw-Java代码同步
     *
     * @param kafkaReceive
     * @return
     */
    @ApiOperation("sapbw-Java代码同步")
    @PostMapping("/sapBwToStg")
    public ResultEntity<Object> sapBwToStg(@RequestBody KafkaReceiveDTO kafkaReceive) {
        return ResultEntityBuild.build(sapBwListener.sapBwToStg(JSON.toJSONString(kafkaReceive)));
    }

    /**
     * powerBi-Java代码同步
     *
     * @param kafkaReceive
     * @return
     */
    @ApiOperation("powerBi-Java代码同步")
    @PostMapping("/powerBiToStg")
    public ResultEntity<Object> powerBiToStg(@RequestBody KafkaReceiveDTO kafkaReceive) {
        return ResultEntityBuild.build(powerBiListener.powerBiToStg(JSON.toJSONString(kafkaReceive)));
    }

    @ApiOperation(value = "数据分发api启用或禁用")
    @PostMapping("/apiEnableOrDisable")
    public ResultEntity<TableApiServiceDTO> apiEnableOrDisable(@RequestBody TableApiServiceDTO tableServiceDTO) {
        return tableNifiSettingService.apiEnableOrDisable(tableServiceDTO);
    }

    /**
     * api-Java代码同步
     *
     * @param kafkaReceive
     * @return
     */
    @ApiOperation("api-Java代码同步")
    @PostMapping("/apiToStg")
    public ResultEntity<Object> apiToStg(@RequestBody KafkaReceiveDTO kafkaReceive) {
        return apiListener.apiToStg(JSON.toJSONString(kafkaReceive));
    }

    /**
     * dw数仓按时间获取单表nifi日志
     *
     * @param dwLogQueryDTO
     * @return
     */
    @ApiOperation("dw数仓按时间获取单表nifi日志")
    @PostMapping("/getDwTblNifiLog")
    public DwLogResultDTO getDwTblNifiLog(@RequestBody DwLogQueryDTO dwLogQueryDTO) {
        return apiListener.getDwTblNifiLog(dwLogQueryDTO);
    }

    @ApiOperation("主数据获取发布日志结果")
    @PostMapping("/getMdmTblNifiLog")
    public List<LogResultDTO> getMdmTblNifiLog(@RequestBody List<String> subRunIds) {
        return etlLog.getMdmTblNifiLog(subRunIds);

    }

    /**
     * 同步日志页面获取数接/数仓的指定表的nifi同步日志  根据表id 名称 类型
     *
     * @param dto
     * @return
     */
    @ApiOperation("同步日志页面获取数接/数仓的指定表的nifi同步日志  根据表id 名称 类型")
    @PostMapping("/getDwAndAccessTblNifiLog")
    public Page<NifiLogResultDTO> getDwAndAccessTblNifiLog(@RequestBody LogPageQueryDTO dto) {
        return apiListener.getDwAndAccessTblNifiLog(dto);
    }

    /**
     * 根据nifi内表的流程是否有流文件 判断表的最近一次同步任务是否真正结束
     *
     * @param dto
     * @return
     */
    @ApiOperation("根据nifi内表的流程是否有流文件 判断表的最近一次同步任务是否真正结束")
    @PostMapping("/checkModelTblNifiSyncJobIsOver")
    public ResultEntity<Boolean> checkModelTblNifiSyncJobIsOver(@RequestBody LogPageQueryDTO dto) {
        boolean flag = false;
        try {
            //首先根据表id-表类型获取到表的nifi组
            TableNifiSettingPO po = iTableNifiSettingService.getByTableId(dto.getTblId(), dto.getTableType());
            //然后判断组内是否有流文件 确定表任务是否已经结束 如果未结束则将最近一条日志记录改为正在运行的状态
            Integer flowFilesQueued;
            Integer activeThreadCount;
            for (int i = 0; i < 3; i++) {
                Thread.sleep(500);
                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(po.getTableComponentId());
                ProcessGroupStatusDTO status = processGroup.getStatus();
                //flowFilesQueued 组内流文件数量,如果为0代表组内无流文件
                //activeThreadCount 组内活跃线程数量，为0代表没有正在工作的组件
                flowFilesQueued = status.getAggregateSnapshot().getFlowFilesQueued();
                activeThreadCount = status.getAggregateSnapshot().getActiveThreadCount();
                log.info("管道内剩余流文件flowFilesQueued:{}", flowFilesQueued);
                log.info("管道内正在执行线程数activeThreadCount:{}", activeThreadCount);
                if (activeThreadCount == 0 && flowFilesQueued == 0) {
                    flag = true;
                    break;
                }
            }
        } catch (Exception e) {
            log.error("获取数仓表的nifi流程状态失败,原因：" + e
                    + ";表id:" + dto.getTblId()
                    + ";表名称" + dto.getTableName()
                    + ";表类型" + dto.getTableType());
            throw new FkException(ResultEnum.DATA_MODEL_GET_NIFI_LOG_ERROR, e);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, flag);
    }

}
