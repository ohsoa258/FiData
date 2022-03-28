package com.fisk.task.service.nifi;

import com.fisk.task.po.TableNifiSettingPO;

/**
 * @author cfk
 */
public interface ITableNifiSettingService {

    TableNifiSettingPO getByTableId(long tableId, long tableType);
}
