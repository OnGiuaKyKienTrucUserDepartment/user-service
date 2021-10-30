package com.example.userservice.service;


import com.example.userservice.VO.Department;
import com.example.userservice.VO.ReponseTemplateVO;
import com.example.userservice.authen.UserPrincipal;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.saveAndFlush(user);
    }

    public UserPrincipal findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        UserPrincipal userPrincipal = new UserPrincipal();

        if (null != user) {
            Set<String> authorities = new HashSet<>();
            if (null != user.getRoles())

                user.getRoles().forEach(r -> {
                    authorities.add(r.getRoleKey());
                    r.getPermissions().forEach(
                            p -> authorities.add(p.getPermissionKey()));
                });

            userPrincipal.setUserId(user.getId());
            userPrincipal.setUsername(user.getUsername());
            userPrincipal.setPassword(user.getPassword());
            userPrincipal.setAuthorities(authorities);

        }

        return userPrincipal;

    }

    @Autowired
    private RestTemplate restTemplate;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    //@Retry(name ="basic")
    @RateLimiter(name ="basicExample")
    public ReponseTemplateVO getUserWithDepartment(Long userId) {
        ReponseTemplateVO vo = new ReponseTemplateVO();
        User user = userRepository.findById(userId).get();
        vo.setUser(user);
        Department department =
                restTemplate.getForObject("http://localhost:9001/department/"
                                + user.getId(),
                        Department.class);

        vo.setDepartment(department);

        return vo;
    }
}
