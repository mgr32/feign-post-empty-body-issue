### Overview

This is a simple project showing that `POST`s with empty body are sent with  `Content-Type: application/x-www-form-urlencoded` header in Feign >= 12.0, and no `Content-Type` header in Feign < 12.0.

#### Steps to reproduce

1. Run `./gradlew clean test --info` - it will run a single test that sends a POST with empty body to WireMock, and logs the headers.
2. Observe the output:

   ```
    PostWithEmptyBodyTest > postWithEmptyBodyTest() STANDARD_OUT
        WireMock received request to http://localhost:8081/post with the following headers: 
    
        Accept: */*
        User-Agent: Java/17.0.6
        Host: localhost:8081
        Connection: keep-alive
        Content-Type: application/x-www-form-urlencoded
        Content-Length: 0
    
    
    
    PostWithEmptyBodyTest > postWithEmptyBodyTest() FAILED
        java.lang.AssertionError: 
        Expecting empty but was: "application/x-www-form-urlencoded"
            at com.example.demo.PostWithEmptyBodyTest.postWithEmptyBodyTest(PostWithEmptyBodyTest.java:49)

   ```

   * Note there is a header `Content-Length: 0` (expected) but also `Content-Type: application/x-www-form-urlencoded` (unexpected)

3. Change `io.github.openfeign:feign-core` version in `build.gradle` from `12.3` to `11.10` (last version with the old behaviour) and re-run the test.
4. Observe the output:

    ```
    PostWithEmptyBodyTest > postWithEmptyBodyTest() STANDARD_OUT
        WireMock received request to http://localhost:8081/post with the following headers: 
    
        Accept: */*
        User-Agent: Java/17.0.6
        Host: localhost:8081
        Connection: keep-alive
    
    ```

   * Note there are no `Content-Length` and `Content-Type` headers.