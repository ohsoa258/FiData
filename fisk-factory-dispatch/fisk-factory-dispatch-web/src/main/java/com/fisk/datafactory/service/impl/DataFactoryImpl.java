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
                    .eq("workflow_id", customWorkflowPo.workflowId)
                    .eq("component_type", dto.channelDataEnum.getName())
                    .list();
            if (CollectionUtils.isEmpty(detailList)) {
                // 当前管道下没有组件
                return ResultEntityBuild.build(ResultEnum.CUSTOMWORKFLOWDETAIL_NOT_EXISTS);
            }

            // 匹配tableId与feign接口传参的tableId保持一致的
            List<NifiCustomWorkflowDetailPO> newList = new ArrayList<>();
            for (NifiCustomWorkflowDetailPO e : detailList) {
                if (StringUtils.isNotBlank(e.tableId) && dto.tableId.equals(e.tableId)) {
                    newList.add(e);
                }
            }
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
        NifiCustomWorkflowDetailPO one = nifiCustomWorkflowDetailImpl.query().eq("pid", itselfPort.pid).eq("table_order", itselfPort.tableOrder + 1).isNotNull("table_id").one();
        if (one != null) {
            List<NifiPortsHierarchyNextDTO> nextList = new ArrayList<>();
            NifiPortsHierarchyNextDTO nifiPortsHierarchyNextDTO = new NifiPortsHierarchyNextDTO();
            nifiPortsHierarchyNextDTO.itselfPort = NifiCustomWorkflowDetailMap.INSTANCES.poToDto(one);
            List<NifiCustomWorkflowDetailDTO> upPortList = new ArrayList<>();
            NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO = new NifiCustomWorkflowDetailDTO();
            nifiCustomWorkflowDetailDTO = NifiCustomWorkflowDetailMap.INSTANCES.poToDto(one);
            upPortList.add(nifiCustomWorkflowDetailDTO);
            nifiPortsHierarchyNextDTO.upPortList = upPortList;
            nextList.add(nifiPortsHierarchyNextDTO);
            nifiPortsHierarchyDTO.nextList = nextList;
        } else {
            // 指向的下一个组件id,逗号隔开
            String outport = itselfPort.outport;
            List<NifiPortsHierarchyNextDTO> nextList = new ArrayList<>();
            if (StringUtils.isNotBlank(outport)) {
                //下一个组件的第一张表
                String[] split = outport.split(",");
                for (String id : split) {
                    NifiCustomWorkflowDetailPO one1 = nifiCustomWorkflowDetailImpl.query().eq("pid", id).eq("table_order", 0).isNotNull("table_id").one();
                    NifiPortsHierarchyNextDTO nifiPortsHierarchyNextDTO = new NifiPortsHierarchyNextDTO();
                    //下一级本身
                    nifiPortsHierarchyNextDTO.itselfPort = NifiCustomWorkflowDetailMap.INSTANCES.poToDto(one1);
                    List<NifiCustomWorkflowDetailDTO> upPortList = new ArrayList<>();
                    //下一级所有的上一级
                    String[] split1 = one1.inport.split(",");
                    for (String inputId : split1) {
                        NifiCustomWorkflowDetailPO one2 = nifiCustomWorkflowDetailImpl.query().eq("pid", inputId)
                                .isNotNull("table_id").orderByDesc("table_order").list().get(0);
                        if (one2 != null) {
                            NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO1 = NifiCustomWorkflowDetailMap.INSTANCES.poToDto(one2);
                            upPortList.add(nifiCustomWorkflowDetailDTO1);
                        }
                    }
                    nifiPortsHierarchyNextDTO.upPortList = upPortList;
                    nextList.add(nifiPortsHierarchyNextDTO);
                }
                nifiPortsHierarchyDTO.nextList = nextList;
            } else {
                log.info("当前组件没有指向下一级的组件");
                return nifiPortsHierarchyDTO;
            }
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
