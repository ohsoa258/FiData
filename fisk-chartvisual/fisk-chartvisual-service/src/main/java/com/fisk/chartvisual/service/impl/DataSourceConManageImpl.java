package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.*;
import com.fisk.chartvisual.entity.CubePO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.enums.DimensionTypeEnum;
import com.fisk.chartvisual.map.DataSourceConMap;
import com.fisk.chartvisual.map.SSASMap;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.service.IDataSourceConManageService;
import com.fisk.chartvisual.util.dbhelper.CubeHelper;
import com.fisk.chartvisual.util.dbhelper.DbHelper;
import com.fisk.chartvisual.util.dbhelper.DbHelperFactory;
import com.fisk.chartvisual.util.dbhelper.TabularHelper;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSqlCommand;
import com.fisk.chartvisual.vo.DataDomainVO;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.chartvisual.vo.DimensionVO;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.mdc.TraceType;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.fisk.common.constants.SSASConstant.Measures_Name;
import static com.fisk.common.constants.SSASConstant.Measures_UniqueName;

/**
 * 数据源管理实现类
 *
 * @author gy
 */
@Service
public class DataSourceConManageImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataSourceConManageService {


    @Resource
    DataSourceConMapper mapper;
    @Resource
    IDataService useDataBase;
    @Resource
    UserHelper userHelper;
    @Resource
    CubeHelper cubeHelper;

    @Override
    public Page<DataSourceConVO> listDataSourceCons(Page<DataSourceConVO> page, DataSourceConQuery query) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        query.userId = userInfo.id;
        return mapper.listDataSourceConByUserId(page, query);
    }

    @Override
    public ResultEnum saveDataSourceCon(DataSourceConDTO dto) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataSourceConPO::getName, dto.name).eq(DataSourceConPO::getCreateUser, userInfo.id);
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }

        DataSourceConPO model = DataSourceConMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateDataSourceCon(DataSourceConEditDTO dto) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        DataSourceConPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getName, dto.name)
                .eq(DataSourceConPO::getCreateUser, userInfo.id)
                .ne(DataSourceConPO::getId, dto.id);
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }

        DataSourceConMap.INSTANCES.editDtoToPo(dto, model);
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataSourceCon(int id) {
        DataSourceConPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_CONNECTION)
    @Override
    public ResultEnum testConnection(TestConnectionDTO dto) {
        if (dto.conType == DataSourceTypeEnum.TABULAR || dto.conType == DataSourceTypeEnum.CUBE) {
            CubeHelper cubeHelper=new CubeHelper();
            return cubeHelper.connection(dto.conStr, dto.conAccount, dto.conPassword)
                    ?
                    ResultEnum.SUCCESS : ResultEnum.VISUAL_CONNECTION_ERROR;
        } else {
            return useDataBase.testConnection(dto.conType, dto.conStr, dto.conAccount, dto.conPassword)
                    ?
                    ResultEnum.SUCCESS : ResultEnum.VISUAL_CONNECTION_ERROR;
        }
    }

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
    @Override
    public ResultEntity<Object> listDataDomain(int id) {
        //获取连接信息
        DataSourceConVO model = mapper.getDataSourceConByUserId(id);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        //创建连接
        IBuildSqlCommand command = DbHelperFactory.getSqlBuilder(model.conType);
        List<DataDomainDTO> data = DbHelper.execQueryResultList(command.buildDataDomainQuery(model.conDbname), model, DataDomainDTO.class);
        if (data != null) {
            //格式化结果。根据表名称/描述字段分组，获取每个表的字段信息 + "#" + StringUtils.defaultString(o.getTableDetails())
            List<DataDomainVO> res = data.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(
                                    () -> new TreeSet<>(
                                            Comparator.comparing(DataDomainDTO::getTableName))),
                            ArrayList::new)
                    )
                    .stream()
                    .map(e ->
                            new DataDomainVO() {{
                                name = e.tableName;
                                details = e.tableDetails;
                                children = data.stream()
                                        .filter(c -> c.tableName.equals(e.tableName))
                                        .map(item -> new DataDomainVO(item.columnName, item.columnDetails))
                                        .collect(Collectors.toList());
                            }})
                    .collect(Collectors.toList());
            return ResultEntityBuild.buildData(ResultEnum.SUCCESS, res);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS);
    }

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
    @Override
    public ResultEntity<Object> SSASDataStructure(int id) {
        //获取连接信息
        List<DimensionVO> dimensionVOList = new ArrayList<>();
        DataSourceConVO model = mapper.getDataSourceConByUserId(id);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        } else {
            cubeHelper.connection(model.conStr, model.conAccount, model.conPassword);
            try {
                CubePO modelStructure = cubeHelper.getModelStructure(model.conDbname, model.conCube);
                //度量
                DimensionVO dimensionVO_Mea = new DimensionVO();
                dimensionVO_Mea.name = Measures_Name;
                dimensionVO_Mea.uniqueName = Measures_UniqueName;
                dimensionVO_Mea.dimensionType = DimensionTypeEnum.MEASURE;
                dimensionVO_Mea.children=  SSASMap.INSTANCES.measurePoToVo(modelStructure.measures);
                dimensionVOList.add(dimensionVO_Mea);
                //维度
                modelStructure.dimensions.forEach(d -> {
                    DimensionVO dimensionVO_Dim = new DimensionVO();
                    dimensionVO_Dim.name = d.name;
                    dimensionVO_Dim.uniqueName = d.uniqueName;
                    dimensionVO_Dim.dimensionType = DimensionTypeEnum.OTHER;
                    dimensionVO_Dim.children=SSASMap.INSTANCES.hierarchiesPoToVo(d.hierarchies);
                    dimensionVOList.add(dimensionVO_Dim);
                });

            } catch (Exception e) {
                log.error("获取SSAS数据结构出错，错误信息:",e);
                return ResultEntityBuild.build(ResultEnum.ERROR);
            }finally {
                cubeHelper.closeConnection();
            }
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dimensionVOList);
    }
}
