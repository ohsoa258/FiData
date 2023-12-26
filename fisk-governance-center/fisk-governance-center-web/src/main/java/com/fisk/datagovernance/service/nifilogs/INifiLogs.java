package com.fisk.datagovernance.service.nifilogs;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.accessAndModel.AccessAndModelTreeDTO;
import com.fisk.common.service.accessAndModel.LogPageQueryDTO;
import com.fisk.common.service.accessAndModel.NifiLogResultDTO;

public interface INifiLogs {

    /**
     * 同步日志页面获取数接和数仓的 应用--表   树形结构
     *
     * @return
     */
    AccessAndModelTreeDTO getAccessAndModelTree();

    /**
     * 同步日志页面获取数接/数仓的指定表的nifi同步日志
     *
     * @param dto
     * @return
     */
    Page<NifiLogResultDTO> getTableNifiLogs(LogPageQueryDTO dto);
}
