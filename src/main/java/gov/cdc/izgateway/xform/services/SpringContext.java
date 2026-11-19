package gov.cdc.izgateway.xform.services;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Gives static access to Spring beans for objects that Spring does not create.
 */
@Component
@Lazy(false)
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * Creates the component. Spring sets the application context after creation.
     */
    public SpringContext() {
        // No state to initialize.
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * Gets the bean of a type from the application context.
     *
     * @param <T> the bean type
     * @param beanClass the bean class
     * @return the bean
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}

