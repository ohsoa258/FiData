package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.BuildFactoryHelper;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.common.service.mdmBEBuild.dto.MasterDataPageDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.masterdatalog.MasterDataLogQueryDTO;
import com.fisk.mdm.map.AttributeMap;
import com.fisk.mdm.service.IMasterDataLog;
import com.fisk.mdm.vo.attribute.AttributeColumnVO;
import com.fisk.mdm.vo.masterdatalog.MasterDataLogPageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class MasterDataLogServiceImpl implements IMasterDataLog {


    @Resource
    AttributeServiceImpl attributeService;

    @Resource
    UserHelper userHelper;
    @Value("${pgsql-mdm.type}")
    private DataSourceTypeEnum type;
    @Value("${pgsql-mdm.url}")
    private String url;
    @Value("${pgsql-mdm.username}")
    private String username;
    @Value("${pgsql-mdm.password}")
    private String password;

    /**
     * 连接Connection
     *
     * @return {@link Connection}
     */
    public Connection getConnection() {
        AbstractDbHelper dbHelper = new AbstractDbHelper();
        Connection connection = dbHelper.connection(url, username,
                password, type);
        return connection;
    }

    @Override
    public ResultEnum addMasterDataLog(Map<String, Object> data, String tableName) {
        data.put("fidata_create_user", userHelper.getLoginUserInfo().id);
        data.put("fidata_create_time", LocalDateTime.now());
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        String sql = buildSqlCommand.buildInsertSingleData(data, tableName);
        log.info("执行新增日志表sql: 【" + sql + "】");
        return AbstractDbHelper.executeSqlReturnKey(sql, getConnection()) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public MasterDataLogPageVO listMasterDataLog(MasterDataLogQueryDTO dto) {
        MasterDataLogPageVO data = new MasterDataLogPageVO();
        //查询该实体下发布的属性
        List<AttributeInfoDTO> attributeInfos = attributeService.listPublishedAttribute(dto.getEntityId());
        if (attributeInfos.isEmpty()) {
            throw new FkException(ResultEnum.ATTRIBUTE_NOT_EXIST);
        }
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.dtoListToVoList(attributeInfos);
        data.setAttributes(attributeColumnVoList);
        String tableName = "";
        //获取总条数
        int rowCount = 0;
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        //获取总条数sql
        String count = buildSqlCommand.buildQueryCount(tableName, null);
        List<Map<String, Object>> columnCount = AbstractDbHelper.execQueryResultMaps(count, getConnection());
        if (!CollectionUtils.isEmpty(columnCount)) {
            rowCount = Integer.valueOf(columnCount.get(0).get("totalnum").toString()).intValue();
        }
        data.setTotal(rowCount);
        //查询字段
        String businessColumnName = StringUtils.join(attributeColumnVoList.stream()
                .map(e -> e.getName()).collect(Collectors.toList()), ",");
        //获取分页sql
        MasterDataPageDTO dataPageDTO = new MasterDataPageDTO();
        dataPageDTO.setColumnNames(businessColumnName);
        dataPageDTO.setVersionId(dto.getVersionId());
        dataPageDTO.setPageIndex(dto.getPageIndex());
        dataPageDTO.setPageSize(dto.getPageSize());
        dataPageDTO.setTableName(tableName);
        dataPageDTO.setExport(false);
        dataPageDTO.setConditions(" and code='" + dto.getCode() + "' ");
        IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
        String sql = sqlBuilder.buildMasterDataPage(dataPageDTO);
        //执行sql，获得结果集
        log.info("listMasterDataLog query sql: 【" + sql + "】");
        List<Map<String, Object>> resultMaps = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
        data.setResultData(resultMaps);
        return data;
    }

}

