package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.access.OperateMsgDTO;
import com.fisk.dataaccess.dto.access.OperateTableDTO;
import com.fisk.dataaccess.dto.access.OverlayCodePreviewAccessDTO;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.dto.table.*;
import com.fisk.dataaccess.dto.tablefield.CAndLDTO;
import com.fisk.dataaccess.dto.tablefield.TableFieldDTO;
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
     * 保存&发布  hudi(hive)建立doris外部目录
     *
     * @param dto
     * @return
     */
    ResultEnum editForHive(TableAccessNonDTO dto);

    /**
     * 保存&发布  Flink CDC 发布流程
     *
     * @param dto
     * @return
     */
    ResultEnum editForFlink(TableAccessFlinkPublishDTO dto);

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
    ResultEnum delFile(long id,long tableId,long userId);

    /**
     * 编辑字段
     *
     * @param dto
     * @return
     */
    ResultEnum updateFile(TableFieldsDTO dto);

    /**
     * 数据接入SQL预览接口
     * @param dto
     * @return
     */
    Object accessOverlayCodePreview(OverlayCodePreviewAccessDTO dto);

    /**
     * 根据字段id集合获取字段详情集合
     *
     * @param fieldIds
     * @return
     */
    List<TableFieldsDTO> getFieldInfosByIds(List<Integer> fieldIds);

    /**
     * 获取数仓建模字段数据分类和数据级别
     *
     * @return
     */
    CAndLDTO getDataClassificationsAndLevels();

    /**
     * 搜索表字段
     * @param key
     * @return
     */
    List<TableFieldDTO> searchColumn(String key);

    /**
     * Flink中止指定job
     *
     * @param jobId
     * @return
     */
    Object cancelFlinkJob(String jobId,String tblId);

}
