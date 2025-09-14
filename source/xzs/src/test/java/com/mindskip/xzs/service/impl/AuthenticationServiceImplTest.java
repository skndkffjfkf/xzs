package com.mindskip.xzs.service.impl;

import com.mindskip.xzs.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
class AuthenticationServiceImplTest {
    @Autowired
    AuthenticationService authService;
    @Test
    void authUser() {
        String username="student";
        String password="123456";
        assertTrue(authService.authUser(username,password));
    }

    @ParameterizedTest
    @CsvFileSource(
            resources = "/authUser.csv",
            numLinesToSkip = 1,
            encoding = "UTF-8",           // 文件编码
            lineSeparator = "\n",         // 行分隔符
            delimiter = ','              // 字段分隔符
    )
    void authUserBatch(String username, String password, boolean expectResult)
    {
        boolean actualResult = authService.authUser(username, password);
        assertEquals(expectResult, actualResult);
    }



}