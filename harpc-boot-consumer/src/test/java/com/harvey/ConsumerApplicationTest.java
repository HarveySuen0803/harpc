package com.harvey;

import com.harvey.bootstrap.HarpcReference;
import com.harvey.service.HelloService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Harvey Suen
 */
@SpringBootTest
public class ConsumerApplicationTest {
    @HarpcReference
    private HelloService helloService;
    
    @Test
    public void consume() {
        System.out.println(helloService.sayHello("harvey"));
    }
}
