package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.BindApiDTO;
import pd.tangqiao.entity.TqAppServiceConfigPO;
import pd.tangqiao.entity.TqSubscribeApiConfigPO;
import pd.tangqiao.entity.TqSubscribeAppServiceConfigPO;
import pd.tangqiao.mapper.TqSubscribeApiConfigMapper;
import pd.tangqiao.service.TqSubscribeApiConfigService;
import pd.tangqiao.service.TqSubscribeAppServiceConfigService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("tqSubscribeApiConfigService")
public class TqSubscribeApiConfigServiceImpl extends ServiceImpl<TqSubscribeApiConfigMapper, TqSubscribeApiConfigPO> implements TqSubscribeApiConfigService {


    @Resource
    TqSubscribeAppServiceConfigService tqSubscribeAppServiceConfigService;
    @Override
    public List<TqSubscribeApiConfigPO> getAll() {
        return this.list();
    }

    @Override
    public ResultEnum addData(TqSubscribeApiConfigPO po) {
        this.save(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editData(TqSubscribeApiConfigPO po) {
        this.updateById(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum bindApi(BindApiDTO dto) {
        List<Integer> apiIds = dto.getApiIds();
        List<TqSubscribeAppServiceConfigPO> list = new ArrayList<>();
        for (Integer apiId : apiIds) {
            TqSubscribeAppServiceConfigPO po = new TqSubscribeAppServiceConfigPO();
            po.setAppId(dto.appId);
            po.setServiceId(apiId);
            list.add(po);
        }
        LambdaQueryWrapper<TqSubscribeAppServiceConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TqSubscribeAppServiceConfigPO::getAppId,dto.appId);
        tqSubscribeAppServiceConfigService.remove(queryWrapper);
        tqSubscribeAppServiceConfigService.saveBatch(list);
        return ResultEnum.SUCCESS;
    }
}
