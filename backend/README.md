# Wedoogift Backend challenge
You are interested in joining our team ? try to accomplish this challenge, we will be glad to see
your code and give you a feedback.

## Guidelines
For each level, write code that creates a new data/output.json file from the data in data/input.json. An output-expected.json file is available to give you a reference on what result is expected.
* Use Java to accomplish this challenge.
* Clone this repo (do not fork it)
* Only do one commit per level and include the `.git` when submitting your test
* We are not expecting a view for this challenge. 

## Evaluation
We will look at:
* How you use OOP.
* Your code quality.
* Your ability to use unit tests.

## Implementation
* In this folder, run `mvn clean install` to execute all tests, generate files `Level1/data/output.json` and `Level2/data/output.json` and compile code.
* In this folder, run `mvn spring-boot:run` to run application on port 8080 of the local host, and exposed secure REST endpoints.
* For authentication, use company name as username, and company id as password.
* You can use curl to call secure endpoints like this
```
curl "http://localhost:8080/<PATH>" --user <COMPANY_NAME>:<COMPANY_ID>
```
