package gov.cdc.izgateway.transformation.configuration;

import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Log
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("********** SecurityConfig.filterChain **********");

        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    //    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//            .authorizeHttpRequests(authorizeRequests ->
//                authorizeRequests
////                    .requestMatchers("/hello").hasRole("ADMIN")
//                    .anyRequest().authenticated()
//            )
//                .oauth2ResourceServer(oauth2ResourceServer ->
//                    oauth2ResourceServer.jwt()
//                );
//        return http.build();
//    }
}

