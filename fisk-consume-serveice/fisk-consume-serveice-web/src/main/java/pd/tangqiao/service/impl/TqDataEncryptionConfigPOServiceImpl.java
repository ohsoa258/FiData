package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqDataEncryptionConfigPO;
import pd.tangqiao.mapper.TqDataEncryptionConfigPOMapper;
import pd.tangqiao.service.TqDataEncryptionConfigPOService;

/**
 * @author 56263
 * @description 针对表【tq_data_encryption_config】的数据库操作Service实现
 * @createDate 2024-10-29 16:18:19
 */
@Service
public class TqDataEncryptionConfigPOServiceImpl extends ServiceImpl<TqDataEncryptionConfigPOMapper, TqDataEncryptionConfigPO>
        implements TqDataEncryptionConfigPOService {

    @Override
    public Object getEncryptList(Integer currentPage, Integer size) {
        return this.page(new Page<>(currentPage, size));
    }

    @Override
    public Object editEncrypt(Integer id) {
        TqDataEncryptionConfigPO one = this.getOne(
                new LambdaQueryWrapper<TqDataEncryptionConfigPO>()
        );
        if (one.getIsEncrypted() == 1) {
            one.setIsEncrypted(0);
        } else {
            one.setIsEncrypted(1);
        }
        return this.updateById(one);
    }

    @Override
    public Object add(TqDataEncryptionConfigPO po) {
        return this.save(po);
    }
}




