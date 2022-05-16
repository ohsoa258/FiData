package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeEditDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeQueryDTO;
import com.fisk.datagovernance.entity.dataquality.NoticePO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeVO;

/**
 * @author dick
 * @version 1.0
 * @description 告警通知接口
 * @date 2022/3/23 12:22
 */
public interface INoticeManageService extends IService<NoticePO> {
    /**
     * 分页查询
     *
     * @return 分页列表
     */
    Page<NoticeVO> getAll(NoticeQueryDTO query);

    /**
     * 添加数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(NoticeDTO dto);

    /**
     * 编辑数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(NoticeEditDTO dto);

    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(int id);

    /**
     * 测试发送邮件通知
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEntity<Object> sendEmialNotice(NoticeDTO dto);
}