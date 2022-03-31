package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.chartvisual.dto.*;
import com.fisk.chartvisual.entity.ComponentsClassPO;
import com.fisk.chartvisual.entity.ComponentsOptionPO;
import com.fisk.chartvisual.entity.ComponentsPO;
import com.fisk.chartvisual.map.ComponentsMap;
import com.fisk.chartvisual.mapper.ComponentsClassMapper;
import com.fisk.chartvisual.mapper.ComponentsMapper;
import com.fisk.chartvisual.mapper.ComponentsOptionMapper;
import com.fisk.chartvisual.service.ComponentsService;
import com.fisk.chartvisual.util.dbhelper.IoCloseUtil;
import com.fisk.chartvisual.util.dbhelper.zip.ZipHelper;
import com.fisk.chartvisual.util.dbhelper.zip.ZipUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static com.fisk.chartvisual.enums.ComponentsTypeEnum.COMPONENTS;
import static com.fisk.chartvisual.enums.ComponentsTypeEnum.MENU;
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
    @Resource
    ComponentsService componentsService;
    @Resource
    ComponentsOptionMapper optionMapper;

    @Override
    public List<ComponentsClassDTO> listData() {
        List<ComponentsClassPO> componentsClassList = classMapper.selectList(null);
        if (CollectionUtils.isNotEmpty(componentsClassList)){
            return componentsClassList.stream().filter(e -> e.getPid() == null).map(e -> {
                ComponentsClassDTO dto = new ComponentsClassDTO();
                dto.setId((int) e.getId());
                dto.setPid(e.getPid());
                dto.setName(e.getName());
                dto.setIcon(e.getIcon());
                dto.setType(MENU);

                // 子级
                List<ComponentsClassPO> componentsClassPoList = this.queryChildren(e.getId());
                if (CollectionUtils.isNotEmpty(componentsClassPoList)){
                    dto.setChildren(
                            componentsClassPoList.stream().filter(Objects::nonNull)
                            .map(item -> {
                                // 查询菜单下的组件
                                List<ComponentsDTO> componentsDTOList = componentsService.selectClassById((int) item.getId()).getData();
                                ComponentsClassDTO dto1 = new ComponentsClassDTO((int) item.getId(), item.getPid(), item.getName(), item.getIcon(), COMPONENTS, componentsDTOList);
                                return dto1;
                            })
                            .collect(Collectors.toList())
                    );
                }

                return dto;
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 查询每个组件的不同版本数据
     * @param componentId
     */
    public List<ComponentsOptionDTO> getOptionData(Integer componentId){
        QueryWrapper<ComponentsOptionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ComponentsOptionPO::getComponentId,componentId)
                .orderByDesc(ComponentsOptionPO::getCreateTime);
        List<ComponentsOptionPO> optionPoList = optionMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(optionPoList)){
            return ComponentsMap.INSTANCES.poToOptionList(optionPoList);
        }

        return null;
    }

    /**
     * 查询菜单表子级
     * @param id
     */
    public List<ComponentsClassPO> queryChildren(Long id){
        QueryWrapper<ComponentsClassPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ComponentsClassPO::getPid,id);
        List<ComponentsClassPO> componentsClassList = classMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(componentsClassList)){
            return componentsClassList;
        }

        return null;
    }

    @Override
    public ResultEntity<List<ComponentsDTO>> selectClassById(Integer id) {
        QueryWrapper<ComponentsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ComponentsPO::getClassId,id);

        List<ComponentsPO> componentsList = componentsMapper.selectList(queryWrapper);

        if (CollectionUtils.isNotEmpty(componentsList)){
            List<ComponentsDTO> dtoList = componentsList.stream().filter(Objects::nonNull).map(e -> {
                // 查询每个组件的不同版本数据
                List<ComponentsOptionDTO> optionData = this.getOptionData((int) e.getId());

                ComponentsDTO dto = new ComponentsDTO();
                dto.setId((int) e.getId());
                dto.setClassId(e.getClassId().intValue());
                dto.setName(e.getName());
                dto.setIcon(e.getIcon());
                if (CollectionUtils.isNotEmpty(optionData)) {
                    dto.setOptionList(optionData);
                }

                return dto;
            }).collect(Collectors.toList());

            return ResultEntityBuild.buildData(ResultEnum.SUCCESS,dtoList);
        }


        return ResultEntityBuild.buildData(ResultEnum.DATA_NOTEXISTS,null);
    }

    @Override
    public ResultEnum saveClass(ComponentsClassDTO classDTO) {
        return classMapper.insert(ComponentsMap.INSTANCES.classDtoToPo(classDTO)) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public String saveComponents(SaveComponentsDTO dto,MultipartFile file) {
        QueryWrapper<ComponentsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ComponentsPO::getName,dto.getName());
        ComponentsPO components = componentsMapper.selectOne(queryWrapper);
        if (components != null){
            return ResultEnum.DATA_EXISTS.getMsg();
        }

        String uploadAddress = this.uploadZip(file);
        // 保存组件表
        ComponentsPO po = ComponentsMap.INSTANCES.compDtoToPo(dto);
        componentsMapper.insert(po);
        // 保存组件版本信息表
        optionMapper.insert(ComponentsMap.INSTANCES.optionDtoToPo(dto.getOption(),(int)po.getId(),uploadAddress));
        return uploadAddress;
    }

    @Override
    public ResultEnum downloadFile(Integer id, HttpServletResponse response) {
        ComponentsOptionPO optionPo = optionMapper.selectById(id);
        if (optionPo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 源文件的路径
        String zipName = optionPo.getPath().substring(10);
        String sourcePath = uploadPath + zipName;

        try {
            zip(sourcePath,response,zipName);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum updateComponents(ComponentsEditDTO dto) {
        boolean components = this.isExistComponents(dto.getId());
        if (components == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 修改组件
        componentsMapper.updateById(ComponentsMap.INSTANCES.compEditDtoToPo(dto));

        // 修改组件配置表
        List<ComponentsOptionPO> optionPoList = ComponentsMap.INSTANCES.optionEditDtoToPo(dto.getOptionList());
        if (CollectionUtils.isNotEmpty(optionPoList)){
            optionPoList.stream().filter(Objects::nonNull).forEach(e -> {
                int res = optionMapper.updateById(e);
                if (res <= 0){
                    throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
                }
            });
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteComponents(Integer id) {
        ComponentsPO components = componentsMapper.selectById(id);
        if (components == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 删除组件
        int res = componentsMapper.deleteById(id);

        // 删除组件配置
        ResultEnum resultEnum = this.deleteCompOption((int) components.getId());
        return resultEnum;
    }

    @Override
    public ResultEnum updateComponentsClass(ComponentsClassEditDTO dto) {
        boolean componentsClass = this.isExistComponentsClass(dto.getId());
        if (componentsClass == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = classMapper.updateById(ComponentsMap.INSTANCES.compClassEditDtoToPo(dto));
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteComponentsClass(Integer id) {
        boolean componentsClass = this.isExistComponentsClass(id);
        if (componentsClass == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = classMapper.deleteById(id);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String saveComponentsOption(SaveComponentsOptionDTO dto, MultipartFile file) {
        // 判断组件是否存在
        QueryWrapper<ComponentsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ComponentsPO::getId,dto.getComponentId());
        ComponentsPO componentsPo = componentsMapper.selectOne(queryWrapper);
        if (componentsPo == null){
            return ResultEnum.DATA_NOTEXISTS.getMsg();
        }

        // 判断组件配置表是否有重复数据
        QueryWrapper<ComponentsOptionPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(ComponentsOptionPO::getComponentId,dto.getComponentId())
                .eq(ComponentsOptionPO::getVersion,dto.getVersion())
                .last("limit 1");
        ComponentsOptionPO optionPo = optionMapper.selectOne(query);
        if (optionPo != null){
            return ResultEnum.DATA_EXISTS.getMsg();
        }

        String uploadAddress = this.uploadZip(file);
        optionMapper.insert(ComponentsMap.INSTANCES.optionDtoToPo(dto,uploadAddress));
        return uploadAddress;
    }

    /**
     * 删除组件配置
     * @param id
     */
    public ResultEnum deleteCompOption(Integer id){
        QueryWrapper<ComponentsOptionPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(ComponentsOptionPO::getComponentId,id);
        int res = optionMapper.delete(query);
        if (res <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 判断组件数据是否存在
     * @param id
     * @return
     */
    public boolean isExistComponents(Integer id){
        ComponentsPO components = componentsMapper.selectById(id);
        if (components == null){
            return false;
        }

        return true;
    }

    /**
     * 判断菜单数据是否存在
     * @param id
     * @return
     */
    public boolean isExistComponentsClass(Integer id){
        ComponentsClassPO classPo = classMapper.selectById(id);
        if (classPo == null){
            return false;
        }

        return true;
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
            IoCloseUtil.close(bos, out);
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
