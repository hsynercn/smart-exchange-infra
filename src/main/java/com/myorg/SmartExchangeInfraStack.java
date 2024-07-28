package com.myorg;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.events.CronOptions;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.RuleProps;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.ApiGateway;
import software.amazon.awscdk.services.events.targets.ApiGatewayProps;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Duration;

import java.util.Arrays;
import java.util.Map;


public class SmartExchangeInfraStack extends Stack {
    public SmartExchangeInfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public SmartExchangeInfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        Function springBootLambda = Function.Builder.create(this, "SpringBootLambda")
        .runtime(Runtime.JAVA_21) // Ensure this matches your Java runtime
        .handler("smart.exchange.provider.aws.api.StreamLambdaHandler::handleRequest") // Adjust this to your handler class
        .code(Code.fromAsset("/Users/huseyincanercan/Desktop/WORKSPACE/smart-exchange-provider-aws-api/target/smart-exchange-provider-aws-api-1.0-SNAPSHOT.jar")) // Use local path
        .memorySize(1024) // Adjust based on your needs
        .timeout(Duration.minutes(2)) // Adjust based on your needs
        .build();

        // Define the DynamoDB table
        Table myTable = Table.Builder.create(this, "ExchangeData")
                .partitionKey(Attribute.builder()
                        .name("id")
                        .type(AttributeType.STRING)
                        .build())
                .tableName("ExchangeData")
                .build();

        // Create an IAM policy statement
        PolicyStatement dynamoDbPolicy = PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(Arrays.asList(
                        "dynamodb:*"
                ))
                .resources(Arrays.asList(myTable.getTableArn()))
                .build();

        // Attach the policy to the Lambda function
        springBootLambda.addToRolePolicy(dynamoDbPolicy);

        // Define the API Gateway REST API
        //add cache to GET /currency endpoint with max TTL
        RestApi api = RestApi.Builder.create(this, "SpringBootApi")
                .restApiName("Spring Boot Service")
                .description("This service serves a Spring Boot application.")
                .deploy(true)
                .deployOptions(StageOptions.builder()
                        .methodOptions(Map.of(
                                "/currency/GET", MethodDeploymentOptions.builder()
                                        .cachingEnabled(true)
                                        .cacheTtl(Duration.minutes(5))
                                        .build()
                        ))
                        .build())
                //.defaultCorsPreflightOptions(CorsOptions.builder()
                //        .allowOrigins(Cors.ALL_ORIGINS)
                //        .allowMethods(Cors.ALL_METHODS)
                //        .allowHeaders(Arrays.asList("*"))
                //        .build())
                .defaultIntegration(new LambdaIntegration(springBootLambda))
                .build();


        // Create a proxy resource and method for the API
        Resource proxyResource = api.getRoot().addProxy();

        Schedule hourlySchedule = Schedule.cron(CronOptions.builder()
                .minute("0")
                .hour("*")
                .build());

        Rule resetCurrencyRule = new Rule(this, "ResetCurrencyRule",
                RuleProps.builder()
                        .schedule(hourlySchedule)
                        .build());

        //add rule target to proxyResource resetcurrency endpoint
        resetCurrencyRule.addTarget(new ApiGateway(api, ApiGatewayProps.builder()
                .path("/resetcurrency")
                .method("GET")
                .build()));


        // Define a CloudFormation output for your API Gateway URL
        CfnOutput.Builder.create(this, "springBootApiUrlOutput")
                .value(api.getUrl())
                .build();


    }
}
