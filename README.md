# Delivery Fee Calculator

## Description

This is my solution for the [Wolt Engineering Summer Internship Pre Assignment](https://github.com/woltapp/engineering-internship-2024/tree/main). It was written in Scala and implements the backend solution for the delivery fee calculator.

## Prerequisites

This project uses sbt 1.9.8 as the compile, run and test tool. Installation steps can be taken from the official documentation: https://www.scala-sbt.org/1.x/docs/Setup.html 

The program was built and tested with Java 21.0.2.

## Build and Run

In the project directory, where the build.sbt file is located: run the command

```
sbt run
```
which will compile and run the Main.scala running the server on the defined port in Main.scala and localhost.



## Testing

```
sbt test
```
will run the testsuite with provided tests.

### Manual Testing with Curl

- Requires a running server using ```sbt run```

```
curl -X POST http://localhost:8000/api/calculate-delivery-fee -H "Content-Type: application/json" -d '{"cart_value": 1000, "delivery_distance": 1000, "number_of_items":4, "time": "2024-01-30T15:00:00Z"}'
```
API expects JSON input with the following structure:
```
{
    "cart_value":INT,
    "delivery_distance":INT,
    "number_of_items":INT,
    "time":STRING
}
Time should be given in valid ISO format.

- for valid requests service responds with
```
{
    "delivery_fee":710
}
```

### Reflections

- Logging is included with  "ch.qos.logback" % "logback-classic" % "1.4.12" in sbt file. This is not necessary for this barebone project but it gets rid of warnings in runing the server and is useful later for a full-fledged server
