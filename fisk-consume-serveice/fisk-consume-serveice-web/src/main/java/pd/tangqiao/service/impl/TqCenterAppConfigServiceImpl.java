package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqCenterApiConfigPO;
import pd.tangqiao.entity.TqCenterAppConfigPO;
import pd.tangqiao.entity.TqCenterAppConfigVO;
import pd.tangqiao.mapper.TqCenterAppConfigMapper;
import pd.tangqiao.service.TqCenterApiConfigService;
import pd.tangqiao.service.TqCenterAppConfigService;

import javax.annotation.Resource;

@Service("tqCenterAppConfigService")
public class TqCenterAppConfigServiceImpl extends ServiceImpl<TqCenterAppConfigMapper, TqCenterAppConfigPO> implements TqCenterAppConfigService {


    @Resource
    TqCenterApiConfigService apiConfigService;
    @Override
    public Page<TqCenterAppConfigVO> getAll(Page<TqCenterAppConfigVO> page) {
        return baseMapper.getAll(page);
    }

    @Override
    public ResultEnum addData(TqCenterAppConfigPO po) {
        this.save(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editData(TqCenterAppConfigPO po) {
        this.updateById(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(int id) {
        this.removeById(id);
        LambdaQueryWrapper<TqCenterApiConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TqCenterApiConfigPO::getAppId,id);
        apiConfigService.remove(queryWrapper);
        return ResultEnum.SUCCESS;
    }
}
