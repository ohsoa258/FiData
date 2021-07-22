package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.BusinessNameDTO;
import com.fisk.datamodel.dto.DataAreaDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.DataAreaPO;
import com.fisk.datamodel.map.DataAreaMap;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.DataAreaMapper;
import com.fisk.datamodel.service.IDataArea;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Service
public class DataAreaImpl extends ServiceImpl<DataAreaMapper, DataAreaPO> implements IDataArea {

    @Resource
    private BusinessAreaMapper businessAreaMapper;
    @Resource
    private BusinessAreaImpl businessAreaImpl;
    @Resource
    private DataAreaMapper mapper;
    @Resource
    UserHelper userHelper;

    @Override
    public List<BusinessNameDTO> getBusinessName() {

        // 查询业务域表id business_name
        List<BusinessAreaPO> businessNames = businessAreaMapper.getName();

        List<BusinessNameDTO> list = new ArrayList<>();
        for (BusinessAreaPO bpo : businessNames) {
            BusinessNameDTO businessNameDTO = new BusinessNameDTO();

            businessNameDTO.setId(bpo.getId());
            businessNameDTO.setBusinessName(bpo.getBusinessName());

            list.add(businessNameDTO);
        }

        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(DataAreaDTO dataAreaDTO) {

        // 获取当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.getId();

        // 根据业务名称查询业务域
        String businessName = dataAreaDTO.getBusinessName();
        BusinessAreaPO modelBusiness = businessAreaImpl.query()
                .eq("business_name", businessName)
                .eq("del_flag", 1)
                .one();

        DataAreaPO modelData = dataAreaDTO.toEntity(DataAreaPO.class);

        modelData.setBusinessid(modelBusiness.getId());
        modelData.setCreateUser(String.valueOf(userId));

        return this.save(modelData) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

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
        // 3.po -> dto
        DataAreaDTO dataAreaDTO = DataAreaMap.INSTANCES.poToDto(po);

        // 将businessName封装进去
        long businessid = po.getBusinessid();
        if (businessid == 0) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }

        BusinessAreaPO modelBusiness = businessAreaImpl.query()
                .eq("id", businessid)
                .eq("del_flag", 1)
                .one();
        if (modelBusiness == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }

        dataAreaDTO.setBusinessName(modelBusiness.getBusinessName());

        return dataAreaDTO;
    }

    @Override
    public ResultEnum updateDataArea(DataAreaDTO dataAreaDTO) {

        // 获取当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();

        // 根据id查询数据域表信息
        long id = dataAreaDTO.getId();
        // select * from tb_area_data where id=#{id}
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

        BusinessAreaPO modelBusiness = businessAreaImpl.query()
                .eq("business_name", businessName)
                .eq("del_flag", 1)
                .one();

        po.setBusinessid(modelBusiness.getId());
        po.setUpdateUser(String.valueOf(userInfo.id));

        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataArea(long id) {

        // 删除数据域表信息
        // 1.非空判断
        DataAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<Map<String,Object>> queryByPage(String key, Integer page, Integer rows) {
//
//        // 1.分页信息的健壮性处理
//        page = Math.min(page, 100);
//        rows = Math.max(rows, 1);

        // 新建分页
        Page<Map<String, Object>> pageMap = new Page<>(page, rows);

        return pageMap.setRecords(baseMapper.queryByPage(pageMap, key));
    }


}
