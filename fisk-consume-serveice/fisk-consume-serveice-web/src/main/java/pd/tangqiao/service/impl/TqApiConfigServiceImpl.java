package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqApiConfigPO;
import pd.tangqiao.entity.TqApiConfigQueryDTO;
import pd.tangqiao.entity.TqApiConfigVO;
import pd.tangqiao.mapper.TqApiConfigMapper;
import pd.tangqiao.service.TqApiConfigService;

@Service("tqApiConfigService")
public class TqApiConfigServiceImpl extends ServiceImpl<TqApiConfigMapper, TqApiConfigPO> implements TqApiConfigService {


    @Override
    public Page<TqApiConfigVO> getAll(TqApiConfigQueryDTO dto) {
        return baseMapper.getAll(dto.page,dto);
    }

    @Override
    public ResultEnum addData(TqApiConfigPO po) {
        this.save(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editData(TqApiConfigPO po) {
        this.updateById(po);
        return ResultEnum.SUCCESS;
    }
}
