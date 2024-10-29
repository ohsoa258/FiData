package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqDataQualityCheckPO;
import pd.tangqiao.mapper.TqDataQualityCheckPOMapper;
import pd.tangqiao.service.TqDataQualityCheckPOService;

/**
 * @author 56263
 * @description 针对表【tq_data_quality_check】的数据库操作Service实现
 * @createDate 2024-10-29 15:11:21
 */
@Service
public class TqDataQualityCheckPOServiceImpl extends ServiceImpl<TqDataQualityCheckPOMapper, TqDataQualityCheckPO>
        implements TqDataQualityCheckPOService {

    @Override
    public Object add(TqDataQualityCheckPO po) {
        return this.save(po);
    }

    @Override
    public Object getFlowList(Integer currentPage, Integer size) {

        return this.page(new Page<>(currentPage, size));
    }
}




