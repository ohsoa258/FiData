package com.fisk.datagovernance.service.impl.monitor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.monitor.ServerMonitorTypeDTO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorConfigPO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorTypePO;
import com.fisk.datagovernance.map.monitor.ServerMonitorTypeMap;
import com.fisk.datagovernance.mapper.monitor.ServerMonitorTypeMapper;
import com.fisk.datagovernance.service.monitor.ServerMonitorConfigService;
import com.fisk.datagovernance.service.monitor.ServerMonitorTypeService;
import com.fisk.datagovernance.vo.monitor.ServerMonitorTypeVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service("serverMonitorTypeService")
public class ServerMonitorTypeServiceImpl extends ServiceImpl<ServerMonitorTypeMapper, ServerMonitorTypePO> implements ServerMonitorTypeService {


    @Resource
    private ServerMonitorConfigService serverMonitorConfigService;
    @Override
    public ResultEnum addOrUpdateServerMonitorType(ServerMonitorTypeDTO serverMonitorTypeDTO) {
        List<ServerMonitorTypePO> list = this.list();
        List<ServerMonitorTypePO> types = list.stream().filter(i -> i.getServerType().equals(serverMonitorTypeDTO.getServerType())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(types)){
            return ResultEnum.DATA_QUALITY_SERVER_TYPE_ADD_IS_EXIST;
        }
        if (serverMonitorTypeDTO.getId() == null || serverMonitorTypeDTO.getId() == 0){
            ServerMonitorTypePO po = new ServerMonitorTypePO();
            po.setServerType(serverMonitorTypeDTO.getServerType());
            this.save(po);
        }else {
            ServerMonitorTypePO po = new ServerMonitorTypePO();
            po.setId(serverMonitorTypeDTO.getId());
            po.setServerType(serverMonitorTypeDTO.getServerType());
            this.updateById(po);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteServerMonitorType(Integer id) {
        LambdaQueryWrapper<ServerMonitorConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServerMonitorConfigPO::getServerType,id);
        List<ServerMonitorConfigPO> serverMonitorConfigPOS = serverMonitorConfigService.list(queryWrapper);
        if (!CollectionUtils.isEmpty(serverMonitorConfigPOS)){
            return ResultEnum.DATA_QUALITY_SERVER_TYPE_DELETE_IS_EXIST;
        }
        this.removeById(id);
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<ServerMonitorTypeVO> getServerMonitorType() {
        List<ServerMonitorTypePO> list = this.list();
        return ServerMonitorTypeMap.INSTANCES.poListToVoList(list);
    }
}
