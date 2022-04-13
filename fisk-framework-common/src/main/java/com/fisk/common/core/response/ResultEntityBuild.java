package com.fisk.common.core.response;

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
        /**
         * 历史版本：
         * res.msg = enums.getMsg() + "。错误信息：" + msg;
         * 历史版本的错误信息可能返回冗余提示
         */
        res.msg = msg;

        return res;
    }

    /**
     * 创建请求结果对象(对接atlas时创建的)
     *
     * @param code 错误状态码
     * @param msg  额外报错描述
     * @param <T>  返回数据的类型
     * @return 请求结果对象
     */
    public static <T> ResultEntity<T> buildData(int code, String msg) {
        if (String.valueOf(code).isEmpty()) {
            code = ResultEnum.SUCCESS.getCode();
        }
        ResultEntity<T> res = new ResultEntity<T>();
        res.code = code;
        res.msg = msg;

        return res;
    }
}
