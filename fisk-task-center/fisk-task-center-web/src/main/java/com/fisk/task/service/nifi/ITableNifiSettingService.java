package com.fisk.task.service.nifi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import com.fisk.task.po.TableNifiSettingPO;

/**
 * @author cfk
 */
public interface ITableNifiSettingService extends IService<TableNifiSettingPO> {

    TableNifiSettingPO getByTableId(long tableId, long tableType);

    ResultEntity<TableServiceDTO> enableOrDisable(TableServiceDTO tableServiceDTO);
}
