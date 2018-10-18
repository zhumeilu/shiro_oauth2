package com.zml.shiro_oauth2_server.config;

import com.zml.shiro_oauth2_server.cache.ICacheClient;
import com.zml.shiro_oauth2_server.cache.RedisClient;
import com.zml.shiro_oauth2_server.shiro.MyCacheManager;
import com.zml.shiro_oauth2_server.shiro.MySessionDAO;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager){

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        //拦截器.
        Map<String,String> filterChainDefinitionMap = new LinkedHashMap<String,String>();
        // 配置不会被拦截的链接 顺序判断
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/accessToken", "anon");
        filterChainDefinitionMap.put("/userInfo", "anon");
        filterChainDefinitionMap.put("/authorize", "anon");
        //配置退出 过滤器,其中的具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");
        //<!-- 过滤链定义，从上向下顺序执行，一般将/**放在最为下边 -->:这是一个坑呢，一不小心代码就不好使了;
        //<!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问-->
        filterChainDefinitionMap.put("/**", "authc");
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/index");

        //未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }


    @Bean
    public MyShiroRealm myShiroRealm(){
        return new MyShiroRealm();
    }

    @Bean
    public DefaultWebSecurityManager defaultWebSecurityManager(){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(myShiroRealm());
        securityManager.setCacheManager(myCacheManager());
        securityManager.setSessionManager(mySessionManager());
        //DefaultWebSecurityManager默认使用CookieRememberMeManager,cookies有效期一年
        return securityManager;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    public HandlerExceptionResolver myExceptionReslover(){
        return new MyExceptionReslover();
    }

    @Bean("sessionDAO")
    public MySessionDAO sessionDAO() {
        MySessionDAO mySessionDAO = new MySessionDAO();
        return mySessionDAO;
    }

    @Bean
    public CacheManager myCacheManager(){
        return new MyCacheManager();
    }
    @Bean
    @ConfigurationProperties(prefix = "redis")
    public ICacheClient cacheClient(){
        return new RedisClient();
    }

    @Bean
    public SessionManager mySessionManager(){
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager() ;
        sessionManager.setSessionDAO(sessionDAO());
//        sessionManager.setSessionIdCookie(new SimpleCookie());
        return sessionManager;
    }

    @Bean
    public RememberMeManager rememberMeManager(){
        return new CookieRememberMeManager();
    }
}
