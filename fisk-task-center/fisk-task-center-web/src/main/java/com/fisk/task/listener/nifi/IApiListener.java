package com.fisk.task.listener.nifi;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.service.accessAndModel.AccessAndModelTableDTO;
import com.fisk.common.service.accessAndModel.LogPageQueryDTO;
import com.fisk.common.service.accessAndModel.NifiLogResultDTO;
import com.fisk.task.dto.DwLogQueryDTO;
import com.fisk.task.dto.DwLogResultDTO;

import java.util.List;

/**
 * @author lsj
 */
public interface IApiListener {

    ResultEntity<Object> apiToStg(String data);

    /**
     * dw数仓按时间获取单表nifi日志
     *
     * @param dwLogQueryDTO
     * @return
     */
    DwLogResultDTO getDwTblNifiLog(DwLogQueryDTO dwLogQueryDTO);

    /**
     * 同步日志页面获取数接/数仓的指定表的nifi同步日志  根据表id 名称 类型
     *
     * @param dto
     * @return
     */
    Page<NifiLogResultDTO> getDwAndAccessTblNifiLog(LogPageQueryDTO dto);

}
