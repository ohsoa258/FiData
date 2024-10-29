package pd.tangqiao.service;

import pd.tangqiao.entity.TqDataFlowSchedulingPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 56263
* @description 针对表【tq_data_flow_scheduling】的数据库操作Service
* @createDate 2024-10-29 15:04:26
*/
public interface TqDataFlowSchedulingPOService extends IService<TqDataFlowSchedulingPO> {

    /**
     * 数据流程调度新增
     *
     * @param po
     * @return
     */
    Object add(TqDataFlowSchedulingPO po);

    /**
     * 数据流程调度回显
     *
     * @return
     */
    Object getFlowList();

}
