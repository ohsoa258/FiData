package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqAppServiceConfigPO;
import pd.tangqiao.mapper.TqAppServiceConfigMapper;
import pd.tangqiao.service.TqAppServiceConfigService;

@Service("tqAppServiceConfigService")
public class TqAppServiceConfigServiceImpl extends ServiceImpl<TqAppServiceConfigMapper, TqAppServiceConfigPO> implements TqAppServiceConfigService {


}
