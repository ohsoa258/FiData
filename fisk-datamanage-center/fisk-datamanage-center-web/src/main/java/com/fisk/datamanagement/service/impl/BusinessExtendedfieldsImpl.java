package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.dto.modelAndIndex.ModelAndIndexMappingDTO;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;
import com.fisk.datamanagement.mapper.BusinessExtendedfieldsMapper;
import com.fisk.datamanagement.service.BusinessExtendedfieldsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xgf
 * @date 2023年11月20日 10:44
 */
@Service
public class BusinessExtendedfieldsImpl extends ServiceImpl<BusinessExtendedfieldsMapper, BusinessExtendedfieldsPO> implements BusinessExtendedfieldsService {

    @Resource
    BusinessExtendedfieldsMapper businessExtendedfieldsMapper;

    /**
     * 展示维度数据
     *
     * @param
     */
    @Override
    public List<BusinessExtendedfieldsPO> addBusinessExtendedfields(String categoryId) {
        // 查询数据
        List<BusinessExtendedfieldsPO> po = businessExtendedfieldsMapper.selectParentpId(categoryId);
        System.out.println(po);
        return po;
    }

    /**
     * 关联数仓表字段和指标标准（维度表字段 指标粒度）
     * 维度表字段则关联 指标粒度
     * 事实表字段则关联 指标所属
     *
     * @param dtos
     * @return
     */
    @Override
    public Object setMetricGranularityByModelField(List<ModelAndIndexMappingDTO> dtos) {
        //获取指标id
        Integer indexId = dtos.get(0).getIndexId();
        //获取字段id
        String fieldId = dtos.get(0).getFieldId();
        //如果此次指标id为空 则认为此次没有选择指标 清空该字段对应的指标粒度表
        if (indexId == null) {
            LambdaQueryWrapper<BusinessExtendedfieldsPO> delWrapper = new LambdaQueryWrapper<>();
            delWrapper.eq(BusinessExtendedfieldsPO::getAttributeid, fieldId);
            return this.remove(delWrapper);
        }

        //每次都删除所有字段绑定的指标粒度
        LambdaQueryWrapper<BusinessExtendedfieldsPO> delAll = new LambdaQueryWrapper<>();
        delAll.eq(BusinessExtendedfieldsPO::getAttributeid, fieldId);
        this.remove(delAll);

        ArrayList<BusinessExtendedfieldsPO> pos = new ArrayList<>();

        /*
        开始存储这次的关联关系
         */

        //转换对象属性
        for (ModelAndIndexMappingDTO dto : dtos) {
            BusinessExtendedfieldsPO po = new BusinessExtendedfieldsPO();
            String tblType = null;
            //0公共域维度 1其他域维度 2事实表
            if (dto.getTblType() == 0) {
                tblType = "公共域维度";
            } else if (dto.getTblType() == 1) {
                tblType = "其他域维度";
            }
            po.setDimdomainid(dto.getAreaOrDimFolderId());
            po.setDimtableid(dto.getTblId());
            po.setAttributeid(dto.getFieldId());
            po.setIndexid(String.valueOf(dto.getIndexId()));
            po.setCreatedUser(dto.getCreateUser());
            pos.add(po);
        }

        this.saveBatch(pos);
        return this.saveBatch(pos);
    }

}
