package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Duration;

import java.util.Arrays;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

public class SmartExchangeInfraStack extends Stack {
    public SmartExchangeInfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public SmartExchangeInfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        // example resource
        // final Queue queue = Queue.Builder.create(this, "SmartExchangeInfraQueue")
        //         .visibilityTimeout(Duration.seconds(300))
        //         .build();
        // Define the Lambda function resource
        Function myFunction = Function.Builder.create(this, "HelloWorldFunction")
                .runtime(Runtime.NODEJS_20_X) // Provide any supported Node.js runtime
                .handler("index.handler")
                .code(Code.fromInline(
                        "exports.handler = async function(event) {" +
                                " return {" +
                                " statusCode: 200," +
                                " body: JSON.stringify('Hello World!')" +
                                " };" +
                                "};"))
                .build();

        // Define the Lambda function URL resource
        FunctionUrl myFunctionUrl = myFunction.addFunctionUrl(FunctionUrlOptions.builder()
                .authType(FunctionUrlAuthType.NONE)
                .build());

        // Define a CloudFormation output for your URL
        CfnOutput.Builder.create(this, "myFunctionUrlOutput")
                .value(myFunctionUrl.getUrl())
                .build();

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
        RestApi api = RestApi.Builder.create(this, "SpringBootApi")
                .restApiName("Spring Boot Service")
                .description("This service serves a Spring Boot application.")
                .build();



        // Create a proxy resource and method for the API
        Resource proxyResource = api.getRoot().addProxy();
        proxyResource.addMethod("GET", new LambdaIntegration(springBootLambda));
        proxyResource.addMethod("POST", new LambdaIntegration(springBootLambda));
        proxyResource.addMethod("PUT", new LambdaIntegration(springBootLambda));
        proxyResource.addMethod("DELETE", new LambdaIntegration(springBootLambda));


        // Define a CloudFormation output for your API Gateway URL
        CfnOutput.Builder.create(this, "springBootApiUrlOutput")
                .value(api.getUrl())
                .build();


    }
}
