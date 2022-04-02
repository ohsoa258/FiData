package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.map.ModelMap;
import com.fisk.mdm.mapper.ModelMapper;
import com.fisk.mdm.service.IModelService;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.vo.model.ModelVO;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service()
public class ModelServiceImpl extends ServiceImpl<ModelMapper, ModelPO> implements IModelService {

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Override
    public ResultEntity<ModelVO> getById(Integer id) {
        ModelVO modelVO = ModelMap.INSTANCES.poToVo(baseMapper.selectById(id));
        if(Objects.isNull(modelVO)){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, modelVO);
    }

    /**
     * 添加模型
     * @param model
     * @return
     */
    @Override
    public ResultEnum addData(ModelDTO model) {
        boolean isInsert = baseMapper.insert(ModelMap.INSTANCES.dtoToPo(model)) > 0;
        if (!isInsert) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 编辑
     * @param model
     * @return
     */
    @Override
    public ResultEnum editData(ModelDTO model) {
        boolean isUpData = baseMapper.updateById(ModelMap.INSTANCES.dtoToPo(model)) > 0;
        if (!isUpData) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @Override
    public ResultEnum deleteDataById(Integer id) {
        boolean isDelete = baseMapper.deleteById(id) > 0;
        if (!isDelete) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 分页查询
     * @param query
     * @return
     */
    @Override
    public Page<ModelVO> getAll(ModelQueryDTO query) {
        return baseMapper.getAll(query.page, query);
    }
}
