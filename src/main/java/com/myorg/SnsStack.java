package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.constructs.Construct;

public class SnsStack extends Stack {

    final SnsTopic topic;

    public SnsStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public SnsStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        topic = SnsTopic.Builder.create(
                Topic.Builder.create(this, "produtoEventosTopic")
                        .topicName("produto-eventos")
                        .build()).build();

        topic.getTopic().addSubscription(
                EmailSubscription.Builder.create("matheus.pieropan@viannasempre.com.br").json(true).build());
    }

    public SnsTopic getTopic() {
        return topic;
    }
}