package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.logs.LogQueryDTO;
import com.fisk.dataservice.entity.LogPO;
import com.fisk.dataservice.vo.logs.ApiLogVO;

/**
 * 日志接口
 *
 * @author dick
 */
public interface ILogsManageService extends IService<LogPO> {

    /**
     * 分页查询
     *
     * @return 日志列表
     */
    Page<ApiLogVO> pageFilter(LogQueryDTO query);

    /**
     * 保存数据
     * @param po 内部接口，不暴露
     * @return 执行结果
     */
    void saveLog(LogPO po);
}
