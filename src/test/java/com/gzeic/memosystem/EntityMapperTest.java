package com.gzeic.memosystem;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzeic.memosystem.entity.Memo;
import com.gzeic.memosystem.entity.User;
import com.gzeic.memosystem.mapper.MemoMapper;
import com.gzeic.memosystem.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class EntityMapperTest {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MemoMapper memoMapper;

    @Test
    public void testUserMapper() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");
        userMapper.insert(user);

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUsername, "testuser");
        User queryUser = userMapper.selectOne(userLambdaQueryWrapper);
        System.out.println("根据用户名查询：" + queryUser.getUsername());

        userMapper.deleteById(queryUser);
    }

    @Test
    public void testMemoMapper() {
        Memo memo = new Memo();
        memo.setTitle("测试备忘录");
        memo.setContent("这是一个测试备忘录");
        memo.setPriority(1);
        memo.setIsCompleted(false);
        memo.setDueDate(LocalDateTime.now().plusDays(1));
        memo.setUserId(1L);
        memoMapper.insert(memo);
        System.out.println("插入备忘录成功：" + memo.getId());
    }
}
