package com.fisk.task.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.datafactory.dto.dataaccess.DataAccessIdDTO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;
import com.fisk.task.listener.nifi.INifiCustomWorkFlow;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.nifi.INiFiHelper;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
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

    @PostMapping("/modifyScheduling")
    public ResultEntity<Object> modifyScheduling(@RequestParam("groupId") String groupId, @RequestParam("ProcessorId") String ProcessorId, @RequestParam("schedulingStrategy") String schedulingStrategy, @RequestParam("schedulingPeriod") String schedulingPeriod) {
        return ResultEntityBuild.build(iNiFiHelper.modifyScheduling(groupId, ProcessorId, schedulingStrategy, schedulingPeriod));

    }

    @PostMapping("/deleteNifiFlow")
    public ResultEntity<Object> deleteNifiFlow(@RequestBody DataModelVO dataModelVO) {
        return ResultEntityBuild.build(iNiFiHelper.deleteNifiFlow(dataModelVO));
    }

    @PostMapping("/getTableNifiSetting")
    public ResultEntity<TableNifiSettingPO> getTableNifiSetting(@RequestBody DataAccessIdDTO dto) {
        ResultEntity<TableNifiSettingPO> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = tableNifiSettingService.query().eq("app_id", dto.appId).eq("table_access_id", dto.tableId).eq("type", dto.olapTableEnum.getValue()).one();
        objectResultEntity.code = 0;
        return objectResultEntity;

    }

    @PostMapping("/getSqlForPgOds")
    public ResultEntity<List<String>> getSqlForPgOds(@RequestBody DataAccessConfigDTO configDTO) {
        ResultEntity<List<String>> SqlForPgOds = new ResultEntity<>();
        SqlForPgOds.data = iNiFiHelper.getSqlForPgOds(configDTO);
        SqlForPgOds.code = 0;
        return SqlForPgOds;
    }

    @PostMapping("/deleteCustomWorkNifiFlow")
    public void deleteCustomWorkNifiFlow(@RequestBody NifiCustomWorkListDTO nifiCustomWorkListDTO) {
        iNifiCustomWorkFlow.deleteCustomWorkNifiFlow(nifiCustomWorkListDTO);
    }

    @PostMapping("/suspendCustomWorkNifiFlow")
    public ResultEntity<Object> suspendCustomWorkNifiFlow(@RequestParam("nifiCustomWorkflowId") String nifiCustomWorkflowId, @RequestParam("ifFire") boolean ifFire) {
        return ResultEntityBuild.build(iNifiCustomWorkFlow.suspendCustomWorkNifiFlow(nifiCustomWorkflowId, ifFire));
    }

    /**
     * 添加系统数据源时调用设置nifi参数
     * @param dto
     */
    @PostMapping("/addDataSetParams")
    public void addDataSetParams(@RequestBody DataSourceSaveDTO dto){
        Map<String, String> map = new HashMap<>();
        map.put(ComponentIdTypeEnum.DB_URL.getName(), dto.getConStr());
        map.put(ComponentIdTypeEnum.DB_USERNAME.getName(), dto.getConAccount());
        map.put(ComponentIdTypeEnum.DB_PASSWORD.getName(), dto.getConPassword());
        iNiFiHelper.buildNifiGlobalVariable(map);
    }
}
