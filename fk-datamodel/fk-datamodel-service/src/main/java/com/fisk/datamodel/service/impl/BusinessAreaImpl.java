package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.constants.FilterSqlConstants;
import com.fisk.common.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.dto.MetaDataConfigDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.*;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.BusinessProcessPO;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.BusinessAreaMap;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.BusinessProcessMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IBusinessArea;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.olap.BuildCreateModelTaskDto;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Service
@Slf4j
public class BusinessAreaImpl extends ServiceImpl<BusinessAreaMapper, BusinessAreaPO> implements IBusinessArea {

    @Resource
    GenerateCondition generateCondition;
    @Resource
    GetMetadata getMetadata;
    @Resource
    UserHelper userHelper;
    @Resource
    BusinessAreaMapper mapper;
    @Resource
    GetConfigDTO getConfig;
    @Resource
    DimensionAttributeImpl dimensionAttribute;
    @Resource
    AtomicIndicatorsImpl atomicIndicators;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    BusinessProcessMapper businessProcessMapper;
    @Resource
    FactMapper factMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(BusinessAreaDTO businessAreaDTO) {
        //判断名称是否重复
        QueryWrapper<BusinessAreaPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessAreaPO::getBusinessName,businessAreaDTO.businessName);
        BusinessAreaPO businessAreaPO=mapper.selectOne(queryWrapper);
        if (businessAreaPO !=null)
        {
            return ResultEnum.BUSINESS_AREA_EXIST;
        }
        BusinessAreaPO po = businessAreaDTO.toEntity(BusinessAreaPO.class);
        boolean save = this.save(po);

        return save ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public BusinessAreaDTO getData(long id) {

        // select * from 表 where id=#{id} and del_flag=1
        BusinessAreaPO po = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();

        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }
        return BusinessAreaMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum updateBusinessArea(BusinessAreaDTO businessAreaDTO) {
        //根据id,判断是否存在
        long id = businessAreaDTO.getId();
        BusinessAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //判断名称是否重复
        QueryWrapper<BusinessAreaPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessAreaPO::getBusinessName,businessAreaDTO.businessName);
        BusinessAreaPO businessAreaPO=mapper.selectOne(queryWrapper);
        if (businessAreaPO !=null && businessAreaPO.id !=businessAreaDTO.id)
        {
            return ResultEnum.BUSINESS_AREA_EXIST;
        }
        BusinessAreaPO po = businessAreaDTO.toEntity(BusinessAreaPO.class);
        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteBusinessArea(long id) {

        // 1.非空判断
        BusinessAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        //获取业务域下所有维度、事实相关数据
        DataModelVO vo=new DataModelVO();
        vo.dataClassifyEnum= DataClassifyEnum.DATAMODELING;
        vo.businessId= String.valueOf(model.id);

        //获取所有维度id
        DataModelTableVO dimensionTable=new DataModelTableVO();
        dimensionTable.type= OlapTableEnum.DIMENSION;
        QueryWrapper<DimensionPO> queryWrapperPo=new QueryWrapper<>();
        queryWrapperPo.select("id").lambda().eq(DimensionPO::getBusinessId,model.id);
        dimensionTable.ids=(List)dimensionMapper.selectObjs(queryWrapperPo).stream().collect(Collectors.toList());
        vo.dimensionIdList=dimensionTable;

        //获取业务域下所有业务过程id
        QueryWrapper<BusinessProcessPO> businessProcessPOQueryWrapper=new QueryWrapper<>();
        businessProcessPOQueryWrapper.select("id").lambda().eq(BusinessProcessPO::getBusinessId,model.id);
        List<Integer> processIds=(List)businessProcessMapper.selectObjs(businessProcessPOQueryWrapper).stream().collect(Collectors.toList());
        if (processIds.size()>0)
        {
            //获取业务过程下所有事实id
            DataModelTableVO factTable=new DataModelTableVO();
            factTable.type= OlapTableEnum.FACT;
            QueryWrapper<FactPO> factPOQueryWrapper=new QueryWrapper<>();
            factPOQueryWrapper.select("id").in("business_process_id",processIds);
            factTable.ids=(List) factMapper.selectObjs(factPOQueryWrapper).stream().collect(Collectors.toList());
            vo.factIdList=factTable;
        }

        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows) {

        Page<Map<String, Object>> pageMap = new Page<>(page, rows);

        return pageMap.setRecords(baseMapper.queryByPage(pageMap, key));
    }

    @Override
    public List<FilterFieldDTO> getBusinessAreaColumn() {
        MetaDataConfigDTO dto=new MetaDataConfigDTO();
        dto.url= getConfig.url;
        dto.userName=getConfig.username;
        dto.password=getConfig.password;
        dto.tableName="tb_area_business";
        dto.filterSql=FilterSqlConstants.BUSINESS_AREA_SQL;
        return getMetadata.getMetadataList(dto);
    }

    @Override
    public Page<BusinessPageResultDTO> getDataList(BusinessQueryDTO query) {
        StringBuilder str = new StringBuilder();
        if (query !=null && StringUtils.isNotEmpty(query.key)) {
            str.append(" and business_name like concat('%', " + "'" + query.key + "'" + ", '%') ");
        }
        //筛选器拼接
        str.append(generateCondition.getCondition(query.dto));
        BusinessPageDTO data = new BusinessPageDTO();
        data.page = query.page;
        data.where = str.toString();

        return baseMapper.queryList(query.page, data);
    }

    @Override
    public ResultEntity<Object> businessAreaPublic(int id)
    {
        try
        {
            /*BuildCreateModelTaskDto dto=new BuildCreateModelTaskDto();
            dto.businessAreaId=id;
            dto.userId=userHelper.getLoginUserInfo().id;
            ResultEntity<Object> objectResultEntity = publishTaskClient.publishOlapCreateModel(dto);
            System.out.println(objectResultEntity);*/
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
            return ResultEntityBuild.build(ResultEnum.PUBLISH_FAILURE);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS);
    }

    @Override
    public ResultEntity<BusinessAreaGetDataDTO> getBusinessAreaPublicData(IndicatorQueryDTO dto)
    {
        BusinessAreaGetDataDTO data=new BusinessAreaGetDataDTO();
        try {
            data.userId=userHelper.getLoginUserInfo().id;
            data.businessAreaId=dto.businessAreaId;
            //根据事实表id获取指标
            data.atomicIndicatorList=atomicIndicators.atomicIndicatorPush(dto.factIds);
            //获取事实表关联的维度
            data.dimensionList=dimensionAttribute.getDimensionMetaDataList(dto.factIds);
            //消息推送
            publishTaskClient.publishOlapCreateModel(data);
        }
        catch (Exception e)
        {
            log.error("BusinessAreaImpl,getBusinessAreaPublicData："+e.getMessage());
            return ResultEntityBuild.build(ResultEnum.VISUAL_QUERY_ERROR,data);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS,data);
    }

    @Override
    public void updatePublishStatus(int id,int isSuccess)
    {
        BusinessAreaPO po=mapper.selectById(id);
        if (po==null)
        {
            log.info(id+":业务域数据不存在");
            return;
        }
        po.setIsPublish(isSuccess);
        //发布时间
        po.setPublishTime(LocalDateTime.now());
        int flat=mapper.updateById(po);
        log.info(po.getBusinessName()+"发布状态:"+flat);
    }

}
