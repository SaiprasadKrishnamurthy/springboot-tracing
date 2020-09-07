package com.github.saiprasadkrishnamurthy.tracing.__inst.core;

import brave.Span;
import brave.Tracer;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.MDC;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@Component
public class ApplicationCodeAspect {

    private final ApplicationContext applicationContext;
    private final Tracer tracer;
    private List<NoSpan> nospans;

    public ApplicationCodeAspect(final ApplicationContext applicationContext, final Tracer tracer, final Environment environment) {
        this.applicationContext = applicationContext;
        this.tracer = tracer;
        nospans = Arrays.stream(environment.getProperty("nospans", "").split(","))
                .map(ns -> {
                    if (ns.trim().contains("::")) {
                        String fn = ns.substring(0, ns.indexOf("::"));
                        String methodName = ns.substring(ns.indexOf("::") + 2);
                        return new NoSpan(fn, methodName);
                    } else {
                        return new NoSpan(ns, null);
                    }
                }).collect(Collectors.toList());
    }

    @Bean
    public Advisor advisorBean(@Value("${instrumentation.base.package}") final String instrumentationBasePkg) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        String expressions = "execution(* " + instrumentationBasePkg + "..*.*(..)) && " +
                "!@annotation(org.springframework.context.annotation.Bean) && !@annotation(org.springframework.context.annotation.Configuration) && " +
                "!execution(* org.springframework..*.*(..)) && " +
                "!execution(* *..__inst..*.*(..))";
        pointcut.setExpression(expressions);
        return new DefaultPointcutAdvisor(pointcut, (MethodInterceptor) methodInvocation -> {
            Class<?> declaringClass = methodInvocation.getMethod().getDeclaringClass();
            String method = methodInvocation.getMethod().getName();
            String packageName = declaringClass.getPackageName();
            boolean noSpan = nospans.stream().anyMatch(ns -> {
                if (ns.getMethodName() != null) {
                    return ns.getFullyQualifiedName().equalsIgnoreCase(declaringClass.getName()) || ns.getMethodName().equalsIgnoreCase(method);
                } else {
                    return ns.getFullyQualifiedName().equalsIgnoreCase(declaringClass.getName()) || ns.getFullyQualifiedName().equalsIgnoreCase(packageName);
                }
            });
            if (!noSpan) {
                Span newSpan = tracer.nextSpan().name(declaringClass.getName() + "::" + method).start();
                newSpan.tag("className", declaringClass.getName());
                newSpan.tag("methodName", method);
                // TODO annotate parameters.
                TraceInfo traceInfo = methodInvocation.getMethod().getAnnotation(TraceInfo.class);
                if (traceInfo != null) {
                    newSpan.tag("documentation", traceInfo.documentation());
                    if (traceInfo.tags().length > 0) {
                        newSpan.tag("tags", String.join(",", traceInfo.tags()));
                    }
                }
                String params = Arrays.stream(methodInvocation.getMethod().getParameterTypes()).map(Class::getName).collect(Collectors.joining(","));
                if (StringUtils.hasText(params)) {
                    newSpan.tag("paramTypes", params);
                }
                try (Tracer.SpanInScope ws = tracer.withSpanInScope(newSpan.start())) {
                    return methodInvocation.proceed();
                } catch (Throwable err) {
                    StringWriter sw = new StringWriter();
                    PrintWriter out = new PrintWriter(sw);
                    err.printStackTrace(out);
                    newSpan.tag("exception", err.toString());
                    newSpan.tag("stackTrace", sw.toString());
                    out.close();
                    throw err;
                } finally {
                    MDC.getCopyOfContextMap().entrySet().stream()
                            .filter(entry -> entry.getKey().startsWith("tag$$") || entry.getKey().startsWith("annotation$$"))
                            .forEach(e -> {
                                if (e.getKey().startsWith("tag$$")) {
                                    newSpan.tag(e.getKey().replace("tag$$", ""), e.getValue());
                                } else {
                                    newSpan.annotate(e.getValue());
                                }
                            });
                    newSpan.finish();
                }
            } else {
                return methodInvocation.proceed();
            }
        });
    }
}
