package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.service.IBusinessArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author: Lock
 */
@Service
public class BusinessAreaImpl extends ServiceImpl<BusinessAreaMapper, BusinessAreaPO> implements IBusinessArea {


    /**
     * 添加业务域
     *
     * @param businessAreaDTO
     * @return
     */
    @Override
    public ResultEnum addData(BusinessAreaDTO businessAreaDTO) {

        // 1.dto->po
        BusinessAreaPO po = businessAreaDTO.toEntity(BusinessAreaPO.class);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        po.setCreateTime(date);
        po.setUpdateTime(date);
        po.setDelFlag(1);

        boolean save = this.save(po);

        return save ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 回显数据: 根据id查询
     *
     * @param id
     * @return
     */
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

    /**
     * 修改业务域
     *
     * @param businessAreaDTO
     * @return
     */
    @Override
    public ResultEnum updateBusinessArea(BusinessAreaDTO businessAreaDTO) {

        // 修改时前端传来的id
        long id = businessAreaDTO.getId();
        BusinessAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        BusinessAreaPO po = businessAreaDTO.toEntity(BusinessAreaPO.class);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        po.setUpdateTime(date);

        boolean update = this.updateById(po);

        return update ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    /**
     * 删除业务域
     *
     * @param id
     * @return
     */
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

    /**
     * 分页查询
     *
     * @param key
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageDTO<BusinessAreaDTO> listBusinessArea(String key, Integer page, Integer rows) {
        // 1.分页信息的健壮性处理
        page = Math.min(page, 100);  // 返回二者间较小的值,即当前页最大不超过100页,避免单词查询太多数据影响效率
        rows = Math.max(rows, 5);    // 每页至少5条

        // 2.使用mybatis-plus自带的分页功能
        Page<BusinessAreaPO> dtoPage = new Page<>(page, rows);

        // 可以排空空格字符的查询
        boolean isKeyExists = StringUtils.isNoneBlank(key);

//        this.query().like(isKeyExists, "name", key)
//                .or()
//                .eq(isKeyExists, "letter", key)
//                .page(brandPage);
        this.query().like(isKeyExists, "business_name", isKeyExists)
//                .or()
                .eq("del_flag", 1)
                .orderByDesc("update_time")
                .page(dtoPage);

        // 取出数据列表
        List<BusinessAreaPO> brandList = dtoPage.getRecords();

        // pojo->dto

        return new PageDTO<>(
                dtoPage.getTotal(), // 总条数
                dtoPage.getSize(),  // 总页数
                BusinessAreaDTO.convertEntityList(brandList) // 当前页数据
        );
    }

}
