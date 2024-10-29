package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqLabelDataManagementPO;
import pd.tangqiao.mapper.TqLabelDataManagementPOMapper;
import pd.tangqiao.service.TqLabelDataManagementPOService;

import java.util.List;

/**
 * @author 56263
 * @description 针对表【tq_label_data_management】的数据库操作Service实现
 * @createDate 2024-10-29 14:35:34
 */
@Service
public class TqLabelDataManagementPOServiceImpl extends ServiceImpl<TqLabelDataManagementPOMapper, TqLabelDataManagementPO>
        implements TqLabelDataManagementPOService {

    /**
     * @param dtos
     * @return
     */
    @Override
    public Object add(List<TqLabelDataManagementPO> dtos) {
        return this.saveBatch(dtos);
    }

    /**
     * 获取标签集合
     *
     * @return
     */
    @Override
    public Object getLables() {
        return this.list();
    }
}




