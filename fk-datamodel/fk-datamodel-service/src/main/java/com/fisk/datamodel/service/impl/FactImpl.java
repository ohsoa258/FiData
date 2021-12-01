package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.dto.fact.FactDropDTO;
import com.fisk.datamodel.dto.fact.FactListDTO;
import com.fisk.datamodel.dto.fact.FactScreenDropDTO;
import com.fisk.datamodel.entity.BusinessProcessPO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.map.FactAttributeMap;
import com.fisk.datamodel.map.FactMap;
import com.fisk.datamodel.mapper.BusinessProcessMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IFact;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class FactImpl implements IFact {

    @Resource
    FactMapper mapper;
    @Resource
    FactAttributeMapper attributeMapper;
    @Resource
    BusinessProcessMapper businessProcessMapper;

    @Override
    public ResultEnum addFact(FactDTO dto)
    {
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                //.eq(FactPO::getBusinessProcessId,dto.businessProcessId)
                .eq(FactPO::getFactTableEnName,dto.factTabName);
        FactPO po=mapper.selectOne(queryWrapper);
        if (po!=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        FactPO model= FactMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteFact(int id)
    {
        FactPO po=mapper.selectById(id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //查询业务域
        BusinessProcessPO businessProcessPO=businessProcessMapper.selectById(po.businessProcessId);
        if (businessProcessPO==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //删除组合对象
        DataModelVO vo=new DataModelVO();
        vo.businessId= String.valueOf(businessProcessPO.businessId);
        vo.dataClassifyEnum= DataClassifyEnum.DATAMODELING;
        vo.delBusiness=false;
        DataModelTableVO tableVO=new DataModelTableVO();
        tableVO.type= OlapTableEnum.FACT;
        List<Long> ids=new ArrayList<>();
        ids.add(Long.valueOf(id));
        tableVO.ids=ids;
        vo.factIdList=tableVO;

        return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public FactDTO getFact(int id)
    {
        return FactMap.INSTANCES.poToDto(mapper.selectById(id));
    }

    @Override
    public ResultEnum updateFact(FactDTO dto)
    {
        FactPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                //.eq(FactPO::getBusinessProcessId,dto.businessProcessId)
            .eq(FactPO::getFactTableEnName,dto.factTabName);
        FactPO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        return mapper.updateById(FactMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public IPage<FactListDTO> getFactList(QueryDTO dto)
    {
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactPO::getBusinessProcessId,dto.id);
        Page<FactPO> data=new Page<>(dto.getPage(),dto.getSize());
        return FactMap.INSTANCES.pagePoToDto(mapper.selectPage(data,queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public List<FactDropDTO> getFactDropList()
    {
        //获取事实表数据
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        List<FactDropDTO> list=FactMap.INSTANCES.dropPoToDto(mapper.selectList(queryWrapper));
        //获取事实字段表数据
        QueryWrapper<FactAttributePO> attribute=new QueryWrapper<>();
        for (FactDropDTO dto:list)
        {
            //向字段集合添加数据,只获取字段为度量类型的数据
             dto.list= FactAttributeMap.INSTANCES.poDropToDto(attributeMapper.selectList(attribute).stream().filter(e->e.getFactId()==dto.id && e.attributeType== FactAttributeEnum.MEASURE.getValue()).collect(Collectors.toList()));
        }
        return list;
    }

    @Override
    public List<FactScreenDropDTO> getFactScreenDropList()
    {
        //获取事实表数据
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        return FactMap.INSTANCES.dropScreenPoToDto(mapper.selectList(queryWrapper.orderByDesc("create_time")));
    }

    @Override
    public ResultEnum updateFactSql(DimensionSqlDTO dto)
    {
        FactPO model=mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        model.sqlScript=dto.sqlScript;
        return mapper.updateById(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

}
