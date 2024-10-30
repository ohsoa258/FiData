package pd.tangqiao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import pd.tangqiao.entity.BindApiDTO;
import pd.tangqiao.entity.TqCenterApiConfigPO;
import pd.tangqiao.entity.TqCenterApiConfigQueryDTO;
import pd.tangqiao.entity.TqCenterApiConfigVO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-29 16:04:32
 */
public interface TqCenterApiConfigService extends IService<TqCenterApiConfigPO> {

    /**
     * 应用列表分页功能
     *
     * @param dto
     * @return 查询结果
     */
    Page<TqCenterApiConfigVO> getAll(TqCenterApiConfigQueryDTO dto);
    List<TqCenterApiConfigPO> getAllApi();

    ResultEnum addData(TqCenterApiConfigPO po);

    ResultEnum editData(TqCenterApiConfigPO po);

    ResultEnum bindApi(BindApiDTO dto);
}

