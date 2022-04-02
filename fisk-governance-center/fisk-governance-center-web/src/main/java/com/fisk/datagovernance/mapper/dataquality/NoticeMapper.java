package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.NoticePO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

/**
 * @author dick
 * @version 1.0
 * @description 告警通知
 * @date 2022/3/23 12:42
 */
@Mapper
public interface NoticeMapper extends FKBaseMapper<NoticePO> {
    /**
     * 查询数据校验分页列表
     *
     * @param page    分页信息
     * @param keyword where条件
     * @return 查询结果
     */
    Page<NoticeVO> getAll(Page<NoticeVO> page, @Param("keyword") String keyword);

    /**
     * 新增一条数据并返回生成的主键id
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO tb_notice_module(`template_id`, `email_server_id`, `module_name`, `notice_type`, `email_subject`, `email_consignee`, `email_cc`, `body`, `module_state`, `create_time`, `create_user`, `del_flag`) VALUES (#{templateId}, #{emailServerId}, #{moduleName}, #{noticeType}, #{emailSubject}, #{emailConsignee}, #{emailCc}, #{body}, #{moduleState}, #{createTime}, #{createUser},1);")
    int insertOne(NoticePO po);
}