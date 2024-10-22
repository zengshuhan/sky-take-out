package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional//原子动作，要么全成功，要么全失败
    @Override
    public void saveWithFlavour(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //向菜品表插入一条数据
        dishMapper.insert(dish);
        //向口味表插入n条数据
        Long dishId=dish.getId();//获取insert语句生成的主键值，在dishmapper中处理的
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors!=null&& !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否删除--是否存在起售中的菜品
        for (long id:ids){
            Dish dish=dishMapper.getById(id);
            if (dish.getStatus()== StatusConstant.ENABLE){//当前菜品起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否删除--是否被套餐关联
        List<Long> setMealIds = setmealDishMapper.getSetMealIdsByDishIds(ids);
        if (setMealIds!=null&&setMealIds.size() > 0){//当前菜品被关联，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
//        for (long id:ids){
//            //删除菜品表中的菜品数量
//            dishMapper.deleteById(id);
//            //删除口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }//会发非常多sql语句 没必要

        //根据菜品id集合批量删除菜品数据
        dishMapper.deleteByIds(ids);
        //根据菜品id集合批量删除关联的菜品口味数据
        dishFlavorMapper.deleteByDishIds(ids);
    }
}
