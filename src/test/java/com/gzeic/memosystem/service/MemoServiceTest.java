package com.gzeic.memosystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gzeic.memosystem.dto.DailySummaryResponse;
import com.gzeic.memosystem.dto.MemoPageRequest;
import com.gzeic.memosystem.dto.MemoRequest;
import com.gzeic.memosystem.entity.Memo;
import com.gzeic.memosystem.enums.PriorityEnum;
import com.gzeic.memosystem.exception.BusinessException;
import com.gzeic.memosystem.service.impl.MemoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 备忘录服务单元测试
 *
 * 对应需求规格说明书中的 UC-03 至 UC-10 测试用例
 *
 * @author 系统分析组
 * @version V1.0 2026-06-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("备忘录服务单元测试")
class MemoServiceTest {

    @Mock
    private com.gzeic.memosystem.mapper.MemoMapper memoMapper;

    @Mock
    private com.gzeic.memosystem.mq.MemoNotifyProducer memoNotifyProducer;

    @InjectMocks
    private MemoServiceImpl memoService;

    private Memo testMemo;
    private MemoRequest testRequest;
    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        // 创建测试备忘录
        testMemo = new Memo();
        testMemo.setId(1L);
        testMemo.setTitle("测试备忘录");
        testMemo.setContent("测试内容");
        testMemo.setUserId(testUserId);
        testMemo.setPriority(2); // MEDIUM
        testMemo.setIsCompleted(false);
        testMemo.setIsNotified(false);
        testMemo.setCreateTime(LocalDateTime.now());
        testMemo.setUpdateTime(LocalDateTime.now());

