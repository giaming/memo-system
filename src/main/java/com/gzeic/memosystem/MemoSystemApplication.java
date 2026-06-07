package com.gzeic.memosystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MemoSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemoSystemApplication.class, args);
        System.out.println("==========================");
        System.out.println("Memo备忘录启动成功！");
        System.out.println("访问地址：http://localhost:8080");
        System.out.println("==========================");
    }

}
