package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TableAccessNonDTO;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.dto.api.ApiConfigDTO;
import com.fisk.dataaccess.dto.api.GenerateApiDTO;
import com.fisk.dataaccess.entity.ApiConfigPO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.map.ApiConfigMap;
import com.fisk.dataaccess.mapper.ApiConfigMapper;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.service.IApiConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-01-17 14:45:02
 */
@Service
public class ApiConfigImpl extends ServiceImpl<ApiConfigMapper, ApiConfigPO> implements IApiConfig {

    @Resource
    private TableAccessImpl tableAccessImpl;
    @Resource
    private TableAccessMapper tableAccessMapper;
    @Resource
    private TableFieldsImpl tableFieldImpl;

    @Override
    public ApiConfigDTO getData(long id) {

        ApiConfigPO po = this.getById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // po -> dto
        ApiConfigDTO apiConfigDTO = ApiConfigMap.INSTANCES.poToDto(po);
        // 根据api_id查询物理表集合
        List<TableAccessPO> poList = getListTableAccessByApiId(id);
        // 根据table_id查询出表详情,并赋值给apiConfigDTO
        apiConfigDTO.list = poList.stream().map(tableAccessPO -> tableAccessImpl.getData(tableAccessPO.id)).collect(Collectors.toList());
        return apiConfigDTO;
    }

    @Override
    public ResultEnum addData(ApiConfigDTO dto) {
        // 当前字段名不可重复
        List<String> list = this.list().stream().map(e -> e.apiName).collect(Collectors.toList());
        if (list.contains(dto.apiName)) {
            return ResultEnum.NAME_EXISTS;
        }

        // dto -> po
        ApiConfigPO model = ApiConfigMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        //保存
        return this.save(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum addApiDetail(ApiConfigDTO dto) {

        if (!CollectionUtils.isEmpty(dto.list)) {
            dto.list.forEach(e->tableFieldImpl.addData(e));
        }

        return ResultEnum.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(ApiConfigDTO dto) {
        // 判断名称是否重复
        QueryWrapper<ApiConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiConfigPO::getApiName, dto.apiName);
        ApiConfigPO po = baseMapper.selectOne(queryWrapper);
        if (po != null && po.id != dto.id) {
            return ResultEnum.WORKFLOWNAME_EXISTS;
        }

        // 参数校验
        ApiConfigPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        // 执行修改
        return this.updateById(ApiConfigMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum editApiDetail(ApiConfigDTO dto) {
        if (!CollectionUtils.isEmpty(dto.list)) {
            dto.list.forEach(e->tableFieldImpl.updateData(e));
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 保存or发布api的物理表
     *
     * @return void
     * @description 保存or发布api的物理表
     * @author Lock
     * @date 2022/2/15 11:26
     * @version v1.0
     * @params dto
     */
    private void savaOrPublishApiDetail(ApiConfigDTO dto) {
        if (!CollectionUtils.isEmpty(dto.list)) {
            dto.list.forEach(e -> tableFieldImpl.addData(e));
        }
    }

    @Override
    public ResultEnum deleteData(long id) {
        // 参数校验
        ApiConfigPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 根据api_id查询物理表集合
        List<TableAccessPO> poList = getListTableAccessByApiId(id);
        // 删除api下所有物理表
        poList.forEach(e -> tableAccessImpl.deleteData(e.id));

        // 删除api
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<ApiConfigDTO> getApiListData(long appId) {

        List<ApiConfigPO> list = this.query().eq("app_id", appId).list();

        return ApiConfigMap.INSTANCES.listPoToDto(list);
    }

    /**
     * 根据api_id查询物理表集合
     *
     * @return java.util.List<com.fisk.dataaccess.entity.TableAccessPO>
     * @description 根据api_id查询物理表集合
     * @author Lock
     * @date 2022/2/15 10:30
     * @version v1.0
     * @params id api_id
     */
    private List<TableAccessPO> getListTableAccessByApiId(long id) {
        QueryWrapper<TableAccessPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableAccessPO::getApiId, id)
                // 只查询table_id
                .select(TableAccessPO::getId);
        return tableAccessMapper.selectList(queryWrapper);
    }

    @Override
    public List<GenerateApiDTO> generateApi(long id) {

        List<GenerateApiDTO> apiDtoList = new ArrayList<>();

        // 1.根据apiId查询所有表
        List<TableAccessPO> accessPoList = tableAccessImpl.query().eq("api_id", id).list();
        // 2.根据tableId查询表结构
        List<TableAccessNonDTO> accessDtoList = accessPoList.stream()
                .map(e -> tableAccessImpl.getData(e.id)).collect(Collectors.toList());

        // 3.根据表结构生成data实例
        for (TableAccessNonDTO e : accessDtoList) {
            GenerateApiDTO apiDto = new GenerateApiDTO();

            apiDto.tableIdentity = e.id;
            apiDto.fieldList = getFieldList(e);

            List<TableAccessNonDTO> list = new ArrayList<>();
            List<TableAccessPO> pid = tableAccessImpl.query().eq("pid", e.id).list();
            for (TableAccessPO f : pid) {
                list.add(tableAccessImpl.getData(f.id));
            }

//            if (!CollectionUtils.isEmpty(list) && e.pid == 0) {
//                // 4.根据表层级关系生成最终的参数
//                bulidChildTree(apiDto, list);
//                apiDtoList.add(apiDto);
//            } else if (CollectionUtils.isEmpty(list) && e.pid == 0) {
//                apiDtoList.add(apiDto);
//            }


        }
        return apiDtoList;
    }

    /*
     * @description 获取父子级关系
     * @author Lock
     * @date 2022/1/18 10:05
     * @version v1.0
     * @params dto
     * @params dtoLost
     * @return com.fisk.dataaccess.dto.api.GenerateApiDTO
     */
    private GenerateApiDTO bulidChildTree(GenerateApiDTO dto, List<TableAccessNonDTO> dtoLost) {
        List<GenerateApiDTO> list = new ArrayList<>();

        for (TableAccessNonDTO e : dtoLost) {
//            if (dto.tableIdentity.equals(e.pid)) {
//
//                GenerateApiDTO generateApiDTO = new GenerateApiDTO();
//                generateApiDTO.tableIdentity = e.id;
//                generateApiDTO.fieldList = getFieldList(e);
//                list.add(generateApiDTO);
//            }
        }

        dto.data = list;
        return dto;
    }

    private List<String> getFieldList(TableAccessNonDTO dto) {
        List<String> fieldList = null;
        List<TableFieldsDTO> list = dto.list;
        if (!CollectionUtils.isEmpty(list)) {
            fieldList = list.stream().map(e -> e.fieldName).collect(Collectors.toList());
        }
        return fieldList;
    }
}