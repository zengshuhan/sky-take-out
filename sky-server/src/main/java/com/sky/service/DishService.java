package com.sky.service;

import com.sky.dto.DishDTO;
import org.springframework.stereotype.Service;


public interface DishService {
    /**
     * 新增菜品和对应的口味数据
     * @param dishDTO
     */
    void saveWithFlavour(DishDTO dishDTO);
}
