package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.constants.FilterSqlConstants;
import com.fisk.common.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.dto.MetaDataConfigDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.*;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.map.BusinessAreaMap;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.service.IBusinessArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Service
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(BusinessAreaDTO businessAreaDTO) {
        // 获取当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();

        // 1.dto->po
        BusinessAreaPO po = businessAreaDTO.toEntity(BusinessAreaPO.class);
        po.createUser = String.valueOf(userInfo.id);

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
        // 获取当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();

        // 修改时前端传来的id
        long id = businessAreaDTO.getId();
        BusinessAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        BusinessAreaPO po = businessAreaDTO.toEntity(BusinessAreaPO.class);
        po.updateUser = String.valueOf(userInfo.id);

        boolean update = this.updateById(po);

        return update ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteBusinessArea(long id) {

        // 1.非空判断
        BusinessAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
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
    public ResultEnum businessAreaPublic(int id)
    {
        try
        {
            BusinessAreaPublishDTO dto=new BusinessAreaPublishDTO();
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
            return ResultEnum.PUBLISH_FAILURE;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public BusinessAreaGetDataDTO getBusinessAreaPublicData(int businessAreaId)
    {
        BusinessAreaGetDataDTO data=new BusinessAreaGetDataDTO();
        try {
            data.dimensionList=dimensionAttribute.getDimensionMetaDataList(businessAreaId);
            data.atomicIndicatorList=atomicIndicators.atomicIndicatorPush(businessAreaId);
        }
        catch (Exception e)
        {
            log.error("BusinessAreaImpl,getBusinessAreaPublicData："+e.getMessage());
            data=null;
        }
        return data;
    }

}
