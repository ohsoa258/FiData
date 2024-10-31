package pd.tangqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import pd.tangqiao.entity.BindApiDTO;
import pd.tangqiao.entity.TqSubscribeApiConfigPO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-29 15:00:58
 */
public interface TqSubscribeApiConfigService extends IService<TqSubscribeApiConfigPO> {

    List<TqSubscribeApiConfigPO> getAll();
    ResultEnum addData(TqSubscribeApiConfigPO po);

    ResultEnum editData(TqSubscribeApiConfigPO po);

    ResultEnum bindApi(BindApiDTO dto);
}

