package com.fisk.mdm.service.impl;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.BuildFactoryHelper;
import com.fisk.common.service.mdmBEBuild.CommonMethods;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.mdm.dto.complextype.GeographyDTO;
import com.fisk.mdm.map.ComplexTypeMap;
import com.fisk.mdm.service.IComplexType;
import com.fisk.mdm.vo.complextype.EchoFileVO;
import com.fisk.mdm.vo.complextype.FileVO;
import com.fisk.mdm.vo.complextype.GeographyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

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
    @Value("${file.uploadurl}")
    private String uploadUrl;

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
    public EchoFileVO uploadFile(Integer versionId, MultipartFile file) {
        EchoFileVO vo = new EchoFileVO();
        try {
            String fileName = file.getOriginalFilename();
            Date date = new Date();
            String path = uploadUrl + new SimpleDateFormat("yyyy-MM-dd").format(date);
            //如果不存在,创建文件夹
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }
            //指定上传路径
            String filePath = path + "/" + fileName;
            //创建新文件对象 指定文件路径为拼接好的路径
            File newFile = new File(filePath);
            //将前端传递过来的文件输送给新文件 这里需要抛出IO异常 throws IOException
            file.transferTo(newFile);
            //上传完成后将文件路径返回给前端用作图片回显或增加时的文件路径值等
            FileVO data = new FileVO();
            data.setFile_name(fileName);
            data.setFile_path(filePath);
            data.setFidata_version_id(versionId);
            vo.setId(addFile(data));
            vo.setFilePath(filePath);
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
