package com.myorg;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.constructs.Construct;

public class DynamodbStack extends Stack {
    Table produtoDynamoDb;


    public DynamodbStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public DynamodbStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        produtoDynamoDb = Table.Builder.create(this, "produtoDynamoDb")
                .tableName("produtos-eventos")
                .billingMode(BillingMode.PROVISIONED)
                .readCapacity(1)
                .writeCapacity(1)
                .partitionKey(Attribute.builder()
                        .name("")
                        .type(AttributeType.STRING)
                        .build())
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    public Table getProdutoDynamoDb() {
        return produtoDynamoDb;
    }
}