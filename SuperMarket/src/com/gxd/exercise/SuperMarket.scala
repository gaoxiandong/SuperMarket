package com.gxd.exercise
import scala.actors._
import scala.actors.Actor._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by gxd on 2016/7/17.
 * 超市实体类
 * 构造参数 无
 */
class  SuperMarket {
  //收银员
  var cashiers=ArrayBuffer[Cashier]()
  //货物库存
  var goods = ArrayBuffer[Good]()
  //客户队列
  val customerQueue = new mutable.Queue[Customer]
  //客户
  val customers = ArrayBuffer[Customer]()

  //增加客户
  def addCustomer(customer: Customer): Unit ={
    customers += customer
  }
  //增加收银员
  def addCashier(cashier: Cashier): Int ={
    cashiers += cashier
    cashiers.length
  }
  //增加库存
  def addGoods(num:Int,goodName:String): Int ={
    for(i <- 1 to num){
      var good = new Good(goodName+i,goodName)
      goods += good
    }
    goods.length
  }
  //客户入队
  def insertQueue(customer: Customer) : Int ={
    customerQueue+=customer
    customerQueue.size
  }
  //客户出队
  def outQueue : Customer ={
    customerQueue.synchronized(
      if(customerQueue.size==0) return null
        else return  customerQueue.dequeue()
    )
  }
  //客户随机购物
  def buyGood(customer: Customer): Boolean ={
    //生成随机数
    val randomNum=math.abs(scala.util.Random.nextInt())
    val goodNum=goods.length
    if (goodNum==0) return false
    val indexNum=randomNum%goodNum
    val good=goods.remove(indexNum)
    //设置选择时间
    good.setChoosetime
    customer.setGoods(good)
    true
  }
  //收银员收银
  def cash(cashierId:Int,customer:Customer): Unit ={
    cashiers(cashierId-1).setFinishCashNum
    customer.setCashFinshTime
    for(good<-customer.goods){
      good.setSaleTime
    }
    println("cashier"+cashierId+" service for "+customer.getCustomerId+" completed")
  }
  //统计每个客户等待时间
  def CustWaitTimeStat: Long ={
    var totalWaitTime:Long=0
    for(customer<-customers){
      totalWaitTime+=customer.getWaitTime
    }
    totalWaitTime/customers.size
  }
  //统计每个商品平均售出时间(按照商品被客户选到的时间计算)
  def GoodSaleTimeStat: Long ={
    var goodsNum:Int=0
    var totalWaitTime:Long=0
    for(customer<-customers){
      for(good<-customer.goods){
        goodsNum+=customer.goods.size
        totalWaitTime+=good.getWaitTime
      }
    }
    totalWaitTime/goodsNum
  }
  //统计每个收银员接待的人数
  def CashierServiceNum: List[String]={
    var infoStat : List[String] = Nil
    for(cashier<-cashiers){
      infoStat=(cashier.getName+" service "+cashier.finishCashNum+" nums")::infoStat
    }
    infoStat
  }
}

//程序入口
object SuperMarket extends App{
  //客户生成间隔
  val genCustTime=3000
  //收银处理时间
  val cashDealTime=10000
  //生成每种商品的数量
  val perGoodNum=15
  //生成商品的总数量
  val GoodTotalNum=perGoodNum*3
  //总的服务次数
  var serviceNum=0
  val sm=new SuperMarket;
  val mainer=self;

  //初始3个营业员
  sm.addCashier(new Cashier(1,"收银员1"))
  sm.addCashier(new Cashier(2,"收银员2"))
  sm.addCashier(new Cashier(3,"收银员3"))
  println("initialize cashiers finish!")
  //初始库存
  sm.addGoods(perGoodNum,"Apple")
  sm.addGoods(perGoodNum,"Cookie")
  sm.addGoods(perGoodNum,"Macbook")
  println("initialize goods finish!")

  //营业开始计时
  val startSaleTime=System.currentTimeMillis()
  //生成客户队列线程
  actor {
    println("generate customer begin!")
    var continue=true
    var i=0
    loopWhile(continue){
      i+=1
      var customer = new Customer("customer" + i)
      continue=sm.buyGood(customer)
      if(!continue){
        mainer ! "generate customer finish!"
      }else{
        sm.insertQueue(customer)
        //将前三个客户通知三个收银员开始工作
        if(i==1) cashier1 ! "cashier1 begin work!"
        else if(i==2) cashier2 ! "cashier2 begin work!"
        else if(i==3) cashier3 ! "cashier3 begin work!"
        println("generate "+customer.getCustomerId)
        Thread.sleep(genCustTime)
      }
    }
  }

  //服务次数累加线程
  val counter=actor{
    for(i<- 1 to GoodTotalNum){
      receive {
        case (number:Int) => actor {serviceNum+=1}
      }
    }
  }

  //收银员1工作线程
  val cashier1=actor {
    var continue=true
    receive{case msg => println(msg)}
    loopWhile(continue){
      var customer = sm.outQueue
      if (customer != null) {
        Thread.sleep(cashDealTime)
        sm.cash(1,customer)
        sm.addCustomer(customer)
        counter ! 1
      }else{
        //如果队列没有客户，休息一秒后再去取,当生产者小于消费者的能力时，会有此延时
        Thread.sleep(1000)
      }
      //由于不知道收银何时结束，由于本案的特殊性，此处判断服务的次数达到商品总数后，结束收银
      if(serviceNum==GoodTotalNum){
        continue=false
        mainer ! "cashier1 work finish!"
      }
    }
  }
  //收银员2工作线程
  val cashier2=actor {
    var continue=true
    receive{case msg => println(msg)}
    loopWhile(continue){
      var customer = sm.outQueue
      if (customer != null) {
        Thread.sleep(cashDealTime)
        sm.cash(2,customer)
        sm.addCustomer(customer)
        counter ! 1
      }else{
        //如果队列没有客户，休息一秒后再去取
        Thread.sleep(1000)
      }
      //由于不知道收银何时结束，由于本案的特殊性，此处判断服务的次数达到商品总数后，结束收银
      if(serviceNum==GoodTotalNum){
        continue=false
        mainer ! "cashier2 work finish!"
      }
    }
  }
  //收银员3工作线程
  val cashier3=actor {
    var continue=true
    receive{case msg => println(msg)}
    loopWhile(continue){
      var customer = sm.outQueue
      if (customer != null) {
        Thread.sleep(cashDealTime)
        sm.cash(3,customer)
        sm.addCustomer(customer)
        counter ! 1
      }else{
        //如果队列没有客户，休息1秒后再去取
        Thread.sleep(1000)
      }
      //由于不知道收银何时结束，由于本案的特殊性，此处判断服务的次数达到商品总数后，结束收银
      if(serviceNum==GoodTotalNum){
        continue=false
        mainer ! "cashier3 work finish!"
      }
    }
  }

  //等待营业完成
  for(i <- 1 to 4) { receive { case msg => println(msg)}}

  //营业结束时间
  val endSaleTime=System.currentTimeMillis()

  //开始打印统计信息
  println("---------------Statistics begin!-------------------")
  println("customers average wait time:"+sm.CustWaitTimeStat/1000 + " seconds")
  println("goods average sale time:"+sm.GoodSaleTimeStat/1000 + " seconds")
  println("total business time:"+(endSaleTime-startSaleTime)/1000+ " seconds")
  println("cashiers statistics:\n"+sm.CashierServiceNum.mkString("\n"))
  println("---------------Statistics end!----------------------")
}
