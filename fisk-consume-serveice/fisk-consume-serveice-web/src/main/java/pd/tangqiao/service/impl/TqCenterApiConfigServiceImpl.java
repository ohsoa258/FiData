package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqCenterApiConfigPO;
import pd.tangqiao.entity.TqCenterApiConfigQueryDTO;
import pd.tangqiao.entity.TqCenterApiConfigVO;
import pd.tangqiao.mapper.TqCenterApiConfigMapper;
import pd.tangqiao.service.TqCenterApiConfigService;

@Service("tqCenterApiConfigService")
public class TqCenterApiConfigServiceImpl extends ServiceImpl<TqCenterApiConfigMapper, TqCenterApiConfigPO> implements TqCenterApiConfigService {


    @Override
    public Page<TqCenterApiConfigVO> getAll(TqCenterApiConfigQueryDTO dto) {
        return baseMapper.getAll(dto.page,dto);
    }

    @Override
    public ResultEnum addData(TqCenterApiConfigPO po) {
        this.save(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editData(TqCenterApiConfigPO po) {
        this.updateById(po);
        return ResultEnum.SUCCESS;
    }
}
