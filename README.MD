# Wallet history

A simple RESTful web applications that exposed an API for tracking the balance history of the most popular bitcoin
wallet. The model consists of two main entities:

- `Wallet` that you should definitely deposit; stores the current balance of the wallet;
- `Transaction` contains information about all awesome deposit operations.

Using the only `/records` API, it is possible to add deposit records, as well as track the history for a certain period
of time. A full description of all endpoints is available via http://localhost:8080/swagger-ui/#/.

### Technologies used

- Kotlin
- Spring Boot
- Spring Web, Validation, Data JPA
- H2 database
- Liquibase

### How to run

To run the application just run the `WalletHistoryApplication#main` from your favorite IDE or run the following command in a
terminal window:

```
./mvnw spring-boot:run
```

*Note*: Wallet history is not very reliable yet, so do not be upset if your data is lost, since the application uses
in-memory database and an initial wallet will be created after launch, but for simple and convenient testing, this
should be enough.