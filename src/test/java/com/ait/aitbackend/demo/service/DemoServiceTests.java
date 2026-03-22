package com.ait.aitbackend.demo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DemoServiceTests {

    @Test
    void testHello(@Autowired DemoService demoService){
        assertThat(demoService.hello("World")).isEqualTo("HELLO World");
    }
}
