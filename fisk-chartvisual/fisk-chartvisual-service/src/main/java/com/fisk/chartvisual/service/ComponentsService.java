package com.fisk.chartvisual.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.ComponentsClassDTO;
import com.fisk.chartvisual.dto.ComponentsDTO;
import com.fisk.chartvisual.entity.ComponentsPO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/2/9 15:23
 */
public interface ComponentsService {

    /**
     * 分页查询所有组件
     * @param page
     * @return
     */
    ResultEntity<Page<ComponentsPO>> listData(Page<ComponentsPO> page);

    /**
     * 根据组件id查询菜单
     * @param id
     * @return
     */
    ResultEntity<List<ComponentsClassDTO>> selectClassById(Integer id);

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
}
