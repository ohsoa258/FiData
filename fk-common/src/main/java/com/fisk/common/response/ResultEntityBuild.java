package com.fisk.common.response;

/**
 * 请求结果对象帮助类
 *
 * @author gy
 */
public class ResultEntityBuild {

    /**
     * 创建请求结果对象
     *
     * @param enums 返回结果
     * @param data  返回数据
     * @param <T>   返回数据的类型
     * @return 请求结果对象
     */
    public static <T> ResultEntity<T> build(ResultEnum enums, T data) {
        if (enums == null) {
            enums = ResultEnum.SUCCESS;
        }
        ResultEntity<T> res = new ResultEntity<T>();
        res.code = enums.getCode();
        res.msg = enums.getMsg();
        if (data != null) {
            res.data = data;
        }

        return res;
    }

    /**
     * 创建请求结果对象
     *
     * @param enums 返回结果
     * @param data  返回数据
     * @param <T>   返回数据的类型
     * @return 请求结果对象
     */
    public static <T> ResultEntity<T> buildData(ResultEnum enums, T data) {
        return build(enums, data);
    }

    /**
     * 创建请求结果对象
     *
     * @param enums 返回结果
     * @param <T>   返回数据的类型
     * @return 请求结果对象
     */
    public static <T> ResultEntity<T> build(ResultEnum enums) {
        if (enums == null) {
            enums = ResultEnum.SUCCESS;
        }
        ResultEntity<T> res = new ResultEntity<T>();
        res.code = enums.getCode();
        res.msg = enums.getMsg();

        return res;
    }

    /**
     * 创建请求结果对象
     *
     * @param enums 返回结果
     * @param msg   额外报错描述
     * @param <T>   返回数据的类型
     * @return 请求结果对象
     */
    public static <T> ResultEntity<T> build(ResultEnum enums, String msg) {
        if (enums == null) {
            enums = ResultEnum.SUCCESS;
        }
        ResultEntity<T> res = new ResultEntity<T>();
        res.code = enums.getCode();
        res.msg = enums.getMsg() + "。错误信息：" + msg;

        return res;
    }
}
