package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.DsTableDTO;
import com.fisk.common.response.ResultEntity;

/**
 * @author WangYan
 * @date 2022/3/4 11:22
 */
public interface DsTableService {

    /**
     * 根据数据源连接获取表名
     * @param id
     * @return
     */
    ResultEntity<DsTableDTO> getTableInfo(Integer id);
}
