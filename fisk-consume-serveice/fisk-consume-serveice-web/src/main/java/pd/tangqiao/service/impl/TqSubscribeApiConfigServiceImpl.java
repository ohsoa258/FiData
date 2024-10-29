package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqSubscribeApiConfigPO;
import pd.tangqiao.mapper.TqSubscribeApiConfigMapper;
import pd.tangqiao.service.TqSubscribeApiConfigService;

import java.util.List;

@Service("tqSubscribeApiConfigService")
public class TqSubscribeApiConfigServiceImpl extends ServiceImpl<TqSubscribeApiConfigMapper, TqSubscribeApiConfigPO> implements TqSubscribeApiConfigService {


    @Override
    public List<TqSubscribeApiConfigPO> getAll() {
        return this.list();
    }

    @Override
    public ResultEnum addData(TqSubscribeApiConfigPO po) {
        this.save(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editData(TqSubscribeApiConfigPO po) {
        this.updateById(po);
        return ResultEnum.SUCCESS;
    }
}
