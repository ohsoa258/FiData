package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.mdm.dto.model.ModelUpdateDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.entity.ModelVersionPO;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ModelVersionStatusEnum;
import com.fisk.mdm.enums.ModelVersionTypeEnum;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.ModelMap;
import com.fisk.mdm.map.ModelVersionMap;
import com.fisk.mdm.mapper.ModelMapper;
import com.fisk.mdm.service.EventLogService;
import com.fisk.mdm.service.IModelService;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.service.IModelVersionService;
import com.fisk.mdm.vo.model.ModelVO;
import com.fisk.system.client.UserClient;
import com.fisk.task.client.PublishTaskClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fisk.system.dto.userinfo.UserDTO;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author ChenYa
 */
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, ModelPO> implements IModelService {

    @Resource
    EventLogService logService;

    @Resource
    IModelVersionService iModelVersionService;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private UserHelper userHelper;

    @Resource
    private UserClient userClient;


    /**
     * 通过id查询
     *
     * @param id id
     * @return {@link ResultEntity}<{@link ModelVO}>
     */
    @Override
    public ResultEntity<ModelVO> getById(Integer id) {
        ModelVO modelVO = ModelMap.INSTANCES.poToVo(baseMapper.selectById(id));
        if(Objects.isNull(modelVO)){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, modelVO);
    }

    /**
     * 添加模型
     * @param modelDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addData(ModelDTO modelDTO) {

        //判断名称是否存在
        QueryWrapper<ModelPO> wrapper = new QueryWrapper<>();
        wrapper.eq("name",modelDTO.name);
        if(baseMapper.selectOne(wrapper) != null){
            return ResultEnum.NAME_EXISTS;
        }

        //添加数据
        ModelPO modelPO = ModelMap.INSTANCES.dtoToPo(modelDTO);
        if (baseMapper.insert(modelPO) <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //创建成功后创建默认版本
        ModelVersionDTO modelVersionDTO = new ModelVersionDTO();
        modelVersionDTO.setModelId((int)modelPO.getId());
        modelVersionDTO.setName("VERSION1");
        modelVersionDTO.setDesc("新增模型生成的默认版本");
        modelVersionDTO.setStatus(ModelVersionStatusEnum.OPEN);
        modelVersionDTO.setType(ModelVersionTypeEnum.SYSTEM_CREAT);
        ModelVersionPO modelVersionPO = ModelVersionMap.INSTANCES.dtoToPo(modelVersionDTO);
        if(iModelVersionService.save(modelVersionPO) == false){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        modelPO.setCurrentVersionId((int)modelVersionPO.getId());
        modelPO.setAttributeLogName("tb_attribute_log_"+modelPO.getId());
        if(baseMapper.updateById(modelPO) <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        //提交创建属性日志表任务
        com.fisk.task.dto.model.ModelDTO dto = new com.fisk.task.dto.model.ModelDTO();
        dto.setAttributeLogName(modelPO.attributeLogName);
        dto.setUserId(userHelper.getLoginUserInfo().getId());
        dto.setSendTime(modelPO.getCreateTime());
        publishTaskClient.pushModelByName(dto);


        // 记录日志
        String desc = "新增一个模型,id:" + modelPO.getId();
        logService.saveEventLog((int)modelPO.getId(),ObjectTypeEnum.MODEL,EventTypeEnum.SAVE,desc);

        //创建成功
        return ResultEnum.SUCCESS;
    }

    /**
     * 编辑
     * @param modelUpdateDTO
     * @return
     */
    @Override
    public ResultEnum editData(ModelUpdateDTO modelUpdateDTO) {
        ModelPO modelPO = baseMapper.selectById(modelUpdateDTO.getId());

        //判断数据是否存在
        if (modelPO == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        //判断修改后的名称是否存在
        QueryWrapper<ModelPO> wrapper = new QueryWrapper<>();
        wrapper.eq("name",modelUpdateDTO.getName())
                .ne("id",modelUpdateDTO.getId());
        if(baseMapper.selectOne(wrapper) != null){
            return ResultEnum.NAME_EXISTS;
        }

        //把DTO转化到查询出来的PO上
        modelPO = ModelMap.INSTANCES.updateDtoToPo(modelUpdateDTO);


        //修改数据
        if (baseMapper.updateById(modelPO) <= 0) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 记录日志
        String desc = "修改一个模型,id:" + modelUpdateDTO.getId();
        logService.saveEventLog((int)modelPO.getId(),ObjectTypeEnum.MODEL,EventTypeEnum.UPDATE,desc);

        //添加成功
        return ResultEnum.SUCCESS;
    }

    /**
     * 删除
     * @param id
     * @return
     */
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
        String desc = "删除一个模型,id:" + id;
        logService.saveEventLog(id,ObjectTypeEnum.MODEL,EventTypeEnum.DELETE,desc);

        //删除成功
        return ResultEnum.SUCCESS;
    }

    /**
     * 分页查询
     * @param query
     * @return
     */
    @Override
    public Page<ModelVO> getAll(ModelQueryDTO query) {

        Page<ModelVO> all = baseMapper.getAll(query.page, query);

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
}
