package com.fisk.datagovernance.service.impl.dataops;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataops.DataObsSqlPO;
import com.fisk.datagovernance.mapper.dataops.DataObsSqlMapper;
import com.fisk.datagovernance.service.dataops.DataObsSqlService;
import org.springframework.stereotype.Service;

@Service("dataObsSqlService")
public class DataObsSqlServiceImpl extends ServiceImpl<DataObsSqlMapper, DataObsSqlPO> implements DataObsSqlService {


}
