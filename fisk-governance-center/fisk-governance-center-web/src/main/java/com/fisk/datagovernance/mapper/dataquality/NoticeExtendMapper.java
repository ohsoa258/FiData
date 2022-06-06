package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.NoticeExtendPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author dick
 * @version 1.0
 * @description 通知扩展信息
 * @date 2022/5/16 18:24
 */
@Mapper
public interface NoticeExtendMapper extends FKBaseMapper<NoticeExtendPO> {

    /**
     * 修改通知扩展属性
     *
     * @return 执行结果
     */
    @Update("UPDATE tb_notice_rule_extend SET del_flag=0 WHERE notice_id = #{noticeId};")
    int updateByNoticeId(@Param("noticeId") int noticeId);
}
