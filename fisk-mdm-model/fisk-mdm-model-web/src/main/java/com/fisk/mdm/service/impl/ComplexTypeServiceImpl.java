package com.fisk.mdm.service.impl;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.BuildFactoryHelper;
import com.fisk.common.service.mdmBEBuild.CommonMethods;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.mdm.dto.complextype.ComplexTypeDetailsParameterDTO;
import com.fisk.mdm.dto.complextype.GeographyDTO;
import com.fisk.mdm.enums.FileTypeEnum;
import com.fisk.mdm.map.ComplexTypeMap;
import com.fisk.mdm.service.IComplexType;
import com.fisk.mdm.vo.complextype.EchoFileVO;
import com.fisk.mdm.vo.complextype.FileVO;
import com.fisk.mdm.vo.complextype.GeographyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class ComplexTypeServiceImpl implements IComplexType {

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
    @Value("${file.uploadUrl}")
    private String uploadUrl;
    @Value("${file.echoPath}")
    private String echoPath;

    @Override
    public Object addGeography(GeographyDTO dto) {
        GeographyVO geographyVO = ComplexTypeMap.INSTANCES.dtoToVo(dto);
        geographyVO.setCreate_user(userHelper.getLoginUserInfo().id);
        geographyVO.setCreate_time(LocalDateTime.now());
        String code = UUID.randomUUID().toString();
        geographyVO.setCode(code);
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        String sql = buildSqlCommand.buildInsertSingleData(CommonMethods.beanToMap(geographyVO), "tb_geography");
        Integer id = AbstractDbHelper.executeSqlReturnKey(sql, getConnection());
        if (id > 0) {
            return code;
        }
        throw new FkException(ResultEnum.SAVE_DATA_ERROR);
    }

    @Override
    public void addGeography(GeographyDTO dto, Connection connection) throws SQLException {
        GeographyVO geographyVO = ComplexTypeMap.INSTANCES.dtoToVo(dto);
        geographyVO.setCreate_user(userHelper.getLoginUserInfo().id);
        geographyVO.setCreate_time(LocalDateTime.now());
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        String sql = buildSqlCommand.buildInsertSingleData(CommonMethods.beanToMap(geographyVO), "tb_geography");
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.execute();
    }

    @Override
    public EchoFileVO uploadFile(Integer versionId, MultipartFile file) {
        EchoFileVO vo = new EchoFileVO();
        try {
            String fileName = file.getOriginalFilename();
            Date date = new Date();
            String nowDateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
            String path = uploadUrl + nowDateStr;
            //如果不存在,创建文件夹
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }
            //指定上传路径
            String filePath = path + "/" + fileName;
            //回显路径
            String echoNewPath = echoPath + nowDateStr + "/" + fileName;
            //创建新文件对象 指定文件路径为拼接好的路径
            File newFile = new File(filePath);
            //将前端传递过来的文件输送给新文件 这里需要抛出IO异常 throws IOException
            file.transferTo(newFile);
            //上传完成后将文件路径返回给前端用作图片回显或增加时的文件路径值等
            FileVO data = new FileVO();
            data.setFile_name(fileName);
            data.setFile_path(echoNewPath);
            data.setFidata_version_id(versionId);
            vo.setCode(addFile(data));
            vo.setFilePath(echoNewPath);
            return vo;
        } catch (IOException e) {
            log.error("uploadFile:", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    /**
     * 新增文件
     *
     * @param data
     * @return
     */
    public String addFile(FileVO data) {
        data.setCreate_user(userHelper.getLoginUserInfo().id);
        data.setCreate_time(LocalDateTime.now());
        String code = UUID.randomUUID().toString();
        data.setCode(code);
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        Map map = CommonMethods.beanToMap(data);
        map.remove("file_type");
        String sql = buildSqlCommand.buildInsertSingleData(map, "tb_file");
        Integer id = AbstractDbHelper.executeSqlReturnKey(sql, getConnection());
        if (id > 0) {
            return code;
        }
        throw new FkException(ResultEnum.SAVE_DATA_ERROR);
    }

    @Override
    public Object getComplexTypeDetails(ComplexTypeDetailsParameterDTO dto) {
        String tableName;
        switch (dto.getDataTypeEnum()) {
            case LATITUDE_COORDINATE:
                tableName = "tb_geography";
                break;
            case FILE:
                tableName = "tb_file";
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        String sql = buildSqlCommand.buildQueryOneData(tableName, " and code='" + dto.getCode() + "'");
        List<Map<String, Object>> resultMaps = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
        if (CollectionUtils.isEmpty(resultMaps) || resultMaps.size() > 1) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        Object data;
        switch (dto.getDataTypeEnum()) {
            case FILE:
                FileVO file = new FileVO();
                file.setFile_path(resultMaps.get(0).get("file_path").toString());
                file.setFile_name(resultMaps.get(0).get("file_name").toString());
                String[] file_names = file.getFile_name().split("\\.");
                file.setFile_type(FileTypeEnum.FILE_IMAGE.getValue());
                if (!"JPEG".equals(file_names[1].toUpperCase())
                        && !"JPG".equals(file_names[1].toUpperCase())
                        && !"PNG".equals(file_names[1].toUpperCase())) {
                    file.setFile_type(FileTypeEnum.FILE.getValue());
                }
                data = file;
                break;
            case LATITUDE_COORDINATE:
                GeographyVO geography = new GeographyVO();
                geography.setLat(resultMaps.get(0).get("lat").toString());
                geography.setLng(resultMaps.get(0).get("lng").toString());
                geography.setMap_type(Integer.valueOf(resultMaps.get(0).get("map_type").toString()).intValue());
                data = geography;
                break;
            default:
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return data;
    }

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

}
