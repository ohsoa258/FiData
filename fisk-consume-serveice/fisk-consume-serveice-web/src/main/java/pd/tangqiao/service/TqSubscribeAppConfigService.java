package pd.tangqiao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import pd.tangqiao.entity.TqAppConfigPO;
import pd.tangqiao.entity.TqAppConfigVO;
import pd.tangqiao.entity.TqSubscribeAppConfigPO;
import pd.tangqiao.entity.TqSubscribeAppConfigVO;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-29 15:00:58
 */
public interface TqSubscribeAppConfigService extends IService<TqSubscribeAppConfigPO> {

    /**
     * 应用列表分页功能
     *
     * @param page 分页对象
     * @return 查询结果
     */
    Page<TqSubscribeAppConfigVO> getAll(Page<TqSubscribeAppConfigVO> page);

    ResultEnum addData(TqSubscribeAppConfigPO po);

    ResultEnum editData(TqSubscribeAppConfigPO po);

    ResultEnum deleteData(int id);
}

