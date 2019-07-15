package com.example.demo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.val;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

/**
 * https://github.com/logstash/logstash-logback-encoder#mdc-fields
 *
 * @author jiiiiiin
 */
public interface MDCUtils {

  /**
   * 设置请求中对于日志查询和统计具有意义的字段
   */
  static void putRequestFields(HttpServletRequest request) {
    val IS_DEBUG = request.getHeader("IS_DEBUG");
    val DEVICE_ID = request.getHeader("DEVICE_ID");
    MDC.put("req_is_debug", StringUtils.isEmpty(IS_DEBUG) ? "false" : "true");
    MDC.put("req_device_id", StringUtils.isEmpty(DEVICE_ID) ? "anonymity_device_id" : DEVICE_ID);
    MDC.put("req_user_agent", request.getHeader("User-Agent"));
    // MDC.put("req_request_url", request.getRequestURL());
    // 获取请求对应的交易名称
    MDC.put("req_request_uri", request.getRequestURI());
    // 获取请求对应的交易对应的方法（如：POST）
    MDC.put("req_request_method", request.getMethod());
    // 返回请求体内容的长度，不包含url query string，字节为单位
    MDC.put("req_content_length", String.valueOf(request.getContentLength()));
    // 获取发出请求的客户端的IP地址
    MDC.put("req_remote_addr", request.getRemoteAddr());
    // 获取发出请求的客户端的端口号
    // MDC.put("req_remote_port", String.valueOf(request.getRemotePort()));
    // 如果用户已经过认证,则返回发出请求的用户登录信息
    val userid = request.getRemoteUser();
    MDC.put("req_remote_user", StringUtils.isEmpty(userid) ? "anonymity_user" : userid);
    // qr放在这里设置不生效，目前调整到控制器的拦截器进行设置
    // MDC.put("req_query_string", request.getQueryString()));
  }

  /**
   * 放置spring mvc拦截器触发时机才能获取到的字段
   */
  static void putSpringMVCInterceptorFields(HttpServletRequest request) {
    MDC.put("req_query_string", String.valueOf(request.getQueryString()));
  }

  /**
   * 放置响应结果中有意义的字段
   * @param response
   */
  static void putResponseFields(HttpServletResponse response) {
    MDC.put("resp_status", String.valueOf(response.getStatus()));
  }
}
