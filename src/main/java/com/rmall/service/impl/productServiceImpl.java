package com.rmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.rmall.common.Const;
import com.rmall.common.ResponseCode;
import com.rmall.common.ServerResponse;
import com.rmall.dao.CategoryMapper;
import com.rmall.dao.ProductMapper;
import com.rmall.pojo.Category;
import com.rmall.pojo.Product;
import com.rmall.service.ICategoryService;
import com.rmall.service.IProductService;
import com.rmall.util.DateTimeUtil;
import com.rmall.util.PropertiesUtil;
import com.rmall.vo.ProductDetailVo;
import com.rmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class productServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 根据传进来的product决定是保存还是更新商品
     * */
    public ServerResponse saveOrUpdateProduct(Product product){
        if(product != null){
            //所有不为空的有关product的业务都放在这里面操作
            if(StringUtils.isNotBlank(product.getSubImages())){
                //在数据库里面sub_images的存放的所有子图的信息是以,隔开的
                String[] subImageArray = product.getSubImages().split(",");
                if(subImageArray.length > 0){
                    //为防止程序卡死在这一环节
                    product.setMainImage(subImageArray[0]);
                }
                if(product.getId()!=null){
                    //商品能获取到ID说明是在数据库中的
                    int updateResultCount = productMapper.updateByPrimaryKey(product);
                    if(updateResultCount > 0){
                        return ServerResponse.createBySuccess("更新商品成功");
                    }
                    return ServerResponse.createByError("更新商品失败");
                }else {
                    //没有获取到ID说明是新增的产品
                    int insertProductResult = productMapper.insert(product);
                    if(insertProductResult > 0){
                        return ServerResponse.createBySuccess("新增保存商品成功");
                    }
                    return ServerResponse.createByError("新增保存商品失败");
                }
            }
        }
        return ServerResponse.createByError("没有获取到商品输入信息，参数错误");
    }

    /**
     * 设置商品的状态信息，是上架还是下架处理
     * */
    public ServerResponse<String> setSaleStatus(Integer productId,Integer status){
        //先只是判断这两个参数是否为空，不然就是输入参数错误
        if(productId == null && status == null){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getCondition());
        }
        //既然是要设置商品的状态信息，说明是需要更改的，那么传入的这两个参数都需要更改
        Product productNew = new Product();
        productNew.setId(productId);
        productNew.setStatus(status);
        int updateSaleStatusResult = productMapper.updateByPrimaryKeySelective(productNew);
        if(updateSaleStatusResult > 0){
            return ServerResponse.createBySuccess("修改产品销售状态成功");
        }
        return ServerResponse.createByError("修改产品销售状态失败");
    }

    /**
     * 后台获取商品的信息
     * */
    public ServerResponse<ProductDetailVo> manageGetProductDetail(Integer productId){
        //空判断
        //入参空判断
        if(productId == null){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getCondition());
        }
        //商品存在性空判断
        Product productSelect = productMapper.selectByPrimaryKey(productId);
        if(productSelect == null){
            return ServerResponse.createByError("商品已下架、库中查不到这个商品");
        }
        //业务：组装返回给controller的VO对象
        ProductDetailVo productDetailVo = assembleProductDetailVo(productSelect);

        return  ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        //设置product对象的VO对象服务器的地址url
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://file.ren.com/"));
        //设置此商品的父级ID
        /**
         * 这里需要批注一下：
         * 我的表结构product 和 category
         * product有自己的Id,它的category_id代表的是它在目录里面属于的是什么种类，是id的上一级
         * 但是category_id也可能会有它的父级节点id，代表着最高级类目
         * */
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            //如果目录对象为空的话说明此商品本来就是最高级别的，所以默认为根节点
            productDetailVo.setParentCategoryId(0);
        }else{
            //如果category_id查得出来不为空，说明其父级别存在
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        //设置商品的创建和更新时间
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }

    /**
     * 后台动态获取商品列表
     * */
    public ServerResponse<PageInfo> manageGetProductList(int pageNum,int pageSize){
        //首先做startPage
        PageHelper.startPage(pageNum, pageSize);
        //在数据库中查询出商品列表，但是此时并不是VO列表
        List<Product> productList = productMapper.selectProductList();
        //创建返回给前台的productListVoList对象
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem : productList){
            ProductListVo productListVoTemp = assembleProductListVo(productItem);
            productListVoList.add(productListVoTemp);
        }
        //PageInfo在创建分页信息的时候要用从后台获取到的productList，但是要展示给前台的信息应该是productListVoList
        //所以需要在创建之后再对resultPage设置setList一下
        PageInfo resultPage = new PageInfo(productList);
        resultPage.setList(productListVoList);
        return ServerResponse.createBySuccess(resultPage);
    }

    //组装ProductListVo对象的方法
    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setName(product.getName());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        productListVo.setSubtitle(product.getSubtitle());

        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://file.ren.com/"));

        return productListVo;
    }

    /**
     * 搜索指定的商品
     * */
    public ServerResponse<PageInfo> searchProduct(String productName, String productId, int pageNum,int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        if(StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectProductByNameAndProductId(productName,productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem : productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 返回给前台指定商品的信息
     * */
    public ServerResponse<ProductDetailVo> frontGetProductDetail(Integer productId){
        //空判断
        //入参空判断
        if(productId == null){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getCondition());
        }
        //商品存在性空判断
        Product productSelect = productMapper.selectByPrimaryKey(productId);
        if(productSelect == null){
            return ServerResponse.createByError("商品已下架或者删除、库中查不到这个商品");
        }
        if(productSelect.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByError("商品已下架或者删除");
        }
        //业务：组装返回给controller的VO对象
        ProductDetailVo productDetailVo = assembleProductDetailVo(productSelect);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 返回给前台的商品信息列表
     * */
    public ServerResponse<PageInfo> frontGetProductListBykeywordAndCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy){
        if(StringUtils.isBlank(keyword) && categoryId == null){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getCondition());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();
        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category != null && StringUtils.isBlank(keyword)){
              //没有该分类，并且还没有关键字，这个时候返回一个空的结果集，不报错
                PageHelper.startPage(pageNum, pageSize);
                List<ProductDetailVo> productDetailVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productDetailVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.getCategoryIdAndDeepChildCategoryId(category.getId()).getData();
        }
        //这一步很重要，将字符串重新拼接，因为后面是要通过关键字来进行查询的
        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum, pageSize);
        //排序处理
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductPriceOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_");
                //这也是为什么我们在Const里面创建价格顺序表的时候，要price_desc和price_asc这样子
                //为了方便在pageHelper的时候，orderBy语句要使用
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product :productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
