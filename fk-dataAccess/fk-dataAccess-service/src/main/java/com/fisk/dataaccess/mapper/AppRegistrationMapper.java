package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: Lock
 * @data: 2021/5/26 14:10
 */
@Mapper
public interface AppRegistrationMapper extends BaseMapper<AppRegistrationPO> {

    @Select("select * from tb_app_registration")
    List<AppRegistrationVO> getData();

    @Select("select * from tb_app_registration limit (#{page}-1)*{rows},rows")
    List<AppRegistrationPO> pageAppReg(Integer page, Integer rows);

}
