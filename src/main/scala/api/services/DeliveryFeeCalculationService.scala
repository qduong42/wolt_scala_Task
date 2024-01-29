package api.services

import api.routes.JsonSupport

import java.time.format.DateTimeFormatter
import java.time.{DayOfWeek, Instant, LocalTime, OffsetTime, ZoneOffset}


trait DeliveryFeeCalculationService {
  private val MaximumDeliveryFee = 1500
  private val CartValueNeededForFreeDelivery = 20000
  private val MinimumCartValueNoSurcharge = 1000
  private val MaximumNumberOfItemsNoSurcharge = 4
  private val MaximumNumberOfItemsNoBulkSurcharge = 12
  private val SurchargePerItemMoreThanFour = 50
  private val BulkSurcharge = 120
  private val DeliveryFeeFirst1000m = 200
  private val DeliveryFeePerAdditional500m = 100
  private val FridayRushSurchargeMultiplier = 1.2f

  private def isOneFieldZero(orderData:JsonSupport.OrderData): Boolean ={
    if (orderData.delivery_distance == 0 || orderData.cart_value == 0 || orderData.number_of_items == 0)
      true
    else
      false
  }

  private def isCartValueMoreThanNeededForFreeDelivery(cartValue: Int):Boolean ={
    if (cartValue >= CartValueNeededForFreeDelivery) {
      return true
    }
    false
  }

  private def deliveryDistanceFee(deliveryDistance:Int): Int ={
    var deliveryDistanceCost = DeliveryFeeFirst1000m
    if (deliveryDistance > 1000) {
      if (deliveryDistance % 500 != 0)
        deliveryDistanceCost += DeliveryFeePerAdditional500m
      deliveryDistanceCost += (((deliveryDistance-1000) / 500)*DeliveryFeePerAdditional500m)
    }
    deliveryDistanceCost
  }

  private def cartValueCheck(cartValue :Int): Int ={
    var feeAfterSurcharge = 0
    if (cartValue < MinimumCartValueNoSurcharge) {
      feeAfterSurcharge = (MinimumCartValueNoSurcharge - cartValue)
    }
    feeAfterSurcharge
  }

  private def calculateNoOfItemsSurcharge(numberOfItems: Int) : Int ={
    var bulkSurchargeFee = 0
    if (numberOfItems > MaximumNumberOfItemsNoSurcharge)
      bulkSurchargeFee += (SurchargePerItemMoreThanFour * (numberOfItems - MaximumNumberOfItemsNoSurcharge))
    if (numberOfItems > MaximumNumberOfItemsNoBulkSurcharge)
        bulkSurchargeFee += BulkSurcharge
    bulkSurchargeFee
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
    val isAfterStart = !timeOfDay.isBefore(RushHourStartTime)
    val isBeforeEnd = timeOfDay.isBefore(RushHourEndTime)
    isAfterStart && isBeforeEnd
  }
  private def calculateFridayRushSurcharge(isoTime: String): Float ={
    val instant = parseIsoTime(isoTime)
    if (isFriday(instant) && isRushHour(instant)) {
      return FridayRushSurchargeMultiplier
    }
    1.0f
  }
  private def isOverMaximumDeliveryFee(deliveryFee: Int):Boolean = {
    if (deliveryFee > MaximumDeliveryFee)
      true
    else
      false
  }
  def calculateDeliveryFee(orderData: JsonSupport.OrderData): Int = {
    var deliveryFeeInCents = 0
    if (isOneFieldZero(orderData))
      return 0
    if(isCartValueMoreThanNeededForFreeDelivery(orderData.cart_value))
      return 0
    deliveryFeeInCents += deliveryDistanceFee(orderData.delivery_distance)
    deliveryFeeInCents += cartValueCheck(orderData.cart_value)
    deliveryFeeInCents += calculateNoOfItemsSurcharge(orderData.number_of_items)
    deliveryFeeInCents = (deliveryFeeInCents * calculateFridayRushSurcharge(orderData.time)).toInt
    if (isOverMaximumDeliveryFee(deliveryFeeInCents))
      return MaximumDeliveryFee
    deliveryFeeInCents
  }
}

// No need for DefaultDeliveryService
