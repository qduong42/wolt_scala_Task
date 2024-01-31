# Delivery Fee Calculator

## Description

This is my solution for the [Wolt Engineering Summer Internship Pre Assignment](https://github.com/woltapp/engineering-internship-2024/tree/main). It was written in Scala and implements the backend solution for the delivery fee calculator.

## Prerequisites

This project uses sbt 1.9.8 as the compile, run and test tool. Installation steps can be taken from the official documentation: https://www.scala-sbt.org/1.x/docs/Setup.html 

The program was built and tested with Java 21.0.2. Installation instructions for Java can be taken from the official documentation: https://docs.oracle.com/en/java/javase/21/install/installation-guide.pdf

## Build and Run

In the project directory, where the build.sbt file is located: run the command

```
sbt run
```
which will compile and run the src/main/scala/api/Main.scala running the server on the defined port in src/main/scala/api/Main.scala and localhost.



## Testing

```
sbt test
```
will run the testsuite in src/test/scala/api.routes/DeliveryApiRoutesSpec with provided tests.

### Manual Testing with Curl example(for wsl instructions see below)

- Requires a running server after executing ```sbt run```

```
curl -X POST http://localhost:8080/api/calculate-delivery-fee -H "Content-Type: application/json" -d '{"cart_value": 1000, "delivery_distance": 1000, "number_of_items":4, "time": "2024-01-30T15:00:00Z"}'
```
API expects JSON input with the following structure:
```
{
    "cart_value":INT,
    "delivery_distance":INT,
    "number_of_items":INT,
    "time":STRING
}
```
- where INT is replaced with positive int and STRING is replaced with Time String in ISO Format.
- Time should be given in valid ISO format. It accepts both Time given in Instant form in format: ```2024-01-30T15:00:00Z``` as well as Offset: ```2024-01-30T15:00:00+02:00```
- For Delivery fee Friday Rush Hour calculation all given time is converted ald calculated with UTC per the specification sheet.

Example:
```
{
    "cart_value":1000,
    "delivery_distance":1000,
    "number_of_items":4,
    "time":2024-01-30T15:00:00Z
}
```

- for valid request like in above example service responds with status ok and following json
```
{
    "delivery_fee":200
}
```

- Zero Value for cart_value or number of items
    - Value accepted as valid, if one or more of cart_value or number_of_items is 0, response in json for delivery_fee will be 0
 
### WSL ubuntu curl with process running on host Windows OS

- If program is run on windows, but you want to curl with a ubuntu WSL terminal, you need to replace ```localhost``` with ip address of windows host machine.
- Modify Main.scala file found under src/main/scala/api, change interface "localhost" to "0.0.0.0" to allow connections from other machines in the same network.
- Run ```ipconfig``` on Windows terminal.
- Take the ipv4 address and replace it in the curl command in ubuntu terminal. eg: ```curl -X POST http://192.168.1.15:8080/api/calculate-delivery-fee -H "Content-Type: application/json" -d '{"cart_value": 1000, "delivery_distance": 1000, "number_of_items":4, "time": "2024-01-30T15:00:00Z"}'```
  
### Invalid Requests handling

- Request missing one or more required member
    - Request Rejected due to JsonDeserialization exception.
    - HTTP Error 400 Bad Request
- GET/DELETE/PUT/etc. Request
    - Request rejected due to Rejection MethodRejected
    - HTTP Error 405
    - Accept Header set to POST
- Request body not in JSON and Header not set to application/json
    - HTTP Error 415 Unsupported Media Type
- Invalid Time
    - HTTP Error 400 Bad Request with message: "Invalid time format. Please provide a valid date and time in ISO format."
- Negative values for fields except time
    - HTTP Error 400 Bad Request with message: "One or more input field values except time is negative"
  
### Reflections

This section contains certain considerations about possible inadequacies about the project, that should be clarified further. These considerations depend on how one would like to build upon the project moving forward and are not yet inadequacies for the current requirements.

- Logging is included with  "ch.qos.logback" % "logback-classic" % "1.4.12" in sbt file. This is not necessary for this barebone project, but it gets rid of warnings in running the server and is useful later for a full-fledged server
- Custom Rejection Handling with own messages might give the developer more control over what response is being sent. Some elements are being handled by Akka HTTP Default rejection handling at the moment. The decision was made to avoid the project to be too bloated.
- Multiplier to fee calculation in regard to Friday Rush hour is currently done in a trunc method. If final fee is 105.5 CENTS it will be displayed as 105 CENTS
- time input is accepted in UTC or OFFSET, but UTC time is used for calculation. This is as specified in the project. For a more accurate representation, Time Zone should be inferred from Time Input and the TimeZone recognized and compared with rush hour in locale time.
- Delivery cost for delivery distance is set to 2 euro if customers for some reason may order food from a restaurant at their same address, they still pay delivery fee for 0m (First 1000m by definition).
- Delivery fee subcalculations functions changed from private def to def so that the tests could directly test subcomponents and not on the response of gross calculated fee.
