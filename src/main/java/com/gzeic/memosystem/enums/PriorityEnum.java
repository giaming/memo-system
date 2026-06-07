package com.gzeic.memosystem.enums;

/**
 * 备忘录优先级枚举
 *
 * 对应需求规格说明书中的业务规则 BR-04：
 * - 优先级分为 HIGH（高）、MEDIUM（中）、LOW（低）三个等级
 * - 默认值为 MEDIUM
 *
 * @author 系统分析组
 * @version V1.0 2026-06-07
 */
public enum PriorityEnum {

    /**
     * 高优先级
     */
    HIGH("高优先级", 1),

    /**
     * 中优先级（默认值）
     */
    MEDIUM("中优先级", 2),

    /**
     * 低优先级
     */
    LOW("低优先级", 3);

    /**
     * 优先级描述
     */
    private final String description;

    /**
     * 优先级数值（用于数据库存储兼容）
     */
    private final Integer value;

    PriorityEnum(String description, Integer value) {
        this.description = description;
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public Integer getValue() {
        return value;
    }

    /**
     * 根据数值获取枚举
     *
     * @param value 优先级数值
     * @return PriorityEnum 或 null
     */
    public static PriorityEnum fromValue(Integer value) {
        if (value == null) {
            return MEDIUM;
        }
        for (PriorityEnum priority : values()) {
            if (priority.value.equals(value)) {
                return priority;
            }
        }
        return MEDIUM;
    }

    /**
     * 根据枚举名称获取枚举（忽略大小写）
     *
     * @param name 枚举名称（如 HIGH、MEDIUM、LOW）
     * @return PriorityEnum 或 null
     */
    public static PriorityEnum fromName(String name) {
        if (name == null || name.isEmpty()) {
            return MEDIUM;
        }
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEDIUM;
        }
    }
}