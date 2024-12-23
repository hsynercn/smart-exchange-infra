# Welcome to your CDK Java project!

This is a blank project for CDK development with Java.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven compatible Java IDE to build and run tests.

## Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation

Enjoy!

# smart-exchange-infra
Cloud infrastructure for Smart Exchange

Initial setup

```bash
npm install -g aws-cdk # Install AWS CDK
```

After this step we can check the version of the CDK installed by running the following command:

```bash
cdk --version
```

We can deploy the stack by running the following command:

```bash
cdk deploy
```

Console sign-in URL: https://huseyincerc-udemy-tutorial.signin.aws.amazon.com/console

Username: AdminForCDKDeployment

Console password: in mail

AWS Lambda Spring Boot App:

https://www.baeldung.com/spring-boot-aws-lambda

https://github.com/aws/serverless-java-container/wiki/Quick-start---Spring-Boot3

ISSUE: I can't use the Lambda URL