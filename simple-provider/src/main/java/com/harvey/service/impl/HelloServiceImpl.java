package com.harvey.service.impl;

import com.harvey.service.HelloService;

/**
 * @author Harvey Suen
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
