package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.DatacheckServerFieldConfigPO;
import com.fisk.datagovernance.mapper.dataquality.DatacheckServerFieldConfigMapper;
import com.fisk.datagovernance.service.dataquality.DatacheckServerFieldConfigService;
import org.springframework.stereotype.Service;

@Service("datacheckServerFieldConfigService")
public class DatacheckServerFieldConfigServiceImpl extends ServiceImpl<DatacheckServerFieldConfigMapper, DatacheckServerFieldConfigPO> implements DatacheckServerFieldConfigService {


}
