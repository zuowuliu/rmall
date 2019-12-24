package com.rmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rmall.common.ServerResponse;
import com.rmall.dao.CategoryMapper;
import com.rmall.pojo.Category;
import com.rmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author 大神爱吃茶
 * */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {


    @Autowired
    private CategoryMapper categoryMapper;

    //打印
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    /**
     * 增加分类节点
     * */
    public ServerResponse<String> addCategory(String categoryName,Integer parentId){
        //判断下传入的参数是否正确
        if(parentId == null && StringUtils.isBlank(categoryName)){
            return ServerResponse.createByError("添加分类的参数错误");
        }
        //创建并insert一个新的分类
        Category categoryNew = new Category();
        categoryNew.setName(categoryName);
        categoryNew.setParentId(parentId);
        int resultCount = categoryMapper.insert(categoryNew);
        if(resultCount > 0){
            return ServerResponse.createBySuccess("添加分类成功");
        }
        return ServerResponse.createByError("添加分类失败");
    }

    /**
     * 更新分类的名称
     * */
    public ServerResponse<String> updateCategoryName(Integer categoryId,String categoryName){
        if(categoryId == null && StringUtils.isBlank(categoryName)){
            return ServerResponse.createByError("更新分类的参数错误");
        }
        //创建并update一个新的分类
        Category categoryNew = new Category();
        categoryNew.setId(categoryId);
        categoryNew.setName(categoryName);
        int resultCount = categoryMapper.updateByPrimaryKeySelective(categoryNew);
        if(resultCount > 0){
            return ServerResponse.createBySuccess("更新分类成功");
        }
        return ServerResponse.createByError("更新分类失败");
    }

    /**
     * 获取子节点目录的信息
     * */
    public ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId){
        //查询子节点
        List<Category> childCategoryList = categoryMapper.selectChildCategoryByParentId(categoryId);
        //测试的结果是没有查到这个id下的子节点目录的话会返回一个null值
        if(CollectionUtils.isEmpty(childCategoryList)){
            //打印日志信息,而不是返回给前台一个空的list信息
            logger.error("未找到当前分类的子分类信息");
            return ServerResponse.createByError("没有找到有此分类，请重试");
        }
        return ServerResponse.createBySuccess(childCategoryList);
    }

    /**
     * 获取当前节点id，并递归其所有子节点的id
     * */

    //递归算法，算出所有的子节点(利用Set排重),同时需要先重写Category中的equals和hashcode方法
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            //这里面放的是父节点的目录对象
            categorySet.add(category);
        }
        //查找子节点，递归算法一定要有一个退出的条件
        List<Category> categoryList = categoryMapper.selectChildCategoryByParentId(categoryId);
        for(Category categoryItem : categoryList){
            //接下来就是遍历所有查询出来的子节点，并且将其加到categorySet中，这样子如果其中的某个节点包含的有子节点的话
            //又会重新递归进行一次遍历,并加到categorySet中去，因为又调用的是findChildCategory方法
            findChildCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }
    public ServerResponse<List<Integer>> getCategoryIdAndDeepChildCategoryId(Integer parentId){
        //利用的是Guava里面的Sets来初始化categorySet
        Set<Category> categorySet = Sets.newHashSet();
        //调用递归算法，返回一个categorySet
        findChildCategory(categorySet, parentId);

        List<Integer> categoryIdList = Lists.newArrayList();
        for(Category categoryItem : categorySet){
            categoryIdList.add(categoryItem.getId());
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }










}
