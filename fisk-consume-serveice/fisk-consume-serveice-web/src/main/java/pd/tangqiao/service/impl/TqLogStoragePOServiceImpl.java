package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqLogStoragePO;
import pd.tangqiao.mapper.TqLogStoragePOMapper;
import pd.tangqiao.service.TqLogStoragePOService;

/**
 * @author 56263
 * @description 针对表【tq_log_storage】的数据库操作Service实现
 * @createDate 2024-10-29 15:40:52
 */
@Service
public class TqLogStoragePOServiceImpl extends ServiceImpl<TqLogStoragePOMapper, TqLogStoragePO>
        implements TqLogStoragePOService {

    @Override
    public Object add(TqLogStoragePO po) {
        return this.save(po);
    }

    @Override
    public Object getLogList(Integer currentPage, Integer size) {
        return this.page(new Page<>(currentPage, size));
    }
}




