package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqDatasourceConfigPO;
import pd.tangqiao.mapper.TqDatasourceConfigPOMapper;
import pd.tangqiao.service.TqDatasourceConfigPOService;

/**
 * @author 56263
 * @description 针对表【tq_datasource_config】的数据库操作Service实现
 * @createDate 2024-10-29 10:23:59
 */
@Service
public class TqDatasourceConfigPOServiceImpl extends ServiceImpl<TqDatasourceConfigPOMapper, TqDatasourceConfigPO>
        implements TqDatasourceConfigPOService {

    /**
     * 添加数据源
     *
     * @param po
     * @return
     */
    @Override
    public Object add(TqDatasourceConfigPO po) {
        return this.save(po);
    }

    /**
     * 编辑数据源
     *
     * @param po
     * @return
     */
    @Override
    public Object edit(TqDatasourceConfigPO po) {
        return this.updateById(po);
    }

    /**
     * 删除数据源
     *
     * @param id
     * @return
     */
    @Override
    public Object del(Integer id) {
        return removeById(id);
    }

    /**
     * 分页回显
     *
     * @param currentPage
     * @param size
     * @return
     */
    @Override
    public Object pageFilter(Integer currentPage, Integer size, Integer type) {
        Page<TqDatasourceConfigPO> tqDatasourceConfigPOPage = new Page<>(currentPage, size);
        return this.page(tqDatasourceConfigPOPage,
                new LambdaQueryWrapper<TqDatasourceConfigPO>().eq(TqDatasourceConfigPO::getSourceType, type)
        );
    }

}




