package com.fisk.task.controller;

import com.davis.client.ApiException;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.task.dto.nifi.NifiRemoveDTO;
import com.fisk.task.service.IBuildTaskService;
import com.fisk.task.service.INifiComponentsBuild;
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
    @PostMapping("/modifyScheduling")
    public ResultEntity<Object> modifyScheduling(@RequestParam("groupId") String groupId, @RequestParam("ProcessorId") String ProcessorId, @RequestParam("schedulingStrategy") String schedulingStrategy, @RequestParam("schedulingPeriod") String schedulingPeriod) {
        return ResultEntityBuild.build(iNifiComponentsBuild.modifyScheduling(groupId, ProcessorId, schedulingStrategy, schedulingPeriod));

    }

    @PostMapping("/deleteNifiFlow")
    public ResultEntity<Object> deleteNifiFlow(@RequestBody List<NifiRemoveDTO> nifiRemoveDTOList) {
        return ResultEntityBuild.build(iNifiComponentsBuild.deleteNifiFlow(nifiRemoveDTOList));
    }

}
