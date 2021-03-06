package com.rmall.dao;

import com.google.common.collect.Lists;
import com.rmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectProductList();

    List<Product> selectProductByNameAndProductId(@Param("productName") String productName, @Param("productId") String productId);

    List<Product> selectByNameAndCategoryIds(@Param("productName") String productName, @Param("categoryIdList") List<Integer> categoryIdList);

    //这里一定要用Integer，因为int无法为NULL，考虑到很多商品已经被删除的情况
    Integer selectStockByProductId(Integer id);
}