package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqLogCollectionPO;
import pd.tangqiao.mapper.TqLogCollectionPOMapper;
import pd.tangqiao.service.TqLogCollectionPOService;

/**
 * @author 56263
 * @description 针对表【tq_log_collection】的数据库操作Service实现
 * @createDate 2024-10-29 15:48:59
 */
@Service
public class TqLogCollectionPOServiceImpl extends ServiceImpl<TqLogCollectionPOMapper, TqLogCollectionPO>
        implements TqLogCollectionPOService {

    @Override
    public Object add(TqLogCollectionPO po) {
        return this.save(po);
    }

    @Override
    public Object getCollectionList(Integer currentPage, Integer size) {
        return this.page(new Page<>(currentPage, size));
    }
}




