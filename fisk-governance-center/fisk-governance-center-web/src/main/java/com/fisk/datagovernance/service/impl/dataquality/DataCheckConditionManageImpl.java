package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.DataCheckConditionPO;
import com.fisk.datagovernance.mapper.dataquality.DataCheckConditionMapper;
import com.fisk.datagovernance.service.dataquality.IDataCheckConditionManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version v1.0
 * @description 数据校验规则-检查条件
 * @date 2022/3/22 14:51
 */
@Service
public class DataCheckConditionManageImpl extends ServiceImpl<DataCheckConditionMapper, DataCheckConditionPO>
        implements IDataCheckConditionManageService {

}
