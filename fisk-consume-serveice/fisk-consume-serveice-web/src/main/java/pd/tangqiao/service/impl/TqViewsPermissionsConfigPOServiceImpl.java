package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqViewsPermissionsConfigPO;
import pd.tangqiao.mapper.TqViewsPermissionsConfigPOMapper;
import pd.tangqiao.service.TqViewsPermissionsConfigPOService;

/**
 * @author 56263
 * @description 针对表【tq_views_permissions_config】的数据库操作Service实现
 * @createDate 2024-10-29 16:05:26
 */
@Service
public class TqViewsPermissionsConfigPOServiceImpl extends ServiceImpl<TqViewsPermissionsConfigPOMapper, TqViewsPermissionsConfigPO>
        implements TqViewsPermissionsConfigPOService {

    @Override
    public Object add(TqViewsPermissionsConfigPO po) {
        return this.save(po);
    }

    @Override
    public Object getVPList(Integer currentPage, Integer size) {
        return this.page(new Page<>(currentPage, size));
    }

    @Override
    public Object changePermissionById(Integer id) {
        TqViewsPermissionsConfigPO one = this.getOne(
                new LambdaQueryWrapper<TqViewsPermissionsConfigPO>()
        );
        if (one.getIsOpen() == 1) {
            one.setIsOpen(0);
        } else {
            one.setIsOpen(1);
        }
        return this.updateById(one);
    }
}




