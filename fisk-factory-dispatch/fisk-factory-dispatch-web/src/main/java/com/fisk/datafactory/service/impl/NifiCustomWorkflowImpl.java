package com.fisk.datafactory.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowNumDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowPageDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowQueryDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datafactory.map.NifiCustomWorkflowDetailMap;
import com.fisk.datafactory.map.NifiCustomWorkflowMap;
import com.fisk.datafactory.mapper.NifiCustomWorkflowDetailMapper;
import com.fisk.datafactory.mapper.NifiCustomWorkflowMapper;
import com.fisk.datafactory.service.INifiCustomWorkflow;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.task.client.PublishTaskClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author wangyan and Lock
 */
@Service
@Slf4j
public class NifiCustomWorkflowImpl extends ServiceImpl<NifiCustomWorkflowMapper, NifiCustomWorkflowPO> implements INifiCustomWorkflow {

    @Resource
    NifiCustomWorkflowMapper mapper;
    @Resource
    NifiCustomWorkflowDetailImpl customWorkflowDetailImpl;
    @Resource
    NifiCustomWorkflowDetailMapper detailMapper;
    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private GetMetadata getMetadata;
    @Resource
    private DataAccessClient dataAccessClient;
    @Resource
    private DataModelClient dataModelClient;
    @Resource
    PublishTaskClient publishTaskClient;


