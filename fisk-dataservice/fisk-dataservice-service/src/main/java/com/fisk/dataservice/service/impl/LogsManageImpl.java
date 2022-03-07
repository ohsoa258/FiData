package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.dto.logs.LogQueryDTO;
import com.fisk.dataservice.entity.LogPO;
import com.fisk.dataservice.mapper.LogsMapper;
import com.fisk.dataservice.service.ILogsManageService;
import com.fisk.dataservice.vo.logs.ApiLogVO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 日志接口实现类
 * @date 2022/3/7 12:36
 */
@Service
public class LogsManageImpl extends ServiceImpl<LogsMapper, LogPO> implements ILogsManageService {

    @Override
    public Page<ApiLogVO> pageFilter(LogQueryDTO query) {
        return baseMapper.filter(query.page, query.apiIds, query.appId);
    }

    @Async
    @Override
    public void saveLog(LogPO po) {
        baseMapper.insert(po);
    }
}
