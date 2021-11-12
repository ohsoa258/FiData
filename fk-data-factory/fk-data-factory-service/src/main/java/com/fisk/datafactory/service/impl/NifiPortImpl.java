package com.fisk.datafactory.service.impl;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datafactory.dto.tasknifi.NifiPortsDTO;
import com.fisk.datafactory.dto.tasknifi.PortRequestParamDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.map.NifiCustomWorkflowDetailMap;
import com.fisk.datafactory.service.INifiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Slf4j
@Service
public class NifiPortImpl implements INifiPort {

    @Resource
    NifiCustomWorkflowImpl nifiCustomWorkflowImpl;
    @Resource
    NifiCustomWorkflowDetailImpl nifiCustomWorkflowDetailImpl;

    @Override
    public ResultEntity<NifiPortsDTO> getFilterData(PortRequestParamDTO dto) {

        NifiPortsDTO nifiPortsDTO = new NifiPortsDTO();
        List<NifiCustomWorkflowDetailPO> inports = new ArrayList<>();
        List<NifiCustomWorkflowDetailPO> outports = new ArrayList<>();

        switch (dto.flag) {
            case 1:
                String workflowId = nifiCustomWorkflowImpl.query().eq("id", dto.id).one().workflowId;
                List<NifiCustomWorkflowDetailPO> list1 = nifiCustomWorkflowDetailImpl.query().eq("workflow_id", workflowId).eq("pid", 0).list();
                inports = list1.stream().filter(item -> item.inport == null).collect(Collectors.toList());
                outports = list1.stream().filter(item -> item.outport == null).collect(Collectors.toList());
                break;
            case 2:
                List<NifiCustomWorkflowDetailPO> list2 = nifiCustomWorkflowDetailImpl.query().eq("pid", dto.pid).list();
                inports = list2.stream().filter(item -> item.inport == null).collect(Collectors.toList());
                outports = list2.stream().filter(item -> item.outport == null).collect(Collectors.toList());
                break;
            default:
                break;
        }

        nifiPortsDTO.inports = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(inports);
        nifiPortsDTO.outports = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(outports);

        return ResultEntityBuild.build(ResultEnum.SUCCESS, nifiPortsDTO);
    }
}
