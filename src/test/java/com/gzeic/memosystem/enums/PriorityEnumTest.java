package com.gzeic.memosystem.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 优先级枚举单元测试
 *
 * 对应需求规格说明书中的 BR-04 业务规则测试
 *
 * @author 系统分析组
 * @version V1.0 2026-06-07
 */
@DisplayName("优先级枚举测试")
class PriorityEnumTest {

    @Test
    @DisplayName("BR-04: 验证枚举值定义")
    void testEnumValues() {
        assertEquals(3, PriorityEnum.values().length);
        assertNotNull(PriorityEnum.HIGH);
        assertNotNull(PriorityEnum.MEDIUM);
        assertNotNull(PriorityEnum.LOW);
    }

    @Test
    @DisplayName("BR-04: HIGH优先级 - 数值=1")
    void testHighPriority() {
        assertEquals(1, PriorityEnum.HIGH.getValue());
        assertEquals("高优先级", PriorityEnum.HIGH.getDescription());
    }

    @Test
    @DisplayName("BR-04: MEDIUM优先级 - 数值=2（默认值）")
    void testMediumPriority() {
        assertEquals(2, PriorityEnum.MEDIUM.getValue());
        assertEquals("中优先级", PriorityEnum.MEDIUM.getDescription());
    }

    @Test
    @DisplayName("BR-04: LOW优先级 - 数值=3")
    void testLowPriority() {
        assertEquals(3, PriorityEnum.LOW.getValue());
        assertEquals("低优先级", PriorityEnum.LOW.getDescription());
    }

    @ParameterizedTest
    @CsvSource({
        "1, HIGH",
        "2, MEDIUM",
        "3, LOW"
    })
    @DisplayName("fromValue: 根据数值获取枚举")
    void testFromValue(int value, String expectedName) {
        PriorityEnum result = PriorityEnum.fromValue(value);
        assertEquals(expectedName, result.name());
    }

    @Test
    @DisplayName("fromValue: null值返回MEDIUM")
    void testFromValue_Null() {
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromValue(null));
    }

    @Test
    @DisplayName("fromValue: 无效数值返回MEDIUM")
    void testFromValue_Invalid() {
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromValue(999));
    }

    @ParameterizedTest
    @CsvSource({
        "HIGH, HIGH",
        "high, HIGH",
        "MEDIUM, MEDIUM",
        "medium, MEDIUM",
        "LOW, LOW",
        "low, LOW"
    })
    @DisplayName("fromName: 根据名称获取枚举（忽略大小写）")
    void testFromName(String name, String expectedName) {
        PriorityEnum result = PriorityEnum.fromName(name);
        assertEquals(expectedName, result.name());
    }

    @Test
    @DisplayName("fromName: null值返回MEDIUM")
    void testFromName_Null() {
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName(null));
    }

    @Test
    @DisplayName("fromName: 空字符串返回MEDIUM")
    void testFromName_Empty() {
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName(""));
    }

    @Test
    @DisplayName("fromName: 无效名称返回MEDIUM")
    void testFromName_Invalid() {
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName("INVALID"));
    }

    @Test
    @DisplayName("BR-04: 默认值为MEDIUM")
    void testDefaultValue() {
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromValue(null));
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName(null));
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName(""));
        assertEquals(PriorityEnum.MEDIUM, PriorityEnum.fromName("INVALID"));
    }
}