package api.services

import api.support.OrderData
import java.time.{Instant, OffsetTime, ZoneOffset}
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import scala.math.Ordered.orderingToOrdered

trait DeliveryFeeCalculationService {
  private val CartValueNeededForFreeDelivery = 20000
  private val DeliveryFeeFirst1000m = 200
  private val DeliveryFeePerAdditional500m = 100
  private val MinimumCartValueNoSurcharge = 1000
  private val MaximumNumberOfItemsNoSurcharge = 4
  private val SurchargePerItemMoreThanFour = 50
  private val MaximumNumberOfItemsNoBulkSurcharge = 12
  private val BulkSurcharge = 120
  private val FridayRushSurchargeMultiplier = 1.2f
  private val MaximumDeliveryFee = 1500

  def isOneFieldNegative(orderData:OrderData): Boolean={
    orderData.delivery_distance < 0 || orderData.cart_value < 0 || orderData.number_of_items < 0
  }

  private def isOneFieldZero(orderData:OrderData): Boolean ={
    orderData.cart_value == 0 || orderData.number_of_items == 0
  }

  private def isCartValueMoreThanNeededForFreeDelivery(cartValue: Int):Boolean ={
    cartValue >= CartValueNeededForFreeDelivery
  }

  def calculateDeliveryDistanceFee(deliveryDistance:Int): Int ={
    var deliveryDistanceCost = DeliveryFeeFirst1000m
    if (deliveryDistance > 1000) {
      if (deliveryDistance % 500 != 0) {
        deliveryDistanceCost += DeliveryFeePerAdditional500m
      }
      deliveryDistanceCost += (((deliveryDistance-1000) / 500)*DeliveryFeePerAdditional500m)
    }
    deliveryDistanceCost
  }

  def calculateCartValueSurcharge(cartValue :Int): Int ={
    var feeAfterSurcharge = 0
    if (cartValue < MinimumCartValueNoSurcharge) {
      feeAfterSurcharge = MinimumCartValueNoSurcharge - cartValue
    }
    feeAfterSurcharge
  }

  private def calculateNumberOfItemsSurcharge(numberOfItems: Int) : Int ={
    var bulkSurcharge = 0
    if (numberOfItems > MaximumNumberOfItemsNoSurcharge)
      bulkSurcharge += (SurchargePerItemMoreThanFour * (numberOfItems - MaximumNumberOfItemsNoSurcharge))
    if (numberOfItems > MaximumNumberOfItemsNoBulkSurcharge)
        bulkSurcharge += BulkSurcharge
    bulkSurcharge
  }

  private def parseIsoTime(isoTime: String): Instant = {
    val formatter = DateTimeFormatter.ISO_INSTANT
    Instant.from(formatter.parse(isoTime))
  }

  private def isFriday(instant: Instant): Boolean = {
    instant.atOffset(ZoneOffset.UTC).getDayOfWeek == DayOfWeek.FRIDAY
  }

  private def isRushHour(instant: Instant): Boolean = {
    val RushHourStartTime: OffsetTime = OffsetTime.of(15, 0, 0, 0, ZoneOffset.UTC)
    val RushHourEndTime: OffsetTime = OffsetTime.of(19, 0, 0, 0, ZoneOffset.UTC)
    val timeOfDay = instant.atOffset(ZoneOffset.UTC).toOffsetTime
    timeOfDay >= RushHourStartTime && timeOfDay <= RushHourEndTime
  }
  def calculateFridayRushSurcharge(isoTime: String): Float ={
    val instant = parseIsoTime(isoTime)
    if (isFriday(instant) && isRushHour(instant)) {
      return FridayRushSurchargeMultiplier
    }
    1.0f
  }

  private def isOverMaximumDeliveryFee(deliveryFee: Int):Boolean = {
    deliveryFee > MaximumDeliveryFee
  }

  def calculateDeliveryFee(orderData: OrderData): Int = {
    var deliveryFeeInCents = 0
    if (isOneFieldZero(orderData))
      return 0
    if(isCartValueMoreThanNeededForFreeDelivery(orderData.cart_value))
      return 0
    deliveryFeeInCents += calculateDeliveryDistanceFee(orderData.delivery_distance)
    deliveryFeeInCents += calculateCartValueSurcharge(orderData.cart_value)
    deliveryFeeInCents += calculateNumberOfItemsSurcharge(orderData.number_of_items)
    deliveryFeeInCents = (deliveryFeeInCents * calculateFridayRushSurcharge(orderData.time)).toInt
    if (isOverMaximumDeliveryFee(deliveryFeeInCents))
      return MaximumDeliveryFee
    deliveryFeeInCents
  }
}
