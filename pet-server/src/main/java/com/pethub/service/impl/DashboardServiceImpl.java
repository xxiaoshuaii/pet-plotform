package com.pethub.service.impl;

import com.pethub.pojo.query.CategoryQuery;
import com.pethub.pojo.query.OrderQuery;
import com.pethub.pojo.query.PetQuery;
import com.pethub.pojo.query.PostQuery;
import com.pethub.pojo.query.UserQuery;
import com.pethub.pojo.vo.CategoryPieItemVO;
import com.pethub.pojo.vo.CategoryVO;
import com.pethub.pojo.vo.DashboardOverviewVO;
import com.pethub.pojo.vo.NoticeVO;
import com.pethub.pojo.vo.OrderTrendItemVO;
import com.pethub.pojo.vo.OrderVO;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PetVO;
import com.pethub.pojo.vo.PostVO;
import com.pethub.pojo.vo.UserVO;
import com.pethub.service.CategoryService;
import com.pethub.service.DashboardService;
import com.pethub.service.NoticeService;
import com.pethub.service.OrderService;
import com.pethub.service.PetService;
import com.pethub.service.PostService;
import com.pethub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int LARGE_PAGE_SIZE = 1000;
    private static final int RECENT_SIZE = 4;
    private static final int NOTICE_SIZE = 5;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final UserService userService;
    private final PetService petService;
    private final PostService postService;
    private final OrderService orderService;
    private final NoticeService noticeService;
    private final CategoryService categoryService;

    @Override
    public DashboardOverviewVO getOverview() {
        PageResultVO<UserVO> userPage = userService.page(buildUserQuery(1));
        PageResultVO<PetVO> petPage = petService.page(buildPetQuery(LARGE_PAGE_SIZE));
        PageResultVO<PostVO> postPage = postService.page(buildPostQuery(LARGE_PAGE_SIZE));
        PageResultVO<OrderVO> orderPage = orderService.page(buildOrderQuery(LARGE_PAGE_SIZE));

        List<PetVO> pets = defaultList(petPage.getRecords());
        List<PostVO> posts = defaultList(postPage.getRecords());
        List<OrderVO> orders = defaultList(orderPage.getRecords());
        LocalDate today = LocalDate.now();

        return new DashboardOverviewVO(
                defaultLong(userPage.getTotal()),
                defaultLong(petPage.getTotal(), pets.size()),
                defaultLong(postPage.getTotal(), posts.size()),
                defaultLong(orderPage.getTotal(), orders.size()),
                (long) pets.stream().filter(item -> item.getStatus() != null && item.getStatus() == 1).count(),
                (long) posts.stream().filter(item -> item.getStatus() != null && item.getStatus() == 0).count(),
                (long) orders.stream().filter(item -> isSameDay(item.getCreateTime(), today)).count()
        );
    }

    @Override
    public List<OrderTrendItemVO> getOrderTrend() {
        return buildOrderTrend(defaultList(orderService.page(buildOrderQuery(LARGE_PAGE_SIZE)).getRecords()), LocalDate.now());
    }

    @Override
    public List<CategoryPieItemVO> getCategoryPie() {
        List<PetVO> pets = defaultList(petService.page(buildPetQuery(LARGE_PAGE_SIZE)).getRecords());
        List<CategoryVO> categories = defaultList(categoryService.page(buildCategoryQuery(LARGE_PAGE_SIZE)).getRecords());
        return buildCategoryPie(pets, categories);
    }

    @Override
    public List<OrderVO> getRecentOrders() {
        return limitList(defaultList(orderService.page(buildOrderQuery(LARGE_PAGE_SIZE)).getRecords()), RECENT_SIZE);
    }

    @Override
    public List<PostVO> getRecentPosts() {
        return limitList(defaultList(postService.page(buildPostQuery(LARGE_PAGE_SIZE)).getRecords()), RECENT_SIZE);
    }

    @Override
    public List<NoticeVO> getLatestNotices() {
        return limitList(defaultList(noticeService.list()), NOTICE_SIZE);
    }

    private UserQuery buildUserQuery(int pageSize) {
        UserQuery query = new UserQuery();
        query.setPageNum(1);
        query.setPageSize(pageSize);
        return query;
    }

    private PetQuery buildPetQuery(int pageSize) {
        PetQuery query = new PetQuery();
        query.setPageNum(1);
        query.setPageSize(pageSize);
        return query;
    }

    private PostQuery buildPostQuery(int pageSize) {
        PostQuery query = new PostQuery();
        query.setPageNum(1);
        query.setPageSize(pageSize);
        return query;
    }

    private OrderQuery buildOrderQuery(int pageSize) {
        OrderQuery query = new OrderQuery();
        query.setPageNum(1);
        query.setPageSize(pageSize);
        return query;
    }

    private CategoryQuery buildCategoryQuery(int pageSize) {
        CategoryQuery query = new CategoryQuery();
        query.setPageNum(1);
        query.setPageSize(pageSize);
        return query;
    }

    private List<OrderTrendItemVO> buildOrderTrend(List<OrderVO> orders, LocalDate today) {
        Map<LocalDate, OrderTrendItemVO> trendMap = new LinkedHashMap<>();
        for (int index = 6; index >= 0; index--) {
            LocalDate day = today.minusDays(index);
            trendMap.put(day, new OrderTrendItemVO(day.format(DAY_FORMATTER), 0));
        }

        for (OrderVO order : orders) {
            LocalDateTime createTime = order.getCreateTime();
            if (createTime == null) {
                continue;
            }
            OrderTrendItemVO trendItemVO = trendMap.get(createTime.toLocalDate());
            if (trendItemVO != null) {
                trendItemVO.setValue(trendItemVO.getValue() + 1);
            }
        }

        return new ArrayList<>(trendMap.values());
    }

    private List<CategoryPieItemVO> buildCategoryPie(List<PetVO> pets, List<CategoryVO> categories) {
        Map<Long, String> categoryMap = new HashMap<>();
        for (CategoryVO category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }

        Map<String, Integer> counter = new LinkedHashMap<>();
        for (PetVO pet : pets) {
            String categoryName = pet.getCategoryName();
            if (categoryName == null || categoryName.isBlank()) {
                categoryName = categoryMap.getOrDefault(pet.getCategoryId(), "未分类");
            }
            counter.put(categoryName, counter.getOrDefault(categoryName, 0) + 1);
        }

        List<CategoryPieItemVO> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            result.add(new CategoryPieItemVO(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private boolean isSameDay(LocalDateTime dateTime, LocalDate date) {
        return dateTime != null && dateTime.toLocalDate().isEqual(date);
    }

    private <T> List<T> limitList(List<T> source, int size) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(source.subList(0, Math.min(size, source.size())));
    }

    private <T> List<T> defaultList(List<T> source) {
        return source == null ? new ArrayList<>() : source;
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private Long defaultLong(Long value, int fallback) {
        return value == null ? (long) fallback : value;
    }
}