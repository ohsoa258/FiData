package com.fisk.datagovernance.service.impl.dataops;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.dto.dataops.DataOpsLogQueryDTO;
import com.fisk.datagovernance.entity.dataops.DataOpsLogPO;
import com.fisk.datagovernance.mapper.dataops.DataOpsLogMapper;
import com.fisk.datagovernance.service.dataops.IDataOpsLogManageService;
import com.fisk.datagovernance.vo.dataops.DataOpsLogVO;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维日志实现类
 * @date 2022/4/22 12:32
 */
@Service
public class DataOpsLogManageImpl extends ServiceImpl<DataOpsLogMapper, DataOpsLogPO> implements IDataOpsLogManageService {
    @Override
    public Page<DataOpsLogVO> getAll(DataOpsLogQueryDTO query) {
        return baseMapper.getAll(query.page, query.keyword);
    }
}
