package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CustomHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        log.info("preHandle:请求前调用");
        // req {getAuthType=null, getUserPrincipal=null, getRemoteAddr=0:0:0:0:0:0:0:1, getRemoteHost=0:0:0:0:0:0:0:1, getQueryString=null, getParameterMap=org.apache.catalina.util.ParameterMap@2507fded,
        // request_method=GET, request_request_url=http://localhost:8080/, getRemotePort=49933, request_request_uri=/, request_content_length=-1,
        // getContentLengthLong=-1, getContentType=null, DEVICE_ID=org.apache.tomcat.util.http.ValuesEnumerator@60057f89}
        // req {request_content_length=-1, getRemoteAddr=0:0:0:0:0:0:0:1, IS_DEBUG=null, getQueryString=null, getRemoteHost=0:0:0:0:0:0:0:1,
        // getParameterMap=java.util.Collections$3@5f1826eb, request_method=GET,
        // DEVICE_ID=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36,
        // request_request_url=http://localhost:8080/, getRemotePort=50177, request_request_uri=/}

        // req_QueryString=hello=world
//        MDC.put("req_ParameterMap", String.valueOf(request.getParameterNames()));
        // getRemoteAddr=0:0:0:0:0:0:0:1, getRemoteHost=0:0:0:0:0:0:0:1
//        MDC.put("req_RemoteAddr", String.valueOf(request.getRemoteAddr()));
//        MDC.put("req_remote_host", String.valueOf(request.getRemoteHost()));
        // req_RemotePort=50658
//        MDC.put("req_remote_port", String.valueOf(request.getRemotePort()));
//        MDC.put("getContentType", String.valueOf(request.getContentType()));
//        MDC.put("getContentLengthLong", String.valueOf(request.getContentLengthLong()));
//        MDC.put("getUserPrincipal", String.valueOf(request.getUserPrincipal()));
//        MDC.put("getAuthType", String.valueOf(request.getAuthType()));

        // 必须放在这里才有值
        MDC.put("req_query_string", String.valueOf(request.getQueryString()));
        log.info("req {}", MDC.getCopyOfContextMap());
        //返回 false 则请求中断
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        log.info("postHandle:请求后调用");
        MDC.put("resp_status", String.valueOf(response.getStatus()));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        log.info("afterCompletion:请求调用完成后回调方法，即在视图渲染完成后回调");

    }

}
