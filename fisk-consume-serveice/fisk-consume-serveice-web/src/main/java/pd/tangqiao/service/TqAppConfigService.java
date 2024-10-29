package pd.tangqiao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import pd.tangqiao.entity.TqAppConfigPO;
import pd.tangqiao.entity.TqAppConfigVO;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-29 13:42:34
 */
public interface TqAppConfigService extends IService<TqAppConfigPO> {

    /**
     * 应用列表分页功能
     *
     * @param page 分页对象
     * @return 查询结果
     */
    Page<TqAppConfigVO> getAll(Page<TqAppConfigVO> page);

    ResultEnum addData(TqAppConfigPO po);

    ResultEnum editData(TqAppConfigPO po);

    ResultEnum deleteData(int id);
}

