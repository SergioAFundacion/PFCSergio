//package config;

//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;

//@Configuration
//public class SecurityConfig {

//  @Bean
//  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//      http
//              .csrf(csrf -> csrf.disable())
//              .cors(cors -> {}) // 👈 Habilita CORS, configuración en otro archivo
//              .authorizeHttpRequests(auth -> auth
//                      .anyRequest().permitAll() // 👈 Permite TODO
//              )
//              .formLogin(form -> form.disable())
//              .httpBasic(httpBasic -> httpBasic.disable());
//
//      return http.build();
//  }
//}

