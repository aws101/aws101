package dev.stratospheric.todoapp.cdk;

import dev.stratospheric.cdk.ApplicationEnvironment;
import dev.stratospheric.cdk.Network;
import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.certificatemanager.DnsValidatedCertificate;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.route53.*;
import software.amazon.awscdk.services.route53.targets.LoadBalancerTarget;
import software.amazon.awscdk.services.ssm.StringParameter;

import java.util.List;

class DomainStack extends Stack {

  private static final String PARAMETER_LOAD_BALANCER_ARN = "loadBalancerArn";
  private static final String PARAMETER_LOAD_BALANCER_SECURITY_GROUP_ID = "loadBalancerSecurityGroupId";
  private static final String PARAMETER_LOAD_BALANCER_DNS_NAME = "loadBalancerDnsName";
  private static final String PARAMETER_LOAD_CANONICAL_HOSTED_ZONE_ID = "loadBalancerCanonicalHostedZoneId";

  private final ApplicationEnvironment applicationEnvironment;

  public DomainStack(
    final Construct scope,
    final String id,
    final Environment awsEnvironment,
    final ApplicationEnvironment applicationEnvironment,
    final String hostedZoneDomain,
    final String applicationDomain) {
    super(scope, id, StackProps.builder()
      .stackName(applicationEnvironment.prefix("Domain"))
      .env(awsEnvironment).build());

    this.applicationEnvironment = applicationEnvironment;

    IHostedZone hostedZone = HostedZone.fromLookup(this, "HostedZone", HostedZoneProviderProps.builder()
      .domainName(hostedZoneDomain)
      .build());

    DnsValidatedCertificate websiteCertificate = DnsValidatedCertificate.Builder.create(this, "WebsiteCertificate")
      .hostedZone(hostedZone)
      .region(awsEnvironment.getRegion())
      .domainName(applicationDomain)
      .subjectAlternativeNames(List.of(applicationDomain))
      .build();

    IApplicationLoadBalancer applicationLoadBalancer = ApplicationLoadBalancer.fromApplicationLoadBalancerAttributes(
      this,
      "LoadBalancer",
      ApplicationLoadBalancerAttributes.builder()
        .loadBalancerArn(getParameterLoadbalancerArn(this, applicationEnvironment))
        .securityGroupId(getParameterLoadbalancerSecurityGroupId(this, applicationEnvironment))
        .loadBalancerCanonicalHostedZoneId(getParameterLoadBalancerCanonicalHostedZoneId(this, applicationEnvironment))
        .loadBalancerDnsName(getParameterLoadBalancerDnsName(this, applicationEnvironment))
        .build()
    );
    ApplicationListener listener = applicationLoadBalancer.addListener(
      "HttpListener",
      BaseApplicationListenerProps.builder()
        .protocol(ApplicationProtocol.HTTP)
        .port(80)
        .build()
    );
    ListenerAction redirectAction = ListenerAction.redirect(
      RedirectOptions.builder()
        .port("443")
        .build()
    );
    listener.addAction(
      "Redirect",
      AddApplicationActionProps.builder()
        .action(redirectAction)
        .build()
    );
    ApplicationListenerRule applicationListenerRule = new ApplicationListenerRule(
      this,
      "HttpListenerRule",
      ApplicationListenerRuleProps.builder()
        .listener(listener)
        .priority(1)
        .conditions(List.of(ListenerCondition.pathPatterns(List.of("*"))))
        .action(redirectAction)
        .build()
    );

    ARecord aRecord = ARecord.Builder.create(this, "ARecord")
      .recordName(applicationDomain)
      .zone(hostedZone)
      .target(RecordTarget.fromAlias(new LoadBalancerTarget(applicationLoadBalancer)))
      .build();

    applicationEnvironment.tag(this);
  }

  private static String createParameterName(ApplicationEnvironment applicationEnvironment, String parameterName) {
    return applicationEnvironment.getEnvironmentName() + "-Network-" + parameterName;
  }

  private static String getParameterLoadbalancerArn(Construct scope, ApplicationEnvironment applicationEnvironment) {
    return StringParameter.valueForStringParameter(
      scope,
      createParameterName(applicationEnvironment, PARAMETER_LOAD_BALANCER_ARN)
    );
  }

  private static String getParameterLoadbalancerSecurityGroupId(Construct scope, ApplicationEnvironment applicationEnvironment) {
    return StringParameter.valueForStringParameter(
      scope,
      createParameterName(applicationEnvironment, PARAMETER_LOAD_BALANCER_SECURITY_GROUP_ID)
    );
  }

  private static String getParameterLoadBalancerDnsName(Construct scope, ApplicationEnvironment applicationEnvironment) {
    return StringParameter.valueForStringParameter(
      scope,
      createParameterName(applicationEnvironment, PARAMETER_LOAD_BALANCER_DNS_NAME)
    );
  }

  private static String getParameterLoadBalancerCanonicalHostedZoneId(Construct scope, ApplicationEnvironment applicationEnvironment) {
    return StringParameter.valueForStringParameter(
      scope,
      createParameterName(applicationEnvironment, PARAMETER_LOAD_CANONICAL_HOSTED_ZONE_ID)
    );
  }
}