        // 创建测试请求
        testRequest = new MemoRequest();
        testRequest.setTitle("新备忘录");
        testRequest.setContent("新内容");
        testRequest.setPriority("HIGH");
        testRequest.setDueDate(LocalDate.now().plusDays(7));
    }

    @Test
    @DisplayName("UC-03: 创建备忘录 - 成功")
    void testCreateMemo_Success() {
        // 模拟mapper插入
        when(memoMapper.insert(any(Memo.class))).thenReturn(1);

        // 执行创建
        Memo result = memoService.createMemo(testRequest, testUserId);

        // 验证
        assertNotNull(result);
        assertEquals("新备忘录", result.getTitle());
        assertEquals("新内容", result.getContent());
        assertEquals(PriorityEnum.HIGH.getValue(), result.getPriority());
        assertEquals(testUserId, result.getUserId());
        assertFalse(result.getIsCompleted());
        assertFalse(result.getIsNotified());

        verify(memoMapper, times(1)).insert(any(Memo.class));
    }

    @Test
    @DisplayName("UC-03: 创建备忘录 - 默认优先级为MEDIUM")
    void testCreateMemo_DefaultPriority() {
        MemoRequest request = new MemoRequest();
        request.setTitle("测试");
        request.setContent("内容");
        // 不设置优先级

        when(memoMapper.insert(any(Memo.class))).thenReturn(1);

        Memo result = memoService.createMemo(request, testUserId);

        assertEquals(PriorityEnum.MEDIUM.getValue(), result.getPriority());
    }

    @Test
    @DisplayName("UC-05: 修改备忘录 - 成功")
    void testUpdateMemo_Success() {
        // 模拟查找
        when(memoMapper.selectById(1L)).thenReturn(testMemo);
        when(memoMapper.updateById(any(Memo.class))).thenReturn(1);

        MemoRequest updateRequest = new MemoRequest();
        updateRequest.setTitle("更新标题");
        updateRequest.setContent("更新内容");
        updateRequest.setPriority("LOW");
        updateRequest.setDueDate(LocalDate.now().plusDays(10));

        Memo result = memoService.updateMemo(1L, updateRequest, testUserId);

        assertNotNull(result);
        assertEquals("更新标题", result.getTitle());
        assertEquals("更新内容", result.getContent());
        assertEquals(PriorityEnum.LOW.getValue(), result.getPriority());

        verify(memoMapper, times(1)).updateById(any(Memo.class));
    }

    @Test
    @DisplayName("UC-05: 修改备忘录 - 备忘录不存在")
    void testUpdateMemo_NotFound() {
        when(memoMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            memoService.updateMemo(999L, testRequest, testUserId);
        });
    }

    @Test
    @DisplayName("UC-05: 修改备忘录 - 无权操作")
    void testUpdateMemo_NoPermission() {
        when(memoMapper.selectById(1L)).thenReturn(testMemo);

        assertThrows(BusinessException.class, () -> {
            memoService.updateMemo(1L, testRequest, 999L); // 不同的用户ID
        });
    }

    @Test
    @DisplayName("UC-06: 删除备忘录 - 成功")
    void testDeleteMemo_Success() {
        when(memoMapper.selectById(1L)).thenReturn(testMemo);
        when(memoMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> memoService.deleteMemo(1L, testUserId));

        verify(memoMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("UC-06: 删除备忘录 - 备忘录不存在")
    void testDeleteMemo_NotFound() {
        when(memoMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            memoService.deleteMemo(999L, testUserId);
        });
    }

    @Test
    @DisplayName("UC-07: 设置完成状态 - 标记为已完成")
    void testSetCompleted_True() {
        when(memoMapper.selectById(1L)).thenReturn(testMemo);
        when(memoMapper.updateById(any(Memo.class))).thenReturn(1);

        Memo result = memoService.setCompleted(1L, true, testUserId);

        assertTrue(result.getIsCompleted());
    }

    @Test
    @DisplayName("UC-07: 设置完成状态 - 标记为未完成")
    void testSetCompleted_False() {
        testMemo.setIsCompleted(true);
        when(memoMapper.selectById(1L)).thenReturn(testMemo);
        when(memoMapper.updateById(any(Memo.class))).thenReturn(1);

        Memo result = memoService.setCompleted(1L, false, testUserId);

        assertFalse(result.getIsCompleted());
    }

    @Test
    @DisplayName("UC-04: 分页查询 - 基本分页")
    void testPageMemos_Basic() {
        // 模拟分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Memo> mockPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        mockPage.setRecords(List.of(testMemo));
        mockPage.setTotal(1);
        mockPage.setPages(1);

        when(memoMapper.selectPage(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class), any()))
            .thenReturn(mockPage);

        MemoPageRequest request = new MemoPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        IPage<Memo> result = memoService.pageMemos(request, testUserId);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("UC-04: 分页查询 - 按优先级筛选")
    void testPageMemos_FilterByPriority() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Memo> mockPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        mockPage.setRecords(List.of(testMemo));
        mockPage.setTotal(1);
        mockPage.setPages(1);

        when(memoMapper.selectPage(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class), any()))
            .thenReturn(mockPage);

        MemoPageRequest request = new MemoPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setPriority("HIGH");

        IPage<Memo> result = memoService.pageMemos(request, testUserId);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("UC-04: 分页查询 - 按完成状态筛选")
    void testPageMemos_FilterByCompleted() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Memo> mockPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        mockPage.setRecords(List.of(testMemo));
        mockPage.setTotal(1);
        mockPage.setPages(1);

        when(memoMapper.selectPage(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class), any()))
            .thenReturn(mockPage);

        MemoPageRequest request = new MemoPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setCompleted(false);

        IPage<Memo> result = memoService.pageMemos(request, testUserId);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("UC-04: 分页查询 - 按标题模糊查询")
    void testPageMemos_SearchByTitle() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Memo> mockPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        mockPage.setRecords(List.of(testMemo));
        mockPage.setTotal(1);
        mockPage.setPages(1);

        when(memoMapper.selectPage(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class), any()))
            .thenReturn(mockPage);

        MemoPageRequest request = new MemoPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setTitle("测试");

        IPage<Memo> result = memoService.pageMemos(request, testUserId);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("UC-08: 每日待办汇总 - 基本统计")
    void testGetDailySummary_Basic() {
        // 模拟各类型统计
        when(memoMapper.selectCount(any())).thenReturn(5L);

        DailySummaryResponse result = memoService.getDailySummary(testUserId);

        assertNotNull(result);
        assertEquals(5, result.getTodayTotal());
        assertEquals(5, result.getCompleted());
        assertEquals(5, result.getUncompleted());
        assertEquals(5, result.getHighPriority());
        assertEquals(5, result.getOverdue());
        assertNotNull(result.getSummaryDate());
    }

    @Test
    @DisplayName("UC-08: 每日待办汇总 - 统计所有指标")
    void testGetDailySummary_AllMetrics() {
        // 模拟不同的计数
        when(memoMapper.selectCount(any())).thenReturn(10L);

        DailySummaryResponse result = memoService.getDailySummary(testUserId);

        assertNotNull(result);
        assertTrue(result.getTodayTotal() >= 0);
        assertTrue(result.getCompleted() >= 0);
        assertTrue(result.getUncompleted() >= 0);
        assertTrue(result.getHighPriority() >= 0);
        assertTrue(result.getOverdue() >= 0);
    }

    @Test
    @DisplayName("获取备忘录详情 - 成功")
    void testGetMemoById_Success() {
        when(memoMapper.selectById(1L)).thenReturn(testMemo);

        Memo result = memoService.getMemoById(1L, testUserId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试备忘录", result.getTitle());
    }

    @Test
    @DisplayName("获取备忘录详情 - 无权查看")
    void testGetMemoById_NoPermission() {
        when(memoMapper.selectById(1L)).thenReturn(testMemo);

        assertThrows(BusinessException.class, () -> {
            memoService.getMemoById(1L, 999L);
        });
    }

    @Test
    @DisplayName("切换完成状态 - 切换为已完成")
    void testToggleComplete_ToCompleted() {
        when(memoMapper.selectById(1L)).thenReturn(testMemo);
        when(memoMapper.updateById(any(Memo.class))).thenReturn(1);

        Memo result = memoService.toggleComplete(1L, testUserId);

        assertTrue(result.getIsCompleted());
    }

    @Test
    @DisplayName("切换完成状态 - 切换为未完成")
    void testToggleComplete_ToUncompleted() {
        testMemo.setIsCompleted(true);
        when(memoMapper.selectById(1L)).thenReturn(testMemo);
        when(memoMapper.updateById(any(Memo.class))).thenReturn(1);

        Memo result = memoService.toggleComplete(1L, testUserId);

        assertFalse(result.getIsCompleted());
    }
}