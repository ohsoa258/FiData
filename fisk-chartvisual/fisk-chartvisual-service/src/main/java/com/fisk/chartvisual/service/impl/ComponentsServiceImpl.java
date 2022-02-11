package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.ComponentsClassDTO;
import com.fisk.chartvisual.dto.ComponentsDTO;
import com.fisk.chartvisual.entity.ComponentsClassPO;
import com.fisk.chartvisual.entity.ComponentsPO;
import com.fisk.chartvisual.map.ComponentsMap;
import com.fisk.chartvisual.mapper.ComponentsClassMapper;
import com.fisk.chartvisual.mapper.ComponentsMapper;
import com.fisk.chartvisual.service.ComponentsService;
import com.fisk.chartvisual.util.dbhelper.IOCloseUtil;
import com.fisk.chartvisual.util.dbhelper.zip.ZipHelper;
import com.fisk.chartvisual.util.dbhelper.zip.ZipUtils;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static com.fisk.chartvisual.util.dbhelper.zip.ZipHelper.isZip;
import static com.fisk.chartvisual.util.dbhelper.zip.ZipUtils.compress;

/**
 * @author WangYan
 * @date 2022/2/9 15:23
 */
@Service
public class ComponentsServiceImpl implements ComponentsService {

    @Value("${folder.uploadPath}")
    private String uploadPath;
    @Value("${folder.accessAddress}")
    private String accessAddress;

    @Resource
    ComponentsMapper componentsMapper;
    @Resource
    ComponentsClassMapper classMapper;

    @Override
    public ResultEntity<Page<ComponentsPO>> listData(Page<ComponentsPO> page) {
        Page<ComponentsPO> componentsPage = componentsMapper.selectPage(page, null);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,componentsPage);
    }

    @Override
    public ResultEntity<List<ComponentsClassDTO>> selectClassById(Integer id) {
        ComponentsClassPO componentsClass = classMapper.selectById(id);
        if (componentsClass == null){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        QueryWrapper<ComponentsClassPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ComponentsClassPO::getPid,componentsClass.getId());
        List<ComponentsClassPO> componentsClassList = classMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(componentsClassList)){
            // 不存在二级菜单
            List<ComponentsClassDTO> res = new ArrayList<>();
            res.add(ComponentsMap.INSTANCES.poToDto(componentsClass));
            return ResultEntityBuild.buildData(ResultEnum.SUCCESS,res);
        }else {
            // 存在二级菜单
            List<ComponentsClassDTO> res = componentsClassList.stream().filter(Objects::nonNull).map(e -> {
                ComponentsClassDTO dto = new ComponentsClassDTO();
                dto.setId(e.getId());
                dto.setPid(id);
                dto.setName(e.getName());
                dto.setIcon(e.getIcon());
                return dto;
            }).collect(Collectors.toList());
            return ResultEntityBuild.buildData(ResultEnum.SUCCESS,res);
        }
    }

    @Override
    public ResultEnum saveClass(ComponentsClassDTO classDTO) {
        return classMapper.insert(ComponentsMap.INSTANCES.classDtoToPo(classDTO)) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public String saveComponents(ComponentsDTO dto,MultipartFile file) {
        String uploadAddress = this.uploadZip(file);
        componentsMapper.insert(ComponentsMap.INSTANCES.compDtoToPo(dto,uploadAddress));
        return uploadAddress;
    }

    @Override
    public ResultEnum downloadFile(Integer id, HttpServletResponse response) {
        ComponentsPO components = componentsMapper.selectById(id);
        if (components == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 源文件的路径
        String zipName = components.getPath().substring(10);
        String sourcePath = uploadPath + zipName;

        try {
            zip(sourcePath,response,zipName);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 文件夹压缩为zip文件
     * @param sourceFileName 源文件的路径
     * @param response 响应对象
     * @param zipName 压缩包名
     */
    public static void zip(String sourceFileName, HttpServletResponse response,String zipName){
        ZipOutputStream out = null;
        BufferedOutputStream bos = null;
        try {
            //将zip以流的形式输出到前台
            response.setHeader("content-type", "application/octet-stream");
            response.setCharacterEncoding("utf-8");
            // 设置浏览器响应头对应的Content-disposition
            //参数中 testZip 为压缩包文件名，尾部的.zip 为文件后缀
            response.setHeader("Content-disposition",
                    "attachment;filename=" + new String(zipName.getBytes("gbk"), "iso8859-1")+".zip");
            //创建zip输出流
            out = new ZipOutputStream(response.getOutputStream());
            //创建缓冲输出流
            bos = new BufferedOutputStream(out);
            File sourceFile = new File(sourceFileName);
            //调用压缩函数
            compress(out, bos, sourceFile, sourceFile.getName());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            IOCloseUtil.close(bos, out);
        }
    }

    /**
     * 将zip压缩文件上传到服务器并解压
     * @param zipFile
     * @return
     */
    public String uploadZip(MultipartFile zipFile){
        boolean zip = isZip(zipFile);
        if (zip){
            File file = new File(uploadPath);
            //如果文件夹不存在  创建文件夹
            if (!file.exists()) {
                file.mkdir();
            }
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            //获取文件名（包括后缀）
            String filename = zipFile.getOriginalFilename();
            String pathName = uploadPath + uuid + "-" + filename;
            try {
                File dest = new File(pathName);
                zipFile.transferTo(dest);
                String destDirPath = uploadPath + uuid;
                // 解压文件
                ZipUtils.unZip(dest, destDirPath);
                // 删除临时文件
                ZipHelper.deleteFile(pathName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String address = accessAddress + uuid;
            return address;
        }

        return null;
    }
}
