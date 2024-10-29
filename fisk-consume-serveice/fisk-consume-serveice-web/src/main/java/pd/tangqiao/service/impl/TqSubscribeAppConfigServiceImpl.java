package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pd.tangqiao.entity.TqSubscribeApiConfigPO;
import pd.tangqiao.entity.TqSubscribeAppConfigPO;
import pd.tangqiao.entity.TqSubscribeAppConfigVO;
import pd.tangqiao.mapper.TqSubscribeAppConfigMapper;
import pd.tangqiao.service.TqSubscribeApiConfigService;
import pd.tangqiao.service.TqSubscribeAppConfigService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("tqSubscribeAppConfigService")
public class TqSubscribeAppConfigServiceImpl extends ServiceImpl<TqSubscribeAppConfigMapper, TqSubscribeAppConfigPO> implements TqSubscribeAppConfigService {


    @Resource
    TqSubscribeApiConfigService apiConfigService;
    @Override
    public Page<TqSubscribeAppConfigVO> getAll(Page<TqSubscribeAppConfigVO> page) {
        Page<TqSubscribeAppConfigVO> all = baseMapper.getAll(page);
        List<TqSubscribeAppConfigVO> records = all.getRecords();
        if (!CollectionUtils.isEmpty(records)){
            List<Integer> appIds = records.stream().map(TqSubscribeAppConfigVO::getId).collect(Collectors.toList());
            LambdaQueryWrapper<TqSubscribeApiConfigPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(TqSubscribeApiConfigPO::getAppId,appIds);
            List<TqSubscribeApiConfigPO> apis = apiConfigService.list(queryWrapper);
            Map<Integer, List<TqSubscribeApiConfigPO>> apiMap = apis.stream().collect(Collectors.groupingBy(TqSubscribeApiConfigPO::getAppId));
            records = records.stream().map(i->{
                List<TqSubscribeApiConfigPO> tqSubscribeApiConfigPOS = apiMap.get(i.id);
                i.setApiNumber(tqSubscribeApiConfigPOS.size());
                i.setApiConfigPOS(tqSubscribeApiConfigPOS);
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
