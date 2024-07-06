package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
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
    }
}
