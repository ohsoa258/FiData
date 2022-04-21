package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.map.NifiCustomWorkflowDetailMap;
import com.fisk.datafactory.mapper.NifiCustomWorkflowDetailMapper;
import com.fisk.datafactory.service.IDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/11 11:56
 */
@Service
@Slf4j
public class DataFactoryImpl implements IDataFactory {

    @Resource
    NifiCustomWorkflowDetailMapper nifiCustomWorkflowDetailMapper;
    @Resource
    NifiCustomWorkflowImpl nifiCustomWorkflowImpl;
    @Resource
    NifiCustomWorkflowDetailImpl nifiCustomWorkflowDetailImpl;

    @Override
    public boolean loadDepend(LoadDependDTO dto) {

        QueryWrapper<NifiCustomWorkflowDetailPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NifiCustomWorkflowDetailPO::getComponentType, dto.channelDataEnum.getName())
                .eq(dto.appId != null, NifiCustomWorkflowDetailPO::getAppId, String.valueOf(dto.appId))
                .eq(dto.tableId != null, NifiCustomWorkflowDetailPO::getTableId, String.valueOf(dto.tableId))
                .select(NifiCustomWorkflowDetailPO::getTableId)
                .select(NifiCustomWorkflowDetailPO::getAppId);
        List<NifiCustomWorkflowDetailPO> list = nifiCustomWorkflowDetailMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public ResultEntity<NifiPortsHierarchyDTO> getNifiPortHierarchy(NifiGetPortHierarchyDTO dto) {
        if (dto.nifiCustomWorkflowDetailId != null) {
            return ResultEntityBuild.build(ResultEnum.SUCCESS, buildNifiPortsHierarchyDTO(dto.nifiCustomWorkflowDetailId));
        } else {
            NifiCustomWorkflowPO customWorkflowPo = nifiCustomWorkflowImpl.query().eq("id", dto.workflowId).one();
            if (customWorkflowPo == null) {
                // 当前管道已删除
                return ResultEntityBuild.build(ResultEnum.CUSTOMWORKFLOW_NOT_EXISTS);
            }
            List<NifiCustomWorkflowDetailPO> detailList = nifiCustomWorkflowDetailImpl.query()
                    .eq("workflow_id", customWorkflowPo.workflowId).eq("component_type", dto.channelDataEnum.getName()).list();
            if (CollectionUtils.isEmpty(detailList)) {
                // 当前管道下没有组件
                return ResultEntityBuild.build(ResultEnum.CUSTOMWORKFLOWDETAIL_NOT_EXISTS);
            }

            // 匹配tableId与feign接口传参的tableId保持一致的
            List<NifiCustomWorkflowDetailPO> newList = detailList.stream().filter(e -> dto.tableId.equals(e.tableId)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(newList)) {
                return ResultEntityBuild.build(ResultEnum.FLOW_TABLE_NOT_EXISTS);
            }
            // 原则上同一管道下,物理表只允许绑定一次,即newList里的参数只有一个
            NifiCustomWorkflowDetailPO detailPo = newList.get(0);
            return ResultEntityBuild.build(ResultEnum.SUCCESS, buildNifiPortsHierarchyDTO(detailPo.id));
        }

    }

    /**
     * 获取当前组件的层级关系
     *
     * @return com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO
     * @description 获取当前组件的层级关系
     * @author Lock
     * @date 2022/2/21 17:35
     * @version v1.0
     * @params nifiCustomWorkflowDetailId
     */
    private NifiPortsHierarchyDTO buildNifiPortsHierarchyDTO(Long nifiCustomWorkflowDetailId) {

        NifiPortsHierarchyDTO nifiPortsHierarchyDTO = new NifiPortsHierarchyDTO();

        NifiCustomWorkflowDetailDTO itselfPort = getPort(nifiCustomWorkflowDetailId);
        if (itselfPort == null) {
            log.info("当前组件已删除");
            return nifiPortsHierarchyDTO;
        }
        nifiPortsHierarchyDTO.itselfPort = itselfPort;
        // 指向的下一个组件id,逗号隔开
        String outport = itselfPort.outport;
        if (StringUtils.isNotBlank(outport)) {
            nifiPortsHierarchyDTO.nextList = getNextList(outport.split(","));
        } else {
            log.info("当前组件没有指向下一级的组件");
            return nifiPortsHierarchyDTO;
        }

        return nifiPortsHierarchyDTO;
    }

    /**
     * 获取当前组件的下一级组件集合
     *
     * @return java.util.List<com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO>
     * @description 获取当前组件的下一级组件集合
     * @author Lock
     * @date 2022/2/21 18:22
     * @version v1.0
     * @params split 下一级组件id集合
     */
    private List<NifiPortsHierarchyNextDTO> getNextList(String[] split) {
        List<NifiPortsHierarchyNextDTO> nextList = new ArrayList<>();
        for (String detailId : split) {
            NifiPortsHierarchyNextDTO dto = new NifiPortsHierarchyNextDTO();
            NifiCustomWorkflowDetailDTO data = getPort(Long.parseLong(detailId));
            if (data == null) {
                log.info("当前组件已删除");
                return nextList;
            }
            dto.itselfPort = data;
            dto.upPortList = getUpPortList(data.id);
            nextList.add(dto);
        }
        return nextList;
    }

    /**
     * 获取当前组件的上一级组件集合
     *
     * @return java.util.List<com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO>
     * @description 获取当前组件的上一级组件集合
     * @author Lock
     * @date 2022/2/21 18:23
     * @version v1.0
     * @params id 当前组件id
     */
    private List<NifiCustomWorkflowDetailDTO> getUpPortList(long id) {
        NifiCustomWorkflowDetailDTO data = getPort(id);
        if (data == null) {
            log.info("当前组件已删除");
            return null;
        }
        String inport = data.inport;
        String[] split = inport.split(",");
        List<NifiCustomWorkflowDetailDTO> list = new ArrayList<>();
        for (String s : split) {
            NifiCustomWorkflowDetailDTO port = getPort(Long.parseLong(s));
            if (port == null) {
                log.info("当前组件已删除");
                return list;
            }
            list.add(port);
        }
        return list;
    }

    /**
     * 根据管道详情id获取当前组件
     *
     * @return com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO
     * @description 根据管道详情id获取当前组件
     * @author Lock
     * @date 2022/2/21 18:17
     * @version v1.0
     * @params id 管道详情id
     */
    private NifiCustomWorkflowDetailDTO getPort(long id) {
        NifiCustomWorkflowDetailPO port = nifiCustomWorkflowDetailImpl.query().eq("id", id).one();
        if (port == null) {
            return null;
        }
        return NifiCustomWorkflowDetailMap.INSTANCES.poToDto(port);
    }
}
