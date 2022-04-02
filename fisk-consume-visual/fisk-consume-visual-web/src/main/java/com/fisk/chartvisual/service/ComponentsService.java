package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.components.*;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author WangYan
 * @date 2022/2/9 15:23
 */
public interface ComponentsService {

    /**
     * 查询所有菜单
     * @return
     */
    List<ComponentsClassDTO> listData();

    /**
     * 根据菜单id查询组件
     * @param id
     * @return
     */
    ResultEntity<List<ComponentsDTO>> selectClassById(Integer id);

    /**
     * 保存菜单
     * @param classDTO
     * @return
     */
    ResultEnum saveClass(ComponentsClassDTO classDTO);

    /**
     * 保存组件
     * @param dto
     * @param file
     * @return
     */
    String saveComponents(SaveComponentsDTO dto, MultipartFile file);

    /**
     * 组件压缩包下载
     * @param id
     * @param response
     * @return
     */
    ResultEnum downloadFile(Integer id, HttpServletResponse response);

    /**
     * 修改组件
     * @param dto
     * @return
     */
    ResultEnum updateComponents(ComponentsEditDTO dto);

    /**
     * 根据id删除组件
     * @param id
     * @return
     */
    ResultEnum deleteComponents(Integer id);

    /**
     * 修改菜单
     * @param dto
     * @return
     */
    ResultEnum updateComponentsClass(ComponentsClassEditDTO dto);

    /**
     * 根据id删除菜单
     * @param id
     * @return
     */
    ResultEnum deleteComponentsClass(Integer id);

    /**
     * 保存组件不同版本信息
     * @param dto
     * @param file
     * @return
     */
    String saveComponentsOption(SaveComponentsOptionDTO dto, MultipartFile file);
}
