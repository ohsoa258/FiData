package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.dto.modelAndIndex.ModelAndIndexMappingDTO;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.fisk.datamanagement.mapper.FactTreeListMapper;
import com.fisk.datamanagement.service.FactTreeListExtendedfieldsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
public class FactTreeListExtendedfieldsImp extends ServiceImpl<FactTreeListMapper, FactTreePOs> implements FactTreeListExtendedfieldsService {
    @Resource
    FactTreeListMapper factTreeListMapper;

    @Override
    public List<FactTreePOs> addFactTreeListExtendedfields(String pid) {
        // 查询数据
        List<FactTreePOs> po = factTreeListMapper.selectParentpIds(pid);

        System.out.println(po);
        return po;

    }

    /**
     * 关联数仓表字段和指标标准（事实表字段 指标所属）
     * 维度表字段则关联 指标粒度
     * 事实表字段则关联 指标所属
     *
     * @param dtos
     * @return
     */
    @Override
    public Object setMetricBelongsByModelField(List<ModelAndIndexMappingDTO> dtos) {
        //获取指标id
        Integer indexId = dtos.get(0).getIndexId();
        //获取字段id
        String fieldId = dtos.get(0).getFieldId();
        //如果此次指标id为空 则认为此次没有选择指标 清空该字段对应的指标粒度表
        if (indexId == null) {
            LambdaQueryWrapper<FactTreePOs> delWrapper = new LambdaQueryWrapper<>();
            delWrapper.eq(FactTreePOs::getFactFieldEnNameId, fieldId);
            return this.remove(delWrapper);
        }

        //每次都删除所有字段绑定的指标粒度
        LambdaQueryWrapper<FactTreePOs> delAll = new LambdaQueryWrapper<>();
        delAll.eq(FactTreePOs::getFactFieldEnNameId, fieldId);
        this.remove(delAll);

        ArrayList<FactTreePOs> pos = new ArrayList<>();

        /*
        开始存储这次的关联关系
         */

        //转换对象属性
        for (ModelAndIndexMappingDTO dto : dtos) {
            FactTreePOs po = new FactTreePOs();
            String tblType = null;
            //0公共域维度 1其他域维度 2事实表
            if (dto.getTblType() == 0) {
                tblType = "公共域维度";
            } else if (dto.getTblType() == 1) {
                tblType = "其他域维度";
            }
            po.setBusinessNameId(dto.getAreaOrDimFolderId());
            po.setFactTabNameId(dto.getTblId());
            po.setFactFieldEnNameId(dto.getFieldId());
            po.setPid(String.valueOf(dto.getIndexId()));
            po.setCreateUser(dto.getCreateUser());
            pos.add(po);
        }

        return this.saveBatch(pos);
    }

}
