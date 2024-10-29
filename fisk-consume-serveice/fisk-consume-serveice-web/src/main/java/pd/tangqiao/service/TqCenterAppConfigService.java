package pd.tangqiao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import pd.tangqiao.entity.TqAppConfigPO;
import pd.tangqiao.entity.TqAppConfigVO;
import pd.tangqiao.entity.TqCenterAppConfigPO;
import pd.tangqiao.entity.TqCenterAppConfigVO;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-29 16:04:32
 */
public interface TqCenterAppConfigService extends IService<TqCenterAppConfigPO> {


    /**
     * 应用列表分页功能
     *
     * @param page 分页对象
     * @return 查询结果
     */
    Page<TqCenterAppConfigVO> getAll(Page<TqCenterAppConfigVO> page);

    ResultEnum addData(TqCenterAppConfigPO po);

    ResultEnum editData(TqCenterAppConfigPO po);

    ResultEnum deleteData(int id);

}

