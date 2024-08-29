package com.fisk.task.service.pipeline;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.task.dto.accessmdm.LogResultDTO;
import com.fisk.task.entity.TBETLlogPO;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

public interface IEtlLog extends IService<TBETLlogPO> {

    /**
     * 获取数据接入表最后同步时间
     *
     * @param tblNames
     * @return
     */
    LocalDateTime getAccessTblLastSyncTime(List<String> tblNames);

    /**
     * 主数据获取发布日志结果
     * @param subRunIds
     * @return
     */
    List<LogResultDTO> getMdmTblNifiLog(@RequestBody List<String> subRunIds);
}
