package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.entity.EntityQueryDTO;
import com.fisk.mdm.dto.viwGroup.*;
import com.fisk.mdm.entity.ViwGroupDetailsPO;
import com.fisk.mdm.entity.ViwGroupPO;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.ViwGroupMap;
import com.fisk.mdm.mapper.ViwGroupDetailsMapper;
import com.fisk.mdm.mapper.ViwGroupMapper;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.ViwGroupService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.viwGroup.ViewGroupDropDownVO;
import com.fisk.mdm.vo.viwGroup.ViwGroupVO;
import com.fisk.system.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.fisk.mdm.utils.mdmBEBuild.impl.BuildPgCommandImpl.PUBLIC;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:27
 * @Version 1.0
 */
@Slf4j
@Service
public class ViwGroupServiceImpl implements ViwGroupService {

    @Value("${pgsql-mdm.type}")
    DataSourceTypeEnum type;
    @Value("${pgsql-mdm.url}")
    String connectionStr;
    @Value("${pgsql-mdm.username}")
    String acc;
    @Value("${pgsql-mdm.password}")
    String pwd;

    @Resource
    ViwGroupMapper viwGroupMapper;
    @Resource
    ViwGroupDetailsMapper detailsMapper;
    @Resource
    ViwGroupService viwGroupService;
    @Resource
    EntityService entityService;
    @Resource
    AttributeService attributeService;
    @Resource
    UserClient userClient;

    public static final String ALIAS_MARK = "a";

