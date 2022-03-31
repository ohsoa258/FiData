package com.fisk.datafactory.service.impl;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
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
            // pid == 0
            case 1:
                List<NifiCustomWorkflowDetailPO> list1 = nifiCustomWorkflowDetailImpl.query().eq("workflow_id", dto.id).eq("pid", 0).ne("component_type","开始").list();
                inports = list1.stream().filter(item -> item.inport == null || "".equals(item.inport)).collect(Collectors.toList());
                outports = list1.stream().filter(item -> item.outport == null || "".equals(item.outport)).collect(Collectors.toList());
                break;
            // pid != 0
            case 2:
                List<NifiCustomWorkflowDetailPO> list2 = nifiCustomWorkflowDetailImpl.query().eq("pid", dto.pid).ne("component_type","开始").list();
                inports = list2.stream().filter(item -> item.inport == null || "".equals(item.inport)).collect(Collectors.toList());
                outports = list2.stream().filter(item -> item.outport == null || "".equals(item.outport)).collect(Collectors.toList());
                break;
            default:
                break;
        }

        nifiPortsDTO.inports = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(inports);
        nifiPortsDTO.outports = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(outports);

        return ResultEntityBuild.build(ResultEnum.SUCCESS, nifiPortsDTO);
    }
}
