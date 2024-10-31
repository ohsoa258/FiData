package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.*;
import pd.tangqiao.mapper.TqCenterApiConfigMapper;
import pd.tangqiao.service.TqAppServiceConfigService;
import pd.tangqiao.service.TqCenterApiConfigService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("tqCenterApiConfigService")
public class TqCenterApiConfigServiceImpl extends ServiceImpl<TqCenterApiConfigMapper, TqCenterApiConfigPO> implements TqCenterApiConfigService {


    @Resource
    TqAppServiceConfigService appServiceConfigService;
    @Override
    public Page<TqCenterApiConfigVO> getAll(TqCenterApiConfigQueryDTO dto) {
        return baseMapper.getAll(dto.page,dto);
    }

    @Override
    public List<TqCenterApiConfigPO> getAllApi() {
        return this.list();
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

    @Override
    public ResultEnum bindApi(BindApiDTO dto) {
        List<Integer> apiIds = dto.getApiIds();
        List<TqAppServiceConfigPO> list = new ArrayList<>();
        for (Integer apiId : apiIds) {
            TqAppServiceConfigPO po = new TqAppServiceConfigPO();
            po.setAppId(dto.appId);
            po.setServiceId(apiId);
            list.add(po);
        }
        LambdaQueryWrapper<TqAppServiceConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TqAppServiceConfigPO::getAppId,dto.appId);
        appServiceConfigService.remove(queryWrapper);
        appServiceConfigService.saveBatch(list);
        return ResultEnum.SUCCESS;
    }
}
