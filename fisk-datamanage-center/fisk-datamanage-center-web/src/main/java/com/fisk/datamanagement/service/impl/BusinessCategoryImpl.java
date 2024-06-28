package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.enums.IndicatorTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.datamanagement.dto.businessclassification.BusinessCategorySortDTO;
import com.fisk.datamanagement.dto.businessclassification.BusinessCategoryTreeDTO;
import com.fisk.datamanagement.dto.businessclassification.BusinessMetaDataTreeDTO;
import com.fisk.datamanagement.dto.businessclassification.ParentBusinessTreeDTO;
import com.fisk.datamanagement.dto.category.BusinessCategoryAssignmentDTO;
import com.fisk.datamanagement.dto.classification.*;
import com.fisk.datamanagement.entity.*;
import com.fisk.datamanagement.mapper.*;
import com.fisk.datamanagement.service.BusinessCategoryAssignmentService;
import com.fisk.datamanagement.service.BusinessCategoryService;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.dimension.DimensionTreeDTO;
import com.fisk.datamodel.dto.fact.FactTreeDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
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
public class BusinessCategoryImpl extends ServiceImpl<BusinessCategoryMapper, BusinessCategoryPO> implements BusinessCategoryService {


    @Resource
    BusinessCategoryMapper businessCategoryMapper;
    @Resource
    private DataModelClient dataModelClient;
    @Resource
    BusinessTargetinfoMapper businessTargetinfoMapper;

    @Resource
    BusinessExtendedfieldsMapper businessExtendedfieldsMapper;

    @Resource
    FactTreeListMapper factTreeListMapper;

    @Resource
    UserHelper userHelper;

    @Resource
    UserClient userClient;

    @Resource
    private BusinessCategoryAssignmentService businessCategoryAssignmentService;

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
        List<BusinessCategoryPO> businessCategoryPOS = businessCategoryMapper.selectList(new QueryWrapper<>());
        List<BusinessCategoryPO> allCategory = findChildren(businessCategoryPOS, (int) po.getId());
        allCategory.add(po);
        idList = allCategory.stream().map(BusinessCategoryPO::getId).collect(Collectors.toList());

