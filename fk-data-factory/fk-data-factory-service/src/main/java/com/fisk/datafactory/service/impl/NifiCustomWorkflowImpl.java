package com.fisk.datafactory.service.impl;

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
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.map.NifiCustomWorkflowMap;
import com.fisk.datafactory.mapper.NifiCustomWorkflowMapper;
import com.fisk.datafactory.service.INifiCustomWorkflow;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * @author wangyan and Lock
 */
@Service
@Slf4j
public class NifiCustomWorkflowImpl extends ServiceImpl<NifiCustomWorkflowMapper, NifiCustomWorkflowPO> implements INifiCustomWorkflow {

    @Resource
    NifiCustomWorkflowMapper mapper;
    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private GetMetadata getMetadata;

    @Override
    public ResultEnum addData(NifiCustomWorkflowDTO dto) {

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
    public NifiCustomWorkflowDTO getData(long id) {

        // 查询
        NifiCustomWorkflowPO po = this.query().eq("id", id).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return NifiCustomWorkflowMap.INSTANCES.poToDto(po);
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

    @Override
    public ResultEnum deleteData(long id) {

        // 参数校验
        NifiCustomWorkflowPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 执行删除
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<FilterFieldDTO> getColumn() {

        return getMetadata.getMetadataList(
                "dmp_taskfactory_db",
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
