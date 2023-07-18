package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.DataCheckLogsPO;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验日志接口
 * @date 2022/3/23 12:22
 */
public interface IDataCheckLogsManageService extends IService<DataCheckLogsPO> {

    /**
     * 保存数据
     * @param pos 内部接口，不暴露
     * @return 执行结果
     */
    void saveLog(List<DataCheckLogsPO> pos);

}
