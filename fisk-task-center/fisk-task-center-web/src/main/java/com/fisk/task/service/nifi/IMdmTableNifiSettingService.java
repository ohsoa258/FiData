package com.fisk.task.service.nifi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.task.po.mdm.MdmTableNifiSettingPO;

/**
 * @author wangjian
 */
public interface IMdmTableNifiSettingService extends IService<MdmTableNifiSettingPO> {

    MdmTableNifiSettingPO getByTableId(long tableId, long tableType);
}
