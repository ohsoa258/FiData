package com.fisk.system.interceptor;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.enums.system.AuditServiceTypeEnum;
import com.fisk.common.framework.jwt.JwtUtils;
import com.fisk.common.framework.jwt.model.Payload;
import com.fisk.common.framework.jwt.model.UserDetail;
import com.fisk.system.dto.auditlogs.AuditLogsDTO;
import com.fisk.system.service.impl.AuditLogsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    @Resource
    private AuditLogsServiceImpl auditLogsService;
    @Resource
    private JwtUtils jwtUtils;

    //前置拦截器 记录用户操作日志
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            //获取被拦截方法的详情
            AuditLogsDTO auditLogsDTO = new AuditLogsDTO();
            auditLogsDTO.setServiceType(AuditServiceTypeEnum.FISK_SYSTEM_CENTER.getValue());
            auditLogsDTO.setRequestType(request.getMethod());
            auditLogsDTO.setRequestAddr(request.getRequestURI());
            auditLogsDTO.setIpAddr(request.getRemoteAddr());
            if ("post".equalsIgnoreCase(auditLogsDTO.getRequestType())
                    || "put".equalsIgnoreCase(auditLogsDTO.getRequestType())
                    || "delete".equalsIgnoreCase(auditLogsDTO.getRequestType())
            ) {
                auditLogsDTO.setParamMap(JSON.toJSONString(new RequestWrapper(request).getBodyString()));
            } else {
                auditLogsDTO.setParamMap(JSON.toJSONString(request.getParameterMap()));
            }

            //获取请求携带的Token
            String token = request.getHeader(SystemConstants.HTTP_HEADER_AUTH);
            if (token != null) {
                token = token.replace(SystemConstants.AUTH_TOKEN_HEADER, "");
                //解析token 获取用户信息
                Payload payload = jwtUtils.parseJwt(token);
                UserDetail userDetail = payload.getUserDetail();
                auditLogsDTO.setUserId(userDetail.getId());
                auditLogsDTO.setUsername(userDetail.getUserAccount());
            } else {
                //没有token则是服务间内部远程调用 不记录日志  登录请求暂不记录
                return true;
            }

            //记录操作日志
            auditLogsService.saveAuditLog(auditLogsDTO);

        } catch (Exception e) {
            log.error("*******************************");
            log.error("拦截器记录用户操作日志报错=msg：" + e.getMessage());
            log.error("拦截器记录用户操作日志报错-method name：" + request.getRequestURI());
            log.error("拦截器记录用户操作日志报错：" + e);
            log.error("*******************************");
        }
        // 继续执行下一个拦截器或处理器
        return true;
    }

//    //后置
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
//                           ModelAndView modelAndView) {
//        // 在请求处理之后执行的逻辑（如果需要）
//    }

//    //返回
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
//            throws Exception {
//        // 在整个请求处理完成后执行的逻辑（如果需要）
//    }
}