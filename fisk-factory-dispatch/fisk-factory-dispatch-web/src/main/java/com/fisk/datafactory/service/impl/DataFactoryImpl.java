package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datafactory.map.NifiCustomWorkflowDetailMap;
import com.fisk.datafactory.map.NifiCustomWorkflowMap;
import com.fisk.datafactory.mapper.NifiCustomWorkflowDetailMapper;
import com.fisk.datafactory.service.IDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
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

            // 获取当前组件的层级关系
            NifiPortsHierarchyDTO nifiPortsHierarchyDto = buildNifiPortsHierarchyDTO(dto.nifiCustomWorkflowDetailId);

            NifiCustomWorkflowDetailPO detailPo = nifiCustomWorkflowDetailImpl.query().eq("id", dto.nifiCustomWorkflowDetailId).one();
            if (detailPo == null) {
                return null;
            }

            // 封装当前组件的其他属性(componentFirstFlag、componentEndFlag、pipeEndFlag、pipeEndDto)
            buildNifiOtherPorts(detailPo, nifiPortsHierarchyDto);

            // 封装当前任务的上一级主任务中的最后一个表任务集合
            buildInportList(detailPo, nifiPortsHierarchyDto);

            return ResultEntityBuild.build(ResultEnum.SUCCESS, nifiPortsHierarchyDto);
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

            // 获取当前组件的层级关系
            NifiPortsHierarchyDTO nifiPortsHierarchyDto = buildNifiPortsHierarchyDTO(detailPo.id);

            // 封装当前组件的其他属性(componentFirstFlag、componentEndFlag、pipeEndFlag、pipeEndDto)
            buildNifiOtherPorts(detailPo, nifiPortsHierarchyDto);

            // 封装当前任务的上一级主任务中的最后一个表任务集合
            buildInportList(detailPo, nifiPortsHierarchyDto);

            return ResultEntityBuild.build(ResultEnum.SUCCESS, nifiPortsHierarchyDto);
        }

    }

    /**
     * 封装当前任务的上一级主任务中的最后一个表任务集合
     *
     * @return void
     * @description 封装当前任务的上一级主任务中的最后一个表任务集合
     * @author Lock
     * @date 2022/6/15 14:18
     * @version v1.0
     * @params nifiPortsHierarchyDto
     * @params detailPo
     */
    private void buildInportList(NifiCustomWorkflowDetailPO detailPo, NifiPortsHierarchyDTO nifiPortsHierarchyDto) {

        try {
            // 当前表任务的主任务组件
            NifiCustomWorkflowDetailPO parentDetailPo = nifiCustomWorkflowDetailImpl.query().eq("id", detailPo.pid).one();

            // 主任务组件的所有inport(上一级id)
            String[] inportIds = parentDetailPo.inport.split(",");
            List<NifiCustomWorkflowDetailDTO> inportList = new ArrayList<>();
            Arrays.stream(inportIds).forEachOrdered(inportId -> {
                // 过滤出上一级所有主任务(数据湖、数仓、分析模型),不含绑定表的组件
                List<NifiCustomWorkflowDetailDTO> listAllTask = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(nifiCustomWorkflowDetailImpl.query()
                        .eq("id", inportId)
                        .list()
                        .stream()
                        .filter(Objects::nonNull)
                        // 过滤出主任务
                        .filter(e -> e.appId != null && !"".equals(e.appId) && (e.tableId == null || "".equals(e.tableId)))
                        .collect(Collectors.toList()));
                // 过滤出上一级所有表任务(接入+建模)
                List<NifiCustomWorkflowDetailDTO> listAllTable = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(nifiCustomWorkflowDetailImpl.query()
                        .eq("id", inportId)
                        .list()
                        .stream()
                        .filter(Objects::nonNull)
                        // 过滤掉不绑定表的任务
                        .filter(e -> e.appId != null && !"".equals(e.appId) && e.tableId != null && !"".equals(e.tableId) && e.tableOrder != null)
                        .collect(Collectors.toList()));

                if (detailPo.tableOrder == 1) {
                    // 匹配
                    matchingDetailDtoList(inportList, listAllTask, listAllTable);
                } else {
                    NifiCustomWorkflowDetailPO one = nifiCustomWorkflowDetailImpl.query().eq("pid", detailPo.pid).eq("table_order", detailPo.tableOrder - 1).one();
                    inportList.add(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(one));
                }

            });

            nifiPortsHierarchyDto.inportList = inportList;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("封装当前任务的上一级主任务中的最后一个表任务集合报错: " + e);
        }
    }

    /**
     * 匹配当前主任务内的最后一个任务
     *
     * @return void
     * @description 匹配当前主任务内的最后一个任务
     * @author Lock
     * @date 2022/6/15 15:01
     * @version v1.0
     * @params inportList
     * @params listAllTask
     * @params listAllTable
     */
    private void matchingDetailDtoList(List<NifiCustomWorkflowDetailDTO> inportList, List<NifiCustomWorkflowDetailDTO> listAllTask, List<NifiCustomWorkflowDetailDTO> listAllTable) {
        for (NifiCustomWorkflowDetailDTO dto : listAllTask) {
            // 数据湖任务
            List<NifiCustomWorkflowDetailDTO> datalakeTaskDtoList = listAllTable.stream()
                    .filter(Objects::nonNull)
                    // 确保在同一个数据湖任务下
                    .filter(e -> e.pid == dto.id)
                    .filter(e -> e.appId.equalsIgnoreCase(dto.appId))
                    // 过滤出数据湖任务下的表
                    .filter(e -> e.componentName.equalsIgnoreCase(ChannelDataEnum.DATALAKE_TASK.getName()))
                    // 根据table_order降序
                    .sorted(Comparator.comparing(NifiCustomWorkflowDetailDTO::getTableOrder).reversed())
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(datalakeTaskDtoList)) {
                inportList.add(datalakeTaskDtoList.get(0));
            }

            // 数仓表任务
            List<NifiCustomWorkflowDetailDTO> dwDimensionTaskDtoList = listAllTable.stream()
                    .filter(Objects::nonNull)
                    // 确保在同一个数仓表任务下
                    .filter(e -> e.pid == dto.id)
                    .filter(e -> e.appId.equalsIgnoreCase(dto.appId))
                    // 过滤出数仓表任务下的表
                    .filter(e -> e.componentName.equalsIgnoreCase(ChannelDataEnum.DW_TASK.getName()))
                    // 根据table_order降序
                    .sorted(Comparator.comparing(NifiCustomWorkflowDetailDTO::getTableOrder).reversed())
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(dwDimensionTaskDtoList)) {
                inportList.add(dwDimensionTaskDtoList.get(0));
            }

            // 分析模型任务
            List<NifiCustomWorkflowDetailDTO> olapTaskDtoList = listAllTable.stream()
                    .filter(Objects::nonNull)
                    // 确保在同一个分析模型任务下
                    .filter(e -> e.pid == dto.id)
                    .filter(e -> e.appId.equalsIgnoreCase(dto.appId))
                    // 过滤出分析模型任务下的表
                    .filter(e -> e.componentName.equalsIgnoreCase(ChannelDataEnum.OLAP_TASK.getName()))
                    // 根据table_order降序
                    .sorted(Comparator.comparing(NifiCustomWorkflowDetailDTO::getTableOrder).reversed())
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(olapTaskDtoList)) {
                inportList.add(olapTaskDtoList.get(0));
            }
        }
    }

    @Override
    public ResultEntity<List<NifiCustomWorkflowDetailDTO>> getNifiPortTaskFirstListById(Long id) {

        List<NifiCustomWorkflowDetailDTO> list = new ArrayList<>();

        NifiCustomWorkflowPO nifiCustomWorkflowPo = nifiCustomWorkflowImpl.query().eq("id", id).select("workflow_id").one();
        if (nifiCustomWorkflowPo == null || StringUtils.isBlank(nifiCustomWorkflowPo.workflowId)) {
            return ResultEntityBuild.build(ResultEnum.SUCCESS, null);
        }

        // 开始组件
        NifiCustomWorkflowDetailPO scheduleTask = nifiCustomWorkflowDetailImpl.query()
                .eq("workflow_id", nifiCustomWorkflowPo.workflowId)
                .eq("component_name", ChannelDataEnum.SCHEDULE_TASK.getName())
                .one();
        if (scheduleTask == null) {
            return ResultEntityBuild.build(ResultEnum.SUCCESS, null);
        }

        // 过滤出所有主任务(数据湖、数仓、分析模型),不含绑定表的组件
        List<NifiCustomWorkflowDetailDTO> listAllTask = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(nifiCustomWorkflowDetailImpl.query()
                .eq("workflow_id", nifiCustomWorkflowPo.workflowId)
                .list()
                .stream()
                .filter(Objects::nonNull)
                // 过滤出主任务
                .filter(e -> e.appId != null && !"".equals(e.appId) && (e.tableId == null || "".equals(e.tableId)))
                // 过滤没有inport上游的
                .filter(e -> StringUtils.isNotBlank(e.inport))
                // 当前组件的pid是开始组件的id
                .filter(e -> e.inport.equalsIgnoreCase(String.valueOf(scheduleTask.id)))
                .collect(Collectors.toList()));

        // 过滤出所有表任务(接入+建模)
        List<NifiCustomWorkflowDetailDTO> listAllTable = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(nifiCustomWorkflowDetailImpl.query()
                .eq("workflow_id", nifiCustomWorkflowPo.workflowId)
                .list()
                .stream()
                .filter(Objects::nonNull)
                // 过滤掉不绑定表的任务
                .filter(e -> e.appId != null && !"".equals(e.appId) && e.tableId != null && !"".equals(e.tableId) && e.tableOrder != null)
                // 过滤没有inport上游的
                .filter(e -> StringUtils.isNotBlank(e.inport))
                // 当前组件的pid是开始组件的id
                .filter(e -> e.inport.equalsIgnoreCase(String.valueOf(scheduleTask.id)))
                .collect(Collectors.toList()));

        for (NifiCustomWorkflowDetailDTO dto : listAllTask) {
            // 数据湖任务
            List<NifiCustomWorkflowDetailDTO> datalakeTaskDtoList = listAllTable.stream()
                    .filter(Objects::nonNull)
                    // 确保在同一个数据湖任务下
                    .filter(e -> e.pid == dto.id)
                    .filter(e -> e.appId.equalsIgnoreCase(dto.appId))
                    // 过滤出数据湖任务下的表
                    .filter(e -> e.componentName.equalsIgnoreCase(ChannelDataEnum.DATALAKE_TASK.getName()))
                    // 根据table_order升序
                    .sorted(Comparator.comparing(NifiCustomWorkflowDetailDTO::getTableOrder))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(datalakeTaskDtoList)) {
                list.add(datalakeTaskDtoList.get(0));
            }

            // 数仓表任务
            List<NifiCustomWorkflowDetailDTO> dwDimensionTaskDtoList = listAllTable.stream()
                    .filter(Objects::nonNull)
                    // 确保在同一个数仓表任务下
                    .filter(e -> e.pid == dto.id)
                    .filter(e -> e.appId.equalsIgnoreCase(dto.appId))
                    // 过滤出数仓表任务下的表
                    .filter(e -> e.componentName.equalsIgnoreCase(ChannelDataEnum.DW_TASK.getName()))
                    // 根据table_order升序
                    .sorted(Comparator.comparing(NifiCustomWorkflowDetailDTO::getTableOrder))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(dwDimensionTaskDtoList)) {
                list.add(dwDimensionTaskDtoList.get(0));
            }

            // 分析模型任务
            List<NifiCustomWorkflowDetailDTO> olapTaskDtoList = listAllTable.stream()
                    .filter(Objects::nonNull)
                    // 确保在同一个分析模型任务下
                    .filter(e -> e.pid == dto.id)
                    .filter(e -> e.appId.equalsIgnoreCase(dto.appId))
                    // 过滤出分析模型任务下的表
                    .filter(e -> e.componentName.equalsIgnoreCase(ChannelDataEnum.OLAP_TASK.getName()))
                    // 根据table_order升序
                    .sorted(Comparator.comparing(NifiCustomWorkflowDetailDTO::getTableOrder))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(olapTaskDtoList)) {
                list.add(olapTaskDtoList.get(0));
            }
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, Objects.requireNonNull(list.stream().distinct().collect(Collectors.toList())));
    }

    @Override
    public ResultEntity<List<DispatchRedirectDTO>> redirect(NifiCustomWorkflowDetailDTO dto) {

        List<DispatchRedirectDTO> list = new ArrayList<>();

        // 查询出符合条件的所有组件
        List<NifiCustomWorkflowDetailPO> detailPoList = nifiCustomWorkflowDetailImpl.query()
                .eq("component_type", dto.componentType)
                .eq("app_id", dto.appId)
                .eq("table_id", dto.tableId)
                .list();

        if (!CollectionUtils.isEmpty(detailPoList)) {
            detailPoList.forEach(po -> {
                DispatchRedirectDTO dispatchRedirectDto = new DispatchRedirectDTO();
                // 查询出当前组件所属哪个管道
                NifiCustomWorkflowPO workflowPo = nifiCustomWorkflowImpl.query().eq("workflow_id", po.workflowId).one();
                dispatchRedirectDto.setPipeDto(NifiCustomWorkflowMap.INSTANCES.poToDto(workflowPo));
                List<NifiCustomWorkflowDetailPO> collect = detailPoList.stream()
                        .filter(Objects::nonNull)
                        // 过滤出同一管道下的组件
                        .filter(e -> e.workflowId.equalsIgnoreCase(workflowPo.workflowId))
                        .collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(collect)) {
                    dispatchRedirectDto.setComponentList(NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(collect));
                }
                list.add(dispatchRedirectDto);
            });
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, list.stream().distinct().collect(Collectors.toList()));
    }

    @Override
    public ResultEntity<List<NifiCustomWorkflowDetailDTO>> getNifiPortTaskLastListById(Long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, buildPipeEndDto(id));
    }

    /**
     * 封装当前组件的其他属性(componentFirstFlag、componentEndFlag、pipeEndFlag、pipeEndDto)
     *
     * @return void
     * @description 封装当前组件的其他属性(componentFirstFlag 、 componentEndFlag 、 pipeEndFlag 、 pipeEndDto)
     * @author Lock
     * @date 2022/6/14 10:00
     * @version v1.0
     * @params detailPo
     * @params nifiPortsHierarchyDto
     */
    private void buildNifiOtherPorts(NifiCustomWorkflowDetailPO detailPo, NifiPortsHierarchyDTO nifiPortsHierarchyDto) {
        try {
            // 判断出当前组件是否为所属组件中的第一个任务
            List<NifiCustomWorkflowDetailPO> ascList = nifiCustomWorkflowDetailImpl.query()
                    .eq("pid", detailPo.pid)
                    .eq("component_name", detailPo.componentName)
                    .list()
                    .stream()
                    .filter(Objects::nonNull)
                    // 过滤掉不绑定表的任务
                    .filter(e -> e.appId != null && !"".equals(e.appId) && e.tableId != null && !"".equals(e.tableId) && e.tableOrder != null)
                    // 根据table_order升序
                    .sorted(Comparator.comparing(NifiCustomWorkflowDetailPO::getTableOrder))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(ascList) && ascList.get(0).id == detailPo.id) {
                nifiPortsHierarchyDto.componentFirstFlag = true;
            }

            // 判断出当前组件是否为所属组件中的最后一个任务
            List<NifiCustomWorkflowDetailPO> descList = nifiCustomWorkflowDetailImpl.query()
                    .eq("pid", detailPo.pid)
                    .eq("component_name", detailPo.componentName)
                    .list()
                    .stream()
                    .filter(Objects::nonNull)
                    // 过滤掉不绑定表的任务
                    .filter(e -> e.appId != null && !"".equals(e.appId) && e.tableId != null && !"".equals(e.tableId) && e.tableOrder != null)
                    // 根据table_order降序
                    .sorted(Comparator.comparing(NifiCustomWorkflowDetailPO::getTableOrder, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(descList) && descList.get(0).id == detailPo.id) {
                nifiPortsHierarchyDto.componentFirstFlag = true;
            }

            // 判断出当前组件是否为管道内最后一个任务
            NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPo = nifiCustomWorkflowDetailImpl.query()
                    .eq("id", detailPo.pid)
                    .one();

            NifiCustomWorkflowPO workflowPO = nifiCustomWorkflowImpl.query().eq("workflow_id", detailPo.workflowId).one();

            // 有inport,没有outport,即最后一个组件
            if (nifiCustomWorkflowDetailPo != null && StringUtils.isNotBlank(nifiCustomWorkflowDetailPo.inport) && StringUtils.isBlank(nifiCustomWorkflowDetailPo.outport)) {
                nifiPortsHierarchyDto.pipeEndFlag = true;
                // 当前管道流程最后一批执行的组件集合
                // 注意去重
                nifiPortsHierarchyDto.pipeEndDto = buildPipeEndDto(workflowPO.id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("封装当前组件的其他属性报错: " + e);
        }
    }

    /**
     * 当前管道流程最后一批执行的组件集合
     *
     * @return java.util.List<com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO>
     * @description 当前管道流程最后一批执行的组件集合
     * @author Lock
     * @date 2022/6/15 14:05
     * @version v1.0
     * @params id 管道id
     */
    private List<NifiCustomWorkflowDetailDTO> buildPipeEndDto(Long id) {
        List<NifiCustomWorkflowDetailDTO> list = new ArrayList<>();

        NifiCustomWorkflowPO nifiCustomWorkflowPo = nifiCustomWorkflowImpl.query().eq("id", id).select("workflow_id").one();
        if (nifiCustomWorkflowPo == null || StringUtils.isBlank(nifiCustomWorkflowPo.workflowId)) {
            return null;
        }

        // 开始组件
        NifiCustomWorkflowDetailPO scheduleTask = nifiCustomWorkflowDetailImpl.query()
                .eq("workflow_id", nifiCustomWorkflowPo.workflowId)
                .eq("component_name", ChannelDataEnum.SCHEDULE_TASK.getName())
                .one();
        if (scheduleTask == null) {
            return null;
        }

        // 过滤出所有主任务(数据湖、数仓、分析模型),不含绑定表的组件
        List<NifiCustomWorkflowDetailDTO> listAllTask = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(nifiCustomWorkflowDetailImpl.query()
                .eq("workflow_id", nifiCustomWorkflowPo.workflowId)
                .list()
                .stream()
                .filter(Objects::nonNull)
                // 过滤出主任务
                .filter(e -> e.appId != null && !"".equals(e.appId) && (e.tableId == null || "".equals(e.tableId)))
                // 过滤出有inport,没有outport
                .filter(e -> StringUtils.isNotBlank(e.inport) && (e.outport == null || "".equals(e.outport)))
                // 当前组件的pid是开始组件的id
//                .filter(e -> e.inport.equalsIgnoreCase(String.valueOf(scheduleTask.id)))
                .collect(Collectors.toList()));

        // 过滤出所有表任务(接入+建模)
        List<NifiCustomWorkflowDetailDTO> listAllTable = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(nifiCustomWorkflowDetailImpl.query()
                .eq("workflow_id", nifiCustomWorkflowPo.workflowId)
                .list()
                .stream()
                .filter(Objects::nonNull)
                // 过滤掉不绑定表的任务
                .filter(e -> e.appId != null && !"".equals(e.appId) && e.tableId != null && !"".equals(e.tableId) && e.tableOrder != null)
                // 过滤没有inport上游的
                .filter(e -> StringUtils.isNotBlank(e.inport))
                // 当前组件的pid是开始组件的id
//                .filter(e -> e.inport.equalsIgnoreCase(String.valueOf(scheduleTask.id)))
                .collect(Collectors.toList()));

        matchingDetailDtoList(list, listAllTask, listAllTable);
        return Objects.requireNonNull(list.stream().distinct().collect(Collectors.toList()));
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
                    NifiCustomWorkflowDetailPO one1 = nifiCustomWorkflowDetailImpl.query().eq("pid", id).eq("table_order", 1).isNotNull("table_id").one();
                    NifiPortsHierarchyNextDTO nifiPortsHierarchyNextDTO = new NifiPortsHierarchyNextDTO();
                    //下一级本身
                    nifiPortsHierarchyNextDTO.itselfPort = NifiCustomWorkflowDetailMap.INSTANCES.poToDto(one1);
                    List<NifiCustomWorkflowDetailDTO> upPortList = new ArrayList<>();
                    //下一级所有的上一级
                    if (StringUtils.isBlank(one1.inport)) {
                        continue;
                    }
                    String[] split1 = one1.inport.split(",");
                    for (String inputId : split1) {
                        List<NifiCustomWorkflowDetailPO> list = nifiCustomWorkflowDetailImpl.query().eq("pid", inputId)
                                .isNotNull("table_id").orderByDesc("table_order").list();
                        if (CollectionUtils.isEmpty(list)) {
                            continue;
                        }
                        NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPO = list.get(0);
                        if (nifiCustomWorkflowDetailPO != null) {
                            NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO1 = NifiCustomWorkflowDetailMap.INSTANCES.poToDto(nifiCustomWorkflowDetailPO);
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
