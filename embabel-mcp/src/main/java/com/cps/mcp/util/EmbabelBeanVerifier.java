package com.cps.mcp.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmbabelBeanVerifier implements CommandLineRunner {

    private final ApplicationContext applicationContext;

    public EmbabelBeanVerifier(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println("       EMBABEL BEAN VERIFICATION RUNNER          ");
        System.out.println("=================================================");

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        List<String> embabelBeans = Arrays.stream(beanNames)
                .filter(name -> {
                    try {
                        Class<?> type = applicationContext.getType(name);
                        if (type != null) {
                            String typeName = type.getName();
                            return typeName.startsWith("com.embabel") || typeName.contains("embabel");
                        }
                    } catch (Exception e) {
                        // Ignore bean type resolution errors
                    }
                    return name.toLowerCase().contains("embabel");
                })
                .sorted()
                .collect(Collectors.toList());

        if (embabelBeans.isEmpty()) {
            System.out.println("No Embabel beans detected in the Spring ApplicationContext.");
        } else {
            System.out.println("Detected " + embabelBeans.size() + " Embabel bean(s):");
            for (String beanName : embabelBeans) {
                try {
                    Object bean = applicationContext.getBean(beanName);
                    System.out.println(" - " + beanName + " -> " + bean.getClass().getName());
                } catch (Exception ex) {
                    System.out.println(" - " + beanName + " -> (Unable to initialize instance: " + ex.getMessage() + ")");
                }
            }
        }
        System.out.println("=================================================");
    }
}
