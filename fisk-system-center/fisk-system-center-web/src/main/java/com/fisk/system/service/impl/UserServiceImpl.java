package com.fisk.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.system.dto.ChangePasswordDTO;
import com.fisk.system.dto.GetConfigDTO;
import com.fisk.system.dto.QueryDTO;
import com.fisk.system.dto.UserInfoCurrentDTO;
import com.fisk.system.dto.userinfo.*;
import com.fisk.system.entity.RoleUserAssignmentPO;
import com.fisk.system.entity.UserPO;
import com.fisk.system.map.UserMap;
import com.fisk.system.mapper.RoleUserAssignmentMapper;
import com.fisk.system.mapper.UserMapper;
import com.fisk.system.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Value("${mdmAuthorize.url}")
    private String url;
    @Value("${mdmAuthorize.userAccount}")
    private String mdmUserAccount;
    @Value("${mdmAuthorize.userPassword}")
    private String mdmUserPassword;

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
        return po == null ? false : true;
    }

    @Override
    public Page<UserDTO> listUserData(UserQueryDTO query) {
        StringBuilder str = new StringBuilder();
        //筛选器拼接
        str.append(generateCondition.getCondition(query.dto));
        UserPageDTO dto = new UserPageDTO();
        dto.page = query.page;
        dto.where = str.toString();
        return mapper.userList(dto.page, dto);
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
        po.valid = true;
        // 3.写入数据库
        return mapper.insert(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateUserValid(UserValidDTO dto) {
        UserPO po = mapper.selectById(dto.id);
        if (po == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        po.valid = dto.valid;
        return mapper.updateById(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public UserDTO getUser(int id) {
        UserDTO po = UserMap.INSTANCES.poToDto(mapper.selectById(id));
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return po;
    }

    @Override
    public ResultEnum deleteUser(int id) {
        UserPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateUser(UserDTO dto) {
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
        model.email = dto.email;
        model.username = dto.username;
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<UserPowerDTO> getPageUserData(QueryDTO dto) {
        List<UserPO> data = new ArrayList<>();
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        if (dto != null && StringUtils.isNotEmpty(dto.name)) {
            queryWrapper.lambda().like(UserPO::getUserAccount, dto.name);
        }
        //查询点击角色下所有用户
        if (dto.roleId != 0) {
            //获取已选中用户
            QueryWrapper<UserPO> userPoQueryWrapper = new QueryWrapper<>();
            if (dto != null && StringUtils.isNotEmpty(dto.name)) {
                userPoQueryWrapper.lambda().like(UserPO::getUserAccount, dto.name);
            }
            QueryWrapper<RoleUserAssignmentPO> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.select("user_id").lambda().eq(RoleUserAssignmentPO::getRoleId, dto.roleId);
            List<Object> list = roleUserAssignmentMapper.selectObjs(queryWrapper1);
            List<Integer> ids = (List<Integer>) (List) list;
            if (ids != null && ids.size() > 0) {
                userPoQueryWrapper.in("id", ids).orderByDesc("create_time");
                List<UserPO> userPo = mapper.selectList(userPoQueryWrapper);
                if (userPo != null || userPo.size() > 0) {
                    data.addAll(userPo);
                }
                //获取未选中用户
                queryWrapper.notIn("id", ids);
                //获取下标
                int index = data.size();
                List<UserPO> userPo1 = mapper.selectList(queryWrapper.orderByDesc("create_time"));
                if (userPo1 != null || userPo1.size() > 0) {
                    data.addAll(index, userPo1);
                }
            } else {
                //获取下标
                int index = data.size();
                List<UserPO> userPo1 = mapper.selectList(queryWrapper.orderByDesc("create_time"));
                if (userPo1 != null || userPo1.size() > 0) {
                    data.addAll(index, userPo1);
                }
            }
        } else {
            data = mapper.selectList(queryWrapper.orderByDesc("create_time"));
        }
        //计算分页
        return userPageQuery(data, dto.size, dto.page);
    }

    @Override
    public Page<UserPowerDTO> userGroupQuery(UserGroupQueryDTO dto) {
        List<UserPO> data = new ArrayList<>();
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(dto.name)) {
            queryWrapper.lambda().like(UserPO::getUsername, dto.name);
        }
        if (!CollectionUtils.isEmpty(dto.userIdList)) {
            //获取已选中用户
            QueryWrapper<UserPO> userPoQueryWrapper = new QueryWrapper<>();
            if (dto != null && StringUtils.isNotEmpty(dto.name)) {
                userPoQueryWrapper.lambda().like(UserPO::getUserAccount, dto.name);
            }
            userPoQueryWrapper.in("id", dto.userIdList).orderByDesc("create_time");
            List<UserPO> userPo = mapper.selectList(userPoQueryWrapper);
            if (userPo != null || userPo.size() > 0) {
                data.addAll(userPo);
            }
            //获取未选中用户
            queryWrapper.notIn("id", dto.userIdList);
            //获取下标
            int index = data.size();
            List<UserPO> userPo1 = mapper.selectList(queryWrapper.orderByDesc("create_time"));
            if (userPo1 != null || userPo1.size() > 0) {
                data.addAll(index, userPo1);
            }
        } else {
            data = mapper.selectList(queryWrapper.orderByDesc("create_time"));
        }
        return userPageQuery(data, dto.size, dto.page);
    }

    /**
     * 计算分页
     *
     * @param data
     * @param size
     * @param page
     * @return
     */
    public Page<UserPowerDTO> userPageQuery(List<UserPO> data, int size, int page) {
        //计算分页
        Integer count = data.size();
        Integer pageCount = 0;
        if (count % size == 0) {
            pageCount = count / size;
        } else {
            pageCount = count / size + 1;
        }
        int fromIndex = 0;
        int toIndex = 0;

        if (page != pageCount) {
            fromIndex = (page - 1) * size;
            toIndex = fromIndex + size;
        } else {
            fromIndex = (page - 1) * size;
            toIndex = count;
        }
        int total = data.size();
        Page<UserPowerDTO> pageData = new Page<>();
        if (total == 0) {
            return pageData;
        }
        data = data.subList(fromIndex, toIndex);
        pageData.setRecords(UserMap.INSTANCES.poToPageDto(data));
        pageData.setCurrent(page);
        pageData.setSize(size);
        pageData.setTotal(total);

        return pageData;
    }

    /**
     * 登录: 根据用户名和密码查询用户?
     *
     * @param userAccount
     * @param password
     * @return 执行结果
     */
    @Override
    public UserDTO queryUser(String userAccount, String password) {

        // 1.根据用户名查询用户,不能根据密码,参数是明文,数据库中的是加密后的
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserPO::getUserAccount, userAccount);
        UserPO po = mapper.selectOne(queryWrapper);
        // 2.判断是否存在
        if (po == null) {
            // 用户名错误
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        if (!po.valid) {
            throw new FkException(ResultEnum.LOGIN_ACCOUNT_DISABLED);
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
    public UserInfoCurrentDTO getCurrentUserInfo() {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        UserInfoCurrentDTO dto = new UserInfoCurrentDTO();
        UserPO model = mapper.selectById(userInfo.id);
        if (model == null) {
            return dto;
        }
        dto.id = model.id;
        dto.userAccount = model.userAccount;
        dto.userName = model.username;
        return dto;
    }

    @Override
    public ResultEnum changePassword(ChangePasswordDTO dto) {
        UserPO po = mapper.selectById(dto.id);
        if (po == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        po.password = passwordEncoder.encode(dto.getPassword());
        return mapper.updateById(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<FilterFieldDTO> getUserInfoColumn() {
        //拼接参数
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.tableName = "tb_user_info";
        dto.tableAlias = "a";
        dto.driver = getConfig.driver;
        dto.filterSql = FilterSqlConstants.USER_INFO_SQL;
        List<FilterFieldDTO> list = getMetadata.getMetadataList(dto);
        //添加创建人
        FilterFieldDTO data = new FilterFieldDTO();
        data.columnName = "b.username";
        data.columnType = "varchar(50)";
        data.columnDes = "创建人";
        list.add(data);
        return list;
    }

    @Override
    public ResultEnum updatePassword(ChangePasswordDTO dto) {
        UserPO userPo = mapper.selectById(dto.id);
        if (userPo == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        if (!passwordEncoder.matches(dto.originalPassword, userPo.getPassword())) {
            return ResultEnum.ORIGINAL_PASSWORD_ERROR;
        }
        userPo.password = passwordEncoder.encode(dto.password);
        return mapper.updateById(userPo) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<List<UserDTO>> getUserListByIds(List<Long> ids) {
        List<UserDTO> userDtos = new ArrayList<>();
        if (CollectionUtils.isEmpty(ids)) {

            return ResultEntityBuild.buildData(ResultEnum.DATA_NOTEXISTS, userDtos);
        }
        userDtos = mapper.getUserListByIds(ids);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, userDtos);
    }

    @Override
    public List<UserDropDTO> listUserDrops() {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        return UserMap.INSTANCES.poToDropDto(mapper.selectList(queryWrapper.orderByDesc("create_time")));
    }

    @Override
    public ResultEntity<String> getMDMUserToken() {
        String token = "";
        try {
            if (StringUtils.isEmpty(url) || StringUtils.isEmpty(mdmUserAccount) || StringUtils.isEmpty(mdmUserPassword)) {
                return ResultEntityBuild.buildData(ResultEnum.USER_NON_EXISTENT, token);
            }
            UserInfo loginUserInfo = userHelper.getLoginUserInfo();
            if (loginUserInfo == null) {
                return ResultEntityBuild.buildData(ResultEnum.USER_NON_EXISTENT, token);
            }
            UserPO userPO = mapper.selectById(loginUserInfo.id);
            if (userPO == null) {
                return ResultEntityBuild.buildData(ResultEnum.USER_NON_EXISTENT, token);
            }
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("CurrentUserAccount", userPO.getUserAccount());
            jsonObj.put("InterUserAccount", mdmUserAccount);
            jsonObj.put("InterUserPassword", mdmUserPassword);
            String params = JSON.toJSONString(jsonObj);

            HttpClient client = new DefaultHttpClient();
            // post请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
            if (StringUtils.isNotBlank(params)) {
                httpPost.setEntity(new StringEntity(params, StandardCharsets.UTF_8));
            }
            HttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            //解析返回数据
            String result = EntityUtils.toString(entity, "UTF-8");
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject.containsKey("code") && (Integer) jsonObject.get("code") == 200) {
                jsonObject = JSON.parseObject(jsonObject.getString("data"));
                token = jsonObject.getString("token");
            }
            if (StringUtils.isEmpty(token)) {
                throw new FkException(ResultEnum.GET_JWT_TOKEN_ERROR);
            }
        } catch (IOException | ParseException e) {
            throw new FkException(ResultEnum.SEND_POST_REQUEST_ERROR);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, token);
    }
}
