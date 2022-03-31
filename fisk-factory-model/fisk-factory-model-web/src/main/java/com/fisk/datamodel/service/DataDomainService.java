package com.fisk.datamodel.service;

import com.fisk.chartvisual.vo.DataDomainVO;
import com.fisk.datamodel.dto.DataDomain.AreaBusinessNameDTO;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/12 11:32
 */
public interface DataDomainService {

    /**
     * 获取数据域
     * @param
     * @return
     */
    List<DataDomainVO> getDataDomain();

    /**
     * 获取维度
     * @param
     * @return
     */
    Object getDimension();

    /**
     * 获取业务板块
     * @param
     * @return
     */
    List<AreaBusinessNameDTO> getBusiness();

    /**
     * 获取业务域
     * @param
     * @return
     */
    Object getAreaBusiness();
}
