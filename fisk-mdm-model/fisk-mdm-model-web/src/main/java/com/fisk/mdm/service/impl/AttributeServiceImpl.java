package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.fisk.mdm.vo.entity.EntityVO;
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

        if (attributeDTO.getEntityId() == null){
            return ResultEnum.PARAMTER_ERROR;
        }

        //判断同实体下是否存在重复名称
        QueryWrapper<AttributePO> attributeWrapper = new QueryWrapper<>();
        attributeWrapper.eq("name", attributeDTO.getName())
                .eq("entity_id", attributeDTO.getEntityId())
                .last("limit 1");
        if (baseMapper.selectOne(attributeWrapper) != null) {
            return ResultEnum.NAME_EXISTS;
        }

        //转换数据
        AttributePO attributePo = AttributeMap.INSTANCES.dtoToPo(attributeDTO);

        //若数据不为浮点型，设置精度为null
        if(attributePo.getDataType() != DataTypeEnum.FLOAT){
            attributePo.setDataTypeDecimalLength(null);
        }

        //若数据类型不为浮点型或文本，数据长度设置为null
        if (attributePo.getDataType() != DataTypeEnum.TEXT &&
                attributePo.getDataType() != DataTypeEnum.FLOAT){
            attributePo.setDataTypeLength(null);
        }

        //若数据类型为“域字段”类型，维护“域字段id”字段
        if(attributePo.getDataType() == DataTypeEnum.DOMAIN){
            //查询出该属性属于哪个实体
            EntityVO entityVo = entityService.getDataById(attributePo.getEntityId());
            if(entityVo == null){
                return ResultEnum.SAVE_DATA_ERROR;
            }
            //查询出该实体下的code属性
            QueryWrapper<AttributePO> codeAttributeWrapper = new QueryWrapper<>();
            codeAttributeWrapper.lambda()
                    .eq(AttributePO::getEntityId,attributeDTO.getDomainId())
                    .eq(AttributePO::getName,"code");
            AttributePO codeAttribute = baseMapper.selectOne(codeAttributeWrapper);
            if(codeAttribute == null){
                return ResultEnum.SAVE_DATA_ERROR;
            }
            //“域字段id”赋值，值为同实体下code属性的id
            attributePo.setDomainId((int)codeAttribute.getId());
        }

        //添加数据
        attributePo.setStatus(AttributeStatusEnum.INSERT);
        if (baseMapper.insert(attributePo) <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 记录日志
        String desc = "新增一个属性,id:" + attributePo.getId();
        logService.saveEventLog((int) attributePo.getId(), ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.SAVE, desc);

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
        AttributePO attributePo = baseMapper.selectById(attributeUpdateDTO.getId());

        //判断数据是否存在
        if (attributePo == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //判断修改后的名称是否存在
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", attributeUpdateDTO.getName())
                .ne("id", attributeUpdateDTO.getId())
                .eq("entity_id",attributeUpdateDTO.getEntityId())
                .last("limit 1");
        if ( baseMapper.selectOne(wrapper) != null) {
            return ResultEnum.NAME_EXISTS;
        }

        //维护历史的状态字段，防止保持状态为新增失效
        if(!Objects.isNull(attributePo.getStatus())) {
            attributeUpdateDTO.setStatus(attributePo.getStatus().getValue());
        }

        //把DTO转化到查询出来的PO上
        attributePo = AttributeMap.INSTANCES.updateDtoToPo(attributeUpdateDTO);

        //如果历史的状态是新增,保持状态为新增
        attributePo.setStatus(attributePo.getStatus() == AttributeStatusEnum.INSERT ?
                AttributeStatusEnum.INSERT : AttributeStatusEnum.UPDATE);


        LambdaUpdateWrapper<AttributePO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AttributePO::getId,attributePo.getId());
        //若修改后数据类型不为浮点型，将数据小数点长度修改为null
        if(!Objects.isNull(attributePo.getDataType()) && attributePo.getDataType() != DataTypeEnum.FLOAT){
            updateWrapper.set(AttributePO::getDataTypeDecimalLength,null);
        }

        //若数据类型不为浮点型或文本，数据长度设置为null
        if (attributePo.getDataType() != DataTypeEnum.TEXT &&
                attributePo.getDataType() != DataTypeEnum.FLOAT){
            updateWrapper.set(AttributePO::getDataTypeLength,null);
        }

        //若数据类型为“域字段”类型，维护“域字段id”字段
        if(attributePo.getDataType() == DataTypeEnum.DOMAIN){
            //查询出该属性属于哪个实体
            EntityVO entityVo = entityService.getDataById(attributePo.getEntityId());
            if(entityVo == null){
                return ResultEnum.SAVE_DATA_ERROR;
            }
            //查询出该实体下的code属性
            QueryWrapper<AttributePO> codeAttributeWrapper = new QueryWrapper<>();
            codeAttributeWrapper.eq("name","code")
                    .eq("entity_id",entityVo.getId())
                    .last("limit 1");
            AttributePO codeAttribute = baseMapper.selectOne(codeAttributeWrapper);
            if(codeAttribute == null){
                return ResultEnum.SAVE_DATA_ERROR;
            }
            //“域字段id”赋值，值为同实体下code属性的id
            attributePo.setDomainId((int)codeAttribute.getId());
        }

        //修改数据
        if (baseMapper.update(attributePo,updateWrapper) <= 0) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 记录日志
        String desc = "修改一个属性,id:" + attributeUpdateDTO.getId();
        logService.saveEventLog((int) attributePo.getId(), ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.UPDATE, desc);

        //添加成功
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
     * 提交未提交的属性
     *
     * @return {@link List}<{@link AttributePO}>
     */
    @Override
    public ResultEntity<ResultEnum> getNotSubmittedData(Integer entityId) {

        //查询实体是否存在
        if (Objects.isNull(entityId) || entityService.getDataById(entityId) == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        //查询实体下是否存在可提交的属性
        QueryWrapper<AttributePO> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_id", entityId)
                .ne("status",AttributeStatusEnum.SUBMITTED);
        List<AttributePO> attributePoList = baseMapper.selectList(wrapper);
        if ( CollectionUtils.isEmpty(attributePoList)) {
            return ResultEntityBuild.build(ResultEnum.NO_DATA_TO_SUBMIT);
        }

        //提交
        com.fisk.task.dto.model.EntityDTO entityDTO = new com.fisk.task.dto.model.EntityDTO();
        entityDTO.setEntityId(entityId);
        entityDTO.setUserId(userHelper.getLoginUserInfo().getId());
        if (publishTaskClient.createBackendTable(entityDTO).getCode() != ResultEnum.SUCCESS.getCode()) {
            return ResultEntityBuild.build(ResultEnum.DATA_SUBMIT_ERROR);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS);
    }


    /**
     * 获取实体ER图信息
     *
     * @return {@link List}<{@link EntityMsgVO}>
     */
    @Override
    public List<EntityMsgVO> getEntityMsg() {
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

    /**
     * 删除属性(后台表已生成该字段，删除需等待提交)
     *
     * @param id 属性id
     * @return {@link ResultEnum}
     */
    public ResultEnum deleteAttribute(Integer id) {
        if(id == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        if(baseMapper.selectById(id) == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        if( baseMapper.deleteAttribute(id) == 0){
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 记录日志
        String desc = "删除一个属性,id:" + id +"（待提交）";
        logService.saveEventLog(id, ObjectTypeEnum.ATTRIBUTES, EventTypeEnum.DELETE, desc);

        return ResultEnum.SUCCESS;
    }

    /**
     * 删除数据（逻辑删除，仅用于删除  未提交的属性）
     *
     * @param id id
     * @return {@link ResultEnum}
     */
    public ResultEnum deleteDataById(Integer id) {
        if(id == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
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

    /**
     * 删除数据
     *
     * @param id id
     * @return {@link ResultEnum}
     */
    @Override
    public ResultEnum deleteData(Integer id){
        if(id == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        //判断数据是否存在
        AttributePO attributePo = baseMapper.selectById(id);
        if(attributePo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        //若状态为新增待提交，则直接逻辑删除
        //若为修改待提交、已提交、删除待提交，说明后台表已生成该字段，删除需等待提交
        if(attributePo.getStatus() == AttributeStatusEnum.INSERT){
            return this.deleteDataById(id);
        }else{
            return this.deleteAttribute(id);
        }
    }


}
