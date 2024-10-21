package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/*
自定义切面，实现公共字段填充处理逻辑
切面=通知+切入点
 */
@Aspect
@Component
@Slf4j//方便加入日志
public class AutoFillAspect {
    /*
    切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")//这句话看不懂
    public void autoFillPointCut(){}

    //前置通知，在通知中进行公共字段赋值
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段自动填充");
        //signature是用来标识方法/类的东西，内部有其间的很多信息（MethodSignature是方法名+参数列表的sig）
        //获取到当前被拦截方法上db的操作类型
        //反射
        MethodSignature methodSignature=(MethodSignature)joinPoint.getSignature();//方法签名对象
        AutoFill autoFill=methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType=autoFill.value();
        //获取当前被拦截方法的参数---获得实体对象
        Object[] args=joinPoint.getArgs();
        if (args==null||args.length==0) return;
        Object entity=args[0];
        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();//LocalThread
        //通过反射，和不同的操作类型，为对象属性赋值
        if (operationType==OperationType.INSERT){
            Method setCreateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
            Method setCreateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
            Method setUpdateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

            setCreateTime.invoke(entity,now);
            setCreateUser.invoke(entity,currentId);
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);
        } else if (operationType == OperationType.UPDATE) {
            Method setUpdateTime=entity.getClass().getDeclaredMethod("setUpdateTime",LocalDateTime.class);
            Method setUpdateUser=entity.getClass().getDeclaredMethod("setUpdateUser",Long.class);

            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);
        }
    }
}
