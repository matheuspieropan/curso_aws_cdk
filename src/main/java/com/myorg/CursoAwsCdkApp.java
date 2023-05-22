package com.myorg;

import software.amazon.awscdk.App;

public class CursoAwsCdkApp {

    public static void main(final String[] args) {
        App app = new App();

        VpcStack vpc = new VpcStack(app, "Vpc");
        ClusterStack cluster = new ClusterStack(app, "Cluster", vpc.getVpc());
        Service01Stack service01 = new Service01Stack(app, "Service01", cluster.getCluster());
        RdsStack rdsStack = new RdsStack(app, "Rds", vpc.getVpc());

        rdsStack.addDependency(vpc);
        cluster.addDependency(vpc);
        service01.addDependency(cluster);
        service01.addDependency(rdsStack);
        app.synth();
    }
}