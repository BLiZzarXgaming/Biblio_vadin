package com.example.application.config;

import com.example.application.views.myview.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configurez les règles d'autorisation ici

        http.authorizeHttpRequests(auth -> auth.requestMatchers(new AntPathRequestMatcher("/public/**"))
                .anonymous());

        super.configure(http);

        setLoginView(http, LoginView.class, "/"); // Remplacez par votre classe de vue de connexion

    }
}
