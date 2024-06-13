package com.fisk.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.system.dto.datasecurity.DataSecurityColumnsDTO;
import com.fisk.system.entity.DataSecurityColumnsPO;
import com.fisk.system.mapper.DataSecurityColumnsPOMapper;
import com.fisk.system.service.DataSecurityColumnsPOService;
import com.fisk.task.enums.OlapTableEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 56263
 * @description 针对表【tb_data_security_columns】的数据库操作Service实现
 * @createDate 2024-06-06 14:20:15
 */
@Service
public class DataSecurityColumnsPOServiceImpl extends ServiceImpl<DataSecurityColumnsPOMapper, DataSecurityColumnsPO>
        implements DataSecurityColumnsPOService {

    @Resource
    private DataAccessClient accessClient;

    /**
     * 数据安全 列级安全 批量保存
     *
     * @param dtoList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Object saveColumns(List<DataSecurityColumnsDTO> dtoList) {
        int roleId = dtoList.get(0).getRoleId();
        //每次都是全删全插
        this.remove(
                new LambdaQueryWrapper<DataSecurityColumnsPO>()
                        .eq(DataSecurityColumnsPO::getRoleId, roleId)
        );

        List<DataSecurityColumnsPO> pos = new ArrayList<>();
        for (DataSecurityColumnsDTO dto : dtoList) {
            DataSecurityColumnsPO po = new DataSecurityColumnsPO();
            po.setRoleId(dto.getRoleId());
            po.setAppId(dto.getAppId());
            po.setTblId(dto.getTblId());
            po.setTblType(dto.getTblType());
            po.setReadableFieldIds(JSON.toJSONString(dto.getReadableFieldIds()));
            pos.add(po);
        }
        return this.saveBatch(pos);
    }

    @Override
    public List<DataSecurityColumnsDTO> getColumns() {
        List<DataSecurityColumnsDTO> dtos = new ArrayList<>();
        List<DataSecurityColumnsPO> list = this.list();
        for (DataSecurityColumnsPO po : list) {
            DataSecurityColumnsDTO dto = new DataSecurityColumnsDTO();
            dto.setId(po.getId());
            dto.setRoleId(po.getRoleId());
            dto.setAppId(po.getAppId());
            dto.setTblId(po.getTblId());
            int tblType = po.getTblType();
            dto.setTblType(tblType);
            //获取所有字段id
            List<Integer> fieldIds = JSON.parseArray(po.getReadableFieldIds(), Integer.class);

            if (tblType == OlapTableEnum.PHYSICS.getValue()) {
                ResultEntity<List<TableFieldsDTO>> resultEntity = accessClient.getFieldInfosByIds(fieldIds);
                if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
                    List<TableFieldsDTO> data = resultEntity.getData();
                    dto.setReadableFieldMap(data.stream()
                            .collect(Collectors.toMap(TableFieldsDTO::getId, TableFieldsDTO::getFieldName)));
                }
            }
            dtos.add(dto);
        }

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

        for (DataSecurityColumnsDTO dto : dtos) {
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
     * 数据安全 列级安全 单个删除
     *
     * @param id
     * @return
     */
    @Override
    public Object deleteColumnSecurityById(Integer id) {
        return this.removeById(id);
    }

    /**
     * 数据安全 列级安全 根据角色id获取该角色的列级安全权限
     *
     * @param roleId
     * @return
     */
    @Override
    public List<DataSecurityColumnsDTO> getColumnsByRoleId(Integer roleId) {
        List<DataSecurityColumnsDTO> dtos = new ArrayList<>();
        List<DataSecurityColumnsPO> list = this.list(
                new LambdaQueryWrapper<DataSecurityColumnsPO>()
                        .eq(DataSecurityColumnsPO::getRoleId, roleId)
        );

        for (DataSecurityColumnsPO po : list) {
            DataSecurityColumnsDTO dto = new DataSecurityColumnsDTO();
            dto.setRoleId(po.getRoleId());
            dto.setAppId(po.getAppId());
            dto.setTblId(po.getTblId());
            int tblType = po.getTblType();
            dto.setTblType(tblType);
            //获取所有字段id
            dto.setReadableFieldIds(JSON.parseArray(po.getReadableFieldIds(), Integer.class));
            dtos.add(dto);
        }

        return dtos;
    }
}




