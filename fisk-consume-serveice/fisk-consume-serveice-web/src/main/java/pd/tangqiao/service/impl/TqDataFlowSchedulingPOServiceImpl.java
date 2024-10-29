package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqDataFlowSchedulingPO;
import pd.tangqiao.mapper.TqDataFlowSchedulingPOMapper;
import pd.tangqiao.service.TqDataFlowSchedulingPOService;

/**
 * @author 56263
 * @description 针对表【tq_data_flow_scheduling】的数据库操作Service实现
 * @createDate 2024-10-29 15:04:26
 */
@Service
public class TqDataFlowSchedulingPOServiceImpl extends ServiceImpl<TqDataFlowSchedulingPOMapper, TqDataFlowSchedulingPO>
        implements TqDataFlowSchedulingPOService {

    /**
     * 数据流程调度新增
     *
     * @param po
     * @return
     */
    @Override
    public Object add(TqDataFlowSchedulingPO po) {
        return this.save(po);
    }

    /**
     * 数据流程调度回显
     *
     * @return
     */
    @Override
    public Object getFlowList() {
        return this.list();
    }

}




