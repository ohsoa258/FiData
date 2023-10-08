package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.entity.TableApiLogPO;
import com.fisk.dataservice.mapper.TableApiLogMapper;
import com.fisk.dataservice.service.ITableApiLogService;
import com.fisk.dataservice.vo.tableapi.ConsumeServerVO;
import com.fisk.dataservice.vo.tableapi.TopFrequencyVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("tableApiLogService")
public class TableApiLogServiceImpl extends ServiceImpl<TableApiLogMapper, TableApiLogPO> implements ITableApiLogService {


    @Override
    public ConsumeServerVO getConsumeServer() {
        ConsumeServerVO consumeServerVO = new ConsumeServerVO();
        consumeServerVO.setTotalNumber(this.baseMapper.getTotalNumber());
        consumeServerVO.setFocusApiTotalNumber(this.baseMapper.focusApiTotalNumber());
        consumeServerVO.setApiNumber(this.baseMapper.getApiNumber());
        consumeServerVO.setFrequency(this.baseMapper.getFrequency());
        consumeServerVO.setFaildNumber(this.baseMapper.faildNumber());
        consumeServerVO.setSuccessNumber(this.baseMapper.successNumber());
        return consumeServerVO;
    }

    @Override
    public List<TopFrequencyVO> getTopFrequency() {
        return this.baseMapper.getTopFrequency();
    }
}
