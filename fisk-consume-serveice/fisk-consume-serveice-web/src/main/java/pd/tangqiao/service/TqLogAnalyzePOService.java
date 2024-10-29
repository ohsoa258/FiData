package pd.tangqiao.service;

import pd.tangqiao.entity.TqLogAnalyzePO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tq_log_analyze】的数据库操作Service
* @createDate 2024-10-29 15:56:49
*/
public interface TqLogAnalyzePOService extends IService<TqLogAnalyzePO> {

    Object add(TqLogAnalyzePO po);

    Object getAnalyzeList(Integer currentPage, Integer size);
}
