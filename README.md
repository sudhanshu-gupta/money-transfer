# Account Transfer

The service is designed to transfer money between accounts. Few lines for the service.
  - Create account for a given user with unique email. If account already exist, it return Bad Request Exception.
  - Get the balance of a account. If account doesn't exist, it return Not Found Exception.
  - Transfer money between the two existing accounting. Sending accound should have balance greater than or equal to the transfer amount. If sender doesn't have sufficient balance, it throws bad request exception.
  - Service supports distributed transaction, by acquiring the distributed lock on the accounts in a mutlithreaded environment. If lock not acquired, it returns the lock exception. As the production ready extension, it could have distributed queue to enqueue the request for accounts and manage request sequentially for a given account.
  - Service can run in a dockerized enviroment. In local, it run in a docker.
  - Service is integrated with OpenApi with Swagger. Api's can be accessed from Swagger UI as well. To access, hit browser with following url - `http://localhost:8082/swagger-ui/` (when run in local)

# Get Started
##### Clone
- `git clone https://github.com/sudhanshu-gupta/money-transfer.git && cd money-transfer`
##### Testing
  - To run tests, run the following script: `./test.sh` or `sh test.sh`. In case of failure, please try running test again or reach out to me.
  - It will run the integration tests and unit tests. Test results along with test case can be viewed by opening the `target/site/surefire-report.html` in the web browser.
##### Local
###### Pre-requisite
- Java 1.8
###### Command
- To run the service locally, run `sh run.sh` or alternatively, run with the following command based on `nux` or `windows` based system.
-  `./mvnw clean package -U quarkus:dev`
                             OR 
- `mvnw.cmd clean package -U quarkus:dev`

##### Tech
- framework: `https://quarkus.io/`
- language: `java8`
- build tool: `maven`
- Datastore: `h2`, `embedded-redis`

### API Specifications
Api can also be accessed from swagger endpoint `http://localhost:8082/swagger-ui/`
#### Create Account
Create new account for a given user with email. Account with same email should not exist before.
```sh
curl -X POST "http://localhost:8082/account" -H "accept: application/json" -H "Content-Type: application/json" -d "{\"balance\":10,\"email\":\"sudhanshu@gmail.com\",\"name\":\"sudhanshu gupta\"}"
```
#### Get Account Balance
Get the current balance of an existing account. If account doesnot exist, it throws error.
```sh
curl -X GET "http://localhost:8082/account/balance" -H "accept: application/json" -H "accountId: 1"
```
#### Transfer money between two existing account
Transfer amount between two existing account, with the precondition, sufficient balance exist in the sending account. If sender doesn't have sufficient balance, it throws error. 
```sh
curl -X POST "http://localhost:8082/account/transfer" -H "accept: application/json" -H "accountId: 2" -H "Content-Type: application/json" -d "{\"amount\":20,\"recipientAccountId\":1}"
```

* For docker based datastore, checkout the `dev` branch.
* In case of more information/help reach out at sudhanshu97gupta@gmail.com
