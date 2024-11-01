package pd.tangqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import pd.tangqiao.entity.TqSubscribeApiConfigVO;
import pd.tangqiao.entity.TqSubscribeAppServiceConfigPO;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-30 15:38:27
 */
public interface TqSubscribeAppServiceConfigService extends IService<TqSubscribeAppServiceConfigPO> {

    List<TqSubscribeApiConfigVO> apilist(List<Integer> appIds);
}

