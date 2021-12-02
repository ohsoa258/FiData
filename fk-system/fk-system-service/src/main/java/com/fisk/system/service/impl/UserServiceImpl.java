package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.constants.FilterSqlConstants;
import com.fisk.common.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.dto.MetaDataConfigDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.system.dto.*;
import com.fisk.system.dto.userinfo.*;
import com.fisk.system.entity.RoleUserAssignmentPO;
import com.fisk.system.entity.UserPO;
import com.fisk.system.map.UserMap;
import com.fisk.system.mapper.RoleUserAssignmentMapper;
import com.fisk.system.mapper.UserMapper;
import com.fisk.system.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 */
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private BCryptPasswordEncoder passwordEncoder;
    @Resource
    UserMapper mapper;
    @Resource
    UserHelper userHelper;
    @Resource
    RoleUserAssignmentMapper roleUserAssignmentMapper;
    @Resource
    GetConfigDTO getConfig;
    @Resource
    GenerateCondition generateCondition;
    @Resource
    GetMetadata getMetadata;

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
    public Page<UserDTO> listUserData(UserQueryDTO query)
    {
        StringBuilder str = new StringBuilder();
        //筛选器拼接
        str.append(generateCondition.getCondition(query.dto));
        UserPageDTO dto=new UserPageDTO();
        dto.page=query.page;
        dto.where = str.toString();
        return  mapper.userList(dto.page,dto);
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
        po.valid=true;
        // 3.写入数据库
        return mapper.insert(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateUserValid(UserValidDTO dto)
    {
        UserPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        po.valid=dto.valid;
        return mapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
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
        model.email=dto.email;
        model.username=dto.username;
        return  mapper.updateById(model)>0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<UserPowerDTO> getPageUserData(QueryDTO dto)
    {
        List<UserPO> data=new ArrayList<>();
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        if (dto !=null && StringUtils.isNotEmpty(dto.name))
        {
            queryWrapper.lambda().like(UserPO::getUserAccount, dto.name);
        }
        //查询点击角色下所有用户
        if (dto.roleId !=0)
        {
            //获取已选中用户
            QueryWrapper<UserPO> userPOQueryWrapper = new QueryWrapper<>();
            if (dto !=null && StringUtils.isNotEmpty(dto.name))
            {
                userPOQueryWrapper.lambda().like(UserPO::getUserAccount, dto.name);
            }
            QueryWrapper<RoleUserAssignmentPO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.select("user_id").lambda().eq(RoleUserAssignmentPO::getRoleId,dto.roleId);
            List<Object> list=roleUserAssignmentMapper.selectObjs(queryWrapper1);
            List<Integer> ids=(List<Integer>)(List)list;
            if (ids !=null && ids.size()>0)
            {
                userPOQueryWrapper.in("id",ids).orderByDesc("create_time");
                List<UserPO> userPo = mapper.selectList(userPOQueryWrapper);
                if (userPo !=null || userPo.size()>0)
                {
                    data.addAll(userPo);
                }
                //获取未选中用户
                queryWrapper.notIn("id",ids);
                //获取下标
                int index=data.size();
                List<UserPO> userPo1 = mapper.selectList(queryWrapper.orderByDesc("create_time"));
                if (userPo1 !=null || userPo1.size()>0)
                {
                    data.addAll(index,userPo1);
                }
            }else {
                //获取下标
                int index=data.size();
                List<UserPO> userPo1 = mapper.selectList(queryWrapper.orderByDesc("create_time"));
                if (userPo1 !=null || userPo1.size()>0)
                {
                    data.addAll(index,userPo1);
                }
            }
        }
        else {
            data=mapper.selectList(queryWrapper.orderByDesc("create_time"));
        }
        //计算分页
        Integer count = data.size();
        Integer pageCount = 0;
        if (count % dto.size == 0) {
            pageCount = count / dto.size;
        } else {
            pageCount = count / dto.size + 1;
        }
        int fromIndex = 0;
        int toIndex = 0;

        if (dto.page != pageCount) {
            fromIndex = (dto.page - 1) * dto.size;
            toIndex = fromIndex + dto.size;
        } else {
            fromIndex = (dto.page - 1) * dto.size;
            toIndex = count;
        }
        int total=data.size();
        Page<UserPowerDTO> page=new Page<>();
        if (total==0)
        {
            return page;
        }
        data=data.subList(fromIndex,toIndex);
        page.setRecords(UserMap.INSTANCES.poToPageDto(data));
        page.setCurrent(dto.getPage());
        page.setSize(dto.getSize());
        page.setTotal(total);

        return page;
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
        po.password=passwordEncoder.encode(dto.getPassword());
        return mapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<FilterFieldDTO> getUserInfoColumn()
    {
        //拼接参数
        MetaDataConfigDTO dto=new MetaDataConfigDTO();
        dto.url= getConfig.url;
        dto.userName=getConfig.username;
        dto.password=getConfig.password;
        dto.tableName="tb_user_info";
        dto.tableAlias="a";
        dto.filterSql= FilterSqlConstants.USER_INFO_SQL;
        List<FilterFieldDTO> list=getMetadata.getMetadataList(dto);
        //添加创建人
        FilterFieldDTO data=new FilterFieldDTO();
        data.columnName="b.username";
        data.columnType="varchar(50)";
        data.columnDes="创建人";
        list.add(data);
        return list;
    }

}
