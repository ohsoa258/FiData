package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.dataaccess.dto.table.TableSyncmodeDTO;
import com.fisk.dataaccess.entity.TableSyncmodePO;
import com.fisk.dataaccess.vo.datafactory.SyncTableCountVO;

/**
 * @author Lock
 */
public interface ITableSyncmode extends IService<TableSyncmodePO> {

    /**
     * 数据接入首页追加，全量，业务主键，业务时间覆盖的统计与展示
     * @return 查询结果
     */
    SyncTableCountVO mainPageCheck(Long appid);

}
