package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.dto.fact.FactDropDTO;
import com.fisk.datamodel.dto.fact.FactListDTO;
import com.fisk.datamodel.dto.fact.FactScreenDropDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.entity.FactAttributePO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.map.FactAttributeMap;
import com.fisk.datamodel.map.FactMap;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IFact;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class FactImpl implements IFact {

    @Resource
    FactMapper mapper;
    @Resource
    FactAttributeMapper attributeMapper;
    @Resource
    FactAttributeImpl factAttributeImpl;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    UserHelper userHelper;

    @Override
    public ResultEnum addFact(FactDTO dto)
    {
        QueryWrapper<FactPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                //.eq(FactPO::getBusinessProcessId,dto.businessProcessId)
                .eq(FactPO::getFactTabName,dto.factTabName);
        FactPO po=mapper.selectOne(queryWrapper);
        if (po!=null)
        {
            return ResultEnum.FACT_EXIST;
        }
        FactPO model= FactMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteFact(int id)
    {
        try {
            FactPO po=mapper.selectById(id);
            if (po==null)
            {
                return ResultEnum.DATA_NOTEXISTS;
            }
            //删除事实字段表
            QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
            queryWrapper.select("id").lambda().eq(FactAttributePO::getFactId,id);
            List<Integer> factAttributeIds=(List)attributeMapper.selectObjs(queryWrapper);
            if (!CollectionUtils.isEmpty(factAttributeIds))
            {
                ResultEnum resultEnum = factAttributeImpl.deleteFactAttribute(factAttributeIds);
                if (ResultEnum.SUCCESS !=resultEnum)
                {
                    throw new FkException(resultEnum);
                }
            }
            //拼接删除niFi参数
            DataModelVO vo = niFiDelTable(po.businessId, id);
            publishTaskClient.deleteNifiFlow(vo);
            //拼接删除DW/Doris库中维度表
            PgsqlDelTableDTO dto = delDwDorisTable(po.factTabName);
            publishTaskClient.publishBuildDeletePgsqlTableTask(dto);

            return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
        }
        catch (Exception e)
        {
            log.error("deleteFact:"+e.getMessage());
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    /**
     * 拼接niFi删除表参数
     * @param businessAreaId
     * @param factId
     * @return
     */
    public DataModelVO niFiDelTable(int businessAreaId,int factId)
    {
        DataModelVO vo=new DataModelVO();
        vo.businessId= String.valueOf(businessAreaId);
        vo.dataClassifyEnum= DataClassifyEnum.DATAMODELING;
        vo.delBusiness=false;
        DataModelTableVO tableVO=new DataModelTableVO();
        tableVO.type= OlapTableEnum.FACT;
        List<Long> ids=new ArrayList<>();
        ids.add(Long.valueOf(factId));
        tableVO.ids=ids;
        vo.factIdList=tableVO;
        return vo;
    }

    /**
     * 拼接删除DW/Doris表
     * @param factName
     * @return
     */
    public PgsqlDelTableDTO delDwDorisTable(String factName)
    {
        PgsqlDelTableDTO dto=new PgsqlDelTableDTO();
        dto.businessTypeEnum= BusinessTypeEnum.DATAMODEL;
        dto.delApp=false;
        List<TableListDTO> tableList=new ArrayList<>();
        TableListDTO table=new TableListDTO();
        table.tableName=factName;
        tableList.add(table);
        dto.tableList=tableList;
        dto.userId=userHelper.getLoginUserInfo().id;
        return dto;
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
            .eq(FactPO::getFactTabName,dto.factTabName);
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

    @Override
    public void updateFactPublishStatus(ModelPublishStatusDTO dto)
    {
        FactPO po=mapper.selectById(dto.id);
        if (po !=null)
        {
            //0:DW发布状态
            if (dto.type==0)
            {
                po.isPublish=dto.status;
            }else {
                po.dorisPublish=dto.status;
            }
            mapper.updateById(po);
        }
    }

}
