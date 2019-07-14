# 学习使用[logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder)

## 目的

+ 将日志输出到本地文件中，以json格式保存
+ 将自定义字段输出到json对象中
+ 使用`<includeCallerData>`标签包含`caller_*`字段
+ 使用`customFields`标签输出全局自定义字段
+ 使用`includeMdcKeyName`标签输出
    
```json
// 应用启动时候
{
    "@timestamp": "2019-07-14T21:56:09.884+08:00",
    "@version": "1",
    "message": "Started DemoApplication in 3.51 seconds (JVM running for 5.406)",
    "logger_name": "com.example.demo.DemoApplication",
    "thread_name": "restartedMain",
    "level": "INFO",
    "level_value": 20000,
    "HOSTNAME": "zhoumideMacBook-Pro.local",
    "caller_class_name": "org.springframework.boot.StartupInfoLogger",
    "caller_method_name": "logStarted",
    "caller_file_name": "StartupInfoLogger.java",
    "caller_line_number": 59,
    "appname": "demo",
    "host": "zhoumideMacBook-Pro.local"
}
// 第一次请求进来之后
{
    "@timestamp": "2019-07-14T21:58:09.966+08:00",
    "@version": "1",
    "message": "call index",
    "logger_name": "com.example.demo.DemoApplication",
    "thread_name": "http-nio-8080-exec-1",
    "level": "INFO",
    "level_value": 20000,
    "HOSTNAME": "zhoumideMacBook-Pro.local",
    "req_device_id": "null",
    "req_content_length": "-1",
    "req_request_uri": "/",
    "req_is_debug": "null",
    "req_request_method": "GET",
    "req_query_string": "hello=world",
    "req_user_agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36",
    "caller_class_name": "com.example.demo.DemoApplication",
    "caller_method_name": "index",
    "caller_file_name": "DemoApplication.java",
    "caller_line_number": 29,
    "appname": "demo",
    "host": "zhoumideMacBook-Pro.local"
}
```

+ 应用启动时候，除了默认字段，还包含了`customFields`
+ 接收到请求之后，会添加`includeMdcKeyName`
    
    
## 参考
    
> [slf4j中的MDC](https://www.cnblogs.com/sealedbook/p/6227452.html)
> [logstash中logback的json编码器插件](https://www.jianshu.com/p/a26da0c55255)