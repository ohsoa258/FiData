package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.dto.businessprocess.*;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IBusinessProcess {

    /**
     * 获取业务过程列表
     * @param dto
     * @return
     */
    IPage<BusinessProcessDTO> getBusinessProcessList(QueryDTO dto);

    /**
     * 添加业务过程
     * @param dto
     * @return
     */
    ResultEnum addBusinessProcess(BusinessProcessDTO dto);

    /**
     * 根据id获取业务过程详情
     * @param id
     * @return
     */
    BusinessProcessAssociationDTO getBusinessProcessDetail(int id);

    /**
     * 更新业务过程数据
     * @param dto
     * @return
     */
    ResultEnum updateBusinessProcess(BusinessProcessDTO dto);

    /**
     * 删除业务过程数据
     * @param ids
     * @return
     */
    ResultEnum deleteBusinessProcess(List<Integer> ids);

    /**
     * 获取业务过程下拉列表
     * @return
     */
    List<BusinessProcessDropDTO> getBusinessProcessDropList();

    /**
     * 根据业务过程id集合,发布事实表相关信息
     * @param dto
     * @return
     */
    ResultEnum businessProcessPublish(BusinessProcessPublishDTO dto);

    /**
     * 业务过程发布
     * @param businessProcessId
     * @return
     */
    List<ModelMetaDataDTO> businessProcessPush(int businessProcessId);

    /**
     * 根据业务过程id,获取业务域id
     * @param factId
     * @return
     */
    BusinessAreaContentDTO getBusinessId(int factId);

    /**
     * 根据业务域id,获取业务过程列表
     * @param businessAreaId
     * @return
     */
    List<BusinessProcessListDTO> getBusinessProcessList(int businessAreaId);

    /**
     * 批量发布事实表
     * @param dto
     * @return
     */
    ResultEnum batchPublishBusinessProcess(BusinessProcessPublishQueryDTO dto);

}
