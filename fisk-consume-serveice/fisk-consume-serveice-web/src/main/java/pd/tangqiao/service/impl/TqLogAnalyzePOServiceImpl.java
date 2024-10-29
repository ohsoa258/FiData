package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqLogAnalyzePO;
import pd.tangqiao.mapper.TqLogAnalyzePOMapper;
import pd.tangqiao.service.TqLogAnalyzePOService;

/**
 * @author 56263
 * @description 针对表【tq_log_analyze】的数据库操作Service实现
 * @createDate 2024-10-29 15:56:49
 */
@Service
public class TqLogAnalyzePOServiceImpl extends ServiceImpl<TqLogAnalyzePOMapper, TqLogAnalyzePO>
        implements TqLogAnalyzePOService {

    @Override
    public Object add(TqLogAnalyzePO po) {
        return this.save(po);
    }

    @Override
    public Object getAnalyzeList(Integer currentPage, Integer size) {
        return this.page(new Page<>(currentPage, size));
    }
}




