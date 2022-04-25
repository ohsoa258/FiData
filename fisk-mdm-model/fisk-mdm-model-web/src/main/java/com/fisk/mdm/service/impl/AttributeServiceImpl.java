package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.mdm.dto.attribute.*;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.enums.DataTypeEnum;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.AttributeMap;
import com.fisk.mdm.map.EntityMap;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.EventLogService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityMsgVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import com.fisk.task.client.PublishTaskClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
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

    @Resource
    private EntityService entityService;

    @Resource
    private UserHelper userHelper;

    @Override
    public ResultEntity<AttributeVO> getById(Integer id) {
        AttributeVO attributeVO = AttributeMap.INSTANCES.poToVo(baseMapper.selectById(id));
        if (Objects.isNull(attributeVO)) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询出模型id
        QueryWrapper<EntityPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(EntityPO::getId, attributeVO.getEntityId());
        EntityPO entityPO = entityMapper.selectOne(queryWrapper);
        attributeVO.setModelId(entityPO.getModelId());
        return ResultEntityBuild.build(ResultEnum.SUCCESS, attributeVO);
    }

    @Override
    public ResultEnum addData(AttributeDTO attributeDTO) {

        //判断同实体下是否存在重复名称
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", attributeDTO.getName())
                .eq("entity_id", attributeDTO.getEntityId());
        if (baseMapper.selectOne(wrapper) != null) {
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
        logService.saveEventLog((int) attributePO.getId(), ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.SAVE, desc);

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
        if (attributePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //判断修改后的名称是否存在
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", attributeUpdateDTO.getName())
                .ne("id", attributeUpdateDTO.getId());
        if (baseMapper.selectOne(wrapper) != null) {
            return ResultEnum.NAME_EXISTS;
        }

        //维护历史的状态字段，防止保持状态为新增失效
        if(!Objects.isNull(attributePO.getStatus())) {
            attributeUpdateDTO.setStatus(attributePO.getStatus().getValue());
        }

        //若修改后数据类型不为浮点型，将数据小数点长度修改为0
        if(!Objects.isNull(attributeUpdateDTO.getDataType()) && attributeUpdateDTO.getDataType() != DataTypeEnum.FLOAT.getValue()){
            attributeUpdateDTO.setDataTypeDecimalLength(0);
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
        logService.saveEventLog((int) attributePO.getId(), ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.UPDATE, desc);

        //添加成功
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteDataById(Integer id) {
        //判断数据是否存在
        if (baseMapper.selectById(id) == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //删除数据
        if (baseMapper.deleteById(id) <= 0) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 记录日志
        String desc = "删除一个属性,id:" + id;
        logService.saveEventLog(id, ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.DELETE, desc);

        //删除成功
        return ResultEnum.SUCCESS;
    }

    @Override
    public Page<AttributeVO> getAll(AttributeQueryDTO query) {

        Page<AttributeVO> voPage = baseMapper.getAll(query.page, query);

        //部分字段枚举类型转换
        Page<AttributePageDTO> dtoPage = AttributeMap.INSTANCES.voToPageDtoPage(voPage);
        Page<AttributePO> poPage = AttributeMap.INSTANCES.pageDtoToPoPage(dtoPage);
        Page<AttributeVO> all = AttributeMap.INSTANCES.poToVoPage(poPage);

        //获取创建人名称
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            ReplenishUserInfo.replenishUserName(all.getRecords(), userClient, UserFieldEnum.USER_ACCOUNT);
        }

        return all;
    }

    /**
     * 提交待添加、待修改属性
     *
     * @return {@link List}<{@link AttributePO}>
     */
    @Override
    public ResultEnum getNotSubmittedData(Integer entityId) {

        //查询实体是否存在
        if (entityService.getDataById(entityId) == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //查询实体下是否存在可提交的属性
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_id", entityId)
                .eq("status", AttributeStatusEnum.INSERT).or()
                .eq("status", AttributeStatusEnum.UPDATE);
        if (baseMapper.selectList(wrapper) == null || baseMapper.selectList(wrapper).size() == 0) {
            return ResultEnum.NO_DATA_TO_SUBMIT;
        }

        //提交
        com.fisk.task.dto.model.EntityDTO entityDTO = new com.fisk.task.dto.model.EntityDTO();
        entityDTO.setEntityId(entityId);
        entityDTO.setUserId(userHelper.getLoginUserInfo().getId());
        if (publishTaskClient.createBackendTable(entityDTO).getCode() != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.DATA_SUBMIT_ERROR;
        }

        return ResultEnum.SUCCESS;
    }


    /**
     * 获取实体ER图信息
     *
     * @return {@link List}<{@link EntityMsgVO}>
     */
    @Override
    public List<EntityMsgVO> getER() {
        return baseMapper.getER();
    }

    @Override
    public ResultEntity<List<AttributeInfoDTO>> getByIds(List<Integer> ids) {
        List<AttributeInfoDTO> list = AttributeMap.INSTANCES.poToVoList(baseMapper.selectBatchIds(ids));
        if (Objects.isNull(list)) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询出模型id
        List<AttributeInfoDTO> collect = list.stream().filter(e -> e.getEntityId() != null).map(e -> {
            QueryWrapper<EntityPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(EntityPO::getId, e.getEntityId());
            EntityPO entityPO = entityMapper.selectOne(queryWrapper);
            e.setModelId(entityPO.getModelId());
            return e;
        }).collect(Collectors.toList());
        return ResultEntityBuild.build(ResultEnum.SUCCESS, collect);
    }

    @Override
    public AttributeInfoDTO getByDomainId(AttributeDomainDTO dto) {
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttributePO::getEntityId,dto.getEntityId())
                .eq(AttributePO::getDomainId,dto.getDomainId())
                .last("limit 1");

        AttributePO attributePO = baseMapper.selectOne(queryWrapper);
        return attributePO == null ? null : AttributeMap.INSTANCES.poToInfoDto(attributePO);
    }

    @Override
    public ResultEnum updateStatus(AttributeStatusDTO statusDto) {
        AttributePO attributePO = baseMapper.selectById(statusDto.getId());
        if (attributePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        AttributePO statusPo = EntityMap.INSTANCES.dtoToStatusPo(statusDto);
        int res = baseMapper.updateById(statusPo);
        return res == 0 ? ResultEnum.SAVE_DATA_ERROR : ResultEnum.SUCCESS;
    }


}
