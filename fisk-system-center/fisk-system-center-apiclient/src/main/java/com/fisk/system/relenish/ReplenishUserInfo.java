package com.fisk.system.relenish;

import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author gy
 * @version 1.0
 * @description 补充用户信息（根据createUser，updateUser设置用户名称/账号）
 * @date 2022/4/22 10:41
 */
@Slf4j
public class ReplenishUserInfo {

    /**
     * 将数据中的createUser和updateUser转化成用户名称 (不返回新数据，直接修改现有字段)
     *
     * @param data      数据列表
     * @param client    feign客户端
     * @param fieldType 需要用户的那个字段做填充
     * @param <T>       数据类型，必须继承BaseUserInfoVO
     */
    public static <T extends BaseUserInfoVO> void replenishUserName(List<T> data, UserClient client, UserFieldEnum fieldType) {
        if (data == null || client == null) {
            throw new NullPointerException();
        }
        if (data.size() == 0) {
            return;
        }

        Set<Long> userIdSet = new HashSet<>();
        // iterate data
        for (T item : data) {
            if (Objects.nonNull(item.createUser)) {
                userIdSet.add(Long.valueOf(item.createUser));
            }
            if (Objects.nonNull(item.updateUser)) {
                userIdSet.add(Long.valueOf(item.updateUser));
            }
        }
        // remote call
        List<Long> userIds = new ArrayList<>(userIdSet);
        ResultEntity<List<UserDTO>> res = client.getUserListByIds(userIds);
        // get res
        if (res.code == ResultEnum.SUCCESS.getCode() && res.getData() != null) {
            data.forEach(e -> {
                res.getData()
                        .stream()
                        .filter(user -> user.id.toString().equals(e.createUser) || user.id.toString().equals(e.updateUser))
                        .forEach(user -> {
                            String value = null;
                            switch (fieldType) {
                                case USER_NAME:
                                    value = user.username;
                                    break;
                                case USER_ACCOUNT:
                                    value = user.userAccount;
                                    break;
                                default:
                                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
                            }
                            if (user.id.toString().equals(e.createUser)) {
                                e.createUser = value;
                            }
                            if (user.id.toString().equals(e.updateUser)) {
                                e.updateUser = value;
                            }
                        });
            });
        } else {
            log.error("远程调用失败，错误code: " + res.getCode() + ",错误信息: " + res.getMsg());
        }
    }

    /**
     * 将数据中的createUser和updateUser转化成用户名称 (不返回新数据，直接修改现有字段)
     *
     * @param data
     * @param client
     * @param fieldType
     * @param <T>
     */
    public static <T extends BaseUserInfoVO> void replenishFiDataUserName(List<Map<String, Object>> data, UserClient client, UserFieldEnum fieldType) {
        if (data == null || client == null) {
            throw new NullPointerException();
        }
        if (data.size() == 0) {
            return;
        }

        Set<Long> userIdSet = new HashSet<>();
        // iterate data
        for (Map item : data) {
            if (Objects.nonNull(item.get("fidata_create_user"))) {
                userIdSet.add(Long.valueOf(item.get("fidata_create_user").toString()));
            }
            if (Objects.nonNull(item.get("fidata_update_user"))) {
                userIdSet.add(Long.valueOf(item.get("fidata_update_user").toString()));
            }
        }
        // remote call
        List<Long> userIds = new ArrayList<>(userIdSet);
        ResultEntity<List<UserDTO>> res = client.getUserListByIds(userIds);
        // get  res
        if (res.code == ResultEnum.SUCCESS.getCode() && res.getData() != null) {
            data.forEach(e -> {
                res.getData()
                        .stream()
                        .filter(user -> user.id.toString().equals(e.get("fidata_create_user")) || user.id.toString().equals(e.get("fidata_update_user")))
                        .forEach(user -> {
                            String value = null;
                            switch (fieldType) {
                                case USER_NAME:
                                    value = user.username;
                                    break;
                                case USER_ACCOUNT:
                                    value = user.userAccount;
                                    break;
                                default:
                                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
                            }
                            if (user.id.toString().equals(e.get("fidata_create_user"))) {
                                e.put("fidata_create_user", value);
                            }
                            if (user.id.toString().equals(e.get("fidata_update_user"))) {
                                e.put("fidata_update_user", value);
                            }
                        });
            });
        } else {
            log.error("远程调用失败，错误code: " + res.getCode() + ",错误信息: " + res.getMsg());
        }
    }


}
