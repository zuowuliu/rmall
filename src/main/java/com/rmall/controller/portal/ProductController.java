package com.rmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.rmall.common.ServerResponse;
import com.rmall.service.IProductService;
import com.rmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
     * (将请求改成RESTFUL风格之后，detail.do也能访问的到)
     * */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> frontGetProductDetail(Integer productId){
        return iProductService.frontGetProductDetail(productId);
    }

    @RequestMapping(value = "/{productId}",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> frontGetProductDetailRESTful(@PathVariable("productId") Integer productId){
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

    //将.do的方法的@RequestMapping改正为@PathVarible(不好)
    @RequestMapping(value="/{keyword}/{categoryId}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> frontGetProductListRESTful(@PathVariable(value = "keyword")String keyword,
                                                @PathVariable(value = "categoryId")Integer categoryId,
                                                @PathVariable(value = "pageNum") Integer pageNum,
                                                @PathVariable(value = "pageSize") Integer pageSize,
                                                @PathVariable(value = "orderBy") String orderBy){

        if(pageNum == null){
            pageNum = 1;
        }
        if(pageSize == null){
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)){
            orderBy = "price_asc";
        }

        return iProductService.frontGetProductListBykeywordAndCategory(keyword,categoryId,pageNum,pageSize,orderBy);
    }

    //http://localhost:8080/product/keyword/手机/1/10/price_asc/
    @RequestMapping(value = "/keyword/{keyword}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> listRESTful(@PathVariable(value = "keyword")String keyword,
                                                @PathVariable(value = "pageNum") Integer pageNum,
                                                @PathVariable(value = "pageSize") Integer pageSize,
                                                @PathVariable(value = "orderBy") String orderBy){
        if(pageNum == null){
            pageNum = 1;
        }
        if(pageSize == null){
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)){
            orderBy = "price_asc";
        }

        return iProductService.frontGetProductListBykeywordAndCategory(keyword,null,pageNum,pageSize,orderBy);
    }

    //http://localhost:8080/product/category/100012/1/10/price_asc/
    @RequestMapping(value = "/category/{categoryId}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> listRESTful(@PathVariable(value = "categoryId")Integer categoryId,
                                                @PathVariable(value = "pageNum") Integer pageNum,
                                                @PathVariable(value = "pageSize") Integer pageSize,
                                                @PathVariable(value = "orderBy") String orderBy){
        if(pageNum == null){
            pageNum = 1;
        }
        if(pageSize == null){
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)){
            orderBy = "price_asc";
        }

        return iProductService.frontGetProductListBykeywordAndCategory("",categoryId,pageNum,pageSize,orderBy);
    }

}
