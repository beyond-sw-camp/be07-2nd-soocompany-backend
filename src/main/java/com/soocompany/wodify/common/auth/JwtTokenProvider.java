package com.soocompany.wodify.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private int expiration;

    public String createToken(String memberId, String role){
        //claims는 사용자 정보(페이로드 정보)
        Claims claims = Jwts.claims().setSubject(memberId);
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+ expiration*60*1000L)) //만료시간 30분 세팅
                .signWith(SignatureAlgorithm.HS256, secretKey)  //암호화할건데 secret key 넣음.
                .compact();
        return token;

    }
}