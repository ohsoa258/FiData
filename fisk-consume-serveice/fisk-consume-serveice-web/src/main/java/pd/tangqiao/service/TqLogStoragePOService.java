package pd.tangqiao.service;

import pd.tangqiao.entity.TqLogStoragePO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tq_log_storage】的数据库操作Service
* @createDate 2024-10-29 15:40:52
*/
public interface TqLogStoragePOService extends IService<TqLogStoragePO> {

    Object add(TqLogStoragePO po);

    Object getLogList(Integer currentPage, Integer size);
}
