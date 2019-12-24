package com.rmall.service;

import com.github.pagehelper.PageInfo;
import com.rmall.common.ServerResponse;
import com.rmall.pojo.Product;
import com.rmall.vo.ProductDetailVo;

public interface IProductService {

    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> manageGetProductDetail(Integer productId);

    ServerResponse<PageInfo> manageGetProductList(int pageNum,int pageSize);

    ServerResponse<PageInfo> searchProduct(String productName, String productId, int pageNum,int pageSize);

    ServerResponse<ProductDetailVo> frontGetProductDetail(Integer productId);//前台获取商品的详细信息

    ServerResponse<PageInfo> frontGetProductListBykeywordAndCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy);
}