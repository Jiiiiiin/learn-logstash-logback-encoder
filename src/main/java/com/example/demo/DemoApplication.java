package com.example.demo;

import static net.logstash.logback.argument.StructuredArguments.value;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.Filter;

// 为了扫描servlet filter组件：https://blog.lqdev.cn/2018/07/19/springboot/chapter-seven/
@ServletComponentScan
@SpringBootApplication
@RestController
@Slf4j
@Configuration
public class DemoApplication {


  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }


  @GetMapping("/hello")
  public String hello(@RequestParam(required = false) String query) {
    return "yeah ELK";
  }

  @GetMapping("/")
  public String index(@RequestParam(required = false) String query) {
    if (!StringUtils.isEmpty(query)) {
      if (query.startsWith("err")) {
        throw new RuntimeException("出错啦");
      } else if ("struct".equals(query)) {
        // 一般情况不要使用struct api，因为我们需要保证以`logback-spring.xml`为准来看数据结构
        log.info("test structured args msg: {}", value("username", "xiaoma"));
      }
    }
    log.info("call index");
    // https://www.jianshu.com/p/a26da0c55255
    // https://www.innoq.com/en/blog/structured-logging/#structuredlogstatements
    // 2019-07-15 16:38:22.462  INFO 31617 --- [nio-8080-exec-6] com.example.demo.DemoApplication         : test structured args msg: xiaoma
    return "hello ELK";
  }

  @Bean
  public FilterRegistrationBean filterRegistrationBean() {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    //当过滤器有注入其他bean类时，可直接通过@bean的方式进行实体类过滤器，这样不可自动注入过滤器使用的其他bean类。
    //当然，若无其他bean需要获取时，可直接new CustomFilter()，也可使用getBean的方式。
    registration.setFilter(new CustomFilter());
    //过滤器名称
    registration.setName("customFilter");
    //拦截路径
    registration.addUrlPatterns("/*");
    //设置顺序
    registration.setOrder(10);
    return registration;
  }

}
