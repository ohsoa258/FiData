package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pd.tangqiao.entity.*;
import pd.tangqiao.mapper.TqSubscribeAppConfigMapper;
import pd.tangqiao.service.TqSubscribeApiConfigService;
import pd.tangqiao.service.TqSubscribeAppConfigService;
import pd.tangqiao.service.TqSubscribeAppServiceConfigService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("tqSubscribeAppConfigService")
public class TqSubscribeAppConfigServiceImpl extends ServiceImpl<TqSubscribeAppConfigMapper, TqSubscribeAppConfigPO> implements TqSubscribeAppConfigService {


    @Resource
    TqSubscribeApiConfigService apiConfigService;

    @Resource
    TqSubscribeAppServiceConfigService appServiceConfigService;
    @Override
    public Page<TqSubscribeAppConfigVO> getAll(Page<TqSubscribeAppConfigVO> page) {
        Page<TqSubscribeAppConfigVO> all = baseMapper.getAll(page);
        List<TqSubscribeAppConfigVO> records = all.getRecords();
        if (!CollectionUtils.isEmpty(records)){
            List<Integer> appIds = records.stream().map(TqSubscribeAppConfigVO::getId).collect(Collectors.toList());
            List<TqSubscribeApiConfigVO> apilist = appServiceConfigService.apilist(appIds);
            Map<Integer, List<TqSubscribeApiConfigVO>> apiMap = apilist.stream().collect(Collectors.groupingBy(TqSubscribeApiConfigVO::getAppId));
            records = records.stream().map(i->{
                List<TqSubscribeApiConfigVO> tqSubscribeApiConfigVOS = apiMap.get(i.id);
                if (!CollectionUtils.isEmpty(tqSubscribeApiConfigVOS)){
                    i.setApiNumber(tqSubscribeApiConfigVOS.size());
                    i.setApiConfigPOS(tqSubscribeApiConfigVOS);
                }
                return i;
            }).collect(Collectors.toList());
            all.setRecords(records);
        }
        return all;
    }

    @Override
    public ResultEnum addData(TqSubscribeAppConfigPO po) {
        this.save(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editData(TqSubscribeAppConfigPO po) {
        this.updateById(po);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(int id) {
        this.removeById(id);
        LambdaQueryWrapper<TqSubscribeApiConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TqSubscribeApiConfigPO::getAppId,id);
        apiConfigService.remove(queryWrapper);
        return ResultEnum.SUCCESS;
    }
}
