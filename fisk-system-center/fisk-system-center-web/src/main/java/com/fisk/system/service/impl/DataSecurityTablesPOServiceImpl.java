package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.system.dto.datasecurity.DataSecurityTablesDTO;
import com.fisk.system.entity.DataSecurityTablesPO;
import com.fisk.system.map.DataSecurityMap;
import com.fisk.system.mapper.DataSecurityTablesPOMapper;
import com.fisk.system.service.DataSecurityTablesPOService;
import com.fisk.task.enums.OlapTableEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 56263
 * @description 针对表【tb_data_security_tables】的数据库操作Service实现
 * @createDate 2024-06-06 14:20:15
 */
@Service
public class DataSecurityTablesPOServiceImpl extends ServiceImpl<DataSecurityTablesPOMapper, DataSecurityTablesPO>
        implements DataSecurityTablesPOService {

    @Resource
    private DataAccessClient accessClient;


    /**
     * 数据安全 表级安全 批量保存
     *
     * @param dtoList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Object saveTables(List<DataSecurityTablesDTO> dtoList) {
        //全删全插
        int roleId = dtoList.get(0).getRoleId();
        this.remove(new LambdaQueryWrapper<DataSecurityTablesPO>().eq(DataSecurityTablesPO::getRoleId, roleId));


        List<DataSecurityTablesPO> pos = DataSecurityMap.INSTANCES.dtosToPos(dtoList);
        return this.saveBatch(pos);
    }

    /**
     * 数据安全 表级安全 分页回显
     *
     * @return
     */
    @Override
    public List<DataSecurityTablesDTO> getTables() {
        List<DataSecurityTablesPO> list = this.list();
        List<DataSecurityTablesDTO> dtos = DataSecurityMap.INSTANCES.posToDtos(list);

        //从数据接入获取所有物理表
        ResultEntity<List<TableAccessDTO>> resultEntity = accessClient.getAllAccessTbls();

        List<TableAccessDTO> data = null;

        if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
            data = resultEntity.getData();
        }

        List<AppBusinessInfoDTO> data1 = null;
        //从数据接入获取所有应用
        ResultEntity<List<AppBusinessInfoDTO>> appList = accessClient.getAppList();
        if (appList.getCode() == ResultEnum.SUCCESS.getCode()) {
            data1 = appList.getData();
        }

        for (DataSecurityTablesDTO dto : dtos) {
            //如果是物理表
            if (dto.getTblType() == OlapTableEnum.PHYSICS.getValue()) {
                if (!CollectionUtils.isEmpty(data)) {
                    //设置物理表名称和显示名称
                    data.stream()
                            .filter(t -> t.getId() == dto.getTblId())
                            .findFirst()
                            .ifPresent(access -> {
                                dto.setTblName(access.getTableName());
                                dto.setTblDisName(access.getDisplayName());
                            });
                }
                if (!CollectionUtils.isEmpty(data1)) {
                    //设置应用名称
                    data1.stream()
                            .filter(t -> t.getId() == dto.getAppId())
                            .findFirst()
                            .ifPresent(app -> {
                                dto.setAppName(app.getName());
                            });
                }
            }
        }

        return dtos;
    }

    /**
     * 数据安全 表级安全 根据角色id获取该角色的表级安全权限
     *
     * @param roleId
     * @return
     */
    @Override
    public List<DataSecurityTablesDTO> getTablesByRoleId(Integer roleId) {
        List<DataSecurityTablesPO> list = this.list(
                new LambdaQueryWrapper<DataSecurityTablesPO>().eq(DataSecurityTablesPO::getRoleId, roleId)
        );
        return DataSecurityMap.INSTANCES.posToDtos(list);
    }

    /**
     * 数据安全 表级安全 单个删除
     *
     * @param id
     * @return
     */
    @Override
    public Object deleteTableSecurityById(Integer id) {
        return this.removeById(id);
    }

    /**
     * 获取所有应用以及表、字段数据
     *
     * @return
     */
    @Override
    public ResultEntity<Object> getAccessAppDetails() {
        return accessClient.getDataAppRegistrationMeta();
    }

}




