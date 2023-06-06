package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class Service02Stack extends Stack {

    public Service02Stack(final Construct scope, final String id, Cluster cluster, SnsTopic topic) {
        this(scope, id, null, cluster, topic);
    }

    public Service02Stack(final Construct scope, final String id, final StackProps props, Cluster cluster, SnsTopic topic) {
        super(scope, id, props);

        Queue producoEventsDlq = Queue.Builder.create(this, "ProducoEventsDlq")
                .queueName("ProducoEventsDlq")
                .build();

        DeadLetterQueue deadLetterQueue = DeadLetterQueue.builder()
                .queue(producoEventsDlq)
                .maxReceiveCount(3)
                .build();

        Queue producoEvents = Queue.Builder.create(this, "producoEvents")
                .queueName("producoEvents")
                .deadLetterQueue(deadLetterQueue)
                .build();

        SqsSubscription sqsSubscription = SqsSubscription.Builder
                .create(producoEvents)
                .build();

        topic.getTopic().addSubscription(sqsSubscription);

        Map<String, String> envVariables = new HashMap<>();
        envVariables.put("aws.region", "us-east-1");
        envVariables.put("aws.sqs.queue.product.events.name", producoEvents.getQueueName());

        ApplicationLoadBalancedFargateService service = ApplicationLoadBalancedFargateService.
                Builder.create(this, "ALB-02").
                serviceName("service-02").cluster(cluster).
                cpu(512).desiredCount(2).listenerPort(8080).memoryLimitMiB(1024).
                taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("aws_project02")
                                .image(ContainerImage.fromRegistry("matheuspieropan/curso-spring-aws-2:1.0.0"))
                                .containerPort(9090)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this, "Service02LogGroup")
                                                .logGroupName("Service02")
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .build())
                                        .streamPrefix("Service02")
                                        .build()))
                                .environment(envVariables)
                                .build()).publicLoadBalancer(true).build();

        service.getTargetGroup().configureHealthCheck(new HealthCheck.Builder().path("/actuator/health").
                port("9090").healthyHttpCodes("200").build());

        ScalableTaskCount scalableTaskCount = service.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2).maxCapacity(4).build());

        scalableTaskCount.scaleOnCpuUtilization("Service01AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50).scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60)).build());
    }
}