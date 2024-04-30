package com.photoapp.security;

import com.photoapp.services.UsersService;
import java.net.InetAddress;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurity {

    private UsersService usersService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Environment environment;
    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        //Configure AuthenticationManagerBuilder
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.userDetailsService(usersService)
                .passwordEncoder(bCryptPasswordEncoder);
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        //Create authenticationFilter
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(usersService, environment, authenticationManager);
        authenticationFilter.setFilterProcessesUrl(environment.getProperty("login.url.path"));
        IpAddressMatcher hasIpAddress = new IpAddressMatcher(Objects.requireNonNull(environment.getProperty("gateway.ipv4")));
        IpAddressMatcher hasIpv4Address = new IpAddressMatcher(Objects.requireNonNull(environment.getProperty("gateway.ip")));

        http.csrf((csrf) -> csrf.disable());
        http.authorizeHttpRequests((auth) -> auth
                        .requestMatchers(new AntPathRequestMatcher(" /users/status/check")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/**"))
                        .access((authentication, context) -> {
                            if(hasIpAddress.matches(context.getRequest()))
                                return new AuthorizationDecision(
                                        hasIpAddress.matches(context.getRequest()));
                            return new AuthorizationDecision(
                                    hasIpv4Address.matches(context.getRequest()));
                        }))
                .addFilter(authenticationFilter)
                .authenticationManager(authenticationManager)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
