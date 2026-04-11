package com.pethub.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Aspect
@Component
public class AutoFillTimeAspect {

    @Before("execution(* com.pethub.mapper..*.insert*(..))")
    public void autoFillForInsert(JoinPoint joinPoint) {
        LocalDateTime now = LocalDateTime.now();
        for (Object arg : joinPoint.getArgs()) {
            fillField(arg, "createTime", now, false);
            fillField(arg, "updateTime", now, false);
        }
    }

    @Before("execution(* com.pethub.mapper..*.update*(..))")
    public void autoFillForUpdate(JoinPoint joinPoint) {
        LocalDateTime now = LocalDateTime.now();
        for (Object arg : joinPoint.getArgs()) {
            fillField(arg, "updateTime", now, true);
        }
    }

    private void fillField(Object target, String fieldName, LocalDateTime value, boolean overwrite) {
        if (target == null) {
            return;
        }

        Field field = findField(target.getClass(), fieldName);
        if (field == null || !LocalDateTime.class.equals(field.getType())) {
            return;
        }

        try {
            field.setAccessible(true);
            Object currentValue = field.get(target);
            if (!overwrite && currentValue != null) {
                return;
            }
            field.set(target, value);
        } catch (IllegalAccessException ignored) {
            // 字段不存在或无法访问时直接跳过，避免影响正常业务流程。
        }
    }

    private Field findField(Class<?> type, String fieldName) {
        Class<?> current = type;
        Set<Class<?>> visited = new HashSet<>();
        while (current != null && visited.add(current)) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
