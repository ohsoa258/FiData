package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.OperateMsgDTO;
import com.fisk.dataaccess.dto.OperateTableDTO;
import com.fisk.dataaccess.dto.TableAccessNonDTO;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.dto.datareview.DataReviewQueryDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;

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
}
