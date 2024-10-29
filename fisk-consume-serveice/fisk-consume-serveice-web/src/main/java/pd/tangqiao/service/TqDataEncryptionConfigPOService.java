package pd.tangqiao.service;

import pd.tangqiao.entity.TqDataEncryptionConfigPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tq_data_encryption_config】的数据库操作Service
* @createDate 2024-10-29 16:18:19
*/
public interface TqDataEncryptionConfigPOService extends IService<TqDataEncryptionConfigPO> {

    Object getEncryptList(Integer currentPage, Integer size);

    Object editEncrypt(Integer id);

    Object add(TqDataEncryptionConfigPO po);
}
