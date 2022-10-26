package com.fisk.task.service.task;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.task.entity.TBETLIncrementalPO;

import java.util.List;
import java.util.Map;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/4 20:49
 * Description:
 */

public interface ITBETLIncremental extends IService<TBETLIncrementalPO> {

    /**
     * converSql
     *
     * @param tableName
     * @param sql
     * @param driveType
     * @return Map<String, String>
     */
    Map<String, String> converSql(String tableName, String sql, String driveType, List<DeltaTimeDTO> deltaTimes);
    /**
     * addEtlIncremental
     *
     * @param tableName
     * @return void
     */
    void addEtlIncremental(String tableName);
}
