package com.fisk.datagovernance.service.monitor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.monitor.ServerMonitorTypeDTO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorTypePO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorTypeVO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-11 15:24:49
 */
public interface ServerMonitorTypeService extends IService<ServerMonitorTypePO> {

    ResultEnum addOrUpdateServerMonitorType(ServerMonitorTypeDTO serverMonitorTypeDTO);

    ResultEnum deleteServerMonitorType(Integer id);

    List<ServerMonitorTypeVO> getServerMonitorType();
}

