package com.mindskip.xzs.service.impl;

import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.domain.User;
import com.mindskip.xzs.exception.BusinessException;
import com.mindskip.xzs.repository.UserMapper;
import com.mindskip.xzs.service.UserService;
import com.mindskip.xzs.viewmodel.admin.user.UserPageRequestVM;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql(scripts ="/sql/inituser.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserServiceImplTest {
    @Autowired
    UserService  userService;
    @Autowired
    UserMapper userMapper;

    @Test
    void getUsersTest() {
        assertTrue(userService.getUsers().size()>0);
        assertEquals(2,userService.getUsers().size());
    }

    @Test
    void updateByIdFilterTest() {
        //1 构造数据
        String userNameUpt="student-Filter";
        User userMKM = new User(); userMKM.setId(1);userMKM.setUserName(userNameUpt);
        //2 验证比较
        assertEquals(1,userService.updateByIdFilter(userMKM));
        assertEquals(userNameUpt,userService.getUserById(1).getUserName());
    }

    @Test
    void updateByIdTest() {
        //1 构造数据
        String userNameUpt="student-Id";
        User userMKM = new User(); userMKM.setId(1);userMKM.setUserName(userNameUpt);
        //2 验证比较
        assertEquals(1,userService.updateById(userMKM));
        assertEquals(userNameUpt,userService.getUserById(1).getUserName());
        //未传入的参数被置为了null
        assertNull(userService.getUserById(1).getUserUuid());
    }

    @Test
    void insertByFilterTest() {
        //1 构造数据
        User userMKM = new User(); userMKM.setUserName("MMM");
        //2 验证比较
        assertEquals(1,userService.insertByFilter(userMKM));
        assertEquals("MMM",userService.getUserById(3).getUserName());
    }

    @Test
    @DisplayName("模拟部分插入失败，回滚的场景")
    void insertUsersTest() {
        //1 构造数据
        User userMMM = new User();
//        userMMM.setUserName("MMM");
//        userMMM.setRealName("MMM");
//        userMMM.setUserUuid(UUID.randomUUID().toString());
//        String encodePwd = authenticationService.pwdEncode("123456");
//        userMMM.setPassword(encodePwd);
//        userMMM.setCreateTime(new Date());
//        userMMM.setDeleted(false);
        User userMKM = new User();
        List<User> userList= Arrays.asList(userMMM,userMKM);
        Integer intPreCount=userMapper.selectAllCount();

        //2. 调用与比较
        Throwable e= assertThrows(BusinessException.class, ()->userService.insertUsers(userList));
        e.printStackTrace();
        assertEquals(intPreCount,userMapper.selectAllCount());
    }

    @Test
    @Sql(scripts ="/sql/inituser.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "CALL insert_users(15)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void userPagelistTest()
    {
        //1 构造数据
        String userName="MMM";
        Integer pageIndex=1;   //页下标：0,1,2...
        Integer pageSize=10;

        //2. 调用与比较
        List<User> users= userService.userPageList(userName,pageIndex,pageSize);
        for (User user: users)  System.out.println(user.getId());

        // 调试输出
        System.out.println("Total users found: " + users.size());

        // 断言应该包含数据
        assertFalse(users.isEmpty());
        assertTrue(users.size() > 0);
    }

    // 添加一个新的测试方法来测试分页对象
    @Test
    @Sql(scripts ="/sql/inituser.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "CALL insert_users(15)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void userPageListWithPageInfoTest() {
        // 测试第1页，每页10条
        String userName = "MMM";
        Integer pageIndex = 0;   // 第1页（从0开始）
        Integer pageSize = 10;

        List<User> users = userService.userPageList(userName, pageIndex, pageSize);

        // 调试输出
        System.out.println("Page 1 - Total users: " + users.size());
        for (User user : users) {
            System.out.println("User: " + user.getRealName() + " (ID: " + user.getId() + ")");
        }

        // 断言第1页应该有数据
        assertFalse(users.isEmpty());

        // 如果有足够数据，测试第2页
        if (users.size() == pageSize) {
            List<User> page2Users = userService.userPageList(userName, 1, pageSize);
            System.out.println("Page 2 - Total users: " + page2Users.size());
            assertFalse(page2Users.isEmpty());
        }
    }

    @Test
    @Sql(scripts ="/sql/inituser.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "CALL insert_users(25)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void userPageTest()
    {
        //1 构造数据
        UserPageRequestVM userVM = new UserPageRequestVM();
        userVM.setUserName("MMM");
        userVM.setRole(3);
        userVM.setPageIndex(3);    //页下标：1,2...
        userVM.setPageSize(10);

        //2. 调用与比较
        PageInfo<User> userPageInfo = userService.userPage(userVM);
        assertEquals(3,userPageInfo.getPageNum());
        assertEquals(25,userPageInfo.getTotal());
        assertEquals(21,userPageInfo.getStartRow());
        assertEquals(25,userPageInfo.getEndRow());
    }

    @ParameterizedTest
    @CsvFileSource( resources = "/userPage.csv", numLinesToSkip = 1)
    @Sql(scripts ="/sql/inituser.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "CALL insert_users(25)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void userPageTest_CSV(int pageIndex, int pageSize,int expectedPageNums, int expectedSize, int expectedStartRow,int expectedEndRow)
    {
        //1 构造数据
        UserPageRequestVM userVM = new UserPageRequestVM();
        userVM.setUserName("MMM");
        userVM.setRole(3);
        userVM.setPageIndex(pageIndex);    //页下标：1,2...
        userVM.setPageSize(pageSize);

        //2. 调用与比较
        PageInfo<User> userPageInfo = userService.userPage(userVM);
        assertEquals(expectedPageNums,userPageInfo.getPageNum());  //总页数
        assertEquals(expectedSize,userPageInfo.getSize());     //当前记录数
        assertEquals(expectedStartRow,userPageInfo.getStartRow());
        assertEquals(expectedEndRow,userPageInfo.getEndRow());
    }
    //TODO:补充更多测试函数
}