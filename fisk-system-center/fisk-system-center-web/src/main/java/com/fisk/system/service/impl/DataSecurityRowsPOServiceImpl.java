package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.system.dto.datasecurity.DataSecurityRowsDTO;
import com.fisk.system.entity.DataSecurityRowsPO;
import com.fisk.system.map.DataSecurityRowsMap;
import com.fisk.system.mapper.DataSecurityRowsPOMapper;
import com.fisk.system.service.DataSecurityRowsPOService;
import com.fisk.task.enums.OlapTableEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 56263
 * @description 针对表【tb_data_security_rows】的数据库操作Service实现
 * @createDate 2024-06-06 14:20:15
 */
@Service
public class DataSecurityRowsPOServiceImpl extends ServiceImpl<DataSecurityRowsPOMapper, DataSecurityRowsPO>
        implements DataSecurityRowsPOService {

    @Resource
    private DataAccessClient accessClient;

    /**
     * 数据安全 行级安全 批量保存
     *
     * @param dtoList
     * @return
     */
    @Override
    public Object saveRows(List<DataSecurityRowsDTO> dtoList) {
        List<DataSecurityRowsPO> pos = DataSecurityRowsMap.INSTANCES.dtosToPos(dtoList);
        return this.saveBatch(pos);
    }

    /**
     * 数据安全 行级安全 回显
     *
     * @return
     */
    @Override
    public Object getRows() {
        List<DataSecurityRowsPO> list = this.list();
        List<DataSecurityRowsDTO> dtos = DataSecurityRowsMap.INSTANCES.posToDtos(list);

        //从数据接入获取所有物理表
        ResultEntity<List<TableAccessDTO>> resultEntity = accessClient.getAllAccessTbls();
        List<TableAccessDTO> data = null;
        if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
            data = resultEntity.getData();
        }

        //从数据接入获取所有应用
        List<AppBusinessInfoDTO> data1 = null;
        ResultEntity<List<AppBusinessInfoDTO>> appList = accessClient.getAppList();
        if (appList.getCode() == ResultEnum.SUCCESS.getCode()) {
            data1 = appList.getData();
        }

        for (DataSecurityRowsDTO dto : dtos) {
            //如果是物理表
            if (dto.getTblType() == OlapTableEnum.PHYSICS.getValue()) {
                //设置物理表名称和显示名称
                if (!CollectionUtils.isEmpty(data)) {
                    data.stream()
                            .filter(t -> t.getId() == dto.getTblId())
                            .findFirst()
                            .ifPresent(access -> {
                                dto.setTblName(access.getTableName());
                                dto.setTblDisName(access.getDisplayName());
                            });
                }
            }
            //设置应用名称
            if (!CollectionUtils.isEmpty(data1)) {
                data1.stream()
                        .filter(t -> t.getId() == dto.getAppId())
                        .findFirst()
                        .ifPresent(app -> {
                            dto.setAppName(app.getName());
                        });
            }
        }

        return dtos;
    }

    /**
     * 数据安全 行级安全 单个删除
     *
     * @param id
     * @return
     */
    @Override
    public Object deleteRowSecurityById(Integer id) {
        return this.removeById(id);
    }

    /**
     * 数据安全 行级安全 根据角色id获取该角色的行级安全权限
     *
     * @param roleId
     * @return
     */
    @Override
    public List<DataSecurityRowsDTO> getRowsByRoleId(Integer roleId) {
        List<DataSecurityRowsPO> list = this.list(
                new LambdaQueryWrapper<DataSecurityRowsPO>()
                        .eq(DataSecurityRowsPO::getRoleId, roleId)
        );
        return DataSecurityRowsMap.INSTANCES.posToDtos(list);
    }

}




