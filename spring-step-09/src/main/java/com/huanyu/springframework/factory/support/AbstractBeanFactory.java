package com.huanyu.springframework.factory.support;


import com.huanyu.springframework.BeansException;
import com.huanyu.springframework.factory.FactoryBean;
import com.huanyu.springframework.factory.config.BeanDefinition;
import com.huanyu.springframework.factory.config.BeanPostProcessor;
import com.huanyu.springframework.factory.config.ConfigurableBeanFactory;
import com.huanyu.springframework.utils.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: AbstractBeanFactory
 * Package: com.huanyu.springframework.support
 * Description:
 * AbstractBeanFactory 首先继承了 DefaultSingletonBeanRegistry，也就具备了使用单例注册类方法。
 * 实现 BeanFactory 接口。
 *
 * @Author: 寰宇
 * @Create: 2024/4/11 15:57
 * @Version: 1.0
 */
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {

    // 在 createBean 中应用 BeanPostProcessors
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<BeanPostProcessor>();

    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return doGetBean(name, args);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return (T) getBean(name);
    }

    // 获取bean
    protected <T> T doGetBean(final String name, final Object[] args) {
        // 从单例中获取bean
        Object bean = getSingleton(name);
        if (bean != null)
            // 如果是 FactoryBean，则需要调用 FactoryBean#getObject 方法获取真正的对象实例
            return (T) getObjectForBeanInstance(bean, name);
        // 单例中没有bean 从AbstractAutowireCapableBeanFactory实现获取
        BeanDefinition beanDefinition = getBeanDefinition(name);
        bean = createBean(name, beanDefinition, args);
        // 获取真正的对象实例
        return (T) getObjectForBeanInstance(bean, name);
    }

    private Object getObjectForBeanInstance(Object beanInstance, String beanName) {
        // 检查 beanInstance 是否为 FactoryBean 的实例
        if (!(beanInstance instanceof FactoryBean)) {
            // 如果不是 FactoryBean，则直接返回该对象实例
            return beanInstance;
        }

        // 尝试从工厂缓存中获取该 FactoryBean 对应的对象实例
        Object object = getCachedObjectForFactoryBean(beanName);

        if (object == null) {
            // 如果工厂缓存中不存在该对象实例，则需要通过 FactoryBean 获取对象实例
            FactoryBean<?> factoryBean = (FactoryBean<?>) beanInstance;
            object = getObjectFromFactoryBean(factoryBean, beanName);
        }

        return object;
    }

    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException;

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor){
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
    }

    // 返回将应用于使用此工厂创建的 bean 的 BeanPostProcessors 列表。
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    // 获取bean的类加载器
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    // 返回保存的 Bean 类加载器对象
    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

}
