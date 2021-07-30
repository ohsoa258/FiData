package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.fact.FactAssociationDTO;
import com.fisk.datamodel.dto.fact.FactDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IFact {

    /**
     * 获取业务过程列表
     * @param dto
     * @return
     */
    IPage<FactDTO> getFactList(QueryDTO dto);

    /**
     * 添加业务过程
     * @param dto
     * @return
     */
    ResultEnum addFact(FactDTO dto);

    /**
     * 根据id获取业务过程详情
     * @param id
     * @return
     */
    FactAssociationDTO getFactDetail(int id);

    /**
     * 更新业务过程数据
     * @param dto
     * @return
     */
    ResultEnum updateFact(FactDTO dto);

    /**
     * 删除业务过程数据
     * @param id
     * @return
     */
    ResultEnum deleteFact(int id);

}
