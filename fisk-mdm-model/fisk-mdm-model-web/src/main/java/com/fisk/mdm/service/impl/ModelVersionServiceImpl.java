package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.entity.ModelVersionPO;
import com.fisk.mdm.map.ModelVersionMap;
import com.fisk.mdm.mapper.ModelVersionMapper;
import com.fisk.mdm.service.IModelVersionService;
import com.fisk.mdm.vo.modelVersion.ModelVersionVO;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author ChenYa
 */
@Service
public class ModelVersionServiceImpl extends ServiceImpl<ModelVersionMapper, ModelVersionPO> implements IModelVersionService {

    /**
     * 新增模型版本
     * @param dto
     * @return
     */
    @Override
    public ResultEnum addData(ModelVersionDTO dto) {

        if(baseMapper.insert(ModelVersionMap.INSTANCES.dtoToPo(dto)) <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<ModelVersionVO> getByModelId(Integer modelId) {

        QueryWrapper<ModelVersionPO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ModelVersionPO::getModelId,modelId);

        return ModelVersionMap.INSTANCES.poToVoList(baseMapper.selectList(wrapper));
    }
}
