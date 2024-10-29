package pd.tangqiao.service;

import pd.tangqiao.entity.TqLogCollectionPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tq_log_collection】的数据库操作Service
* @createDate 2024-10-29 15:48:59
*/
public interface TqLogCollectionPOService extends IService<TqLogCollectionPO> {

    Object add(TqLogCollectionPO po);

    Object getCollectionList(Integer currentPage, Integer size);
}
