package com.rmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.rmall.common.Const;
import com.rmall.common.ResponseCode;
import com.rmall.common.ServerResponse;
import com.rmall.dao.CartMapper;
import com.rmall.dao.ProductMapper;
import com.rmall.pojo.Cart;
import com.rmall.pojo.Product;
import com.rmall.service.ICartService;
import com.rmall.util.BigDecimalUtil;
import com.rmall.util.PropertiesUtil;
import com.rmall.vo.CartProductVo;
import com.rmall.vo.CartVo;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/9 0009 上午 9:38
 */
@Service(value = "iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加购物车
     * */
    public ServerResponse<CartVo> add(Integer userId,Integer count ,Integer productId){
        //先进行参数的空判断
        if(productId==null||count==null){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getCondition());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if(cart == null){
            //说明这个用户的购物车里面没有这个商品，需要新增这个商品的信息
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            //添加进购物车的商品默认是选中状态的
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            //这里的setUserId满足了第一次的注册的用户用他的id去查的时候肯定找不出来cart信息，
            //所以需要新增一个cart数据，所以需要把当前的userId设置进去
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        }else {
            //如果不为空，说明此产品已经在该用户的购物车里面了
            count = count + cart.getQuantity();
            cart.setQuantity(count);
            //当查得到这个商品所以需要更新其数量
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        //由于已经在list()方法(即在查找详情方法里面已经封装好了)里面封装好了需要判断是否超出购买限制的业务逻辑
        return this.list(userId);
    }

    /**
     * 更新商品的数量接口，用于更新购物车里面某一种商品的数量
     * 不是添加
     * */
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        //先进行参数的空判断
        if(productId==null||count==null){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getCondition());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if(cart!=null){
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        //但是照样还是需要确定是否需要限制购买
        return this.list(userId);
    }

    /**
     * 删除商品接口
     * */
    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds) {
        //利用Guava提供的方法来实现将字符串分割并自动转成一个数组List
        /**
         * 不然我们一般使用的方法是先split之后转为数组再遍历数组保存到list中
         * */
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getCondition());
        }else {
            cartMapper.deleteByUserIdProductIds(userId,productList);
            //但是照样还是需要确定是否需要限制购买
            return this.list(userId);
        }
    }

    /**
     * 查找商品的接口，返回购物车详情
     * */
    public ServerResponse<CartVo> list(Integer userId) {
        //先进行参数的空判断
        if(userId==null){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getCondition());
        }
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

/************************************************************/

    /**
     * 全选或者全不选
     * 指定某一种商品的全选或者全不选
     * */
    //根据传进来的参数来指定是哪一种选择类型
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, int checked) {
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return this.list(userId);
    }

    /**
     * 获取当前用户的购物车的商品数量
     * */
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        if(userId==null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    //限制购物车添加和购买的数量大于库存的情况
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        Cart cart = new Cart();//新new一个购物车对象
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);//根据用户id查询他所有的购物车数据记录信息，每条对应一个商品(数据库里)
        //返回前台的购物车商品列表,但是最后要封装成CartVo对象的列表才行
        List<CartProductVo> cartPoductVoList = Lists.newArrayList();
        //浮点运算的时候容易导致精度丢失的问题,使用BigDecimal，但是使用时一定要使用它的String构造器
        // 下面这个就是在初始化整个购物车的总价
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if(CollectionUtils.isNotEmpty(cartList)){
            for(Cart cartItem : cartList){
                //以下的这四条是购物车和购物车商品对象共同拥有的
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if(product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存,限制商品购买的有效数量
                    int buyLimitCount = 0;
                    //cartItem.getQuantity()这种都是购物车中指定商品的数量
                    if(product.getStock() >= cartItem.getQuantity()){
                        //库存充足的时候也需要赋值buyLimitCount,因为使用的是buyLimitCount来返回VO对象
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                        buyLimitCount = cartItem.getQuantity();
                    }else{
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        buyLimitCount=product.getStock();
                        //在购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价(当前购物车中某一个产品的总价)
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                    if(cartItem.getChecked()==Const.Cart.CHECKED){
                        //如果是已经被勾选了的状态就将此商品的总价添加到总的购物车
                        cartTotalPrice=BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                    }
                    cartPoductVoList.add(cartProductVo);
                }
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartPoductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }
    private boolean getAllCheckedStatus(Integer userId){
        if(userId==null){
            return  false;
        }
        int result = cartMapper.selectCartProductCheckedStatusByUserId(userId);
        if(result == 0){
            return true;
        }else {
            return false;
        }
    }
}
