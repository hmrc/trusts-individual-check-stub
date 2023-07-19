# Trusts individual check stub

This service is responsible for stubbing API#1585 Individual Match in Integration Framework.
It mocks the responses from ITMP where an individual is matched using their national insurance number, name and date of birth.

To run locally using the micro-service provided by the service manager:

***sm2 --start TRUSTS_ALL***

If you want to run your local copy, then stop the frontend ran by the service manager and run your local code by using the following (port number is 9847 but is defaulted to that in build.sbt).

# Test data

**Successful match for lead trustee nino:**
AA100001A

`sbt run`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
