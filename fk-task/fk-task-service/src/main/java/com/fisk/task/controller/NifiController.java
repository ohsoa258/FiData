package com.fisk.task.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.datafactory.dto.dataaccess.DataAccessIdDTO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.task.TableNifiSettingPO;
import com.fisk.task.service.nifi.INifiComponentsBuild;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/nifi")
@Slf4j
public class NifiController {
    @Resource
    INifiComponentsBuild iNifiComponentsBuild;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @PostMapping("/modifyScheduling")
    public ResultEntity<Object> modifyScheduling(@RequestParam("groupId") String groupId, @RequestParam("ProcessorId") String ProcessorId, @RequestParam("schedulingStrategy") String schedulingStrategy, @RequestParam("schedulingPeriod") String schedulingPeriod) {
        return ResultEntityBuild.build(iNifiComponentsBuild.modifyScheduling(groupId, ProcessorId, schedulingStrategy, schedulingPeriod));

    }

    @PostMapping("/deleteNifiFlow")
    public ResultEntity<Object> deleteNifiFlow(@RequestBody DataModelVO dataModelVO) {
        return ResultEntityBuild.build(iNifiComponentsBuild.deleteNifiFlow(dataModelVO));
    }

    @PostMapping("/getTableNifiSetting")
    public ResultEntity<TableNifiSettingPO> getTableNifiSetting(@RequestBody DataAccessIdDTO dto) {
        ResultEntity<TableNifiSettingPO> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data=tableNifiSettingService.query().eq("app_id",dto.appId).eq("table_access_id",dto.tableId).eq("type",dto.olapTableEnum.getValue()).one();
        objectResultEntity.code=0;
        return objectResultEntity;

    }

    @PostMapping("/getSqlForPgOds")
    public ResultEntity<List<String>> getSqlForPgOds(@RequestBody DataAccessConfigDTO configDTO) {
        ResultEntity<List<String>> SqlForPgOds = new ResultEntity<>();
        SqlForPgOds.data = iNifiComponentsBuild.getSqlForPgOds(configDTO);
        SqlForPgOds.code = 0;
        return SqlForPgOds;
    }


}
