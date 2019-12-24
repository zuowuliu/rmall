package com.rmall.service;

import com.rmall.common.ServerResponse;
import com.rmall.pojo.Category;

import java.util.List;

public interface ICategoryService {
    ServerResponse<String> addCategory(String categoryName,Integer parentId);
    ServerResponse<String> updateCategoryName(Integer categoryId,String categoryName);
    ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId);
    ServerResponse<List<Integer>> getCategoryIdAndDeepChildCategoryId(Integer parentId);
}
