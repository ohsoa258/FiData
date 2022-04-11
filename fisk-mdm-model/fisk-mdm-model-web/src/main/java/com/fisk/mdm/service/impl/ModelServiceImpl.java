package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.eventlog.EventLogDTO;
import com.fisk.mdm.dto.model.ModelUpdateDTO;
import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ModelVersionStatusEnum;
import com.fisk.mdm.enums.ModelVersionTypeEnum;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.ModelMap;
import com.fisk.mdm.mapper.ModelMapper;
import com.fisk.mdm.service.EventLogService;
import com.fisk.mdm.service.IModelService;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.service.IModelVersionService;
import com.fisk.mdm.vo.model.ModelVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, ModelPO> implements IModelService {

    @Resource
    EventLogService logService;

    @Resource
    IModelVersionService iModelVersionService;

    /**
     * 根据id查询
     * @param id
     * @return
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
        if(iModelVersionService.addData(modelVersionDTO) == ResultEnum.SAVE_DATA_ERROR){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 记录日志
       String desc = "新增一个模型,id:" + modelPO.getId();
        if (logService.saveEventLog((int)modelPO.getId(),ObjectTypeEnum.MODEL,EventTypeEnum.SAVE,desc) == ResultEnum.SAVE_DATA_ERROR){
            return ResultEnum.SAVE_DATA_ERROR;
        }

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
        wrapper.eq("name",modelUpdateDTO.getName());
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

        if (logService.saveEventLog((int)modelPO.getId(),ObjectTypeEnum.MODEL,EventTypeEnum.UPDATE,desc) == ResultEnum.SAVE_DATA_ERROR){
            return ResultEnum.SAVE_DATA_ERROR;
        }

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

        if (logService.saveEventLog(id,ObjectTypeEnum.MODEL,EventTypeEnum.DELETE,desc) == ResultEnum.SAVE_DATA_ERROR){
            return ResultEnum.SAVE_DATA_ERROR;
        }

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

        return all;
    }
}
