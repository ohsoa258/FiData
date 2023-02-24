package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.access.OperateMsgDTO;
import com.fisk.dataaccess.dto.access.OperateTableDTO;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.dto.table.*;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;

import java.util.List;

/**
 * @author Lock
 */
public interface ITableFields extends IService<TableFieldsPO> {
    /**
     * 分页
     *
     * @param query query
     * @return 分页结果
     */
    Page<DataReviewVO> listData(DataReviewQueryDTO query);

    /**
     * 根据id查询表字段
     *
     * @param id id
     * @return dto
     */
    TableFieldsDTO getTableField(int id);

    /**
     * 添加物理表字段
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(TableAccessNonDTO dto);

    /**
     * 修改物理表字段
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateData(TableAccessNonDTO dto);

    /**
     * 对表进行操作时,查询依赖
     *
     * @param dto dto
     * @return 执行结果
     */
    OperateMsgDTO loadDepend(OperateTableDTO dto);

    /**
     * 删除表版本
     *
     * @param keyStr keyStr
     * @return 执行结果
     */
    ResultEnum delVersionData(String keyStr);

    /**
     * 预览业务时间覆盖
     *
     * @param dto
     * @return
     */
    Object previewCoverCondition(TableBusinessDTO dto);

    /**
     * 获取表字段信息列表
     *
     * @param tableAccessId
     * @return
     */
    List<FieldNameDTO> getTableFileInfo(long tableAccessId);

    /**
     * 批量发布
     *
     * @param dto
     * @return
     */
    ResultEnum batchPublish(BatchPublishDTO dto);

    /**
     * 新增字段
     *
     * @param dto
     * @return
     */
    ResultEnum addFile(TableFieldsDTO dto);

    /**
     * 删除字段
     *
     * @param id
     * @return
     */
    ResultEnum delFile(long id);

}