    @Override
    public List<ViwGroupVO> getDataByGroupId(Integer id) {
        ViwGroupPO viwGroupPo = viwGroupMapper.selectById(id);
        if (viwGroupPo == null){
            return null;
        }

        List<ViwGroupVO> list = new ArrayList<>();
        ViwGroupVO viwGroupVo = ViwGroupMap.INSTANCES.groupPoToVo(viwGroupPo);

        // 查询视图组
        QueryWrapper<ViwGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupDetailsPO::getGroupId,id);
        List<ViwGroupDetailsPO> detailsPoList = detailsMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(detailsPoList)){
            List<ViwGroupDetailsDTO> collect = detailsPoList.stream().map(e -> {
                AttributeVO data = attributeService.getById(e.getAttributeId()).getData();
                ViwGroupDetailsDTO dto = ViwGroupMap.INSTANCES.detailsPoToDto(e);
                if (data != null) {
                    dto.setName(data.getName());
                    dto.setDisplayName(data.getDisplayName());
                    dto.setDesc(data.getDesc());
                    dto.setDataType(data.getDataType());
                    dto.setDataTypeLength(data.getDataTypeLength());
                    dto.setDataTypeDecimalLength(data.getDataTypeDecimalLength());
                }
                return dto;
            }).collect(Collectors.toList());

            viwGroupVo.setGroupDetailsList(collect);
        }

        list.add(viwGroupVo);
        return list;
    }

    @Override
    public List<ViwGroupVO> getDataByEntityId(Integer entityId) {
        if (entityId == null){
            return null;
        }

        QueryWrapper<ViwGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupPO::getEntityId,entityId);
        List<ViwGroupPO> viwGroupPoList = viwGroupMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(viwGroupPoList)){
            List<ViwGroupVO> collect = viwGroupPoList.stream().filter(e -> e.getId() != 0).map(e -> {
                ViwGroupVO viwGroupVo = viwGroupService.getDataByGroupId((int) e.getId()).get(0);
                return viwGroupVo;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    @Override
    public ResultEnum addViwGroup(ViwGroupDTO dto) {
        QueryWrapper<ViwGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupPO::getEntityId,dto.getEntityId())
                .eq(ViwGroupPO::getName,dto.getName())
                .last("limit 1");
        ViwGroupPO groupPo = viwGroupMapper.selectOne(queryWrapper);
        if (groupPo != null){
            return ResultEnum.DATA_EXISTS;
        }

        ViwGroupPO viwGroupPo = ViwGroupMap.INSTANCES.groupDtoToPo(dto);
        int res = viwGroupMapper.insert(viwGroupPo);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateData(ViwGroupUpdateDTO dto) {
        ViwGroupPO viwGroupPo = ViwGroupMap.INSTANCES.groupUpdateDtoToPo(dto);
        int res = viwGroupMapper.updateById(viwGroupPo);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum deleteGroupById(Integer id) {
        boolean existViwGroup = this.isExistViwGroup(id);
        if (existViwGroup == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = viwGroupMapper.deleteById(id);
        if (res <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 删除组下的数据
        QueryWrapper<ViwGroupDetailsPO> queryWrapper = new QueryWrapper();
        queryWrapper.lambda()
                .eq(ViwGroupDetailsPO::getGroupId,id);
        detailsMapper.delete(queryWrapper);

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteAttribute(ViwGroupDetailsDTO dto) {
        boolean existViwGroup = this.isExistViwGroup(dto.getGroupId());
        if (existViwGroup == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        QueryWrapper<ViwGroupDetailsPO> queryWrapper = new QueryWrapper();
        queryWrapper.lambda()
                .eq(ViwGroupDetailsPO::getGroupId,dto.getGroupId())
                .eq(ViwGroupDetailsPO::getAttributeId,dto.getAttributeId());
        int res = detailsMapper.delete(queryWrapper);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addAttribute(ViwGroupDetailsAddDTO dto) {

        // 删除属性组下的实体数据
        QueryWrapper<ViwGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupDetailsPO::getGroupId,dto.getGroupId());
        detailsMapper.delete(queryWrapper);

        // 新增视图组数据
        ViwGroupDetailsDTO detailsDto = new ViwGroupDetailsDTO();
        detailsDto.setGroupId(dto.getGroupId());
        dto.getDetailsNameList().stream().forEach(e -> {
            detailsDto.setAttributeId(e.getAttributeId());
            detailsDto.setAliasName(e.getAliasName());
            ViwGroupDetailsPO detailsPo = ViwGroupMap.INSTANCES.detailsDtoToDto(detailsDto);
            int res = detailsMapper.insert(detailsPo);
            if (res <= 0){
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        });

        return ResultEnum.SUCCESS;
    }

    @Override
    public ViwGroupQueryRelationDTO getRelationByEntityId(ViwGroupQueryDTO dto) {
        // 查询出视图组中的属性
        QueryWrapper<ViwGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupDetailsPO::getGroupId,dto.getGroupId());
        List<ViwGroupDetailsPO> detailsPoList = detailsMapper.selectList(queryWrapper);
        // 视图组id集合
        List<Integer> attributeIds = detailsPoList.stream().filter(e -> e.getAttributeId() != null).map(e -> e.getAttributeId()).collect(Collectors.toList());

        // 查询出域字段关联的实体
        EntityQueryDTO attributeInfo = this.getAttributeInfo(dto.getEntityId(),attributeIds);

        // 获取出选中属性的id
        List<Integer> checkedIds = new ArrayList<>();
        for (EntityQueryDTO child : attributeInfo.getChildren()) {
            List<Integer> ids = this.getCheckedIds(child);
            checkedIds.addAll(ids);
        }

        List<EntityQueryDTO> relationList = new ArrayList<>();
        relationList.add(attributeInfo);
        ViwGroupQueryRelationDTO dto1 = new ViwGroupQueryRelationDTO();
        dto1.setRelationList(relationList);
        dto1.setCheckedArr(checkedIds);
        return dto1;
    }

    /**
     * 获取出选中属性的id
     * @param child
     * @return
     */
    public List<Integer> getCheckedIds(EntityQueryDTO child){
        List<Integer> checkIds = new ArrayList<>();

        // 获取同级
        if (child.getType().equals(ObjectTypeEnum.ATTRIBUTES.getName()) && child.getIsCheck().equals(1)){
            checkIds.add(child.getId());
        }

        // 获取子级
        List<EntityQueryDTO> children = child.getChildren();
        if (CollectionUtils.isNotEmpty(children)){
            children.stream().filter(e -> e.getType().equals(ObjectTypeEnum.ATTRIBUTES.getName()) && e.getIsCheck().equals(1))
                    .forEach(e -> {
                        checkIds.add(e.getId());
                    });
        }

        return checkIds;
    }

    @Override
    public ResultEnum createCustomView(Integer id) {
        ViwGroupVO viwGroupVo = viwGroupService.getDataByGroupId(id).get(0);
        if (viwGroupVo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        String sql = null;
        try {
            // 1.拼接自定义视图Sql
            sql = this.buildCreateCustomViw(viwGroupVo);

            // 2.连接对象
            AbstractDbHelper dbHelper = new AbstractDbHelper();
            Connection connection = dbHelper.connection(connectionStr, acc,
                    pwd,type);

            // 3.执行Sql
            dbHelper.executeSql(sql, connection);
        }catch (SQLException ex){
            log.error("【创建自定义视图Sql】:" + sql + "【创建自定义视图失败,异常信息】:" + ex);
            ex.printStackTrace();
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 拼接自定义视图Sql
     * @param viwGroupVo
     * @return
     */
    public String buildCreateCustomViw(ViwGroupVO viwGroupVo) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE VIEW " + PUBLIC + ".");
        str.append(viwGroupVo.getName());
        str.append(" AS ").append("SELECT ");

        // 获取主实体下的属性
        List<ViwGroupDetailsDTO> mainDetailsList = new ArrayList<>();
        List<ViwGroupDetailsDTO> groupDetailsList = viwGroupVo.getGroupDetailsList();
        EntityInfoVO infoVo = entityService.getAttributeById(viwGroupVo.getEntityId());
        infoVo.getAttributeList().stream().forEach(e -> {
            groupDetailsList.stream().forEach(item -> {
                if (e.getId().equals(item.getAttributeId())){
                    mainDetailsList.add(item);
                }
            });
        });

        // 获取从表视图中的属性
        List<ViwGroupDetailsDTO> secondaryDetailsList = groupDetailsList.stream().filter(e -> !mainDetailsList.stream().map(item -> item.getAttributeId())
                        .collect(Collectors.toList()).contains(e.getAttributeId()))
                .collect(Collectors.toList());


        // 从表属性进行分组
        secondaryDetailsList.stream().filter(e -> e.getAttributeId()!=null).forEach(e -> {
            AttributeVO data = attributeService.getById(e.getAttributeId()).getData();
            if (data != null){
                e.setEntityId(data.getEntityId());
            }
        });

        // 从表当中,把视图组组中的属性根据实体id进行分组,方便后面join表名
        LinkedHashMap<Integer, List<ViwGroupDetailsDTO>> secondaryMap = secondaryDetailsList.stream().collect(Collectors.groupingBy(ViwGroupDetailsDTO::getEntityId
                , LinkedHashMap::new,Collectors.toList()));

        // 存储的是别名对应的字段名
        Map<String,String> aliasMap = new HashMap<>(16);

        AtomicInteger count = new AtomicInteger(0);
        int secondaryCount = count.incrementAndGet();
        String mainName = mainDetailsList.stream().map(e -> {
            String columnName = attributeService.getById(e.getAttributeId()).getData().getColumnName();
            String alias = ALIAS_MARK + secondaryCount;
            String name = alias + "." + columnName + " AS " + e.getAliasName();
            aliasMap.put(columnName,alias);
            return name;
        }).collect(Collectors.joining(","));

        // 追加主表属性
        str.append(mainName);
        str.append(",");

        // 所有从表字段的集合,统一拼接
        List<String> list = new ArrayList<>();
        for (Integer entityIdKey : secondaryMap.keySet()) {
            List<ViwGroupDetailsDTO> detailsList = secondaryMap.get(entityIdKey);
            String collect = detailsList.stream().map(e -> {
                String columnName = attributeService.getById(e.getAttributeId()).getData().getColumnName();
                String alias = ALIAS_MARK + count.incrementAndGet();
                String name = alias + "." + columnName + " AS " + e.getAliasName();
                aliasMap.put(columnName,alias);
                return name;
            }).collect(Collectors.joining(","));

            list.add(collect);
        }

        // 追加从表属性
        String secondaryFields = list.stream().collect(Collectors.joining(","));
        str.append(secondaryFields);

        // 主表表名称
        AtomicInteger count1 = new AtomicInteger(0);
        EntityVO entityVo = entityService.getDataById(viwGroupVo.getEntityId());
        str.append(" FROM " + entityVo.getTableName() + " ").append(ALIAS_MARK + count1.incrementAndGet());

        // 删除存在域字段的实体,因为后面拼接域字段的表名会重复
        for (Integer key : secondaryMap.keySet()) {
            AtomicReference<Boolean> isDomain = null;
            List<ViwGroupDetailsDTO> detailsList = secondaryMap.get(key);
            detailsList.stream().forEach(e -> {
                if (e.getDomainId() != null){
                    isDomain.set(true);
                }
            });

            if (isDomain.get() == true){
                secondaryMap.remove(key);
            }
        }

        // 不存在域属性的left join
        String leftJoinNoDoMain = secondaryMap.keySet().stream().map(e -> {

            ViwGroupDetailsDTO detailsDto = secondaryMap.get(e).get(0);

            // 别名名称
            String aliasAdd = ALIAS_MARK + count1.incrementAndGet();
            int res = count1.get() - 1;
            String aliasReduce = ALIAS_MARK + res;
            AttributeVO data = attributeService.getById(detailsDto.getAttributeId()).getData();
            EntityInfoVO entityVo1 = entityService.getAttributeById(data.getEntityId());

            StringBuilder secondaryStr = new StringBuilder();
            secondaryStr.append(entityVo1.getTableName() + " " + aliasAdd);
            secondaryStr.append(" ON ");
            secondaryStr.append(aliasReduce + "." + "fidata_version_id = " + aliasAdd + "." + "fidata_version_id");

            return secondaryStr.toString();
        }).collect(Collectors.joining(" LEFT JOIN "));

        // 查询出属性的域字段
        groupDetailsList.stream().forEach(e -> {
            AttributeVO data = attributeService.getById(e.getAttributeId()).getData();
            e.setDomainId(data.getDomainId());
        });

        // 根据域字段进行分组
        LinkedHashMap<Integer, List<ViwGroupDetailsDTO>> doMainMap = groupDetailsList.stream().filter(e -> e.getDomainId() != null)
                .collect(Collectors.groupingBy(ViwGroupDetailsDTO::getDomainId, LinkedHashMap::new, Collectors.toList()));

        // 存在域属性的left join
        String leftJoinDoMain = doMainMap.keySet().stream().map(e -> {

            ViwGroupDetailsDTO detailsDto = doMainMap.get(e).get(0);

            // 别名名称
            String aliasAdd = ALIAS_MARK + count1.incrementAndGet();
            int res = count1.get() - 1;
            String aliasReduce = ALIAS_MARK + res;
            AttributeVO data = attributeService.getById(detailsDto.getAttributeId()).getData();
            AttributeVO doMainData = attributeService.getById(e).getData();
            // 查询域属性的实体名称
            EntityInfoVO entityVo1 = entityService.getAttributeById(doMainData.getEntityId());

            StringBuilder secondaryStr = new StringBuilder();
            secondaryStr.append(entityVo1.getTableName() + " " + aliasAdd);
            secondaryStr.append(" ON ");
            secondaryStr.append(aliasReduce + "." + "fidata_version_id = " + aliasAdd + "." + "fidata_version_id");

            if (data.getDomainId() != null) {
                // 存在域字段
                secondaryStr.append(" AND ");
                secondaryStr.append(aliasMap.get(data.getColumnName()) + "." + data.getColumnName());
                secondaryStr.append(" = ");
                secondaryStr.append(aliasAdd + "." + "fidata_id");
            }

            return secondaryStr.toString();
        }).collect(Collectors.joining(" LEFT JOIN "));

        // 从表表名
        str.append(" LEFT JOIN ");
        str.append(leftJoinNoDoMain);
        // 追加域字段表名称
        if (StringUtils.isNotBlank(leftJoinDoMain)){
            str.append(" LEFT JOIN ");
            str.append(leftJoinDoMain);
        }
        return str.toString();
    }

    /**
     * 根据实体id获取属性,拼接成需要返回的参数
     * @param entityId
     * @param attributeIds
     * @return
     */
    public EntityQueryDTO getAttributeInfo(Integer entityId,List<Integer> attributeIds){
        EntityInfoVO entityInfoVo = entityService.getAttributeById(entityId);
        if (entityInfoVo == null){
            return null;
        }

        EntityQueryDTO dto = new EntityQueryDTO();
        dto.setId(entityInfoVo.getId());
        dto.setName(entityInfoVo.getName());
        dto.setType(ObjectTypeEnum.ENTITY.getName());

        // 属性信息
        List<AttributeInfoDTO> attributeList = entityInfoVo.getAttributeList();
        List<EntityQueryDTO> collect = attributeList.stream().filter(e -> e.getDomainId() == null).map(e -> {
            EntityQueryDTO dto1 = new EntityQueryDTO();
            dto1.setId(e.getId());
            dto1.setName(e.getName());
            dto1.setType(ObjectTypeEnum.ATTRIBUTES.getName());

            // 判断是否在视图组中存在
            if (attributeIds.contains(e.getId())){
                dto1.setIsCheck(1);
            }else {
                dto1.setIsCheck(0);
            }

            return dto1;
        }).collect(Collectors.toList());

        // 域字段递归
        List<EntityQueryDTO> doMainList = attributeList.stream().filter(e -> e.getDomainId() != null).map(e -> {
            AttributeVO data = attributeService.getById(e.getDomainId()).getData();
            EntityQueryDTO attributeInfo = this.getAttributeInfo(data.getEntityId(),attributeIds);
            return attributeInfo;
        }).collect(Collectors.toList());
        collect.addAll(doMainList);
        dto.setChildren(collect);

        return dto;
    }

    /**
     * 判断自定义视图组是否存在
     * @param id
     * @return
     */
    public boolean isExistViwGroup(Integer id) {
        ViwGroupPO viwGroupPo = viwGroupMapper.selectById(id);
        if (viwGroupPo == null) {
            return false;
        }

        return true;
    }

    /**
     * 根据实体id,获取自定义视图
     *
     * @param entityId
     * @return
     */
    public List<ViewGroupDropDownVO> getViewGroupByEntityId(Integer entityId) {
        QueryWrapper<ViwGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ViwGroupPO::getEntityId, entityId);
        List<ViwGroupPO> poList = viwGroupMapper.selectList(queryWrapper);
        return ViwGroupMap.INSTANCES.poListToDropDownVo(poList);
    }

}
