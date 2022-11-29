package com.fisk.task.service.nifi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.task.po.TableNifiSettingPO;

/**
 * @author cfk
 */
public interface ITableNifiSettingService extends IService<TableNifiSettingPO> {

    TableNifiSettingPO getByTableId(long tableId, long tableType);
}
