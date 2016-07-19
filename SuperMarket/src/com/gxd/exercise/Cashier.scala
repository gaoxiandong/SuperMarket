package com.gxd.exercise

/**
 * Created by Administrator on 2016/7/17.
 * 收银员实体
 * 构造参数，收银员ID，收银员名称
 */
class Cashier (cashierId:Int,name:String) {
  //完成收银数量
  var finishCashNum : Int = 0

  def setFinishCashNum = finishCashNum+=1
  def getName:String = name
}
