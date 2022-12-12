package com.fisk.common.framework.feign;

/**
 * @author gy
 * @version 1.0
 * @description HystrixConcurrencyStrategy 这个类主要提供了一些方法允许自定义线程隔离的一些配置
 * Hystrix默认使用线程隔离机制，即每次请求从线程池中获取一个线程去执行请求，这样会导致请求线程获取不到主线程的TraceID。
 * 所以需要重写Hystrix的获取线程方法，获取线程前，先获取主线程的MDC信息，然后设置给请求线程。
 * @date 2022/12/7 16:49
 */
//@Configuration
public class MdcHystrixConcurrencyStrategy {
//extends HystrixConcurrencyStrategy
// {

    /*@PostConstruct
    public void hystrixInit() {
        HystrixPlugins.getInstance().registerConcurrencyStrategy(new MdcHystrixConcurrencyStrategy());
    }

    *//**
     * 允许修饰Callable，比如做些上下文数据传递。
     * 这里用来获取主线程的MDC信息
     *
     * @param callable callable
     * @param <T>      T
     * @return callable
     *//*
    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        // 获取当前线程的MDC信息
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                // 设置给请求线程
                MDC.setContextMap(contextMap);
                return callable.call();
            } finally {
                MDC.clear();
            }
        };
    }*/
}
