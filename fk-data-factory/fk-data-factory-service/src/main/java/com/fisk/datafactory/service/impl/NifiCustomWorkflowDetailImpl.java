package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.entity.NifiComponentsPO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.map.NifiCustomWorkflowDetailMap;
import com.fisk.datafactory.mapper.NifiCustomWorkflowDetailMapper;
import com.fisk.datafactory.service.INifiComponent;
import com.fisk.datafactory.service.INifiCustomWorkflow;
import com.fisk.datafactory.service.INifiCustomWorkflowDetail;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wangyan and Lock
 */
@Service
@Slf4j
public class NifiCustomWorkflowDetailImpl extends ServiceImpl<NifiCustomWorkflowDetailMapper, NifiCustomWorkflowDetailPO> implements INifiCustomWorkflowDetail {

    @Resource
    INifiCustomWorkflow workflowService;
    @Resource
    NifiCustomWorkflowDetailMapper mapper;
    @Resource
    INifiComponent componentService;

    @Override
    public NifiCustomWorkflowDetailDTO addData(NifiCustomWorkflowDetailDTO dto) {

        // dto-> po
        NifiCustomWorkflowDetailPO model = NifiCustomWorkflowDetailMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null) {
            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
        }

        try {
            baseMapper.insert(model);
        } catch (Exception e) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        dto.id = model.id;
        // 保存
        return dto;
    }

    @Override
    public NifiCustomWorkflowDetailDTO getData(long id) {

        NifiCustomWorkflowDetailPO model = this.query().eq("id", id).one();
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return NifiCustomWorkflowDetailMap.INSTANCES.poToDto(model);
    }

    @Transactional
    @Override
    public ResultEnum editData(NifiCustomWorkflowDetailVO dto) {

        // 修改tb_nifi_custom_wokflow
        NifiCustomWorkflowDTO workflowDTO = dto.dto;
        if (workflowDTO == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        try {
            workflowService.editData(workflowDTO);
        } catch (Exception e) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        dto.list.stream().peek(e -> {
            NifiCustomWorkflowDetailPO po = this.getById(e.id);
            if (po != null) {
                e.flag = componentService.getById(po.componentsId).flag;
            }
        });
        List<NifiCustomWorkflowDetailPO> list = NifiCustomWorkflowDetailMap.INSTANCES.listDtoToPo(dto.list);

        // 批量保存tb_nifi_custom_wokflow_detail
        return this.updateBatchById(list) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(long id) {
        // 参数校验
        NifiCustomWorkflowDetailPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 执行删除
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum editWorkflow(NifiCustomWorkflowDetailDTO dto) {
        // 参数校验
        NifiCustomWorkflowDetailPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        if (model.componentsId != 0) {
            NifiComponentsPO componentsPo = componentService.getById(model.componentsId);
            dto.flag = componentsPo.flag;
        }

        // dto -> po
        NifiCustomWorkflowDetailPO po = NifiCustomWorkflowDetailMap.INSTANCES.dtoToPo(dto);
        // 执行修改
        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }
}
