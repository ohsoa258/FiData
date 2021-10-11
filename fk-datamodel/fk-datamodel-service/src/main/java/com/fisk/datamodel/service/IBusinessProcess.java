package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessAssociationDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDropDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessPushListDTO;
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
     * @param id
     * @return
     */
    ResultEnum deleteBusinessProcess(int id);

    /**
     * 获取业务过程下拉列表
     * @return
     */
    List<BusinessProcessDropDTO> getBusinessProcessDropList();

    /**
     * 根据业务过程id,发布事实表相关信息
     * @param id
     * @return
     */
    ResultEnum businessProcessPublish(int id);

    /**
     * 业务过程发布
     * @param businessProcessId
     * @return
     */
    List<ModelMetaDataDTO> businessProcessPush(int businessProcessId);

    /**
     * 更改业务过程发布状态
     * @param id
     * @param isSuccess
     */
    void updatePublishStatus(int id,int isSuccess);

    /**
     * 根据业务过程id,获取业务域id
     * @param businessProcessId
     * @return
     */
    int getBusinessId(int businessProcessId);

}
