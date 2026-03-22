package com.ait.aitbackend.demo.service;

import org.springframework.stereotype.Service;

@Service
public class DemoService {
    public String hello(String name) {
        return "HELLO " + name;
    }
}
