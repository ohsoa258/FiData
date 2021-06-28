package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.BusinessNameDTO;
import com.fisk.datamodel.dto.DataAreaDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.DataAreaPO;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.DataAreaMapper;
import com.fisk.datamodel.service.IDataArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: Lock
 */
@Service
public class DataAreaImpl extends ServiceImpl<DataAreaMapper, DataAreaPO> implements IDataArea {

    @Resource
    private BusinessAreaMapper businessAreaMapper;

    @Autowired
    private BusinessAreaImpl businessAreaImpl;

    /**
     * 添加数据域时,显示所有业务域
     *
     * @return
     */
    @Override
    public List<BusinessNameDTO> getBusinessName() {

        // 查询业务域表id business_name
//        List<BusinessAreaPO> areaPOList = businessAreaImpl.query().eq("del_flag", 1).list();
        List<BusinessAreaPO> businessNames = businessAreaMapper.getName();

        List<BusinessNameDTO> list = new ArrayList<>();
        for (BusinessAreaPO businessAreaPO : businessNames) {
            BusinessNameDTO businessNameDTO = new BusinessNameDTO();

            businessNameDTO.setId(businessAreaPO.getId());
            businessNameDTO.setBusinessName(businessAreaPO.getBusinessName());

            list.add(businessNameDTO);
        }

        return list;
    }

    /**
     * 添加数据域
     *
     * @param dataAreaDTO
     * @return
     */
    @Override
    @Transactional
    public ResultEnum addData(DataAreaDTO dataAreaDTO) {

        String businessName = dataAreaDTO.getBusinessName();
        BusinessAreaPO businessAreaPO = businessAreaImpl.query()
                .eq("business_name", businessName)
                .eq("del_flag", 1)
                .one();

        DataAreaPO po = dataAreaDTO.toEntity(DataAreaPO.class);

        po.setBusinessid(businessAreaPO.getId());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        po.setCreateTime(date);
        po.setUpdateTime(date);
        po.setDelFlag(1);

        return this.save(po) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 回显数据域
     * @param id
     * @return
     */
    @Override
    public DataAreaDTO getData(long id) {

        // 1.查询 select * from 数据域表 where (id=1 and del_flag=1)
        DataAreaPO po = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();

        // 2.非空判断
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }

        DataAreaDTO dataAreaDTO = new DataAreaDTO(po);

        // 将businessName封装进去
        long businessid = po.getBusinessid();
        if (businessid == 0) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }

        BusinessAreaPO businessAreaPO = businessAreaImpl.query()
                .eq("id", businessid)
                .eq("del_flag", 1)
                .one();
        if (businessAreaPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }

        dataAreaDTO.setBusinessName(businessAreaPO.getBusinessName());


//        BusinessNameDTO businessNameDTO = new BusinessNameDTO();
//
//        // 业务表id
//        long businessid = po.getBusinessid();
//
//        businessNameDTO.setId(businessid);
//
//        // select * from 业务域表 where (id=1 and del_flag=1)
//        BusinessAreaPO businessAreaPO = businessAreaImpl.query()
//                .eq("id", businessid)
//                .eq("del_flag", 1)
//                .one();
//
//        businessNameDTO.setBusinessName(businessAreaPO.getBusinessName());

        return dataAreaDTO;
    }

    /**
     * 修改业务表
     * @param dataAreaDTO
     * @return
     */
    @Override
    public ResultEnum updateDataArea(DataAreaDTO dataAreaDTO) {

        // 根据id查询数据域表信息
        long id = dataAreaDTO.getId();
        // select * from 数据域表 where id=1
        DataAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        DataAreaPO po = dataAreaDTO.toEntity(DataAreaPO.class);

        // 将businessName转换成business_id
        String businessName = dataAreaDTO.getBusinessName();
        if (businessName == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        BusinessAreaPO businessAreaPO = businessAreaImpl.query()
                .eq("business_name", businessName)
                .eq("del_flag", 1)
                .one();

        po.setBusinessid(businessAreaPO.getId());
        po.setDelFlag(1);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        po.setUpdateTime(date);

        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    /**
     * 删除数据域
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)// 任何异常都回滚数据
    public ResultEnum deleteDataArea(long id) {

        // 删除数据域表信息
        // 1.非空判断
        DataAreaPO areaPO = this.getById(id);
        if (areaPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 将del_flag的状态改为0
        areaPO.setDelFlag(0);

        // update 表名 set del_flag=1 where id=1;
        boolean update = this.updateById(areaPO);

        return update ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 数据域分页查询
     * @param key
     * @param page
     * @param rows
     * @return
     */
    @Override
    public Page<Map<String,Object>> queryByPage(String key, Integer page, Integer rows) {

        // 1.分页信息的健壮性处理
        page = Math.min(page, 100);  // 返回二者间较小的值,即当前页最大不超过100页,避免单次查询太多数据影响效率
        rows = Math.max(rows, 1);    // 每页至少1条

        // 新建分页
        Page<Map<String, Object>> pageMap = new Page<>(page, rows);

        return pageMap.setRecords(baseMapper.queryByPage(pageMap, key));
    }


}
