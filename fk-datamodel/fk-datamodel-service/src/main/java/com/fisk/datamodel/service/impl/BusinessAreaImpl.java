package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.dto.BusinessPageDTO;
import com.fisk.datamodel.dto.BusinessPageResultDTO;
import com.fisk.datamodel.dto.BusinessQueryDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.map.BusinessAreaMap;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.service.IBusinessArea;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(BusinessAreaDTO businessAreaDTO) {
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

//        // 1.分页信息的健壮性处理
//        page = Math.min(page, 100);
//        rows = Math.max(rows, 1);

        Page<Map<String, Object>> pageMap = new Page<>(page, rows);

        return pageMap.setRecords(baseMapper.queryByPage(pageMap, key));
    }

    @Override
    public List<FilterFieldDTO> getBusinessAreaColumn()
    {
        return getMetadata.getMetadataList("dmp_datamodel_db","tb_area_business","");
    }

    @Override
    public Page<BusinessPageResultDTO> getDataList(BusinessQueryDTO query)
    {
        StringBuilder str = new StringBuilder();
        if (query.key !=null && query.key.length()>0)
        {
            str.append(" and business_name like concat('%', "+"'"+query.key+"'"+ ", '%') ");
        }
        //筛选器拼接
        str.append(generateCondition.getCondition(query.dto));
        BusinessPageDTO data=new BusinessPageDTO();
        data.page=query.page;
        data.where=str.toString();

        return  baseMapper.queryList(query.page,data);
    }

}