    @Override
    public ResultEnum addData(NifiCustomWorkflowDTO dto) {

        List<String> workflowNameList = this.list().stream().map(po -> po.workflowName).collect(Collectors.toList());
        if (workflowNameList.contains(dto.workflowName)) {
            return ResultEnum.WORKFLOWNAME_EXISTS;
        }

        // dto -> po
        NifiCustomWorkflowPO model = NifiCustomWorkflowMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        model.workflowId = UUID.randomUUID().toString();

        //保存
        return this.save(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public NifiCustomWorkflowDetailVO getData(long id) {

        // 查询
        NifiCustomWorkflowPO po = this.query().eq("id", id).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        NifiCustomWorkflowDetailVO vo = new NifiCustomWorkflowDetailVO();

        vo.dto = NifiCustomWorkflowMap.INSTANCES.poToDto(po);
        List<NifiCustomWorkflowDetailPO> list = customWorkflowDetailImpl.query().eq("workflow_id", po.workflowId).list();
        if (CollectionUtils.isNotEmpty(list)) {
            vo.list = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(list);
        }

        List<NifiCustomWorkflowDetailDTO> dtoList = vo.list;

        if (CollectionUtils.isNotEmpty(dtoList)) {
            for (NifiCustomWorkflowDetailDTO e : dtoList) {
                ChannelDataEnum channelDataEnum = ChannelDataEnum.getValue(e.componentType);
                switch (channelDataEnum) {
                    // 数据湖表任务
                    case DATALAKE_TASK:
                        if (e.appId == null || "".equals(e.appId) || e.tableId == null || "".equals(e.tableId)) {
                            break;
                        }
                        getDataAccessIdsDtoAccess(e, 3);
                        break;
                    // 数仓维度表任务组
                    case DW_DIMENSION_TASK:
                        if (e.appId == null || "".equals(e.appId) || e.tableId == null || "".equals(e.tableId)) {
                            break;
                        }
                        getDataAccessIdsDtoModel(e, 4);
                        break;
                    // 数仓事实表任务组
                    case DW_FACT_TASK:
                        if (e.appId == null || "".equals(e.appId) || e.tableId == null || "".equals(e.tableId)) {
                            break;
                        }
                        getDataAccessIdsDtoModel(e, 5);
                        break;
                    // 分析模型维度表任务组
                    case OLAP_DIMENSION_TASK:
                        if (e.appId == null || "".equals(e.appId) || e.tableId == null || "".equals(e.tableId)) {
                            break;
                        }
                        getDataAccessIdsDtoModel(e, 6);
                        break;
                    //分析模型事实表任务
                    case OLAP_FACT_TASK:
                        if (e.appId == null || "".equals(e.appId) || e.tableId == null || "".equals(e.tableId)) {
                            break;
                        }
                        getDataAccessIdsDtoModel(e, 7);
                        break;
                    case OLAP_WIDETABLE_TASK:
                        getDataAccessIdsDtoModel(e, 8);
                        break;
                    default:
                        break;
                }
            }
        }

        return vo;
    }

    /**
     * 组装应用名称和表名
     *
     * @return void
     * @description 组装应用名称和表名
     * @author Lock
     * @date 2022/3/18 18:26
     * @version v1.0
     * @params e
     * @params flag
     */
    private void getDataAccessIdsDtoAccess(NifiCustomWorkflowDetailDTO e, int flag) {

        DataAccessIdsDTO dto = new DataAccessIdsDTO();
        dto.appId = Long.valueOf(e.appId);
        dto.tableId = Long.valueOf(e.tableId);
        dto.flag = flag;
        try {
            ResultEntity<Object> result = dataAccessClient.getAppNameAndTableName(dto);
            getName(e, result);
        } catch (Exception ex) {
            log.error("远程调用失败，方法名：【data-access:getAppNameAndTableName】");
            e.appName = null;
            e.tableName = null;
        }
    }

    /**
     * 组装业务域名称和表名
     *
     * @return void
     * @description 组装业务域名称和表名
     * @author Lock
     * @date 2022/3/18 18:27
     * @version v1.0
     * @params e
     * @params flag
     */
    private void getDataAccessIdsDtoModel(NifiCustomWorkflowDetailDTO e, int flag) {

        DataAccessIdsDTO dto = new DataAccessIdsDTO();
        dto.appId = e.appId == null ? null : Long.valueOf(e.appId);
        dto.tableId = e.tableId == null ? null : Long.valueOf(e.tableId);
        dto.flag = flag;
        ResultEntity<Object> result = null;
        try {
            result = dataModelClient.getAppNameAndTableName(dto);
            getName(e, result);
        } catch (Exception ex) {
            log.error("远程调用失败，方法名：【data-model:getAppNameAndTableName】");
            e.appName = null;
            e.tableName = null;
        }
    }

    /**
     * feign接口调用,封装名称
     *
     * @return void
     * @description feign接口调用, 封装名称
     * @author Lock
     * @date 2022/3/18 18:28
     * @version v1.0
     * @params e
     * @params result
     */
    private void getName(NifiCustomWorkflowDetailDTO e, ResultEntity<Object> result) {
        if (result.code == 0) {
            ResultEntity<ComponentIdDTO> resultEntity = JSON.parseObject(JSON.toJSONString(result.data), ResultEntity.class);
            ComponentIdDTO dto = JSON.parseObject(JSON.toJSONString(resultEntity.data), ComponentIdDTO.class);
            e.appName = dto.appName;
            e.tableName = dto.tableName;
        }
    }

    @Override
    public ResultEnum editData(NifiCustomWorkflowDTO dto) {

        // 判断名称是否重复
        QueryWrapper<NifiCustomWorkflowPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NifiCustomWorkflowPO::getWorkflowName, dto.workflowName);
        NifiCustomWorkflowPO workflowPo = mapper.selectOne(queryWrapper);
        if (workflowPo != null && workflowPo.id != dto.id) {
            return ResultEnum.WORKFLOWNAME_EXISTS;
        }

        // 参数校验
        NifiCustomWorkflowPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        NifiCustomWorkflowPO po = NifiCustomWorkflowMap.INSTANCES.dtoToPo(dto);
        // 执行修改
        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum deleteData(long id) {

        // 参数校验
        NifiCustomWorkflowPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 执行删除
        int i = mapper.deleteByIdWithFill(model);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        List<NifiCustomWorkflowDetailPO> list = customWorkflowDetailImpl.query().eq("workflow_id", model.workflowId).list();
        try {
            if (CollectionUtils.isNotEmpty(list)) {
                List<Integer> collect = list.stream().map(e -> detailMapper.deleteByIdWithFill(e)).collect(Collectors.toList());
                publishTaskClient.deleteTableTopicByComponentId(collect);
            }
        } catch (Exception e) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public List<FilterFieldDTO> getColumn() {

        return getMetadata.getMetadataList(
                "dmp_datafactory_db",
                "tb_nifi_custom_workflow",
                "",
                FilterSqlConstants.CUSTOM_WORKFLOW_SQL);
    }

    @Override
    public Page<NifiCustomWorkflowVO> listData(NifiCustomWorkflowQueryDTO query) {
        StringBuilder querySql = new StringBuilder();
        if (query.key != null && query.key.length() > 0) {
            querySql.append(" and workflow_name like concat('%', " + "'" + query.key + "'" + ", '%') ");
        }

        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));
        NifiCustomWorkflowPageDTO data = new NifiCustomWorkflowPageDTO();
        data.page = query.page;
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();
        Page<NifiCustomWorkflowVO> filter = baseMapper.filter(query.page, data);

        try {
            if (CollectionUtils.isNotEmpty(filter.getRecords())) {
                // 分页对象添加子表组件id集合
                // TODO 调用task feign接口,查询呼吸灯状态
                ResultEntity<List<NifiCustomWorkflowVO>> result = publishTaskClient.getNifiCustomWorkflowDetails(buildRecords(filter.getRecords()));
                if (result.code == ResultEnum.SUCCESS.getCode()) {
                    filter.setRecords(result.data);
                    return filter;
                }
            }
        } catch (Exception e) {
            // 此时task异常保存,不查询呼吸灯状态
            return filter;
        }
        return filter;
    }

    /**
     * 分页对象添加子表组件id集合
     *
     * @return java.util.List<com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO>
     * @description 分页对象添加子表组件id集合
     * @author Lock
     * @date 2022/3/11 11:37
     * @version v1.0
     * @params filter
     */
    private List<NifiCustomWorkflowVO> buildRecords(List<NifiCustomWorkflowVO> records) {
        records.forEach(vo -> {
            List<Long> componentIds;
            // 查询当前管道下所有组件
            List<NifiCustomWorkflowDetailPO> workflowDetailList = customWorkflowDetailImpl.query().eq("workflow_id", vo.workflowId).list();
            // 将绑定表的组件id集合筛选出来
            componentIds = workflowDetailList.stream()
                    .filter(workflow -> StringUtils.isNotBlank(workflow.tableId))
                    .map(workflow -> workflow.id).collect(Collectors.toList());
            vo.componentIds = componentIds;
        });
        return records;
    }

    @Override
    public NifiCustomWorkflowNumDTO getNum() {
        NifiCustomWorkflowNumDTO dto = new NifiCustomWorkflowNumDTO();

        // 未发布即未运行
        dto.notRun = baseMapper.getNum(0);
        // 发布成功即成功
        dto.success = baseMapper.getNum(1);
        // 发布失败即失败
        dto.failure = baseMapper.getNum(2);
        // 正在发布即运行
        dto.running = baseMapper.getNum(3);
        return dto;
    }

    @Override
    public void updatePublishStatus(NifiCustomWorkflowDTO dto) {
        NifiCustomWorkflowPO model = baseMapper.selectById(dto.id);
        if (model != null) {
            model.status = dto.status;
            baseMapper.updateById(model);
        }
    }
}
