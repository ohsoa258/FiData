package com.fisk.task.controller;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.datafactory.dto.dataaccess.DataAccessIdDTO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;
import com.fisk.task.listener.nifi.INifiCustomWorkFlow;
import com.fisk.task.listener.nifi.ISftpDataUploadListener;
import com.fisk.task.po.app.TableNifiSettingPO;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
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
import java.util.Objects;

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
    ISftpDataUploadListener iSftpDataUploadListener;
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
        if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            log.error("system服务添加数据源失败，[{}]", resultEntity.getMsg());
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
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
        } catch (Exception e) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
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


}
