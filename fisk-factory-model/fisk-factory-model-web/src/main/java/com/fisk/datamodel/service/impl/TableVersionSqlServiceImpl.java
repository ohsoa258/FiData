package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.dto.versionsql.VersionSqlDTO;
import com.fisk.datamodel.entity.TableVersionSqlPO;
import com.fisk.datamodel.map.versionsql.VersionSqlMap;
import com.fisk.datamodel.mapper.TableVersionSqlPOMapper;
import com.fisk.datamodel.service.ITableVersionSqlService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author 56263
 * @description 针对表【tb_version_sql】的数据库操作Service实现
 * @createDate 2023-12-27 09:47:32
 */
@Service
public class TableVersionSqlServiceImpl extends ServiceImpl<TableVersionSqlPOMapper, TableVersionSqlPO>
        implements ITableVersionSqlService {


    @Resource
    private UserClient userClient;

    /**
     * 通过表id和表类型获取表的所有版本sql
     *
     * @param tblId   表id
     * @param tblType 0维度 1事实
     * @return
     */
    @Override
    public List<VersionSqlDTO> getVersionSqlByTableIdAndType(Integer tblId, Integer tblType) {
        LambdaQueryWrapper<TableVersionSqlPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TableVersionSqlPO::getTableId, tblId)
                .eq(TableVersionSqlPO::getTableType, tblType);
        List<TableVersionSqlPO> list = list(wrapper);
        List<VersionSqlDTO> versionSqlDTOS = VersionSqlMap.INSTANCES.poListToDtoList(list);


        //以下代码是为了将创建人从id转为用户名称username
        //不抛出异常的原因：不要因为创建人转换错误导致sql版本整个获取失败
        try {
            //获取平台所有用户信息
            ResultEntity<List<UserDTO>> resultEntity = userClient.getAllUserList();
            if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
                List<UserDTO> userDTOS = resultEntity.getData();
                if (!CollectionUtils.isEmpty(userDTOS)){
                    for (VersionSqlDTO versionSqlDTO : versionSqlDTOS) {
                        userDTOS.stream()
                                .filter(userDTO -> String.valueOf(userDTO.getId()).equals(versionSqlDTO.getCreateUser()))
                                .findFirst()
                                .ifPresent(userDTO -> versionSqlDTO.createUser = userDTO.getUsername());
                    }

                    //若versionSqlDTOS和userDTOS都很大的话 使用lambda并行流
//                    versionSqlDTOS.parallelStream().forEach(versionSqlDTO -> {
//                        userDTOS.stream()
//                                .filter(userDTO -> String.valueOf(userDTO.getId()).equals(versionSqlDTO.getCreateUser()))
//                                .findFirst()
//                                .ifPresent(userDTO -> versionSqlDTO.createUser = userDTO.getUsername());
//                    });

                }
            }
        } catch (Exception e) {
            log.error("获取平台所有用户信息失败,原因：", e);
        }

        //将创建人从id转为用户名称
        return versionSqlDTOS;
    }

}




