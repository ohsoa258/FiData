package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.FolderDTO;
import com.fisk.chartvisual.dto.FolderEditDTO;
import com.fisk.chartvisual.vo.FolderVO;
import com.fisk.common.core.response.ResultEntity;

import java.util.List;

/**
 * 文件夹管理
 * @author gy
 */
public interface IFolderManageService {

    /**
     * 创建文件夹
     * @param dto dto
     * @return 创建结果
     */
    ResultEntity<Long> save(FolderDTO dto);

    /**
     * 查询所有文件夹（层级）
     * @return 查询结果
     */
    List<FolderVO> listData();

    /**
     * 删除文件夹
     * @param id id
     * @return 删除结果
     */
    ResultEntity<Object> delete(long id);

    /**
     * 编辑文件夹
     * @param dto dto
     * @return 编辑结果
     */
    ResultEntity<Object> update(FolderEditDTO dto);
}
