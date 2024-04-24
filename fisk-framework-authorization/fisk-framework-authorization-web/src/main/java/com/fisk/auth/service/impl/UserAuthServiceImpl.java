package com.fisk.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fisk.auth.constants.JwtConstants;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.auth.dto.ssologin.SSOResultEntityDTO;
import com.fisk.auth.dto.ssologin.SSOUserInfoDTO;
import com.fisk.auth.dto.ssologin.TicketInfoDTO;
import com.fisk.auth.entity.SsoAccessRecordsPO;
import com.fisk.auth.service.SsoAccessRecordsService;
import com.fisk.auth.service.UserAuthService;
import com.fisk.auth.utils.HttpUtils;
import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.jwt.JwtUtils;
import com.fisk.common.framework.jwt.model.Payload;
import com.fisk.common.framework.jwt.model.UserDetail;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.AssignmentDTO;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.enums.ssologin.SSORoleInfoEnum;
import io.jsonwebtoken.JwtParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static com.fisk.auth.constants.JwtConstants.COOKIE_NAME;

/**
 * @author Lock
 * @date 2021/5/17 13:53
 */
@Service
@Slf4j
public class UserAuthServiceImpl implements UserAuthService {

    @Resource
    private UserClient userClient;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisUtil redis;

    @Resource
    private SsoAccessRecordsService ssoAccessRecordsService;

    /**
     * JWT解析器
     */
    private JwtParser jwtParser;

    @Override
    public ResultEntity<String> login(UserAuthDTO userAuthDTO) {

        // 1.授权中心携带用户名密码，到用户中心(数据库)查询用户
        // 请求user服务获取用户信息
        UserDTO userDTO = null;

        try {
            ResultEntity<UserDTO> res = userClient.queryUser(userAuthDTO.getUserAccount(), userAuthDTO.getPassword());
            if (res.code == ResultEnum.SUCCESS.getCode()) {
                userDTO = res.data;
            } else {
                return ResultEntityBuild.build(ResultEnum.getEnum(res.code));
            }
        } catch (Exception e) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

        // 创建自定义荷载对象
        UserDetail userDetail = UserDetail.of(userDTO.getId(), userDTO.getUserAccount());
        // 生成jwt: token
        String token = SystemConstants.AUTH_TOKEN_HEADER + jwtUtils.createJwt(userDetail);
        UserInfo userInfo = UserInfo.of(userDTO.getId(), userDTO.getUserAccount(), token);

        int permanentToken = 102;
        if (userInfo.id == permanentToken) {
            redis.set(RedisKeyBuild.buildLoginUserInfo(userInfo.id), userInfo, -1);
        } else {
            boolean res = redis.set(RedisKeyBuild.buildLoginUserInfo(userInfo.id), userInfo, RedisKeyEnum.AUTH_USERINFO.getValue());
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, token);
    }

    @Override
    public void logout(HttpServletRequest request) {
        // 1.获取token
        String token = request.getHeader(SystemConstants.HTTP_HEADER_AUTH).replace(SystemConstants.AUTH_TOKEN_HEADER, "");
        // 2.校验token的有效性
        Payload payload = null;
        try {
            payload = jwtUtils.parseJwt(token);
        } catch (Exception e) {
            // 3.如果无效，什么都不做
            return;
        }
        UserDetail userDetail = payload.getUserDetail();
        // 4删除redis数据
        redis.del(RedisKeyBuild.buildLoginUserInfo(userDetail.getId()));
    }

