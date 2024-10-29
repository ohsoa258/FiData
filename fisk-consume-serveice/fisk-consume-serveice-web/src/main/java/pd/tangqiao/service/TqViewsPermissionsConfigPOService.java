package pd.tangqiao.service;

import pd.tangqiao.entity.TqViewsPermissionsConfigPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tq_views_permissions_config】的数据库操作Service
* @createDate 2024-10-29 16:05:26
*/
public interface TqViewsPermissionsConfigPOService extends IService<TqViewsPermissionsConfigPO> {

    Object add(TqViewsPermissionsConfigPO po);

    Object getVPList(Integer currentPage, Integer size);

    Object changePermissionById(Integer id);
}
