package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.map.ModelMap;
import com.fisk.mdm.mapper.ModelMapper;
import com.fisk.mdm.service.IModelService;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.vo.model.ModelVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service()
public class ModelServiceImpl extends ServiceImpl<ModelMapper, ModelPO> implements IModelService {

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
     * @param model
     * @return
     */
    @Override
    public ResultEnum addData(ModelDTO model) {
        //根据name查询，若已存在则不添加
        QueryWrapper<ModelPO> wrapper = new QueryWrapper<>();
        wrapper.eq("name",model.name);
        ModelPO modelPO = baseMapper.selectOne(wrapper);
        if(modelPO != null){
            return ResultEnum.DATA_EXISTS;
        }

        boolean isInsert = baseMapper.insert(ModelMap.INSTANCES.dtoToPo(model)) > 0;
        if (!isInsert) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 编辑
     * @param model
     * @return
     */
    @Override
    public ResultEnum editData(ModelDTO model) {
        ModelPO modelPO = baseMapper.selectById(model.id);

        //判断数据是否存在
        if (modelPO==null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        //把DTO转化到查询出来的PO上
        modelPO = ModelMap.INSTANCES.dtoToPo(model);

        boolean isUpData = baseMapper.updateById(modelPO) > 0;
        if (!isUpData) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }
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
        if (baseMapper.selectById(id)==null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        boolean isDelete = baseMapper.deleteById(id) > 0;
        if (!isDelete) {
            return ResultEnum.DATA_NOTEXISTS;
        }
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
