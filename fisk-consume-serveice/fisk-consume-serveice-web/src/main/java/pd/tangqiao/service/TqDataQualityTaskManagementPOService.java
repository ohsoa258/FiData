package pd.tangqiao.service;

import pd.tangqiao.entity.TqDataQualityTaskManagementPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tq_data_quality_task_management】的数据库操作Service
* @createDate 2024-10-29 14:21:05
*/
public interface TqDataQualityTaskManagementPOService extends IService<TqDataQualityTaskManagementPO> {

    /**
     * 添加质量任务
     * @param po
     * @return
     */
    Object add(TqDataQualityTaskManagementPO po);

    /**
     * 查询质量任务集
     * @return
     */
    Object getQualityTasks();
}
