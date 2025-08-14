package com.zklcsoftware;

import java.util.List;
import java.util.Map;

import com.zklcsoftware.aimodel.util.ConstantUtil;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

@Configuration
@EnableOAuth2Sso
@Order(4)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	
    @Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/webjars/**", "/css/**", "/fonts/**",
						"/images/**", "/static/**", "/js/**",
						"/fileDownload/**", "/druid/**","/ws/**","/test/cloudfile/**","/busFiles/**","/appImg/**","/api/mcptest/v1/**","/mcptest/v1/**","/getJcPdfContent"
						);
	}

	@Override
    protected void configure(HttpSecurity http) throws Exception {
		http.headers().frameOptions().disable();
        http
        // 禁用csrf配置
        	.csrf().disable()
                .authorizeRequests()
                	.antMatchers("/login").permitAll()
				     .antMatchers("/admin/**").hasAuthority("ADMIN")
                    .antMatchers("/**").hasAuthority("USER")
                    .antMatchers("/index.html").hasAnyAuthority("USER")//添加匿名登录
                	.anyRequest().authenticated()
	            .and()
		            .formLogin()
		            .loginPage("/login")
		            .defaultSuccessUrl("/")//登陆成功后跳转到此路径
		            .permitAll()
	            .and()
	            	.logout()
	                .permitAll();
        
    }

   @Bean
    public OAuth2RestTemplate oauth2RestTemplate(OAuth2ProtectedResourceDetails resource, OAuth2ClientContext context) {
        return new OAuth2RestTemplate(resource, context);
    }

	@Bean
	public AuthoritiesExtractor authoritiesExtractor(OAuth2RestOperations template) {
		return new AuthoritiesExtractor() {
			@Override
			public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
				String userTypeStr = (String)map.get("userType");
				String userTypeCode = String.valueOf(map.get("userTypeCode"));
				 if ("教师".equals(userTypeStr)) {
					String post = (String)map.get("post");
					if (post.indexOf("系统管理员") != -1 || (ConstantUtil.USER_TYPE_101007+"").equals(userTypeCode)) {//职务是系统管理员 或者用户类型是101007平台admin 则赋予管理权限
						return AuthorityUtils.commaSeparatedStringToAuthorityList("ADMIN,USER");
					}
				}
				return AuthorityUtils.commaSeparatedStringToAuthorityList("USER");
			}
		};
	}
}
