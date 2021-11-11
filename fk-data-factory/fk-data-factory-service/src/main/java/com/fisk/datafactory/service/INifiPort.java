package com.fisk.datafactory.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.datafactory.dto.tasknifi.NifiPortsDTO;
import com.fisk.datafactory.dto.tasknifi.PortRequestParamDTO;

/**
 * @author Lock
 */
public interface INifiPort {
    ResultEntity<NifiPortsDTO> getFilterData(PortRequestParamDTO dto);
}
