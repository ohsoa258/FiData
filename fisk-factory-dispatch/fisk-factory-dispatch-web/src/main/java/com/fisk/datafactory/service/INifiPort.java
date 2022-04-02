package com.fisk.datafactory.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datafactory.dto.tasknifi.NifiPortsDTO;
import com.fisk.datafactory.dto.tasknifi.PortRequestParamDTO;

/**
 * @author Lock
 */
public interface INifiPort {
    /**
     * nifi管道需要的数据
     *
     * @param dto dto
     * @return dto
     */
    ResultEntity<NifiPortsDTO> getFilterData(PortRequestParamDTO dto);
}
