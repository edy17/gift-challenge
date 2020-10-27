package org.diehl.wedoogift.presentation.security;

import org.diehl.wedoogift.domain.model.Endowments;
import org.diehl.wedoogift.domain.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private FileRepository<Endowments> endowmentsRepository;

    @Value("${data.input.file}")
    private String inputFileName;

    public SecurityConfig(FileRepository<Endowments> endowmentsRepository) {
        this.endowmentsRepository = endowmentsRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        Endowments inputEndowments = this.endowmentsRepository.searchByFilePath(inputFileName);
        inputEndowments.getCompanies().forEach(company -> {
            try {
                auth.inMemoryAuthentication()
                        .passwordEncoder(NoOpPasswordEncoder.getInstance())
                        .withUser(company.getName())
                        .password(company.getId().toString())
                        .roles(SecurityConstants.ROLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
