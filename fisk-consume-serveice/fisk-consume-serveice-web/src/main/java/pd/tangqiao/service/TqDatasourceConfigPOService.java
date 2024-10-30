package pd.tangqiao.service;

import pd.tangqiao.entity.TqDatasourceConfigPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tq_datasource_config】的数据库操作Service
* @createDate 2024-10-29 10:23:59
*/
public interface TqDatasourceConfigPOService extends IService<TqDatasourceConfigPO> {

    /**
     * 添加数据源
     *
     * @param po
     * @return
     */
    Object add(TqDatasourceConfigPO po);

    /**
     * 编辑数据源
     *
     * @param po
     * @return
     */
    Object edit(TqDatasourceConfigPO po);

    /**
     * 删除数据源
     *
     * @param id
     * @return
     */
    Object del(Integer id);

    /**
     * 分页回显
     *
     * @param currentPage
     * @param size
     * @return
     */
    Object pageFilter(Integer currentPage, Integer size,Integer type);

}
