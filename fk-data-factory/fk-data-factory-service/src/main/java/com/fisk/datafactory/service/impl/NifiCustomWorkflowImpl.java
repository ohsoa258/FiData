package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.constants.FilterSqlConstants;
import com.fisk.common.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowPageDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowQueryDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.map.NifiCustomWorkflowDetailMap;
import com.fisk.datafactory.map.NifiCustomWorkflowMap;
import com.fisk.datafactory.mapper.NifiCustomWorkflowDetailMapper;
import com.fisk.datafactory.mapper.NifiCustomWorkflowMapper;
import com.fisk.datafactory.service.INifiCustomWorkflow;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import lombok.extern.slf4j.Slf4j;
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
        return vo;
    }

    @Override
    public ResultEnum editData(NifiCustomWorkflowDTO dto) {

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

    @Transactional
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

        return baseMapper.filter(query.page, data);
    }
}
