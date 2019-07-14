package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * xml配置过滤器时，是可指定执行顺序的，但使用@WebFilter时，没有这个配置属性的(需要配合@Order进行)，所以接下来介绍下通过FilterRegistrationBean进行过滤器的注册。
 * 通过过滤器的java类名称，进行顺序的约定，比如LogFilter和AuthFilter，此时AuthFilter就会比LogFilter先执行，因为首字母A比L前面。
 *
 * 关于@webFilter使用@Order无效问题
 *
 * https://blog.lqdev.cn/2018/08/26/%E6%97%A5%E5%B8%B8%E7%A7%AF%E7%B4%AF/correct-webfilter/
 *
 * 最后关键就是这个Arrays.sort(dirContents)了。所以简单来说，可以通过class类名来达到排序效果。但这种方案要限制类名，还是使用FilterRegistrationBean之类的来设置吧。
 *
 * FilterRegistrationBean是springboot提供的，此类提供setOrder方法，可以为filter设置排序值，让spring在注册web filter之前排序后再依次注册。
 *
 */
//@Order(0) 无效
//@WebFilter(filterName = "customFilter", urlPatterns = {"/*"})
@Slf4j
public class CustomFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("filter 初始化");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("doFilter 请求处理");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        MDC.put("req_is_debug", String.valueOf(request.getHeader("IS_DEBUG")));
        MDC.put("req_device_id", String.valueOf(request.getHeader("DEVICE_ID")));
        MDC.put("req_user_agent", String.valueOf(request.getHeader("User-Agent")));
        // "req_request_uri":"/favicon.ico","req_is_debug":"null","req_request_url":"http://localhost:8080/favicon.ico"
//        MDC.put("req_request_url", String.valueOf(request.getRequestURL()));
        MDC.put("req_request_uri", String.valueOf(request.getRequestURI()));
        MDC.put("req_request_method", String.valueOf(request.getMethod()));
        MDC.put("req_content_length", String.valueOf(request.getContentLength()));
//        MDC.put("req_query_string", String.valueOf(request.getQueryString()));
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        log.info("filter 销毁");
    }
}
