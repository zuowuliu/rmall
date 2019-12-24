package com.rmall.controller.portal;

import com.rmall.common.ServerResponse;
import com.rmall.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/7 0007 下午 16:36
 */
@Controller
@RequestMapping(value = "/product/")
public class ProductController {


    @Autowired
    private IProductService iProductService;

    /**
     * 前台接口
     * 获取商品的信息给用户
     * */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse frontGetProductDetail(Integer productId){
        return iProductService.frontGetProductDetail(productId);
    }

    /**
     * 前台接口
     * 获取商品的信息列表给用户
     * */
    @RequestMapping(value="list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse frontGetProductList(@RequestParam(value = "keyword",required = false)String keyword,
                                         @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                         @RequestParam(value = "orderBy",defaultValue = "") String orderBy){
        return iProductService.frontGetProductListBykeywordAndCategory(keyword,categoryId,pageNum,pageSize,orderBy);
    }
}
