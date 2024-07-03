package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.metasynctime.ClassificationTypeDTO;
import com.fisk.datamanagement.dto.metasynctime.MetaSyncDTO;
import com.fisk.datamanagement.entity.MetaSyncTimePO;
import com.fisk.datamanagement.mapper.MetaSyncTimePOMapper;
import com.fisk.datamanagement.service.MetaSyncTimePOService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 56263
 * @description 针对表【tb_meta_sync_time】的数据库操作Service实现
 * @createDate 2024-05-17 09:57:37
 */
@Service
public class MetaSyncTimePOServiceImpl extends ServiceImpl<MetaSyncTimePOMapper, MetaSyncTimePO>
        implements MetaSyncTimePOService {


    @Resource
    private UserClient userClient;

    /**
     * 获取服务类型树
     *
     * @return
     */
    @Override
    public List<ClassificationTypeDTO> getServiceType() {
        List<ClassificationTypeDTO> dtos = new ArrayList<>();
        for (ClassificationTypeEnum value : ClassificationTypeEnum.values()) {
            ClassificationTypeDTO dto = new ClassificationTypeDTO();
            dto.setId(value.getValue());
            dto.setTypeENName(value);
            dto.setTypeCNName(value.getName());
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * 根据服务类型获取服务的元数据同步日志 分页
     *
     * @param type
     * @return
     */
    @Override
    public Page<MetaSyncDTO> getMetaSyncLogByType(ClassificationTypeEnum type, Integer current, Integer size) {
        Page<MetaSyncTimePO> page = new Page<>(current, size);
        if (!type.equals(ClassificationTypeEnum.ALL)) {
            page = this.page(page,
                    new LambdaQueryWrapper<MetaSyncTimePO>()
                            .eq(MetaSyncTimePO::getServiceType, type.getValue())
                            .orderByDesc(MetaSyncTimePO::getCreateTime)
            );
        } else {
            page = this.page(page,
                    new LambdaQueryWrapper<MetaSyncTimePO>()
                            .orderByDesc(MetaSyncTimePO::getCreateTime)
            );
        }
        Page<MetaSyncDTO> page1 = new Page<>(current, size);

        ArrayList<MetaSyncDTO> dtos = new ArrayList<>();
        for (MetaSyncTimePO po : page.getRecords()) {
            MetaSyncDTO dto = new MetaSyncDTO();
            dto.setId(po.getId());
            dto.setCreateTime(po.getCreateTime());

            //将用户id切换为用户名
            if (po.getCreateUser() != null) {
                ResultEntity<UserDTO> resultEntity = userClient.getUserV2(Integer.parseInt(po.getCreateUser()));
                if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
                    dto.setCreateUser(resultEntity.getData().getUsername());
                }
            } else {
                dto.setCreateUser("定时任务");
            }

            dto.setUpdateTime(po.getUpdateTime());
            String serviceType = ClassificationTypeEnum.getEnumByValue(po.getServiceType()) != null ?
                    ClassificationTypeEnum.getEnumByValue(po.getServiceType()).getName() : po.getServiceType().toString();
            dto.setServiceType(serviceType);
            dto.setStatus(po.getStatus());
            dtos.add(dto);
        }

        page1.setRecords(dtos);
        page1.setTotal(page.getTotal());
        page1.setPages(page.getPages());
        return page1;
    }
}




