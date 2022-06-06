package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.entity.ModelVersionPO;
import com.fisk.mdm.vo.modelVersion.ModelVersionVO;

import java.util.List;

/**
 * @author ChenYa
 */
public interface IModelVersionService extends IService<ModelVersionPO> {
    /**
     * 新增模型版本
     * @param dto
     * @return
     */
    ResultEnum addData(ModelVersionDTO dto);

    List<ModelVersionVO> getByModelId(Integer modelId);
}
