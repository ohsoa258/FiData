package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.entity.ModelVersionPO;
import com.fisk.mdm.map.ModelVersionMap;
import com.fisk.mdm.mapper.ModelVersionMapper;
import com.fisk.mdm.service.IModelVersionService;
import com.fisk.mdm.vo.modelVersion.ModelVersionDropDownVO;
import com.fisk.mdm.vo.modelVersion.ModelVersionVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author ChenYa
 */
@Service
public class ModelVersionServiceImpl extends ServiceImpl<ModelVersionMapper, ModelVersionPO> implements IModelVersionService {


    @Resource
    ModelVersionMapper modelVersionMapper;

    /**
     * 新增模型版本
     * @param dto
     * @return
     */
    @Override
    public ResultEnum addData(ModelVersionDTO dto) {
        QueryWrapper<ModelVersionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ModelVersionPO::getName,dto.getName());
        ModelVersionPO modelVersionPo = modelVersionMapper.selectOne(queryWrapper);
        if (modelVersionPo != null){
            return ResultEnum.DATA_EXISTS;
        }

        return modelVersionMapper.insert(ModelVersionMap.INSTANCES.dtoToPo(dto)) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<ModelVersionVO> getByModelId(Integer modelId) {

        QueryWrapper<ModelVersionPO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ModelVersionPO::getModelId,modelId);

        return ModelVersionMap.INSTANCES.poToVoList(baseMapper.selectList(wrapper));
    }

    /**
     * 获取模型版本列表
     * @param modelId
     * @return
     */
    public List<ModelVersionDropDownVO> getModelVersionDropDown(int modelId){
        QueryWrapper<ModelVersionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(ModelVersionPO::getModelId,modelId);
        List<ModelVersionPO> list=baseMapper.selectList(queryWrapper);
        return ModelVersionMap.INSTANCES.poListToDropDownVoList(list);
    }
}
