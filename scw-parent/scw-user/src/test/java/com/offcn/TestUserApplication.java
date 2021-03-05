package com.offcn;

import com.offcn.user.UserStartApplication;
import org.junit.Test;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserStartApplication.class)
public class TestUserApplication {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
    public  void TestRedis(){
        redisTemplate.boundValueOps("name").set("james");
        Object name = redisTemplate.boundValueOps("name").get();
        stringRedisTemplate.opsForValue().set("key","lucy");
        System.out.println("存入完毕");
        String key = stringRedisTemplate.opsForValue().get("key");
        System.out.println("key:"+key);
        System.out.println("name:"+name);
    }
}
