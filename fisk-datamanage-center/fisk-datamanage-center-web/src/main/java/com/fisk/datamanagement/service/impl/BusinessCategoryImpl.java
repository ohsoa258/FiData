package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.datamanagement.dto.businessclassification.BusinessCategoryTreeDTO;
import com.fisk.datamanagement.dto.classification.*;
import com.fisk.datamanagement.entity.BusinessCategoryPO;
import com.fisk.datamanagement.entity.BusinessClassificationPO;
import com.fisk.datamanagement.mapper.BusinessCategoryMapper;
import com.fisk.datamanagement.service.BusinessCategoryService;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xgf
 * @date 2023年11月20日 10:44
 */
@Service
public class BusinessCategoryImpl implements BusinessCategoryService {
    @Resource
    BusinessCategoryMapper businessCategoryMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 更改指标名称属性_
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEnum updateCategory(BusinessCategoryDTO dto) {
        // 参数校验
        if (CollectionUtils.isEmpty(dto.getClassificationDefs())) {
            throw new FkException(ResultEnum.ERROR, "修改业务分类参数错误");
        }
        BusinessCategoryDefsDTO param = dto.getClassificationDefs().get(0);

        // 查询是否存在重复数据
        List<String> nameList = businessCategoryMapper.selectNameList(param.getGuid(), DelFlagEnum.NORMAL_FLAG.getValue());
        if (nameList.contains(param.name)) {
            throw new FkException(ResultEnum.ERROR, "业务分类名称已存在");
        }

        // 查询当前业务分类
        QueryWrapper<BusinessCategoryPO> qw = new QueryWrapper<>();
        qw.eq("id", param.guid).eq("del_flag", 1);
        BusinessCategoryPO model = businessCategoryMapper.selectOne(qw);
        if (Objects.isNull(model)) {
            throw new FkException(ResultEnum.ERROR, "业务分类不存在");
        }
        model.setName(param.name);
        model.setDescription(param.description);
        if (businessCategoryMapper.updateById(model) <= 0) {
            throw new FkException(ResultEnum.ERROR, "修改业务分类失败");
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 更改指标数据顺序
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEnum updateCategorySort(List<String> dto) {
        for (int i = 0; i < dto.size(); i++) {
            // 查询当前业务分类
            QueryWrapper<BusinessCategoryPO> qw = new QueryWrapper<>();
            qw.eq("id", dto.get(i)).eq("del_flag", 1);
            BusinessCategoryPO model = businessCategoryMapper.selectOne(qw);
            model.setSort(i);
            if (businessCategoryMapper.updateById(model) <= 0) {
                throw new FkException(ResultEnum.ERROR, "修改业务分类失败");
            }
        }
        return ResultEnum.SUCCESS;
    }


    /**
     * 根据指标id删除指标数据
     *
     * @param categoryId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteCategory(String categoryId) {
        // 查询数据
        QueryWrapper<BusinessCategoryPO> qw = new QueryWrapper<>();
        qw.eq("id", categoryId);
        BusinessCategoryPO po = businessCategoryMapper.selectOne(qw);
        if (po == null) {
            throw new FkException(ResultEnum.ERROR, "业务分类不存在");
        }

        List<Long> idList = new ArrayList<>();

        // 查询子集
        qw = new QueryWrapper<>();
        qw.eq("pid", po.getId());
        List<BusinessCategoryPO> children = businessCategoryMapper.selectList(qw);
        if (!CollectionUtils.isEmpty(children)) {
            idList = children.stream().map(BusinessCategoryPO::getId).collect(Collectors.toList());
        }
        idList.add(po.getId());
        if (businessCategoryMapper.deleteBatchIds(idList) > 0) {
            return ResultEnum.SUCCESS;
        } else {
            throw new FkException(ResultEnum.ERROR, "删除业务分类失败");
        }
    }


    /**
     * 向数据库中添加指标数据
     *
     * @param
     */
    @Override
    public ResultEnum addCategory(BusinessCategoryDTO dto) {
        List<BusinessCategoryDefsDTO> classificationDefList = dto.getClassificationDefs();
        for (BusinessCategoryDefsDTO item : classificationDefList) {
            if (StringUtils.isEmpty(item.name)) {
                throw new FkException(ResultEnum.ERROR, "指标主题名称不能为空");
            }
            // 查询数据
            QueryWrapper<BusinessCategoryPO> qw = new QueryWrapper<>();
            qw.eq("name", item.name).eq("del_flag", 1).eq("pid", item.pid);
            BusinessCategoryPO bcPO = businessCategoryMapper.selectOne(qw);
            if (bcPO != null) {
                throw new FkException(ResultEnum.ERROR, "指标主题名称已经存在");
            }
            // 添加数据
            BusinessCategoryPO model = new BusinessCategoryPO();
            model.setName(item.name);
            model.setDescription(item.description);
            model.setSort((int) System.currentTimeMillis());
            // 设置父级id
            if (!CollectionUtils.isEmpty(item.superTypes)) {

                String s = businessCategoryMapper.selectParentId(item.superTypes.get(0));
                s = s == null ? "0" : s;
//                model.setPid(Integer.valueOf(s));
                if (item.superTypes.get(0) == null) {
                    model.setPid(null);
                } else {
                    model.setPid(Integer.valueOf(item.superTypes.get(0)));
                }

            } else {
                model.setPid(null);
            }
            // 设置创建者信息
            //model.setCreateUser(userHelper.getLoginUserInfo().id.toString());
            int flag = businessCategoryMapper.insert(model);
//            //添加业务分类下的属性
//            for (ClassificationAttributeDefsDTO a : item.getAttributeDefs()) {
//                BusinessCategoryPidDefsDTO attributeDTO=new BusinessCategoryPidDefsDTO();
//                attributeDTO.p=String.valueOf(model.getId());
//                attributeDTO.name=attributeDef.getName();
//                attributeDTO.value=attributeDef.getValue();
//                this.addClassification(attributeDTO);
//            }

            if (flag < 0) {
                throw new FkException(ResultEnum.ERROR, "保存失败");
            }
        }
        return ResultEnum.SUCCESS;
    }






    @Override
    public List<BusinessCategoryTreeDTO> getCategoryTree() {
        // 查询所有数据
        List<BusinessCategoryPO> data = businessCategoryMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isEmpty(data)) {
            return new ArrayList<>();
        }

        // 数据转换
        List<BusinessCategoryTreeDTO> allData = data.stream().map(item -> {
            BusinessCategoryTreeDTO dto = new BusinessCategoryTreeDTO();
            dto.setId(String.valueOf(item.id));
            dto.setGuid(String.valueOf(item.id));
            if (item.pid == null || item.pid == 0) {
                dto.setPid("0");
            } else {
                dto.setPid(item.getPid().toString());
            }
            dto.setName(item.name);
            dto.setDescription(item.description);
            dto.setCreateTime(item.createTime);
            dto.setSort(item.sort);
            return dto;
        }).collect(Collectors.toList());

        List<BusinessCategoryTreeDTO> parentList = MallClassTree(allData, "0");

        return parentList;
    }


    public final List<BusinessCategoryTreeDTO> MallClassTree(List<BusinessCategoryTreeDTO> list, String pid) {
        List<BusinessCategoryTreeDTO> parentList = list.stream().filter(item -> (pid + "").equals(item.pid)).collect(Collectors.toList());

        if (!parentList.isEmpty()) {
            List<BusinessCategoryTreeDTO> ResultList = new ArrayList<>();
            for (int i=0;i<parentList.size();i++){
                List<BusinessCategoryTreeDTO>  children=new ArrayList<>();
                for (int j=0;j<list.size();j++){
                    if (list.get(j).pid.equals(list.get(i).id)){
                        System.out.println(parentList.get(i).id);
                       children= MallClassTree(list,parentList.get(i).id);
                    }
                }
                // 递归处理
                parentList.get(i).setChild(children);
                ResultList.add(parentList.get(i));
            }
            ResultList.sort(Comparator.comparing(BusinessCategoryTreeDTO::getSort));

            return ResultList;
        }
        return null;
    }


}
