package com.starlive.shopping.global.config;

import com.starlive.shopping.domain.oauth2.OAuth2LoginFailure;
import com.starlive.shopping.domain.oauth2.OAuth2LoginSuccess;
import com.starlive.shopping.domain.oauth2.OAuth2LogoutSuccess;
import com.starlive.shopping.domain.oauth2.OAuth2UserService;
import com.starlive.shopping.global.filter.JwtAuthenticationFilter;
import com.starlive.shopping.global.filter.OAuth2LogoutCheckFilter;
import com.starlive.shopping.global.response.FailedAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configurable
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final DefaultOAuth2UserService oAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccess oAuth2LoginSuccess;
    private final OAuth2LogoutSuccess oAuth2LogoutSuccess;
    private final AuthenticationFailureHandler oAuth2LoginFailure;

    public WebSecurityConfig(OAuth2UserService oAuth2UserService,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        OAuth2LoginSuccess oAuth2LoginSuccess, OAuth2LogoutSuccess oAuth2LogoutSuccess,
        OAuth2LoginFailure oAuth2LoginFailure) {
        this.oAuth2UserService = oAuth2UserService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2LoginSuccess = oAuth2LoginSuccess;
        this.oAuth2LogoutSuccess = oAuth2LogoutSuccess;
        this.oAuth2LoginFailure = oAuth2LoginFailure;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity httpSecurity,
        CorsConfigurationSource corsConfigurationSource) throws Exception {

        httpSecurity
            .cors(cors -> cors
                .configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(request -> request
                .requestMatchers("/,", "/oauth2/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .oauth2Login(oauth2 -> oauth2
                .redirectionEndpoint(endpoint -> endpoint
                    .baseUri("/oauth2/callback/*"))
                .userInfoEndpoint(endpoint -> endpoint
                    .userService(oAuth2UserService))
                .successHandler(oAuth2LoginSuccess)
                .failureHandler(oAuth2LoginFailure))
            .addFilterBefore(new OAuth2LogoutCheckFilter(), LogoutFilter.class)
            .logout(oauth2 -> oauth2
                .logoutUrl("/oauth2/logout")
                .deleteCookies("JSESSIONID", "refreshToken")
                .logoutSuccessHandler(oAuth2LogoutSuccess))
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new FailedAuthentication())
            );

        return httpSecurity.build();
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}