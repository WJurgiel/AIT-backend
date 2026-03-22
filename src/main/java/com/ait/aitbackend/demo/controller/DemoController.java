package com.ait.aitbackend.demo.controller;

import com.ait.aitbackend.demo.service.DemoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/test")
@AllArgsConstructor
public class DemoController {

    private DemoService demoService;

    @GetMapping("/hello/{name}")
    @ResponseBody
    public String hello(@PathVariable String name) {
        return demoService.hello(name);
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello2(@RequestParam String name) {
        return demoService.hello(name);
    }



}
