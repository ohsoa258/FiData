package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqSubscribeApiConfigVO;
import pd.tangqiao.entity.TqSubscribeAppServiceConfigPO;
import pd.tangqiao.mapper.TqSubscribeAppServiceConfigMapper;
import pd.tangqiao.service.TqSubscribeAppServiceConfigService;

import java.util.List;

@Service("tqSubscribeAppServiceConfigService")
public class TqSubscribeAppServiceConfigServiceImpl extends ServiceImpl<TqSubscribeAppServiceConfigMapper, TqSubscribeAppServiceConfigPO> implements TqSubscribeAppServiceConfigService {


    @Override
    public List<TqSubscribeApiConfigVO> apilist(List<Integer> appIds) {
        return baseMapper.apilist(appIds);
    }
}
