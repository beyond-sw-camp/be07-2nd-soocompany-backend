//package com.soocompany.wodify.post.service;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class LikeInventoryService {
//    @Qualifier("3")
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    public LikeInventoryService(@Qualifier("3") RedisTemplate<String, Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    //  상품 등록 시 increaseStock 호출
//    public Long increaseStock(Long itemId, int quantity) {
//        return redisTemplate.opsForValue().increment(String.valueOf(itemId), quantity);//  반환값 : 잔량
//    }
//
//    //  주문 등록 시 decreaseStock 호출
//    public Long decreaseStock(Long itemId, int quantity) {
//        Object remains = redisTemplate.opsForValue().get(String.valueOf(itemId));
//        if (Integer.parseInt(remains.toString()) < quantity)
//            return -1L;
//        else
//            return redisTemplate.opsForValue().decrement(String.valueOf(itemId), quantity);//  반환값 : 잔량
//    }
//}