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
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public Integer addGeography(GeographyDTO dto) {
        GeographyVO geographyVO = ComplexTypeMap.INSTANCES.dtoToVo(dto);
        geographyVO.setCreate_user(userHelper.getLoginUserInfo().id);
        geographyVO.setCreate_time(LocalDateTime.now());
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        String sql = buildSqlCommand.buildInsertSingleData(CommonMethods.beanToMap(geographyVO), "tb_geography");
        return AbstractDbHelper.executeSqlReturnKey(sql, getConnection());
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
            vo.setId(addFile(data));
            vo.setFilePath(echoNewPath);
            return vo;
        } catch (IOException e) {
            log.error("uploadFile:", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    public Integer addFile(FileVO data) {
        data.setCreate_user(userHelper.getLoginUserInfo().id);
        data.setCreate_time(LocalDateTime.now());
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        String sql = buildSqlCommand.buildInsertSingleData(CommonMethods.beanToMap(data), "tb_file");
        return AbstractDbHelper.executeSqlReturnKey(sql, getConnection());
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
        String sql = buildSqlCommand.buildQueryOneData(tableName, " and id=" + dto.getId());
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

    public HttpServletResponse download(String path, HttpServletResponse response) {
        try {
            String newPath = newPath(path);
            log.info("下载文件路径:" + newPath);
            // path是指欲下载的文件的路径。
            File file = new File(newPath);
            // 取得文件名。
            String filename = file.getName();
            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(newPath));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            log.error("downloadFile ex:", ex);
        }
        return response;
    }

    public String newPath(String path) {
        String[] split = path.trim().split("/");
        String newPath = File.separator + "root" + File.separator + "nginx" + File.separator + "app";
        for (int i = 0; i < split.length; i++) {
            newPath += split[i] + File.separator;
        }
        return newPath.substring(0, newPath.length() - 1);
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
