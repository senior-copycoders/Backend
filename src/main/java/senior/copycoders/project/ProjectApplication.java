package senior.copycoders.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import senior.copycoders.project.api.filters.JwtCsrfFilter;


@SpringBootApplication
public class ProjectApplication {


    public static void main(String[] args) {

        SpringApplication.run(ProjectApplication.class, args);
    }


//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtCsrfFilter(), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }


}
