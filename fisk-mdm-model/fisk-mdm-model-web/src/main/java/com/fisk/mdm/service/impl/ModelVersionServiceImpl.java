package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.entity.ModelVersionPO;
import com.fisk.mdm.map.ModelVersionMap;
import com.fisk.mdm.mapper.ModelVersionMapper;
import com.fisk.mdm.service.IModelVersionService;
import org.springframework.stereotype.Service;


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
}
