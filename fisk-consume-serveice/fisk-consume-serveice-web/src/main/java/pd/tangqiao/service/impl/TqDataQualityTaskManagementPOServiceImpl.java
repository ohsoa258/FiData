package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import pd.tangqiao.entity.TqDataQualityTaskManagementPO;
import pd.tangqiao.service.TqDataQualityTaskManagementPOService;
import pd.tangqiao.mapper.TqDataQualityTaskManagementPOMapper;
import org.springframework.stereotype.Service;

/**
* @author 56263
* @description 针对表【tq_data_quality_task_management】的数据库操作Service实现
* @createDate 2024-10-29 14:21:05
*/
@Service
public class TqDataQualityTaskManagementPOServiceImpl extends ServiceImpl<TqDataQualityTaskManagementPOMapper, TqDataQualityTaskManagementPO>
    implements TqDataQualityTaskManagementPOService{

    /**
     * 添加质量任务
     *
     * @param po
     * @return
     */
    @Override
    public Object add(TqDataQualityTaskManagementPO po) {
        return this.save(po);
    }

    /**
     * 查询质量任务集
     *
     * @return
     */
    @Override
    public Object getQualityTasks() {
        return this.list();
    }
}




