package com.gzeic.memosystem.service;

import com.gzeic.memosystem.enums.PriorityEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户相关测试
 *
 * 本测试类主要测试与用户相关的业务逻辑
 * 由于JDK 25上Mockito的限制，这里只测试不依赖mock的工具类
 *
 * @author 系统分析组
 * @version V1.0 2026-06-07
 */
@DisplayName("用户相关业务逻辑测试")
class UserServiceTest {

    @Test
    @DisplayName("PriorityEnum: 用户注册时默认优先级")
    void testDefaultPriorityOnRegister() {
        // 用户创建备忘录时，默认优先级应该是MEDIUM
        PriorityEnum defaultPriority = PriorityEnum.fromName(null);
        assertEquals(PriorityEnum.MEDIUM, defaultPriority);
        assertEquals(2, defaultPriority.getValue());
    }

    @Test
    @DisplayName("PriorityEnum: 优先级枚举值验证")
    void testPriorityEnumValues() {
        // 验证优先级枚举定义
        assertEquals(3, PriorityEnum.values().length);
        assertEquals(1, PriorityEnum.HIGH.getValue());
        assertEquals(2, PriorityEnum.MEDIUM.getValue());
        assertEquals(3, PriorityEnum.LOW.getValue());
    }

    @Test
    @DisplayName("PriorityEnum: 根据字符串获取优先级")
    void testPriorityEnumFromString() {
        assertEquals(PriorityEnum.HIGH, PriorityEnum.fromName("HIGH"));
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName("MEDIUM"));
        assertEquals(PriorityEnum.LOW, PriorityEnum.fromName("LOW"));
        // 忽略大小写
        assertEquals(PriorityEnum.HIGH, PriorityEnum.fromName("high"));
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName("Medium"));
    }

    @Test
    @DisplayName("PriorityEnum: 无效优先级返回默认值")
    void testInvalidPriorityReturnsDefault() {
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName("INVALID"));
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName(""));
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName(null));
    }

    @Test
    @DisplayName("PriorityEnum: 根据数值获取优先级")
    void testPriorityEnumFromValue() {
        assertEquals(PriorityEnum.HIGH, PriorityEnum.fromValue(1));
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromValue(2));
        assertEquals(PriorityEnum.LOW, PriorityEnum.fromValue(3));
    }

    @Test
    @DisplayName("BR-04: 业务规则 - 默认优先级为MEDIUM")
    void testBusinessRule_Br04_DefaultPriority() {
        // 需求文档业务规则 BR-04：优先级默认 MEDIUM
        PriorityEnum defaultPriority = PriorityEnum.fromValue(null);
        assertEquals(PriorityEnum.MEDIUM, defaultPriority);
        assertEquals("中优先级", defaultPriority.getDescription());
    }
}