package com.harvey.service.impl;

import com.harvey.bootstrap.HarpcService;
import com.harvey.service.HelloService;
import org.springframework.stereotype.Service;

/**
 * @author Harvey Suen
 */
@Service
@HarpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
