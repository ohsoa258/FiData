package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.system.dto.*;
import com.fisk.system.entity.UserPO;
import com.fisk.system.map.UserMap;
import com.fisk.system.mapper.UserMapper;
import com.fisk.system.service.IUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private BCryptPasswordEncoder passwordEncoder;
    @Resource
    UserHelper userHelper;
    @Resource
    UserMapper mapper;

    /**
     * 校验手机号或用户名是否存在
     *
     * @param data 用户名或手机号
     * @return true：可以使用; false：不可使用
     */
    @Override
    public Boolean exist(String data) {

        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserPO::getUserAccount, data);
        UserPO po = mapper.selectOne(queryWrapper);
        return  po==null?false:true;
    }

    @Override
    public List<UserDTO> listUserData()
    {
        return  mapper.userList();
    }


    @Override
    public ResultEnum register(UserDTO dto) {
        //1.判断用户名是否已存在
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserPO::getUserAccount, dto.userAccount);
        UserPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }
        // 2.对密码进行加密
        dto.password = passwordEncoder.encode(dto.getPassword());
        UserPO po = UserMap.INSTANCES.dtoToPo(dto);
        UserInfo userInfo = userHelper.getLoginUserInfo();
        po.createUser = userInfo.id.toString();
        // 3.写入数据库
        return mapper.insert(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public UserDTO getUser(int id)
    {
        UserDTO po=UserMap.INSTANCES.poToDto(mapper.selectById(id));
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return po;
    }

    @Override
    public ResultEnum deleteUser(int id)
    {
        UserPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        model.updateUser=userInfo.id.toString();
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateUser(UserDTO dto)
    {
        UserPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserPO::getUserAccount, dto.userAccount);
        UserPO data = mapper.selectOne(queryWrapper);
        if (data != null && data.id != dto.id) {
            return ResultEnum.NAME_EXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        model.email=dto.email;
        model.username=dto.username;
        model.updateUser=userInfo.id.toString();
        return  mapper.updateById(model)>0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public IPage<UserPowerDTO> getPageUserData(QueryDTO dto)
    {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        if (dto.name !=null && dto.name.length()!=0)
        {
            queryWrapper.lambda().like(UserPO::getUserAccount, dto.name);
        }
        Page<UserPO> data=new Page<UserPO>(dto.getPage(),dto.getSize());
        return UserMap.INSTANCES.poToPageDto(mapper.selectPage(data,queryWrapper.orderByDesc("create_time")));
    }

    /**
     * 登录: 根据用户名和密码查询用户?
     *
     * @param username username
     * @param password password
     * @return 执行结果
     */
    @Override
    public UserDTO queryUser(String username, String password) {

        // 1.根据用户名查询用户,不能根据密码,参数是明文,数据库中的是加密后的
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserPO::getUsername, username);
        UserPO po = mapper.selectOne(queryWrapper);
        // 2.判断是否存在
        if (po == null) {
            // 用户名错误
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        // 3.校验密码(原理)先根据密文推算出盐值,然后明文+盐值,密码,再次比较新密文和密文
        if (!passwordEncoder.matches(password, po.getPassword())) {
            // 密码错误
            throw new FkException(ResultEnum.USER_ACCOUNTPASSWORD_ERROR);
        }
        // 4.转换DTO
        return UserMap.INSTANCES.poToDto(po);
    }

    @Override
    public UserInfoCurrentDTO getCurrentUserInfo()
    {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        UserInfoCurrentDTO dto=new UserInfoCurrentDTO();
        UserPO model = mapper.selectById(userInfo.id);
        if (model == null) {
            return dto;
        }
        dto.userAccount=model.userAccount;
        dto.userName=model.username;
        return dto;
    }

    @Override
    public ResultEnum changePassword(ChangePasswordDTO dto)
    {
        UserPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        UserInfo userInfo = userHelper.getLoginUserInfo();
        po.password=passwordEncoder.encode(dto.getPassword());
        po.updateUser=userInfo.id.toString();
        return mapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }
}
