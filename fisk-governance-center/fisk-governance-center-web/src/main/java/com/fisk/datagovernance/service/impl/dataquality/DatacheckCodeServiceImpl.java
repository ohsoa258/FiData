package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.DatacheckCodePO;
import com.fisk.datagovernance.mapper.dataquality.DatacheckCodeMapper;
import com.fisk.datagovernance.service.dataquality.DatacheckCodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("datacheckCodeService")
public class DatacheckCodeServiceImpl extends ServiceImpl<DatacheckCodeMapper, DatacheckCodePO> implements DatacheckCodeService {


    @Resource
    private DatacheckCodeMapper datacheckCodeMapper;
    @Override
    public List<String> getCheckCodeList() {
        return datacheckCodeMapper.getCheckCodeList();
    }
}
