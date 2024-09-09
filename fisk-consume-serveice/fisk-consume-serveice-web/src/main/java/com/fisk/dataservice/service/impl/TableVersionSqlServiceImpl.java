package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.api.VersionSqlDTO;
import com.fisk.dataservice.entity.TableVersionSqlPO;
import com.fisk.dataservice.map.VersionSqlMap;
import com.fisk.dataservice.mapper.TableVersionSqlPOMapper;
import com.fisk.dataservice.service.ITableVersionSqlService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 56263
 * @description 针对表【tb_version_sql】的数据库操作Service实现
 * @createDate 2023-12-27 09:47:32
 */
@Service
public class TableVersionSqlServiceImpl extends ServiceImpl<TableVersionSqlPOMapper, TableVersionSqlPO>
        implements ITableVersionSqlService {


    @Resource
    UserClient userClient;
    /**
     * 通过表id和表类型获取表的所有版本sql
     *
     * @param apiId 表id
     * @return
     */
    @Override
    public List<VersionSqlDTO> getVersionSqlByTableIdAndType(Integer apiId) {
        LambdaQueryWrapper<TableVersionSqlPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TableVersionSqlPO::getApiId, apiId);
        List<TableVersionSqlPO> list = list(wrapper);
        List<Long> userIds = list.stream().map(i -> Long.valueOf(i.getCreateUser())).collect(Collectors.toList());
        List<VersionSqlDTO> versionSqlDTOS = VersionSqlMap.INSTANCES.poListToDtoList(list);
        ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
        if (userListByIds.code == ResultEnum.SUCCESS.getCode()
                && CollectionUtils.isNotEmpty(userListByIds.getData())) {
            versionSqlDTOS.forEach(e -> {
                userListByIds.getData()
                        .stream()
                        .filter(user -> user.getId().toString().equals(e.createUser))
                        .findFirst()
                        .ifPresent(user -> e.createUser = user.userAccount);
            });
        }
        return versionSqlDTOS;
    }
  
}




