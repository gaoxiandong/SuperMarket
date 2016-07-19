package com.gxd.exercise

/**
 * Created by Administrator on 2016/7/17.
 * 客户实体
 * 构造参数 custmerId,客户id
 */
class Customer (custmerId:String){
  //入队时间
  val createTime = System.currentTimeMillis()
  //结账完成时间
  var cashFinshTime : Long = 0
  //所购商品
  var goods : List[Good] = Nil

  def setCashFinshTime = cashFinshTime = System.currentTimeMillis()
  def setGoods(good: Good)=goods=good :: goods
  def getCustomerId: String =custmerId
  def getWaitTime : Long =cashFinshTime-createTime
}
