package pd.tangqiao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import pd.tangqiao.entity.TqApiConfigPO;
import pd.tangqiao.entity.TqApiConfigQueryDTO;
import pd.tangqiao.entity.TqApiConfigVO;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-29 13:46:14
 */
public interface TqApiConfigService extends IService<TqApiConfigPO> {

    /**
     * 应用列表分页功能
     *
     * @param dto
     * @return 查询结果
     */
    Page<TqApiConfigVO> getAll(TqApiConfigQueryDTO dto);

    ResultEnum addData(TqApiConfigPO po);

    ResultEnum editData(TqApiConfigPO po);
}

