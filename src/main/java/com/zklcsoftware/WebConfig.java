package com.zklcsoftware;

import java.util.List;
import java.util.Locale;
import javax.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import com.zklcsoftware.common.web.controller.FileUploadInterceptor;
import com.zklcsoftware.common.web.filter.SomeFilter;
import net.kaczmarzyk.spring.data.jpa.web.SpecificationArgumentResolver;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableScheduling
@EnableJpaRepositories
public class WebConfig implements WebMvcConfigurer {

    @Value("${uploadfiledir.uploadFilePath}")
    private String uploadFilePath;//服务器路径
    
	/*
	 * 设置登录地址 
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/login").setViewName("login");
		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

	/*
	 * 设置通过URL参数改变语言环境
	 */
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang");
		return lci;
	}
	
	
    @Bean
    public FileUploadInterceptor fileUploadInterceptor() {
        FileUploadInterceptor fui = new FileUploadInterceptor();
        return fui;
    }
	

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
		registry.addInterceptor(fileUploadInterceptor()).addPathPatterns("/api/common/uploadFile");
	}
	
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/project_architecture/**").addResourceLocations("file:" + uploadFilePath);
    }

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(Locale.CHINA);
		return slr;
	}

	/*
	 * 设置资源文件路径和字符集
	 */
	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:/locale/messages");
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setCacheSeconds(3600); // refresh cache once per hour
		return messageSource;
	}

	/*
	 * 让Bean验证也使用messages.properties，并支持UTF-8编码
	 */
	@Override
	public Validator getValidator() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.setValidationMessageSource(messageSource());
		return validator;
	}

	/*
	 * 添加自定义Filter
	 */
	@Bean
	public FilterRegistrationBean someFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(someFilter());
		registration.addUrlPatterns("/app/*");
		return registration;
	}
	
	// 支持动态查询 
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new SpecificationArgumentResolver());
	}

	/*
	 * 创建一个自定义Filter
	 */
	@Bean
	public Filter someFilter() {
		return new SomeFilter();
	}
	
	 /**
	 * 
	 * <p>
	 * 功能 采用默认的防火墙过滤器兼容业务代码
	 * </p>
	 */
	@Bean
	public HttpFirewall defaultHttpFirewall() {
	    return new DefaultHttpFirewall();
	}
}
