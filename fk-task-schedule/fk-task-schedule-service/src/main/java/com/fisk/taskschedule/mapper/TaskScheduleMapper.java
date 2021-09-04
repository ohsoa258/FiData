package com.fisk.taskschedule.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.taskschedule.entity.TaskSchedulePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author Lock
 */
@Mapper
public interface TaskScheduleMapper extends FKBaseMapper<TaskSchedulePO> {


    /**
     * 查询主键
     * @param jobId jobId
     * @return 主键
     */
    @Select("select id from tb_task_schedule where job_id = #{job_id} and job_pid = 0 and flag = 1 and del_flag = 1")
    int getId(@Param("job_id") int jobId);

    /**
     * 查询主键
     * @param jobPid jobPid
     * @param jobId jobId
     * @return 主键
     */
    @Select("select id from tb_task_schedule where job_pid = #{job_pid} and job_id = #{job_id} and flag = 1 and del_flag = 1")
    int getIdTwo(@Param("job_pid") int jobPid, @Param("job_id") int jobId);

    /**
     * 查询对象
     * @param jobId jobId
     * @return TaskSchedulePO
     */
    @Select("select * from tb_task_schedule where job_pid = 0 and job_id = #{job_id} and flag = 1 and del_flag = 1")
    TaskSchedulePO getData(@Param("job_id") int jobId);

    /**
     * 查询对象
     * @param jobId jobId
     * @param jobPid jobPid
     * @return TaskSchedulePO
     */
    @Select("select * from tb_task_schedule where job_id = #{job_id} and job_pid = #{job_pid} and flag = 1 and del_flag = 1")
    TaskSchedulePO getDataTwo(@Param("job_id") int jobId, @Param("job_pid") int jobPid);

    /**
     * 查询对象
     * @param jobId jobId
     * @param flag flag
     * @return TaskSchedulePO
     */
    @Select("select * from tb_task_schedule where job_id = #{job_id} and flag = #{flag} and del_flag = 1")
    TaskSchedulePO getTaskSchedule(@Param("job_id") int jobId, @Param("flag") int flag);
}