        LambdaQueryWrapper<BusinessTargetinfoPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BusinessTargetinfoPO::getPid, idList);
        List<BusinessTargetinfoPO> businessTargetinfoPOList = businessTargetinfoMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(businessTargetinfoPOList)) {
            List<Long> ids = businessTargetinfoPOList.stream().map(BusinessTargetinfoPO::getId).collect(Collectors.toList());
            businessTargetinfoMapper.deleteBatchIds(ids);
            LambdaQueryWrapper<BusinessExtendedfieldsPO> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.in(BusinessExtendedfieldsPO::getIndexid, ids);
            businessExtendedfieldsMapper.delete(deleteWrapper);

            LambdaQueryWrapper<FactTreePOs> factDeleteWrapper = new LambdaQueryWrapper<>();
            factDeleteWrapper.in(FactTreePOs::getPid, ids);
            factTreeListMapper.delete(factDeleteWrapper);
        }
        if (businessCategoryMapper.deleteBatchIds(idList) > 0) {
            return ResultEnum.SUCCESS;
        } else {
            throw new FkException(ResultEnum.ERROR, "删除业务分类失败");
        }
    }

    private List<BusinessCategoryPO> findChildren(List<BusinessCategoryPO> trees, Integer id) {
        List<BusinessCategoryPO> children = new ArrayList<>();
        for (BusinessCategoryPO tree : trees) {
            if (id == tree.getPid()) {
                children.add(tree);
                children.addAll(findChildren(trees, (int) tree.getId()));
            }
        }
        return children;
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

            if (flag < 0) {
                throw new FkException(ResultEnum.ERROR, "保存失败");
            }
            if (model.getPid() == null || model.getPid() == 0) {
                UserInfo userInfo = userHelper.getLoginUserInfo();
                ResultEntity<List<RoleInfoDTO>> businessAssignment = userClient.getRolebyUserId(userInfo.getId().intValue());
                List<RoleInfoDTO> businessAssignmentIds = new ArrayList<>();
                if (businessAssignment.code == ResultEnum.SUCCESS.getCode()) {
                    businessAssignmentIds = businessAssignment.data;
                } else {
                    throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
                }
                List<Integer> roleIds = businessAssignmentIds.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
                List<BusinessCategoryAssignmentPO> businessCategoryAssignmentPOList = new ArrayList<>();
                for (Integer id : roleIds) {
                    BusinessCategoryAssignmentPO businessCategoryAssignmentPO = new BusinessCategoryAssignmentPO();
                    businessCategoryAssignmentPO.setCategoryId((int) model.id);
                    businessCategoryAssignmentPO.setRoleId(id);
                    businessCategoryAssignmentPOList.add(businessCategoryAssignmentPO);
                }
                if (CollectionUtils.isNotEmpty(businessCategoryAssignmentPOList)) {
                    businessCategoryAssignmentService.saveBatch(businessCategoryAssignmentPOList);
                }
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<Integer> getBusinessCategoryAssignment(String pid) {
        List<Integer> result = new ArrayList<>();
        LambdaQueryWrapper<BusinessCategoryAssignmentPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BusinessCategoryAssignmentPO::getCategoryId, pid);
        List<BusinessCategoryAssignmentPO> categoryAssignmentPOList = businessCategoryAssignmentService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(categoryAssignmentPOList)) {
            result = categoryAssignmentPOList.stream().map(BusinessCategoryAssignmentPO::getRoleId).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public ResultEnum addBusinessCategoryAssignment(BusinessCategoryAssignmentDTO dto) {
        if (dto.menuId == null) {
            throw new FkException(ResultEnum.ERROR, "菜单id不能为空");
        }
        if (CollectionUtils.isEmpty(dto.roleIds)) {
            throw new FkException(ResultEnum.ERROR, "角色id不能为空");
        }
        List<BusinessCategoryAssignmentPO> businessCategoryAssignmentPOList = new ArrayList<>();
        for (Integer roleId : dto.roleIds) {
            BusinessCategoryAssignmentPO businessCategoryAssignmentPO = new BusinessCategoryAssignmentPO();
            businessCategoryAssignmentPO.setCategoryId(dto.menuId);
            businessCategoryAssignmentPO.setRoleId(roleId);
            businessCategoryAssignmentPOList.add(businessCategoryAssignmentPO);
        }
        LambdaQueryWrapper<BusinessCategoryAssignmentPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BusinessCategoryAssignmentPO::getCategoryId, dto.menuId);
        businessCategoryAssignmentService.remove(queryWrapper);
        if (CollectionUtils.isNotEmpty(businessCategoryAssignmentPOList)) {
            businessCategoryAssignmentService.saveBatch(businessCategoryAssignmentPOList);
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

        UserInfo userInfo = userHelper.getLoginUserInfo();
        ResultEntity<List<RoleInfoDTO>> rolebyUserId = userClient.getRolebyUserId(userInfo.getId().intValue());
        List<RoleInfoDTO> businessAssignment = new ArrayList<>();
        if (rolebyUserId.code == ResultEnum.SUCCESS.getCode()) {
            businessAssignment = rolebyUserId.data;
        } else {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        List<Integer> roleIds = businessAssignment.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        List<Integer> businessCategoryIds = businessCategoryAssignmentService.getCategoryIds(roleIds);
        List<BusinessCategoryTreeDTO> parent = allData.stream().filter(i -> "0".equals(i.getPid()) && businessCategoryIds.contains(Integer.valueOf(i.getId()))).collect(Collectors.toList());
        List<BusinessCategoryTreeDTO> child = allData.stream().filter(i -> !"0".equals(i.getPid())).collect(Collectors.toList());
        List<BusinessCategoryTreeDTO> all = new ArrayList<>();
        all.addAll(parent);
        all.addAll(child);
        List<BusinessCategoryTreeDTO> parentList = MallClassTree(all, "0");

        return parentList;
    }


    public final List<BusinessCategoryTreeDTO> MallClassTree(List<BusinessCategoryTreeDTO> list, String pid) {
        List<BusinessCategoryTreeDTO> parentList = list.stream().filter(item -> (pid + "").equals(item.pid)).collect(Collectors.toList());
        if (!parentList.isEmpty()) {
            List<BusinessCategoryTreeDTO> ResultList = new ArrayList<>();
            for (int i = 0; i < parentList.size(); i++) {
                List<BusinessCategoryTreeDTO> children = new ArrayList<>();
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).pid.equals(parentList.get(i).id)) {
                        children = MallClassTree(list, parentList.get(i).id);
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


    @Override
    public JSONArray getDimensionTreeList() {
        List<DimensionTreeDTO> aa = dataModelClient.getDimensionTree();
        JSONArray array = new JSONArray();
        array.add(aa.get(0).getPublicDim());
        array.add(aa.get(0).getOtherDimsByArea());
        for (int i = 0; i < array.size(); i++) {
            JSONArray arrays = array.getJSONArray(i);
            for (int o = 0; o < arrays.size(); o++) {
                JSONObject array1 = arrays.getJSONObject(o);
                array1.put("name", array1.getString("businessName"));
                JSONArray array2 = array1.getJSONArray("dimensionList");
                if (array2.size() > 0) {
                    for (int j = 0; j < array2.size(); j++) {
                        JSONObject array3 = array2.getJSONObject(j);
                        array3.put("name", array3.getString("dimensionCnName"));
                        array3.put("dimensionList", array3.getJSONArray("attributeList"));
                        array3.remove("attributeList");
                        JSONArray array6 = array3.getJSONArray("dimensionList");
                        if (array3.getJSONArray("dimensionList") == null) {
                            array6 = array3.getJSONArray("attributeList");
                        }
                        if (array6.size() > 0) {
                            for (int k = 0; k < array6.size(); k++) {
                                JSONObject array7 = array6.getJSONObject(k);
                                array7.put("name", array7.getString("dimensionFieldCnName"));
                                //JSONArray extendedfields = new JSONArray();
                                JSONObject json = new JSONObject();
                                if (i == 0) {
                                    json.put("dimdomaintype", "公共域维度");
                                }
                                if (i == 1) {
                                    json.put("dimdomaintype", "其他域维度");
                                }
                                if (array1.size() > 0) {
                                    json.put("dimdomainid", array1.getString("id"));
                                    json.put("dimdomain", array1.getString("businessName"));
                                }
                                if (array3.size() > 0) {
                                    json.put("dimtableid", array3.getString("id"));
                                    json.put("dimtable", array3.getString("dimensionCnName"));
                                    json.put("dimtablename", array3.getString("dimensionTabName"));
                                }
                                if (array6.size() > 0) {
                                    json.put("attributeid", array7.getString("id"));
                                    json.put("attribute", array7.getString("dimensionFieldCnName"));
                                    json.put("attributeEnName", array7.getString("dimensionFieldEnName"));
                                }
                                array7.put("extendedfields", json);
                                array6.set(k, array7);
                            }
                        }
                        array2.set(j, array3);
                    }
                }
                arrays.set(o, array1);
            }
            array.set(i, arrays);
        }
        return array;

    }

    @Override
    public JSONArray getFactTreeList() {
        List<FactTreeDTO> aa = dataModelClient.getFactTree();
        JSONArray array = new JSONArray();
        array.add(aa.get(0).getFactByArea());
        JSONArray jsonArray = array.getJSONArray(0);
        JSONObject json = new JSONObject();
        JSONArray data = new JSONArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject data1 = new JSONObject();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            data1.put("id", jsonObject.getString("id"));
            data1.put("name", jsonObject.getString("businessName"));
            JSONArray array1 = jsonObject.getJSONArray("factList");
            JSONArray array4 = new JSONArray();
            for (int j = 0; j < array1.size(); j++) {
                JSONObject data2 = new JSONObject();
                JSONObject jsonObject1 = array1.getJSONObject(j);
                data2.put("id", jsonObject1.getString("id"));
                data2.put("name", jsonObject1.getString("factTabName"));
                data2.put("cnName", jsonObject1.getString("factTableCnName"));
                JSONArray array2 = jsonObject1.getJSONArray("attributeList");
                JSONArray array3 = new JSONArray();
                for (int m = 0; m < array2.size(); m++) {
                    JSONObject data3 = new JSONObject();
                    JSONObject jsonObject2 = array2.getJSONObject(m);
                    JSONObject data4 = new JSONObject();
                    data3.put("id", jsonObject2.getString("id"));
                    data3.put("name", jsonObject2.getString("factFieldEnName"));
                    data3.put("cnName", jsonObject2.getString("factFieldCnName"));
                    data4.put("businessNameId", jsonObject.getString("id"));
                    data4.put("businessName", jsonObject.getString("businessName"));
                    data4.put("factTabNameId", jsonObject1.getString("id"));
                    data4.put("factTabName", jsonObject1.getString("factTabName"));
                    data4.put("factTableCnName", jsonObject1.getString("factTableCnName"));
                    data4.put("factFieldEnNameId", jsonObject2.getString("id"));
                    data4.put("factFieldEnName", jsonObject2.getString("factFieldEnName"));
                    data4.put("factFieldCnName", jsonObject2.getString("factFieldCnName"));
                    data3.put("other", data4);
                    array3.add(data3);
                }
                data2.put("chidList", array3);
                array4.add(data2);
            }
            data1.put("chidList", array4);
            data.add(data1);
        }
        return data;
    }

    @Override
    public List<ParentBusinessTreeDTO> getParentBusinessDataList() {
        // 查询所有数据
        List<BusinessCategoryPO> data = businessCategoryMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isEmpty(data)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<BusinessTargetinfoPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BusinessTargetinfoPO::getIndicatorType, IndicatorTypeEnum.ATOMIC_INDICATORS.getName());
        List<BusinessTargetinfoPO> businessTargetinfoPOS = businessTargetinfoMapper.selectList(queryWrapper);
        Map<String, List<ParentBusinessTreeDTO>> parentBusinessTreeDTOMap = businessTargetinfoPOS.stream().map(i -> {
            ParentBusinessTreeDTO parentBusinessTreeDTO = new ParentBusinessTreeDTO();
            parentBusinessTreeDTO.setId(String.valueOf(i.getId()));
            parentBusinessTreeDTO.setPid(i.getPid());
            parentBusinessTreeDTO.setType(2);
            parentBusinessTreeDTO.setName(i.getIndicatorName());
            return parentBusinessTreeDTO;
        }).collect(Collectors.groupingBy(ParentBusinessTreeDTO::getPid));

        // 数据转换
        List<ParentBusinessTreeDTO> allData = data.stream().map(item -> {
            ParentBusinessTreeDTO dto = new ParentBusinessTreeDTO();
            dto.setId(String.valueOf(item.id));
            if (item.pid == null || item.pid == 0) {
                dto.setPid("0");
            } else {
                dto.setPid(item.getPid().toString());
            }
            dto.setName(item.name);
            dto.setSort(item.sort);
            dto.setType(1);
            List<ParentBusinessTreeDTO> parentBusinessTreeDTO = parentBusinessTreeDTOMap.get(String.valueOf(item.getId()));
            dto.setChild(parentBusinessTreeDTO);
            return dto;
        }).collect(Collectors.toList());
        UserInfo userInfo = userHelper.getLoginUserInfo();
        ResultEntity<List<RoleInfoDTO>> rolebyUserId = userClient.getRolebyUserId(userInfo.getId().intValue());
        List<RoleInfoDTO> businessAssignment = new ArrayList<>();
        if (rolebyUserId.code == ResultEnum.SUCCESS.getCode()) {
            businessAssignment = rolebyUserId.data;
        } else {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        List<Integer> roleIds = businessAssignment.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        List<Integer> businessCategoryIds = businessCategoryAssignmentService.getCategoryIds(roleIds);
        List<ParentBusinessTreeDTO> parent = allData.stream().filter(i -> "0".equals(i.getPid()) && businessCategoryIds.contains(Integer.valueOf(i.getId()))).collect(Collectors.toList());
        List<ParentBusinessTreeDTO> child = allData.stream().filter(i -> !"0".equals(i.getPid())).collect(Collectors.toList());
        List<ParentBusinessTreeDTO> all = new ArrayList<>();
        all.addAll(parent);
        all.addAll(child);
        List<ParentBusinessTreeDTO> parentList = childClassTree(all, "0");
        return parentList;
    }

    @Override
    public List<BusinessMetaDataTreeDTO> getAllBusinessMetaDataList() {
        List<BusinessMetaDataTreeDTO> allList = new ArrayList<>();
        // 从数据库中查询所有BusinessCategoryPO对象
        List<BusinessCategoryPO> businessCategoryPOS = this.businessCategoryMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isNotEmpty(businessCategoryPOS)) {
            List<BusinessMetaDataTreeDTO> businessMetaDataTreeDTOS = businessCategoryPOS.stream().map(i -> {

                // 将BusinessCategoryPO对象转换为BusinessMetaDataTreeDTO对象
                BusinessMetaDataTreeDTO businessMetaDataTreeDTO = new BusinessMetaDataTreeDTO();
                businessMetaDataTreeDTO.setId(~(int) i.getId() + 1);
                businessMetaDataTreeDTO.setName(i.getName());
                // 处理可能的空指针异常，如果pid为null或0，设置为0
                businessMetaDataTreeDTO.setPid(i.getPid() != null && i.getPid() != 0 ? ~i.getPid() + 1 : 0);
                businessMetaDataTreeDTO.setType(1);
                businessMetaDataTreeDTO.setSort(i.getSort());
                return businessMetaDataTreeDTO;
            }).collect(Collectors.toList());
            allList.addAll(businessMetaDataTreeDTOS);
        }
        // 从数据库中查询所有BusinessTargetinfoPO对象
        List<BusinessTargetinfoPO> businessTargetinfoPOList = businessTargetinfoMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isNotEmpty(businessTargetinfoPOList)) {
            List<BusinessMetaDataTreeDTO> businessMetaDataTreeDTOS = businessTargetinfoPOList.stream().map(i -> {
                // 将BusinessTargetinfoPO对象转换为BusinessMetaDataTreeDTO对象
                BusinessMetaDataTreeDTO businessMetaDataTreeDTO = new BusinessMetaDataTreeDTO();
                businessMetaDataTreeDTO.setId((int) i.getId());
                businessMetaDataTreeDTO.setName(i.getIndicatorName());
                businessMetaDataTreeDTO.setPid(~Integer.parseInt(i.getPid()) + 1);
                businessMetaDataTreeDTO.setType(2);
                businessMetaDataTreeDTO.setSort(0);
                return businessMetaDataTreeDTO;
            }).collect(Collectors.toList());
            allList.addAll(businessMetaDataTreeDTOS);
        }
        // 从allList中筛选出pid为0的元素，作为根节点列表
        List<BusinessMetaDataTreeDTO> parentList = allList.stream().filter(item -> item.getPid() == 0).collect(Collectors.toList());
        // 若根节点列表为空，返回一个空列表
        if (CollectionUtils.isEmpty(parentList)) {
            return new ArrayList<>();
        } else {
            parentList.sort(Comparator.comparing(BusinessMetaDataTreeDTO::getSort).reversed());
        }
        // 递归处理子集
        bussinessCategoryTree(allList, parentList);
        return parentList;
    }

    @Override
    public ResultEnum businessCategorySort(BusinessCategorySortDTO dto) {
        Integer tragetId = dto.getTragetId();
        BusinessCategoryPO businessCategoryPO = this.getById(dto.getMenuId());
        BusinessCategoryPO tragetMenuPO = this.getById(tragetId);
        if (dto.getCrossLevel()) {
            if (tragetId == null || tragetId == 0) {
                Integer pid = businessCategoryPO.getPid();
                Integer sort = businessCategoryPO.getSort();

                LambdaQueryWrapper<BusinessCategoryPO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(BusinessCategoryPO::getPid, dto.getPid());
                List<BusinessCategoryPO> all = this.list(queryWrapper);
                if (!org.springframework.util.CollectionUtils.isEmpty(all)) {
                    List<BusinessCategoryPO> menus = new ArrayList<>();
                    for (BusinessCategoryPO menuPO : all) {
                        menuPO.setSort(menuPO.getSort() + 1);
                        menus.add(menuPO);
                    }
                    this.updateBatchById(menus);
                }
                businessCategoryPO.setPid(dto.getPid());
                businessCategoryPO.setSort(1);
                this.updateById(businessCategoryPO);

                LambdaQueryWrapper<BusinessCategoryPO> selectMenus = new LambdaQueryWrapper<>();
                selectMenus.eq(BusinessCategoryPO::getPid, pid);
                selectMenus.gt(BusinessCategoryPO::getSort, sort);
                List<BusinessCategoryPO> lastMenus = this.list(selectMenus);
                List<BusinessCategoryPO> menus = new ArrayList<>();
                for (BusinessCategoryPO menuPO : lastMenus) {
                    menuPO.setSort(menuPO.getSort() - 1);
                    menus.add(menuPO);
                }
                if (!org.springframework.util.CollectionUtils.isEmpty(menus)) {
                    this.updateBatchById(menus);
                }
            } else {
                Integer pid = businessCategoryPO.getPid();
                Integer sort = businessCategoryPO.getSort();
                LambdaQueryWrapper<BusinessCategoryPO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(BusinessCategoryPO::getPid, dto.getPid());
                queryWrapper.ge(BusinessCategoryPO::getSort, tragetMenuPO.getSort() + 1);
                List<BusinessCategoryPO> lastMenus = this.list(queryWrapper);
                List<BusinessCategoryPO> menus = new ArrayList<>();
                for (BusinessCategoryPO menuPO : lastMenus) {
                    menuPO.setSort(menuPO.getSort() + 1);
                    menus.add(menuPO);
                }
                if (!org.springframework.util.CollectionUtils.isEmpty(menus)) {
                    this.updateBatchById(menus);
                }
                businessCategoryPO.setPid(dto.getPid());
                businessCategoryPO.setSort(tragetMenuPO.getSort() + 1);
                this.updateById(businessCategoryPO);

                LambdaQueryWrapper<BusinessCategoryPO> selectMenus = new LambdaQueryWrapper<>();
                selectMenus.eq(BusinessCategoryPO::getPid, pid);
                selectMenus.gt(BusinessCategoryPO::getSort, sort);
                List<BusinessCategoryPO> Menus = this.list(selectMenus);
                menus = new ArrayList<>();
                for (BusinessCategoryPO menuPO : Menus) {
                    menuPO.setSort(menuPO.getSort() - 1);
                    menus.add(menuPO);
                }
                if (!org.springframework.util.CollectionUtils.isEmpty(menus)) {
                    this.updateBatchById(menus);
                }
            }
        } else {
            if (tragetId == null || tragetId == 0) {
                LambdaQueryWrapper<BusinessCategoryPO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(BusinessCategoryPO::getPid, businessCategoryPO.getPid());
                queryWrapper.lt(BusinessCategoryPO::getSort, businessCategoryPO.getSort());
                List<BusinessCategoryPO> list = this.list(queryWrapper);
                List<BusinessCategoryPO> menus = new ArrayList<>();
                for (BusinessCategoryPO menuPO : list) {
                    menuPO.setSort(menuPO.getSort() + 1);
                    menus.add(menuPO);
                }
                if (!org.springframework.util.CollectionUtils.isEmpty(menus)) {
                    this.updateBatchById(menus);
                }
                businessCategoryPO.setSort(1);
                this.updateById(businessCategoryPO);
            } else {
                if (tragetMenuPO.getSort() > businessCategoryPO.getSort()) {
                    LambdaQueryWrapper<BusinessCategoryPO> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(BusinessCategoryPO::getPid, businessCategoryPO.getPid());
                    queryWrapper.gt(BusinessCategoryPO::getSort, businessCategoryPO.getSort());
                    queryWrapper.le(BusinessCategoryPO::getSort, tragetMenuPO.getSort());
                    List<BusinessCategoryPO> list = this.list(queryWrapper);
                    List<BusinessCategoryPO> menus = new ArrayList<>();
                    for (BusinessCategoryPO menuPO : list) {
                        menuPO.setSort(menuPO.getSort() - 1);
                        menus.add(menuPO);
                    }
                    if (!org.springframework.util.CollectionUtils.isEmpty(menus)) {
                        this.updateBatchById(menus);
                    }
                    businessCategoryPO.setSort(tragetMenuPO.getSort());
                    this.updateById(businessCategoryPO);
                } else if (tragetMenuPO.getSort() < businessCategoryPO.getSort()) {
                    LambdaQueryWrapper<BusinessCategoryPO> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(BusinessCategoryPO::getPid, businessCategoryPO.getPid());
                    queryWrapper.gt(BusinessCategoryPO::getSort, tragetMenuPO.getSort());
                    queryWrapper.lt(BusinessCategoryPO::getSort, businessCategoryPO.getSort());
                    List<BusinessCategoryPO> list = this.list(queryWrapper);
                    List<BusinessCategoryPO> menus = new ArrayList<>();
                    for (BusinessCategoryPO menuPO : list) {
                        menuPO.setSort(menuPO.getSort() + 1);
                        menus.add(menuPO);
                    }
                    if (!org.springframework.util.CollectionUtils.isEmpty(menus)) {
                        this.updateBatchById(menus);
                    }
                    businessCategoryPO.setSort(tragetMenuPO.getSort() + 1);
                    this.updateById(businessCategoryPO);
                }
            }
        }
        return ResultEnum.SUCCESS;
    }

    // 递归处理子节点，构建树形结构
    private void bussinessCategoryTree(List<BusinessMetaDataTreeDTO> allList, List<BusinessMetaDataTreeDTO> parentList) {
        Map<Integer, List<BusinessMetaDataTreeDTO>> childrenMap = new HashMap<>();
        for (BusinessMetaDataTreeDTO dto : allList) {
            int parentId = dto.getPid() != null ? dto.getPid() : 0;
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(dto);
        }
        for (BusinessMetaDataTreeDTO parent : parentList) {
            List<BusinessMetaDataTreeDTO> children = childrenMap.get(parent.getId());
            if (children != null) {
                children.sort(Comparator.comparing(BusinessMetaDataTreeDTO::getSort).reversed());
                parent.setChildren(children);
                bussinessCategoryTree(allList, children);
            }
        }
    }

    public final List<ParentBusinessTreeDTO> childClassTree(List<ParentBusinessTreeDTO> list, String pid) {
        List<ParentBusinessTreeDTO> parentList = list.stream().filter(item -> (pid + "").equals(item.pid)).collect(Collectors.toList());
        if (!parentList.isEmpty()) {
            List<ParentBusinessTreeDTO> ResultList = new ArrayList<>();
            for (int i = 0; i < parentList.size(); i++) {
                List<ParentBusinessTreeDTO> children = new ArrayList<>();
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).pid.equals(parentList.get(i).id)) {
                        children = childClassTree(list, parentList.get(i).id);
                    }
                }
                // 递归处理
                parentList.get(i).setChild(children);
                ResultList.add(parentList.get(i));
            }
            ResultList.sort(Comparator.comparing(ParentBusinessTreeDTO::getSort));

            return ResultList;
        }
        return null;
    }
}
