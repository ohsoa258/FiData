package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqAppConfigPO;
import pd.tangqiao.entity.TqAppConfigVO;
import pd.tangqiao.mapper.TqAppConfigMapper;
import pd.tangqiao.service.TqAppConfigService;

@Service("tqAppConfigService")
public class TqAppConfigServiceImpl extends ServiceImpl<TqAppConfigMapper, TqAppConfigPO> implements TqAppConfigService {


    @Override
    public Page<TqAppConfigVO> getAll(Page<TqAppConfigVO> page) {
        return baseMapper.getAll(page);
    }

    @Override
    public ResultEnum addData(TqAppConfigPO po) {
        this.save(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editData(TqAppConfigPO po) {
        this.updateById(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(int id) {
        this.removeById(id);
        return ResultEnum.SUCCESS;
    }
}
