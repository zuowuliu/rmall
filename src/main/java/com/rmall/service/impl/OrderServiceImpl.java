package com.rmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rmall.common.Const;
import com.rmall.common.ServerResponse;
import com.rmall.dao.*;
import com.rmall.pojo.*;
import com.rmall.service.IOrderService;
import com.rmall.util.BigDecimalUtil;
import com.rmall.util.DateTimeUtil;
import com.rmall.util.FTPUtil;
import com.rmall.util.PropertiesUtil;
import com.rmall.vo.OrderItemVo;
import com.rmall.vo.OrderProductVo;
import com.rmall.vo.OrderVo;
import com.rmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/18 0018 下午 22:45
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    /**
     * 静态方法和静态初始化代码块：构造AlipayTradeService tradeService对象
     * 457行： AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);返回支付宝结果
     */
    private static AlipayTradeService tradeService;
    static{
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    private static Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);


    //**********************************支付相关********************************

    //*************************************************************************

    /**
     * 支付实现
     * */
    public ServerResponse pay(Long orderNo,Integer userId,String path){
        Map<String,String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByError("该用户没有该订单");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        //一个一个的修改(从当面付demo中copy过来的代码)


        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("rmall商品扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";



        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        //获取订单的详细商品列表
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo,userId);
        for(OrderItem orderItem : orderItemList){
             /* 创建一个商品信息，参数含义分别为商品id（使用国标:建议使用国际标准的商品id表示形式）、商品名称、商品价格（单位为分）、商品数量
              商品价格（单位为分）：乘以1000转为分，单位转为long型
              public static GoodsDetail newInstance(String goodsId, String goodsName, long price, int quantity)
             */
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                //回调地址
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);



        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);

        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");
                System.out.println("支付宝预下单成功");

                //下单成功要将返回的二维码信息，利用工具类生成二维码图片，保存到图片服务器，返回二维码在图片服务器的位置给前端
                AlipayTradePrecreateResponse response = result.getResponse();
                System.out.println("result:  "+result.toString());
                dumpResponse(response);

                /**
                 * 在这里已经预下单成功了，所以需要将生成二维码上传到图片服务器
                 * */
                File folder = new File(path);
                if(!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                //这里是上传到服务器的地址(response.getOutTradeNo()是订单号),添加"/"是因为path后面没有"/"
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                //这是上传到服务器的文件的名字
                //qr-%s.png是String.format的格式化字符形式，可以将订单号拼接到上面去
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                System.out.println("path的路径是："+path);
                System.out.println("qrPath的路径："+qrPath);
                System.out.println("response.getQrCode()"+response.getQrCode());
                //生成二维码图片，保存到qrPath下,qrpath是自己在服务器创建的地址,类似图片上传的upload
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                //让这个path路径下面可以存在有qrFileName这个文件
                File targetFile = new File(path,qrFileName);
                System.out.println(" File targetFile = new File(path,qrFileName);的路径"+targetFile.getAbsolutePath());
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码异常",e);
                }

                logger.info("qrPath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();//targetFile自带的有getName方法
                resultMap.put("qrUrl", qrUrl);
                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByError("支付宝预下单失败！！！");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByError("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByError("不支持的交易状态，交易返回异常!!!");
        }
    }
    //简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    /**
     * 支付宝回调
     * */
    public ServerResponse alipayCallback(Map<String,String> params){
        //获取订单号（内部订单号）
        long orderNo = Long.parseLong(params.get("out_trade_no"));
        //支付宝交易号
        String tradeNo = params.get("trade_no");
        //交易状态
        String tradeStatus = params.get("trade_status");
        //订单对象
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByError("非rmall商城的订单，回调忽略");
        }
        //然后判断订单的状态
        if(order.getStatus() >= Const.OrderSatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            //付款时间 gmt_payment 交易付款时间	Date 格式为yyyy-MM-dd HH:mm:ss
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            //支付成功TRADE_SUCCESS，重置订单状态为已付款状态
            order.setStatus(Const.OrderSatusEnum.PAID.getCode());
            //更新订单状态
            orderMapper.updateByPrimaryKeySelective(order);
        }

        //生成payInfo对象
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        //支付宝交易号
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }


    /**
     * 轮询支付订单的状态
     * */
    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByError("该用户没有该订单");
        }
        //然后判断订单的状态
        if(order.getStatus() >= Const.OrderSatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }

        return ServerResponse.createByError();
    }


    //*******************************订单相关***********************************

    //*************************************前台************************************
    /**
     * 创建订单
     *
     * 1、查找购物车待支付的记录
     * 2、获取orderItem对象getCartOrderItem
     * 3、计算该用户的订单总价(getCartOrderItem只是获取到了每一种待购买的商品的总价，并不包含订单的总价)
     * 4、生成订单号(集成在生成订单的方法里面)，生成订单
     * */
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        //查询指定用户的购物车中处于被选中状态的商品(但是购物车表信息中不包含商品的详情和单价总价那些，需要获取orderItem对象信息)
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        //计算该用户待结算的总价
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);
        //生成订单Order(在表中还需要shippingid/orderNo/payment)
        Order order = this.assembleOrder(userId, shippingId, payment);
        if(order == null){
            return serverResponse.createByError("生成订单错误");
        }
        if(CollectionUtils.isEmpty(orderItemList)){
            return serverResponse.createByError("用户购物车为空");
        }
        for(OrderItem orderItem : orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        //mybatis批量插入(用于更新orderItem表)
        orderItemMapper.batchInsert(orderItemList);
        //生成成功，减少产品的库存
        this.reduceProductStock(orderItemList);
        //清理购物车
        this.cleanCart(cartList);
        //返回给前端数据VO(应包含订单信息，订单明细信息，收货信息)
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 获取orderItem的对象
     * */
    public ServerResponse getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList = Lists.newArrayList();
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByError("该用户勾选的购物车记录为空");
        }
        //校验购物车的数据，包含产品的状态和数量(即检查选出来的商品是否是用户需要购买的商品,是否具有购买合法性)
        //遍历查出来的购物车记录cartList
        for(Cart cartItem : cartList){
            //组装包含商品信息的订单item
            OrderItem orderItem = new OrderItem();
            //根据cart信息里面包含的商品的id来查询具体的商品信息
            Product  product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            //校验商品的售卖状态合法性
            if(Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()){
                return ServerResponse.createByError(product.getName()+"不是在线售卖的状态..");
            }
            //校验商品的库存
            if(cartItem.getQuantity() > product.getStock()){
                return ServerResponse.createByError(product.getName()+"购买量已大于仅剩的库存量..");
            }
            //校验完成之后就组装需要使用的orderItem对象(就利用查出来的product的相关信息来组装orderItem)
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartItem.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     * 获取order的总价，整个订单的总价
     * */
    public BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    /**
     * 组装生成订单
     * */
    public Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order = new Order();
        long orderNo = this.generatorOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderSatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);
        order.setShippingId(shippingId);
        order.setUserId(userId);
        //发货时间
        //付款时间
        int resultCount = orderMapper.insert(order);
        if(resultCount > 0){
            return order;
        }
        return null;
    }

    /**
     * 生成订单号(用一个私private方法来创建生成订单号)
     * */
    private long generatorOrderNo(){
        long systemCurrentMillis = System.currentTimeMillis();
        return systemCurrentMillis+new Random().nextInt(100);
    }

    /**
     * 将orderItem更新成功之后减少产品的库存
     * */
    private void reduceProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            //更新库存的产品数量，购物车有多少就减去多少
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     * 下单之后清空已勾选的购物车记录
     * */
    private void cleanCart(List<Cart> cartList){
        for(Cart cart : cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /**
     * 组装生成orderVo
     * 组装的orderVo应该包含order的信息，orderItemList的信息，以及收货地址的一些信息
     * 在order里面包含的有shippingId可以查到shipping的信息，然后封装到orderVo里面
     * */
    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        //根据传进来的order的paymentType来做Vo的PaymentType的描述
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderSatusEnum.codeOf(order.getStatus()).getValue());
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        List<OrderItemVo> orderItemVolist = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVolist.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVolist);
        return orderVo;
    }

    /**
     * 组装shippingVo
     * */
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    /**
     * 组装orderItemVo
     * */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        return orderItemVo;
    }

    //*************************************************************************

    /**
     * 删除未付款的订单
     *
     * 1、判断订单是否存在
     * 2、判断订单是否已经被支付
     * 3、更新订单的状态为已取消CENCELED
     * */
    public ServerResponse<String> cancelOrder(Integer userId,long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByError("该用户此订单不存在");
        }
        if(order.getStatus() != Const.OrderSatusEnum.NO_PAY.getCode()){
            return ServerResponse.createByError("已付款，无法取消订单");
        }
        //更新order
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderSatusEnum.CANCELED.getCode());
        int resultCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(resultCount > 0){
            return ServerResponse.createBySuccess("取消订单成功");
        }
        return ServerResponse.createByError("取消订单失败");
    }

    //*************************************************************************

    /**
     * 获取购物车中已被选中的商品的详情
     *
     * 1、创建OrderProductVo
     * 2、组装OrderProductVo(重要的是里面的OrderItemVoList属性)
     *
     * */
    public ServerResponse<OrderProductVo> getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo = new OrderProductVo();
        //从购物车中获取数据

        //根据userId获取选中状态的cart
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        //获取orderItem
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");

        for(OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return ServerResponse.createBySuccess(orderProductVo);
    }

    //*************************************************************************

    /**
     * 获取订单详情
     *
     * (获取orderVo)
     * */

    public ServerResponse<OrderVo> getOrderDetail(Integer userId,long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByError("没有找到该用户的该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo, userId);
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    //*************************************************************************

    /**
     * 获取订单列表
     *
     * (组装OrderVo,通过orderVoList来构建分页)
     * */
    public ServerResponse<PageInfo> list(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, userId);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 组装orderVoList对象
     * */
    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            if(userId == null){
                //todo 管理员查询的时候不需要传userId
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.getByOrderNoUserId(order.getOrderNo(), userId);
            }
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }


    //**********************************订单相关***************************************

    //***************************************后台*************************************

    /**
     * （1）后台获取订单列表
     *
     * */
    public ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAll();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, null);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }


    /**
     * （2）后台获取订单详情
     *
     * */
    public ServerResponse<OrderVo> manageDetail(long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByError("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * （3）后台根据订单号搜索订单
     *
     * */

    public ServerResponse<PageInfo> manageSearch(long orderNo,Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByError("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        //根据在dao中查询到的结果来组装pageInfo
        PageInfo resultPage = new PageInfo(Lists.newArrayList(order));
        resultPage.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccess(resultPage);
    }

    /**
     * （4）根据订单号来发货
     *
     * */
    public ServerResponse<String> manageSendGoods(long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByError("订单不存在");
        }
        //根据订单的状态来确认是否已发货
        if(order.getStatus() == Const.OrderSatusEnum.PAID.getCode()){
            order.setStatus(Const.OrderSatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            return ServerResponse.createBySuccess("发货成功");
        }
        return ServerResponse.createByError();
    }

}
