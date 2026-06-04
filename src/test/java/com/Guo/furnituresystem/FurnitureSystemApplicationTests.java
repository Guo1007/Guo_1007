package com.Guo.furnituresystem;

import com.Guo.furnituresystem.entity.pojo.Furniture;
import com.Guo.furnituresystem.service.Impl.FurnitureServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import static com.Guo.furnituresystem.utils.RedisConstants.CACHE_FURNITURE_KEY;

@SpringBootTest
class FurnitureSystemApplicationTests {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private FurnitureServiceImpl furnitureService;

    @Test
    void testHot() {
        Furniture furniture = furnitureService.getById(1L);
        furnitureService.setLogicalExpire(CACHE_FURNITURE_KEY + 1, furniture, 5L, TimeUnit.SECONDS);
    }

}
