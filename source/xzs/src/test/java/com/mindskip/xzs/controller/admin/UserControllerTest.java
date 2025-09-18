package com.mindskip.xzs.controller.admin;

import com.mindskip.xzs.domain.User;
import com.mindskip.xzs.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)   //只测Controller本身逻辑，不受安全过滤器干扰
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private UserService userService;

    private static final Logger LOG= LoggerFactory.getLogger(UserControllerTest.class);

    //查询示范
    @Test
    @DisplayName("POST /api/admin/user/select/{id} 返回<UserResponseVM>")
    void selectByIdTest() throws Exception {
        //0。 准备Mock数据。如果隔离下层Service，则可以Mock依赖组件的行为，单纯测试Web行为
        User u= new User();
        u.setId(1);u.setUserName("student");u.setUserUuid(UUID.randomUUID().toString());
        Mockito.doReturn(u).when(userService).getUserById(1);
        //1.构造请求数据
        Integer id = 1;
        String jsonBody="";
        //2. 发起HTTP请求
        mockMvc.perform(post("/api/admin/user/select/{id}", id)  //请求URL
                        .contentType(MediaType.APPLICATION_JSON)          //请求Header
                        .content(jsonBody))                              //请求Body
                .andDo(print())                                 //Print  Messgage
                //响应消息返回结果校验
                .andExpect(status().isOk())                 //响应：状态码
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.response.id").value(id))
                .andExpect(jsonPath("$.response.userName").value("student"))
                .andExpect(jsonPath("$.response.userUuid").isNotEmpty())
        ;
    }

    @Test
    @DisplayName("POST /api/admin/user/edit 返回<User>")
    @Sql(scripts = "/sql/inituser.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void usereditTest() throws  Exception {
        //1.构造请求数据
        /*TODO: 请根据输入参数结构进行修改*/
        String userName="student";
        String realName="student";
        String password="123456";
        int role=3;
        int status =1;
        String jsonBody = String.format("{  \"userName\": \"%s\", \"realName\": \"%s\", \"password\": \"%s\", " +
                "\"role\": %d, \"status\": %d }", userName, realName, password,role,status);
        LOG.info("===POST /api/admin/user/edit：{}",jsonBody);

        //2.发送请求
        mockMvc.perform(post("/api/admin/user/edit")
                        .contentType(MediaType.APPLICATION_JSON)          //请求Header
                        .content(jsonBody))                              //请求Body
                .andDo(print())                                 //Print  Messgage
                //3. 验证返回结果：响应消息校验
                /*TODO: 请结合返回状态status().XXX进行修改。一些解释
                      - status().is2xxSuccessful()  -> 2xx Successful Responses
                      - status().is3xxRedirection() -> 3xx Redirection Messages
                      - status().is4xxClientError() -> 4xx Client Error Response
                      - status().is5xxServerError() -> 5xx Server Error Responses */
                .andExpect(status().isOk())                 //响应：状态码
                /*TODO: 具体字段校验,请根据实际情况修改*/
                .andExpect(jsonPath("$.code").value(2))
                .andExpect(jsonPath("$.message").value("用户已存在"))
        ;
    }

    @DisplayName("POST /api/admin/user/page/list 返回分页<UserResponseVM>")
    @ParameterizedTest
    @CsvFileSource( resources = "/userPage.csv", numLinesToSkip = 1)
    @Sql(scripts ="/sql/inituser.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "CALL insert_users(25)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void pageListTest_CSV(int pageIndex, int pageSize,int expectedPageNums, int expectedSize) throws Exception
    {
        //1.构造请求数据
        String  userName="MMM";
        Integer role=3;
        String jsonBody = String.format("{  \"pageIndex\": %d, \"pageSize\": %d, \"role\": %d, \"userName\": \"%s\" }",
                pageIndex, pageSize,role,userName);
        LOG.info("===POST /api/admin/user/page/list：{}",jsonBody);

        //2.发送请求
        mockMvc.perform(post("/api/admin/user/page/list")
                        .contentType(MediaType.APPLICATION_JSON)       //请求Header
                        .content(jsonBody))                            //请求Body
                .andDo(print())                                         //Print  Messgage
                //3. 验证返回结果：响应消息校验
                .andExpect(status().isOk())                 //响应：状态码
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.response.pageNum").value(expectedPageNums))
                .andExpect(jsonPath("$.response.pageSize").value(pageSize))
                .andExpect(jsonPath("$.response.size").value(expectedSize))
        ;
    }
    //TODO:补充更多测试函数

}