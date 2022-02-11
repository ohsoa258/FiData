package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.ComponentsClassDTO;
import com.fisk.chartvisual.dto.ComponentsClassEditDTO;
import com.fisk.chartvisual.dto.ComponentsDTO;
import com.fisk.chartvisual.dto.ComponentsEditDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
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
     * 根据组件id查询菜单
     * @param id
     * @return
     */
    ResultEntity<ComponentsDTO> selectClassById(Integer id);

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
    String saveComponents(ComponentsDTO dto,MultipartFile file);

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
}
