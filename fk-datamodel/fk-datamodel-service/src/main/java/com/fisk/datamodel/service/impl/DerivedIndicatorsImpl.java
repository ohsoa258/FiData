package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsAddDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsListDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsQueryDTO;
import com.fisk.datamodel.entity.DerivedIndicatorsAttributePO;
import com.fisk.datamodel.entity.DerivedIndicatorsPO;
import com.fisk.datamodel.map.DerivedIndicatorsMap;
import com.fisk.datamodel.mapper.DerivedIndicatorsMapper;
import com.fisk.datamodel.mapper.DerivedIndicatorsAttributeMapper;
import com.fisk.datamodel.service.IDerivedIndicators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class DerivedIndicatorsImpl
        extends ServiceImpl<DerivedIndicatorsAttributeMapper,DerivedIndicatorsAttributePO>
        implements IDerivedIndicators {

    @Resource
    DerivedIndicatorsMapper mapper;
    @Resource
    DerivedIndicatorsAttributeMapper attributeMapper;
    @Resource
    UserHelper userHelper;

    @Override
    public Page<DerivedIndicatorsListDTO> getDerivedIndicatorsList(DerivedIndicatorsQueryDTO dto)
    {
        return mapper.queryList(dto.dto,dto);
    }

    @Override
    public ResultEnum deleteDerivedIndicators(long id)
    {
        DerivedIndicatorsPO po=mapper.selectById(id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addDerivedIndicators(DerivedIndicatorsDTO dto) {
        //判断是否重复
        QueryWrapper<DerivedIndicatorsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DerivedIndicatorsPO::getDerivedName, dto.derivedName)
                .eq(DerivedIndicatorsPO::getFactId, dto.factId);
        DerivedIndicatorsPO po = mapper.selectOne(queryWrapper);
        if (po != null) {
            return ResultEnum.DATA_EXISTS;
        }
        //添加派生指标数据，并返回插入id
        DerivedIndicatorsAddDTO poAdd=DerivedIndicatorsMap.INSTANCES.poToPo(dto);
        Date date = new Date(System.currentTimeMillis());
        poAdd.createTime=date;
        poAdd.createUser=userHelper.getLoginUserInfo().id.toString();
        int addId=mapper.insertAndGetId(poAdd);
        //判断insert是否成功，并返回插入主键id是否成功
        if (addId==0 || poAdd.id==0)
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //派生指标聚合字段集合
        if (poAdd.attributeId !=null && poAdd.attributeId.size()>0)
        {
            List<DerivedIndicatorsAttributePO> ids = new ArrayList<>();
            for (Integer item : dto.attributeId) {
                DerivedIndicatorsAttributePO model = new DerivedIndicatorsAttributePO();
                model.factAttributeId = item;
                model.derivedIndicatorsId=poAdd.id;
                ids.add(model);
            }
            boolean result = this.saveBatch(ids);
            if (!result) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public DerivedIndicatorsDTO getDerivedIndicators(long id)
    {
        DerivedIndicatorsPO po=mapper.selectById(id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }
        DerivedIndicatorsDTO dto=DerivedIndicatorsMap.INSTANCES.poToDto(po);
        //获取聚合字段
        QueryWrapper<DerivedIndicatorsAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DerivedIndicatorsAttributePO::getDerivedIndicatorsId,po.id);
        List<DerivedIndicatorsAttributePO> idList =attributeMapper.selectList(queryWrapper).stream().collect(Collectors.toList());
        List<Integer> list=new ArrayList<>();
        for (DerivedIndicatorsAttributePO item:idList)
        {
            list.add(item.factAttributeId);
        }
        dto.attributeId =list;
        return  dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum updateDerivedIndicators(DerivedIndicatorsDTO dto)
    {
        DerivedIndicatorsPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }
        po=DerivedIndicatorsMap.INSTANCES.dtoToPo(dto);
        //保存派生指标数据
        int flat=mapper.updateById(po);
        if (flat==0)
        {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }
        //删除聚合数据
        QueryWrapper<DerivedIndicatorsAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DerivedIndicatorsAttributePO::getDerivedIndicatorsId,dto.id);
        List<DerivedIndicatorsAttributePO> listData=attributeMapper.selectList(queryWrapper);
        if ( listData !=null && listData.size()>0)
        {
            boolean res=this.remove(queryWrapper);
            if (!res) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
            }
        }
        //添加最新聚合数据
        List<DerivedIndicatorsAttributePO> ids=new ArrayList<>();
        for (Integer item : dto.attributeId) {
            DerivedIndicatorsAttributePO model = new DerivedIndicatorsAttributePO();
            model.factAttributeId = item;
            model.derivedIndicatorsId=dto.id;
            ids.add(model);
        }
        boolean result = this.saveBatch(ids);
        if (!result) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR,"数据保存失败");
        }
        return ResultEnum.SUCCESS;
    }

}
