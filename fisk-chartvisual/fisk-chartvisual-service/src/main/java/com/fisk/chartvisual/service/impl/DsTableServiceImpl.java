package com.fisk.chartvisual.service.impl;

import com.fisk.chartvisual.dto.DsTableDTO;
import com.fisk.chartvisual.dto.TableInfoDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.DsTableService;
import com.fisk.chartvisual.util.dbhelper.AbstractDbHelper;
import com.fisk.chartvisual.util.dbhelper.DbHelperFactory;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSqlCommand;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import static com.fisk.chartvisual.enums.DataSourceInfoTypeEnum.*;

/**
 * @author WangYan
 * @date 2022/3/4 11:22
 */
@Service
public class DsTableServiceImpl implements DsTableService {

    @Resource
    DataSourceConMapper sourceConMapper;

    @Override
    public ResultEntity<DsTableDTO> getTableInfo(Integer id) {
        DataSourceConPO model = sourceConMapper.selectById(id);
        if (model == null){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS, ResultEnum.DATA_NOTEXISTS.getMsg());
        }

        // 查询数据源连接配置
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,this.dataSourceInfo(model,connection,db));
    }

    /**
     * 获取数据源连接信息
     * @param model
     */
    public DsTableDTO dataSourceInfo(DataSourceConPO model,Connection connection,AbstractDbHelper db){
        IBuildSqlCommand buildSqlCommand = DbHelperFactory.getSqlBuilder(model.conType);
        List<TableInfoDTO> resultList = db.execQueryResultList(buildSqlCommand.buildQueryAllTables(model.conDbname), connection, TableInfoDTO.class);

        DsTableDTO dto = new DsTableDTO();
        dto.setName(model.getConDbname());
        dto.setCount(resultList.size());
        dto.setType(DATABASE_NAME);

        dto.setChildren(
                resultList.stream().map(e -> {
                    List<String> fields = db.execQueryResultList(buildSqlCommand.buildQueryFiled(model.conDbname, e.getTable_name()),
                            connection, String.class);

                    return new DsTableDTO(e.getTable_name(), fields.size(),TABLE_NAME);
                }).collect(Collectors.toList())
        );

        return dto;
    }
}
