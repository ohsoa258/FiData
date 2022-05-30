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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
    public ViwGroupVO getDataByGroupId(Integer id) {
        ViwGroupPO viwGroupPo = viwGroupMapper.selectById(id);
        if (viwGroupPo == null){
            return null;
        }

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

        return viwGroupVo;
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
                ViwGroupVO viwGroupVo = viwGroupService.getDataByGroupId((int) e.getId());
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
    public EntityQueryDTO getRelationByEntityId(ViwGroupQueryDTO dto) {
        // 查询出视图组中的属性
        QueryWrapper<ViwGroupDetailsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ViwGroupDetailsPO::getGroupId,dto.getGroupId());
        List<ViwGroupDetailsPO> detailsPoList = detailsMapper.selectList(queryWrapper);
        // 视图组id集合
        List<Integer> attributeIds = detailsPoList.stream().filter(e -> e.getAttributeId() != null).map(e -> e.getAttributeId()).collect(Collectors.toList());

        // 查询出域字段关联的实体
        EntityQueryDTO attributeInfo = this.getAttributeInfo(dto.getEntityId(),attributeIds);
        return attributeInfo;
    }

    @Override
    public ResultEnum createCustomView(Integer id) {
        ViwGroupVO viwGroupVo = viwGroupService.getDataByGroupId(id);
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
        List<ViwGroupDetailsDTO> secondaryDetailsList = new ArrayList<>();
        List<ViwGroupDetailsDTO> groupDetailsList = viwGroupVo.getGroupDetailsList();
        EntityInfoVO infoVo = entityService.getAttributeById(viwGroupVo.getEntityId());
        infoVo.getAttributeList().stream().forEach(e -> {
            groupDetailsList.stream().forEach(item -> {
                if (e.getId().equals(item.getAttributeId())){
                    mainDetailsList.add(item);
                }
            });
        });

        groupDetailsList.stream().forEach(e -> {
            mainDetailsList.stream().forEach(item -> {
                if (!e.getAttributeId().equals(item.getAttributeId())){
                    secondaryDetailsList.add(e);
                }
            });
        });

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

        AtomicInteger count = new AtomicInteger(1);
        String mainName = mainDetailsList.stream().map(e -> {
            String name = ALIAS_MARK + count + "." + e.getName() + " AS " + e.getAliasName();
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
                String name = ALIAS_MARK + count.getAndIncrement() + "." + e.getName() + " AS " + e.getAliasName();
                return name;
            }).collect(Collectors.joining(","));

            list.add(collect);
        }

        // 追加从表属性
        String secondaryFields = list.stream().collect(Collectors.joining(","));
        str.append(secondaryFields);

        // 主表表名称
        AtomicInteger count1 = new AtomicInteger(1);
        EntityVO entityVo = entityService.getDataById(viwGroupVo.getEntityId());
        str.append(" FROM " + entityVo.getTableName() + " ").append(count1);

        String leftJoinTable = groupDetailsList.stream().map(e -> {
            String aliasAdd = ALIAS_MARK + "." + count1.getAndIncrement();
            String aliasReduce = ALIAS_MARK + "." + count1.getAndIncrement();
            AttributeVO data = attributeService.getById(e.getAttributeId()).getData();
            EntityInfoVO entityVo1 = entityService.getAttributeById(data.getEntityId());
            List<String> columnName = entityVo1.getAttributeList().stream().map(ec -> {
                if (ec.getName().equals("code")) {
                    return ec.getColumnName();
                }
                return null;
            }).collect(Collectors.toList());

            StringBuilder secondaryStr = new StringBuilder();
            if (data.getDomainId() == null) {
                // 非域字段
                secondaryStr.append(entityVo1.getTableName() + aliasAdd);
                secondaryStr.append(" ON ");
                secondaryStr.append(aliasAdd + "." + "fidata_version_id = " + aliasReduce + "." + "fidata_version_id");
            }

            secondaryStr.append(" AND ");
            secondaryStr.append(aliasReduce + "." + data.getName());
            secondaryStr.append(" = ");
            secondaryStr.append(aliasAdd + "." + columnName.get(0));

            return secondaryStr.toString();
        }).collect(Collectors.joining(" LEFT JOIN "));

        // 从表表名
        str.append(leftJoinTable);
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
