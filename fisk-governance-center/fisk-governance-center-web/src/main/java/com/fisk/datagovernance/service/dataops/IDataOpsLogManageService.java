package com.fisk.datagovernance.service.dataops;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.dto.dataops.DataOpsLogQueryDTO;
import com.fisk.datagovernance.entity.dataops.DataOpsLogPO;
import com.fisk.datagovernance.vo.dataops.DataOpsLogVO;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维日志接口
 * @date 2022/4/22 12:33
 */
public interface IDataOpsLogManageService extends IService<DataOpsLogPO> {

    /**
     * 分页查询
     *
     * @return 分页列表
     */
    Page<DataOpsLogVO> getAll(DataOpsLogQueryDTO query);

    /**
     * 保存日志
     */
    void saveLog(DataOpsLogPO po);
}
