package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.modelVersion.ModelCopyDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionUpdateDTO;
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

    /**
     * 根据模型id查看模型版本
     * @param modelId
     * @return
     */
    List<ModelVersionVO> getByModelId(Integer modelId);

    /**
     * 修改模型版本信息
     * @param dto
     * @return
     */
    ResultEnum updateData(ModelVersionUpdateDTO dto);

    /**
     * 根据id删除版本信息
     * @param id
     * @return
     */
    ResultEnum deleteDataById(Integer id);

    /**
     * 复制数据
     * @param dto
     * @return
     */
    ResultEnum copyDataByModelId(ModelCopyDTO dto);

    /**
     * 判断模型下是否存在未发布的实体
     * @param modelId
     * @return true : 不存在   false:存在
     */
    Boolean getNotReleaseData(Integer modelId);
}
