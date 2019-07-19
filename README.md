# 学习使用[logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder)

## 实践

### 将日志输出到本地文件中，以json格式保存

这里使用`RollingFileAppender`+`AsyncAppender`来实现

### 将自定义字段输出到json对象中

使用`LogstashEncoder`完成

### 使用`<includeCallerData>`标签包含`caller_*`字段

```json
{
	"caller_class_name": "com.example.demo.CustomFilter",
	"caller_file_name": "CustomFilter.java",
	"caller_line_number": 42,
	"caller_method_name": "doFilter"
}
```

这几个输出，就方便定位当前日志是在哪一个类的那个方法的哪一行打印出来的。

### 使用`customFields`标签输出全局自定义字段

```xml
<customFields>{"app_name":"${spring.application.name}", "host_name":"${HOSTNAME}"}</customFields>
```

这里可以使用logback注入的properties配置文件中的值，建议直接`<property resource="application.properties"/>`这样绑定到springboot的配置文件，而不是使用`springProperty`，因为某些情况会出现读取不到属性的情况，如配置[ctripcorp/apollo](https://github.com/ctripcorp/apollo)使用；

`${HOSTNAME}`是位于logback上下文中的值，标识计算机主机名，便于在负载均衡的情况下分辨具体的服务器；

**这里这两个字段方便后续做服务实例的统计；**


### 使用`includeMdcKeyName`标签输出
    + 添加MDC自定义字段参考[CustomFilter.java](https://github.com/Jiiiiiin/learn-logstash-logback-encoder/blob/master/src/main/java/com/example/demo/CustomFilter.java) [CustomHandlerInterceptor.java](https://github.com/Jiiiiiin/learn-logstash-logback-encoder/blob/master/src/main/java/com/example/demo/CustomHandlerInterceptor.java)


```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <!--      请求者信息字段 https://www.jianshu.com/p/a26da0c55255-->
      <!--      编码器吗，布局器和追加器默认的不包含请求者信息。请求者信息是昂贵的计算，应该在繁忙的的环境中避免使用。-->
      <!--      如果要打开请求者信息功能，在配置中包含属性<includeCallerData>。-->
      <includeCallerData>true</includeCallerData>
      <includeMdcKeyName>req_is_debug</includeMdcKeyName>
      <includeMdcKeyName>req_device_id</includeMdcKeyName>
      <includeMdcKeyName>req_user_agent</includeMdcKeyName>
      <includeMdcKeyName>req_request_uri</includeMdcKeyName>
      <includeMdcKeyName>req_request_method</includeMdcKeyName>
      <includeMdcKeyName>req_content_length</includeMdcKeyName>
      <includeMdcKeyName>req_remote_addr</includeMdcKeyName>
      <includeMdcKeyName>req_remote_user</includeMdcKeyName>
      <includeMdcKeyName>req_query_string</includeMdcKeyName>
      <includeMdcKeyName>resp_status</includeMdcKeyName>

      <!--启动时候会报错因为没有该字段，如自定义的MDC-->
      <!--<excludeMdcKeyName>req_user_agent</excludeMdcKeyName>-->
      <!--自定义字段-->
      <!--http://www.lstop.pub/2017/03/14/logback发送日志到filebeat/: customFields 自定义的字段，推荐加上HOSTNAME，计算机主机名，便于在负载均衡的情况下分辨具体的服务器-->
      <!--            Note that logback versions prior to 1.1.10 included a HOSTNAME property by default in the context. As of logback 1.1.10, the HOSTNAME property is lazily calculated (see LOGBACK-1221), and will no longer be included by default.-->
      <!--但是目前测试下来 HOSTNAME是包含的-->
      <customFields>{"app_name":"${spring.application.name}", "host_name":"${HOSTNAME}"}</customFields>
    </encoder>
```

```java
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

```

req_is_debug 用来配合查看是否是一个分流流量，如网关通过该标签将流量进行分流到一个测试实例

req_device_id 记录用户的设备id，通过设个设备id来锁定到用户的设备

req_user_agent 通过这个信息后期可以统计出访问某些交易用户所使用的设备的习惯，也可以对一些非法请求做过滤统计

req_request_uri 记录请求的接口

req_request_method 配合锁定请求的接口

req_content_length 记录请求的长度，表单长度，单位字节

req_remote_addr 用于后期用ip做地理位置定位查看用户分布情况

req_remote_user 便于日志查看用户唯一标识（如用户名），也可以看出用户是否认证

req_query_string url参数字符串，表单数据建议序列化到数据库之后需要再进行关联查询，而不是直接放在es中

resp_status 响应状态码，便于统计接口*物理*上的调用状态，如果接口不是符合真实的RESTful规范，还需要手动解析业务状态

```json
// 应用启动时候
{
	"@timestamp": "2019-07-15T16:11:09.652+08:00",
	"@version": "1",
	"app_name": "demo",
	"caller_class_name": "org.springframework.boot.StartupInfoLogger",
	"caller_file_name": "StartupInfoLogger.java",
	"caller_line_number": 50,
	"caller_method_name": "logStarting",
	"host_name": "jiiiiiins-MacBook-Pro.local",
	"HOSTNAME": "jiiiiiins-MacBook-Pro.local",
	"level": "INFO",
	"level_value": 20000,
	"logger_name": "com.example.demo.DemoApplication",
	"message": "Starting DemoApplication on jiiiiiins-MacBook-Pro.local with PID 31300 (/Users/jiiiiiin/Documents/GitHub/learn-logstash-logback-encoder/target/classes started by jiiiiiin in /Users/jiiiiiin/Documents/GitHub/learn-logstash-logback-encoder)",
	"thread_name": "main"
}

// 第一次请求进来之后
{
	"@timestamp": "2019-07-15T16:12:51.688+08:00",
	"@version": "1",
	"app_name": "demo",
	"caller_class_name": "com.example.demo.DemoApplication",
	"caller_file_name": "DemoApplication.java",
	"caller_line_number": 33,
	"caller_method_name": "index",
	"host_name": "jiiiiiins-MacBook-Pro.local",
	"HOSTNAME": "jiiiiiins-MacBook-Pro.local",
	"level": "INFO",
	"level_value": 20000,
	"logger_name": "com.example.demo.DemoApplication",
	"message": "call index",
	"req_content_length": "-1",
	"req_device_id": "anonymity_device_id",
	"req_is_debug": "false",
	"req_query_string": "query=ok",
	"req_remote_addr": "0:0:0:0:0:0:0:1",
	"req_request_method": "GET",
	"req_request_uri": "/",
	"req_user_agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
	"thread_name": "http-nio-8080-exec-3"
}

// 出现异常
{
	"@timestamp": "2019-07-15T16:22:17.428+08:00",
	"@version": "1",
	"app_name": "demo",
	"caller_class_name": "org.apache.juli.logging.DirectJDKLog",
	"caller_file_name": "DirectJDKLog.java",
	"caller_line_number": 175,
	"caller_method_name": "log",
	"host_name": "jiiiiiins-MacBook-Pro.local",
	"HOSTNAME": "jiiiiiins-MacBook-Pro.local",
	"level": "ERROR",
	"level_value": 40000,
	"logger_name": "org.apache.catalina.core.ContainerBase.[Tomcat].[localhost].[/].[dispatcherServlet]",
	"message": "Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.RuntimeException: 出错啦] with root cause",
	"req_content_length": "-1",
	"req_device_id": "anonymity_device_id",
	"req_is_debug": "false",
	"req_query_string": "query=err",
	"req_remote_addr": "0:0:0:0:0:0:0:1",
	"req_remote_user": "anonymity_user",
	"req_request_method": "GET",
	"req_request_uri": "/",
	"req_user_agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
	"resp_status": "404",
	"stack_trace": "java.lang.RuntimeException: 出错啦
	at com.example.demo.DemoApplication.index(DemoApplication.java:31)
	at sun.reflect.GeneratedMethodAccessor52.invoke(Unknown Source)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:45005)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190)
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138)
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:104)
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:892)
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:797)
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1039)
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:942)
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1005)
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:897)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:634)
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:882)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:741)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:99)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:109)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:92)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:109)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.springframework.web.filter.HiddenHttpMethodFilter.doFilterInternal(HiddenHttpMethodFilter.java:93)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:109)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:200)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:109)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202)
	at org.apache.catalina.core.StandardContextValve.__invoke(StandardContextValve.java:96)
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:41002)
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:490)
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:139)
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343)
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:408)
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66)
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:853)
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1587)
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
	at java.lang.Thread.run(Thread.java:748)
",
	"thread_name": "http-nio-8080-exec-6"
}
```

### 注意
+ 应用启动时候，除了默认字段，还包含了`customFields`
+ 接收到请求之后，会添加`includeMdcKeyName`
+ 一般不要使用`Structured Log Statements`，因为es哪里应该是一个固定建模
+ 上面相当于我们只是用了LoggerEvents提供者，并且手动定义了AccessEvents提供者中默认提供的一些字段
    
### 其他
+ [定制版本](https://www.jianshu.com/p/a26da0c55255)
+ [定制时区](https://www.jianshu.com/p/a26da0c55255)
+ [定制日志的名称长度](https://www.jianshu.com/p/a26da0c55255)
+ [定制追踪栈](https://www.jianshu.com/p/a26da0c55255)
+ [复合编码器/样式布局器](https://www.jianshu.com/p/a26da0c55255)




# 结合filebeat->logstash->es->kibana

+ [filebeat配置](/src/main/resources/fb-demo.yml)
+ [logstash配置](/src/main/resources/logstash-demo.cnf)

+ filebeat格式化之后的输出：


```json
{
  "@timestamp": "2019-07-15T08:11:09.652Z",
  "@metadata": {
    "beat": "filebeat",
    "type": "_doc",
    "version": "7.1.1"
  },
  "host_name": "jiiiiiins-MacBook-Pro.local",
  "@version": "1",
  "agent": {
    "type": "filebeat",
    "ephemeral_id": "216677d9-2010-4397-b346-6738629946ea",
    "hostname": "jiiiiiins-MacBook-Pro.local",
    "id": "850274bd-b4cb-4c11-aca3-571c8343b42f",
    "version": "7.1.1"
  },
  "host": {
    "name": "jiiiiiins-MacBook-Pro.local"
  },
  "caller_file_name": "StartupInfoLogger.java",
  "caller_class_name": "org.springframework.boot.StartupInfoLogger",
  "thread_name": "main",
  "log": {
    "file": {
      "path": ""
    },
    "offset": 0
  },
  "logger_name": "com.example.demo.DemoApplication",
  "caller_line_number": 50,
  "level": "INFO",
  "tags": [
    "dev"
  ],
  "level_value": 20000,
  "app_name": "demo",
  "input": {
    "type": "stdin"
  },
  "document_type": "logstash-logback-encoder",
  "caller_method_name": "logStarting",
  "ecs": {
    "version": "1.0.0"
  },
  "message": "Starting DemoApplication on jiiiiiins-MacBook-Pro.local with PID 31300 (/Users/jiiiiiin/Documents/GitHub/learn-logstash-logback-encoder/target/classes started by jiiiiiin in /Users/jiiiiiin/Documents/GitHub/learn-logstash-logback-encoder)",
  "HOSTNAME": "jiiiiiins-MacBook-Pro.local"
}
```

+ logstash格式化数据：

```ruby
{
                 "agent" => {
            "hostname" => "jiiiiiins-MacBook-Pro.local",
                  "id" => "850274bd-b4cb-4c11-aca3-571c8343b42f",
                "type" => "filebeat",
        "ephemeral_id" => "bcd2c171-ad40-4d60-b91b-4a6b00ee5fd9",
             "version" => "7.1.1"
    },
          "req_is_debug" => "false",
                   "log" => {
          "file" => {
            "path" => ""
        },
        "offset" => 0
    },
      "req_query_string" => "query=struct",
       "req_remote_addr" => "0:0:0:0:0:0:0:1",
    "req_content_length" => "-1",
       "req_request_uri" => "/",
           "resp_status" => "404",
                   "ecs" => {
        "version" => "1.0.0"
    },
            "service_id" => "logstash-logback-encoder",
                  "host" => {
        "name" => "jiiiiiins-MacBook-Pro.local"
    },
              "@version" => "1",
           "logger_name" => "com.example.demo.DemoApplication",
        "req_user_agent" => "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
         "req_device_id" => "anonymity_device_id",
                 "level" => "INFO",
             "@metadata" => {
             "index" => "js_elk_test_logstash-logback-encoder_logs_2019.07.16",
        "ip_address" => "127.0.0.1",
           "version" => "7.1.1",
              "type" => "_doc",
             "debug" => "true",
              "beat" => "filebeat"
    },
               "message" => "call index",
               "proj_id" => "js_elk_test",
       "req_remote_user" => "anonymity_user",
                  "tags" => [
        [0] "json",
        [1] "beats_input_codec_json_applied"
    ],
                 "input" => {
        "type" => "stdin"
    },
              "app_name" => "demo",
            "@timestamp" => 2019-07-16T09:23:28.009Z,
              "HOSTNAME" => "jiiiiiins-MacBook-Pro.local",
           "level_value" => 20000,
           "thread_name" => "http-nio-8080-exec-6",
    "req_request_method" => "GET",
             "host_name" => "jiiiiiins-MacBook-Pro.local"
}
```



## 注意
+ logstash-logback-encoder插件默认帮我们转换的`"@timestamp":"2019-07-15T22:11:25.886+08:00"`日志生成时间，会被filebeat替换成自己收集日志的当前时间（0时区）

为了解决这个问题，使用下面两个配置：
https://chenja.iteye.com/blog/2383771

```yml
json:
      # 将json字段添加到输出的根，同`fields_under_root`，默认这个值是FALSE
      keys_under_root: true
      # 解决logback映射出来的@timesamp被filebeat默认创建event记录时间字段冲掉的问题
      # https://chenja.iteye.com/blog/2383771
      overwrite_keys: true
```




# 参考

[在 IBM Cloud 上使用 Java 进行编程](https://cloud.ibm.com/docs/java?topic=java-spring-logging)

[ELK快速搭建日志平台](https://www.cnblogs.com/cjsblog/p/9517060.html)

[Logback配置](https://www.cnblogs.com/cjsblog/p/9113131.html)

[slf4j中的MDC](https://www.cnblogs.com/sealedbook/p/6227452.html) 

[logstash中logback的json编码器插件](https://www.jianshu.com/p/a26da0c55255) 

[Structured Logging with Structured Arguments](https://www.innoq.com/en/blog/structured-logging/#structuredlogstatements)

[filebeat解析log类型日志官方配置文档](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-input-log.html)
