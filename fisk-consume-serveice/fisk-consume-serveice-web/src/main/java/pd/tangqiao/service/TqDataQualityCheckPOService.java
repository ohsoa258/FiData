package pd.tangqiao.service;

import pd.tangqiao.entity.TqDataQualityCheckPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tq_data_quality_check】的数据库操作Service
* @createDate 2024-10-29 15:11:21
*/
public interface TqDataQualityCheckPOService extends IService<TqDataQualityCheckPO> {

    Object add(TqDataQualityCheckPO po);

    Object getFlowList(Integer currentPage, Integer size);

    Object check(Integer id);
}
