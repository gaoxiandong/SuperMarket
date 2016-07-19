package com.gxd.exercise

/**
 * Created by Administrator on 2016/7/17.
 * 商品实体
 * 构造参数 商品id，商品名称
 */
class Good(goodId:String,name:String){
  //创建时间
  val createTime = System.currentTimeMillis()
  //售出时间
  var saleTime : Long = 0
  //被客户选择时间
  var chooseTime : Long = 0

  def setSaleTime = saleTime=System.currentTimeMillis()
  def setChoosetime = chooseTime=System.currentTimeMillis()
  def getWaitTime: Long = chooseTime-createTime
}

