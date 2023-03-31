package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.system.dto.GetConfigDTO;
import com.fisk.system.dto.QueryDTO;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.roleinfo.RoleInfoQueryDTO;
import com.fisk.system.dto.roleinfo.RolePageDTO;
import com.fisk.system.dto.roleinfo.RolePowerDTO;
import com.fisk.system.entity.RoleInfoPO;
import com.fisk.system.map.RoleInfoMap;
import com.fisk.system.mapper.RoleInfoMapper;
import com.fisk.system.service.IRoleInfoService;
import com.fisk.system.service.IUserService;
import com.fisk.system.vo.roleinfo.RoleInfoVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class RoleInfoImpl implements IRoleInfoService{

    @Resource
    GetConfigDTO getConfig;
    @Resource
    RoleInfoMapper mapper;
    @Resource
    GenerateCondition generateCondition;
    @Resource
    GetMetadata getMetadata;
    /**
     * 获取所有角色
     *
     * @return 返回值
     */
    @Override
    public Page<RoleInfoDTO> listRoleData(RoleInfoQueryDTO query)
    {
        StringBuilder str = new StringBuilder();
        //筛选器拼接
        str.append(generateCondition.getCondition(query.dto));
        RolePageDTO dto=new RolePageDTO();
        dto.page=query.page;
        dto.where = str.toString();
        return mapper.roleList(dto.page,dto);
    }

    @Override
    public ResultEnum addRole(RoleInfoDTO dto){

        QueryWrapper<RoleInfoPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RoleInfoPO::getRoleName, dto.roleName);

        RoleInfoPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }
        RoleInfoPO po= RoleInfoMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteRole(int id)
    {
        RoleInfoPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public RoleInfoDTO getRoleById(int id) {
        RoleInfoDTO po =RoleInfoMap.INSTANCES.poToDto(mapper.selectById(id));
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return po;
    }

    @Override
    public List<RoleInfoDTO> getRoleByIds(List<Integer> ids) {
        List<RoleInfoDTO> dtoList =RoleInfoMap.INSTANCES.poListToDtoList(mapper.selectBatchIds(ids));
        if (dtoList == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return dtoList;
    }

    @Override
    public ResultEnum updateRole(RoleInfoDTO dto)
    {
        /*判断是否存在*/
        RoleInfoPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        /*判断名称是否重复*/
        QueryWrapper<RoleInfoPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RoleInfoPO::getRoleName, dto.roleName);
        RoleInfoPO data = mapper.selectOne(queryWrapper);
        if (data != null && data.id != dto.id) {
            return ResultEnum.NAME_EXISTS;
        }
        model.roleDesc=dto.roleDesc;
        model.roleName=dto.roleName;

        return  mapper.updateById(model)>0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public IPage<RolePowerDTO> getPageRoleData(QueryDTO dto)
    {
        QueryWrapper<RoleInfoPO> queryWrapper = new QueryWrapper<>();
        if (dto !=null && StringUtils.isNotEmpty(dto.name))
        {
            queryWrapper.lambda()
                    .like(RoleInfoPO::getRoleName, dto.name);
        }
        Page<RoleInfoPO> data=new Page<RoleInfoPO>(dto.getPage(),dto.getSize());
        return RoleInfoMap.INSTANCES.poToPageDto(mapper.selectPage(data,queryWrapper.select().orderByDesc("create_time")));
    }

    @Override
    public List<FilterFieldDTO> getRoleInfoColumn()
    {
        //拼接参数
        MetaDataConfigDTO dto=new MetaDataConfigDTO();
        dto.url= getConfig.url;
        dto.userName=getConfig.username;
        dto.password=getConfig.password;
        dto.tableName="tb_role_info";
        dto.tableAlias = "a";
        dto.driver = getConfig.driver;
        dto.filterSql = FilterSqlConstants.BUSINESS_AREA_SQL;
        List<FilterFieldDTO> list=getMetadata.getMetadataList(dto);
        //添加创建人
        FilterFieldDTO data=new FilterFieldDTO();
        data.columnName="b.username";
        data.columnType="varchar(50)";
        data.columnDes="创建人";
        list.add(data);
        return list;
    }

    @Override
    public List<RoleInfoVo> getTreeRols() {
        List<RoleInfoVo> treeRols = mapper.getTreeRols();
        if (treeRols == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return treeRols;
    }
}