    @Override
    public ResultEntity<String> getToken(UserAuthDTO dto) {
        UserDetail userDetail = UserDetail.of(dto.getTemporaryId(), dto.getUserAccount());
        // 生成jwt: token
        String token = SystemConstants.AUTH_TOKEN_HEADER + jwtUtils.createJwt(userDetail);
        UserInfo userInfo = UserInfo.of(dto.getTemporaryId(), dto.getUserAccount(), token);
        // 六个小时有效期 21600秒
        redis.set(RedisKeyBuild.buildLoginUserInfo(userInfo.id), userInfo, RedisKeyEnum.AUTH_USERINFO.getValue());
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, token);
    }

    /**
     * 浦东应急局--单点登录
     *
     * @param ticketInfoDTO
     * @return
     */
    @Override
    public ResultEntity<String> singleLogin(TicketInfoDTO ticketInfoDTO) {
        try {
            String param = JSON.toJSONString(ticketInfoDTO);
            //根据票据获取用户信息的地址
            String url = "http://10.220.105.60:8494/cityoutapi/login/getUserInfoByTicket";
            //获取
            String result = HttpUtils.HttpPost(url, param);
            //解析返回的对象
            SSOResultEntityDTO dto = JSON.parseObject(result, SSOResultEntityDTO.class);
            String code = dto.getCODE();
            String msg = dto.getMSG();
            //20000代表成功
            if (!"20000".equals(code)) {
                String errorMsg = "SSO单点登录获取票据失败！未获取到有效用户信息......" + "CODE:" + code + " MSG:" + msg;
                log.error(errorMsg);
                return ResultEntityBuild.build(ResultEnum.SSO_AUTH_FAILURE, errorMsg);
            }

            SSOUserInfoDTO data = dto.getDATA();

            //用户id
            String id = data.getU_ID();
            //用户真实名字
            String name = data.getU_TRUENAME();
            //用户电话号码
            String phoneNumber = data.getU_MOBILE();
            //部门id
            String departId = data.getD_ID();
            //部门名称
            String departName = data.getD_NAME();
            //用户角色集合
            List<String> roles = data.getROLELIST();

            UserDTO userDTO = new UserDTO();
            //该奇葩email可以用作批量删除临时用户（游客）
            userDTO.setEmail("56263FISKSOFT@fisksoft.com");
            //保持账号的唯一性
            userDTO.setUserAccount((phoneNumber + System.currentTimeMillis()));
            userDTO.setUsername("临时账号" + id);
            userDTO.setPassword("Password0101!");
            userDTO.setCreateUser("浦东应急管理局--临时账号");
            userDTO.setValid(true);

            //注册临时用户到白泽
            ResultEntity<Object> register = userClient.register(userDTO);
            int code1 = register.getCode();
            String msg1 = register.getMsg();
            if (ResultEnum.SUCCESS.getCode() != code1) {
                String errorMsg = "SSO单点登录注册临时用户失败......" + "CODE:" + code + " MSG:" + msg1;
                log.error(errorMsg);
                return ResultEntityBuild.build(ResultEnum.SSO_REGISTER_FAILURE, errorMsg);
            }

            List<Integer> userIds = new ArrayList<>();
            //通过刚插入的账号获取新插入的用户id
            ResultEntity<UserDTO> userDTOResultEntity = userClient.queryUser(userDTO.getUserAccount(), userDTO.getPassword());
            int code3 = userDTOResultEntity.getCode();
            String msg3 = userDTOResultEntity.getMsg();
            if (ResultEnum.SUCCESS.getCode() != code3) {
                String errorMsg = "SSO单点登录获取刚插入的临时用户失败......" + "CODE:" + code + " MSG:" + msg3;
                log.error(errorMsg);
                return ResultEntityBuild.build(ResultEnum.SSO_GET_TEMPORARY_USER_FAILURE, errorMsg);
            }
            int fiUserId = Math.toIntExact(userDTOResultEntity.getData().id);
            userIds.add(fiUserId);

            //todo:根据通过票据获得的来自政务系统的用户角色集合roles，来决定为用户分配什么角色

            //给临时用户配置角色 3普通用户

            //todo:这里可以加个if else,判断是赋予普通用户权限还是管理员权限 参照枚举SSORoleInfoEnum
            String roleName = "普通用户";

            SSORoleInfoEnum ssoRoleInfoEnum = SSORoleInfoEnum.valueOf(roleName);
            //根据角色名称获取角色id
            ResultEntity<RoleInfoDTO> resultEntity = userClient.getRoleByRoleName(ssoRoleInfoEnum.getName());
            int code4 = resultEntity.getCode();
            String msg4 = resultEntity.getMsg();
            if (ResultEnum.SUCCESS.getCode() != code4) {
                String errorMsg = "SSO单点登录获取角色id失败......" + "CODE:" + code4 + " MSG:" + msg4;
                log.error(errorMsg);
                return ResultEntityBuild.build(ResultEnum.SSO_GET_ROLE_ID_FAILURE, errorMsg);
            }
            int roleId = (int) resultEntity.getData().getId();

            AssignmentDTO assignmentDTO = new AssignmentDTO();
            assignmentDTO.setId(roleId);
            assignmentDTO.setList(userIds);
            ResultEntity<Object> result1 = userClient.addRoleUser(assignmentDTO);
            int code2 = result1.getCode();
            String msg2 = result1.getMsg();
            if (ResultEnum.SUCCESS.getCode() != code2) {
                String errorMsg = "SSO单点登录为临时用户分配角色失败......" + "CODE:" + code2 + " MSG:" + msg2;
                log.error(errorMsg);
                return ResultEntityBuild.build(ResultEnum.SSO_ASSIGNMENT_FAILURE, errorMsg);
            }

            //临时用户登录 并返回token给前端
            UserAuthDTO userAuthDTO = new UserAuthDTO();
            userAuthDTO.setUserAccount(userDTO.getUserAccount());
            userAuthDTO.setPassword(userDTO.getPassword());
            ResultEntity<String> login = login(userAuthDTO);
            String token = login.getData();
            //todo:记录临时账号访问日志
            SsoAccessRecordsPO ssoAccessRecordsPO = new SsoAccessRecordsPO();
            ssoAccessRecordsPO.setFiUid((long) fiUserId);
            ssoAccessRecordsPO.setSsoUserInfo("");
            ssoAccessRecordsPO.setVisitTime(new Date());
            ssoAccessRecordsPO.setRoleInfo(roleName);
            boolean b = ssoAccessRecordsService.saveRecord(ssoAccessRecordsPO);

            //todo:临时用户定期删除 待讨论

            return ResultEntityBuild.buildData(ResultEnum.SUCCESS, token);
        } catch (Exception e) {
            throw new FkException(ResultEnum.ERROR, e);
        }
    }

    @Override
    public ResultEntity<String> qsLogin(String token) {
        try {
            //1、解析前端传递的token
            JSONObject userInfo;
            try {
                // JWT由三部分组成，使用"."分隔。我们只需要中间的载荷部分。
                String[] tokenParts = token.split("\\.");
                String encodedPayload = tokenParts[1];
                // 使用Base64.Decoder进行解码
                Base64.Decoder decoder = Base64.getUrlDecoder();
                byte[] decodedPayloadBytes = decoder.decode(encodedPayload);

                // 将解码后的字节数组转换为UTF-8编码的字符串
                String decodedPayload = new String(decodedPayloadBytes, StandardCharsets.UTF_8);
                userInfo = JSON.parseObject(decodedPayload);

                log.info("解析过的jwt payload:" + userInfo.toString());
            } catch (Exception ex) {
                log.error("解析jwt报错" + ex);
                throw new FkException(ResultEnum.ERROR, "token格式不正确");
            }
            //2、通过解析token拿到用户账号
            String account = (String) userInfo.get("account");
            log.info("*****账户名：" + account);

            //3、将获取到的用户账号和平台里的用户相比较 如果比较成功 则触发平台自己的登录  如果校验不通过，则不登录并告知前端用户信息不匹配，则登录失败
            ResultEntity<List<UserDTO>> resultEntity = userClient.getAllUserListWithPwd();
            if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.ERROR, "获取用户列表失败");
            }

            String fidataToken = null;

            List<UserDTO> userDTOS = resultEntity.getData();
            for (UserDTO userDTO : userDTOS) {
                if (userDTO.getUserAccount().equals(account)) {
                    //4、如果用户账号匹配成功 则触发平台自己的登录
                    UserAuthDTO userAuthDTO = new UserAuthDTO();
                    userAuthDTO.setUserAccount(userDTO.getUserAccount());
                    String password = userDTO.getPassword();
                    userAuthDTO.setPassword(password);
                    ResultEntity<String> loginResult = insideLogin(userAuthDTO);
                    if (loginResult.getCode() != ResultEnum.SUCCESS.getCode()) {
                        throw new FkException(ResultEnum.ERROR, "登录失败");
                    }
                    fidataToken = loginResult.getData();
                    break;
                }

            }

            if (fidataToken != null) {
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, fidataToken);
            } else {
                return ResultEntityBuild.buildData(ResultEnum.SSO_LOGIN_FAILURE, "单点登录失败,用户信息不匹配。");
            }

        } catch (Exception e) {
            log.error("强生交通单点登录方法报错msg：" + e.getMessage());
            log.error("强生交通单点登录方法报错stack：" + e);
            throw new FkException(ResultEnum.SSO_LOGIN_FAILURE, e);
        }
    }

    /**
     * 系统内部登录 不通过加密后的密码
     *
     * @param userAuthDTO
     * @return
     */
    public ResultEntity<String> insideLogin(UserAuthDTO userAuthDTO) {

        // 1.授权中心携带用户名，到用户中心(数据库)查询用户
        // 请求user服务获取用户信息
        UserDTO userDTO = null;

        try {
            ResultEntity<UserDTO> res = userClient.queryUserNoPwd(userAuthDTO.getUserAccount());
            if (res.code == ResultEnum.SUCCESS.getCode()) {
                userDTO = res.data;
            } else {
                return ResultEntityBuild.build(ResultEnum.getEnum(res.code));
            }
        } catch (Exception e) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

        // 创建自定义荷载对象
        UserDetail userDetail = UserDetail.of(userDTO.getId(), userDTO.getUserAccount());
        // 生成jwt: token
        String token = SystemConstants.AUTH_TOKEN_HEADER + jwtUtils.createJwt(userDetail);
        UserInfo userInfo = UserInfo.of(userDTO.getId(), userDTO.getUserAccount(), token);

        int permanentToken = 102;
        if (userInfo.id == permanentToken) {
            redis.set(RedisKeyBuild.buildLoginUserInfo(userInfo.id), userInfo, -1);
        } else {
            boolean res = redis.set(RedisKeyBuild.buildLoginUserInfo(userInfo.id), userInfo, RedisKeyEnum.AUTH_USERINFO.getValue());
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, token);
    }


    private void writeCookie(HttpServletResponse response, String token) {
        // cookie的作用域
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(JwtConstants.DOMAIN);
        // 是否禁止JS操作cookie，避免XSS攻击
        cookie.setHttpOnly(true);
        // cookie有效期，-1就是跟随当前会话，浏览器关闭就消失
        cookie.setMaxAge(-1);
        // cookie作用的路径，/代表一切路径
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
