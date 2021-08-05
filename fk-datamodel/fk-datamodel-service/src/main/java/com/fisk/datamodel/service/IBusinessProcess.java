package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessAssociationDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDropDTO;

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

}
