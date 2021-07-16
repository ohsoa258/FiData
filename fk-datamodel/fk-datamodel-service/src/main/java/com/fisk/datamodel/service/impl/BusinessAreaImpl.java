package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.*;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.service.IBusinessArea;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Service
public class BusinessAreaImpl extends ServiceImpl<BusinessAreaMapper, BusinessAreaPO> implements IBusinessArea {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(BusinessAreaDTO businessAreaDTO) {

        // 1.dto->po
        BusinessAreaPO po = businessAreaDTO.toEntity(BusinessAreaPO.class);

        Date date = new Date(System.currentTimeMillis());
        po.setCreateTime(date);
        po.setUpdateTime(date);
        po.setDelFlag(1);

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
        return new BusinessAreaDTO(po);
    }

    @Override
    public ResultEnum updateBusinessArea(BusinessAreaDTO businessAreaDTO) {

        // 修改时前端传来的id
        long id = businessAreaDTO.getId();
        BusinessAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        BusinessAreaPO po = businessAreaDTO.toEntity(BusinessAreaPO.class);

        // 设置删除状态
        po.setDelFlag(1);

        Date date = new Date(System.currentTimeMillis());
        po.setUpdateTime(date);

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

        // 将del_flag的状态改为0
        model.setDelFlag(0);

        // update 表名 set del_flag=1 where id=1;
        boolean update = this.updateById(model);

        return update ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows) {

        // 1.分页信息的健壮性处理
        page = Math.min(page, 100);
        rows = Math.max(rows, 1);

        Page<Map<String, Object>> pageMap = new Page<>(page, rows);

        return pageMap.setRecords(baseMapper.queryByPage(pageMap, key));
    }

    @Override
    public List<BusinessFilterDTO> getBusinessAreaColumn()
    {
        return baseMapper.columnList();
    }

    @Override
    public Page<BusinessPageResultDTO> getDataList(BusinessPageDTO query)
    {
        StringBuilder str = new StringBuilder();
        if (query.key !=null && query.key.length()>0)
        {
            str.append(" and business_name like concat('%', "+"'"+query.key+"'"+ ", '%') ");
        }
        for (BusinessQueryDTO model: query.dto)
        {
            switch (model.queryType)
            {
                case "大于":
                    str.append(" and " +model.columnName+">" +"'"+model.columnValue+"' ");
                    break;
                case "小于":
                    str.append(" and " +model.columnName+"<"+"'" +model.columnValue+"' ");
                    break;
                case "等于":
                    str.append(" and " +model.columnName+"="+"'"+model.columnValue+"' ");
                    break;
                case "包含":
                    str.append(" and " +model.columnName+" like concat('%'," + "'" + model.columnValue+"'" + ", '%') " );
                    break;
            }
        }
        BusinessPage data=new BusinessPage();
        data.page=query.page;
        data.where=str.toString();

        return  baseMapper.queryList(query.page,data);
    }

}
