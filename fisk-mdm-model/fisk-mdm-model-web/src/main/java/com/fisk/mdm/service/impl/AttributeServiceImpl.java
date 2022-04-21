package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.dto.attribute.AttributeQueryDTO;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.Entity;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.AttributeMap;
import com.fisk.mdm.map.ModelMap;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EventLogService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.model.ModelVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.model.EntityDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author WangYan
 * @date 2022/4/5 14:49
 */
@Service
public class AttributeServiceImpl extends ServiceImpl<AttributeMapper, AttributePO> implements AttributeService {

    @Resource
    EventLogService logService;

    @Resource
    private UserClient userClient;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private EntityMapper entityMapper;

    @Override
    public ResultEntity<AttributeVO> getById(Integer id) {
        AttributeVO attributeVO = AttributeMap.INSTANCES.poToVo(baseMapper.selectById(id));
        if(Objects.isNull(attributeVO)){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询出模型id
        QueryWrapper<EntityPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(EntityPO::getId,attributeVO.getEntityId());
        EntityPO entityPO = entityMapper.selectOne(queryWrapper);
        attributeVO.setModelId(entityPO.getModelId());
        return ResultEntityBuild.build(ResultEnum.SUCCESS, attributeVO);
    }

    @Override
    public ResultEnum addData(AttributeDTO attributeDTO) {

        //判断同实体下是否存在重复名称
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("name",attributeDTO.getName())
                .eq("entity_id",attributeDTO.getEntityId());
        if(baseMapper.selectOne(wrapper) != null){
            return ResultEnum.NAME_EXISTS;
        }

        //添加数据
        AttributePO attributePO = AttributeMap.INSTANCES.dtoToPo(attributeDTO);
        attributePO.setStatus(AttributeStatusEnum.INSERT);
        if (baseMapper.insert(attributePO) <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 记录日志
        String desc = "新增一个属性,id:" + attributePO.getId();
        logService.saveEventLog((int)attributePO.getId(), ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.SAVE,desc);

        //创建成功
        return ResultEnum.SUCCESS;
    }

    /**
     * 编辑数据
     *
     * @param attributeUpdateDTO 属性更新dto
     * @return {@link ResultEnum}
     */
    @Override
    public ResultEnum editData(AttributeUpdateDTO attributeUpdateDTO) {
        AttributePO attributePO = baseMapper.selectById(attributeUpdateDTO.getId());

        //判断数据是否存在
        if (attributePO == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        //判断修改后的名称是否存在
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("name",attributeUpdateDTO.getName())
                .ne("id",attributeUpdateDTO.getId());
        if(baseMapper.selectOne(wrapper) != null){
            return ResultEnum.NAME_EXISTS;
        }

        //把DTO转化到查询出来的PO上
        attributePO = AttributeMap.INSTANCES.updateDtoToPo(attributeUpdateDTO);

        //如果历史的状态是新增,保持状态为新增
        attributePO.setStatus(attributePO.getStatus() == AttributeStatusEnum.INSERT ?
                AttributeStatusEnum.INSERT : AttributeStatusEnum.UPDATE);

        //修改数据
        if (baseMapper.updateById(attributePO) <= 0) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 记录日志
        String desc = "修改一个属性,id:" + attributeUpdateDTO.getId();
        logService.saveEventLog((int)attributePO.getId(),ObjectTypeEnum.ATTRIBUTES,EventTypeEnum.UPDATE,desc);

        //添加成功
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteDataById(Integer id) {
        //判断数据是否存在
        if (baseMapper.selectById(id) == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        //删除数据
        if (baseMapper.deleteById(id) <= 0) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 记录日志
        String desc = "删除一个属性,id:" + id;
        logService.saveEventLog(id,ObjectTypeEnum.ATTRIBUTES,EventTypeEnum.DELETE,desc);

        //删除成功
        return ResultEnum.SUCCESS;
    }

    @Override
    public Page<AttributeVO> getAll(AttributeQueryDTO query) {

        Page<AttributeVO> all = baseMapper.getAll(query.page, query);


        //获取创建人名称
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<Long> userIds = all.getRecords()
                    .stream()
                    .filter(e -> StringUtils.isNotEmpty(e.createUser))
                    .map(e -> Long.valueOf(e.createUser))
                    .distinct()
                    .collect(Collectors.toList());
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
            if (userListByIds.code == ResultEnum.SUCCESS.getCode() && userListByIds.getData() != null) {
                all.getRecords().forEach(e -> {
                    userListByIds.getData()
                            .stream()
                            .filter(user -> user.getId().toString().equals(e.createUser))
                            .findFirst()
                            .ifPresent(user -> e.createUser = user.userAccount);
                });
            }
        }

        return all;
    }

    /**
     * 提交待添加、待修改数据
     *
     * @return {@link List}<{@link AttributePO}>
     */
    @Override
    public ResultEnum getNotSubmittedData() {
        //获取需要提交的属性
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("status",AttributeStatusEnum.INSERT).or()
                .eq("status",AttributeStatusEnum.UPDATE);
        List<AttributePO> attributePOS = baseMapper.selectList(wrapper);

        if(attributePOS == null || attributePOS.size() == 0){
            return ResultEnum.DATA_NOTEXISTS;
        }

        //获取需要提交的属性的实体id
        List<Integer> entityIds = new ArrayList<>();
        for (AttributePO attributePO:attributePOS) {
            entityIds.add(attributePO.getEntityId());
        }

        //去重
        List<Integer> collect = entityIds.stream().distinct().collect(Collectors.toList());

        //提交
        EntityDTO entityDTO = new EntityDTO();
        for (int entityId : collect) {
            entityDTO.setEntityId(entityId);
            publishTaskClient.createBackendTable(entityDTO);
        }

        return ResultEnum.SUCCESS;
    }


    /**
     * 获取实体、属性信息
     *
     * @return {@link List}<{@link Entity}>
     */
    @Override
    public List<Entity> getER() {
        return baseMapper.getER();
    }


}
