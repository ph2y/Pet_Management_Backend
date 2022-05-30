package com.sju18.petmanagement.global.message;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class MessageConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localResolver=new SessionLocaleResolver();
        localResolver.setDefaultLocale(Locale.US);
        return localResolver;
    }

    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(getValidationMessageSource());
        return bean;
    }

    @Bean
    public static MessageSource getValidationMessageSource() {
        ReloadableResourceBundleMessageSource msgSrc = new ReloadableResourceBundleMessageSource();
        msgSrc.setBasenames(
                "static/messages/account/validation",
                "static/messages/pet/validation",
                "static/messages/community/validation",
                "static/messages/map/validation",
                "static/messages/position/validation"
        );
        return msgSrc;
    }

    @Bean
    public static MessageSource getStorageMessageSource() {
        ReloadableResourceBundleMessageSource msgSrc = new ReloadableResourceBundleMessageSource();
        msgSrc.setBasenames(
                "static/messages/storage/error"
        );
        return msgSrc;
    }

    @Bean
    public static MessageSource getAccountMessageSource() {
        ReloadableResourceBundleMessageSource msgSrc = new ReloadableResourceBundleMessageSource();
        msgSrc.setBasenames(
                "static/messages/account/error",
                "static/messages/account/response",
                "static/messages/account/validation"
        );
        return msgSrc;
    }

    @Bean
    public static MessageSource getPetMessageSource() {
        ReloadableResourceBundleMessageSource msgSrc = new ReloadableResourceBundleMessageSource();
        msgSrc.setBasenames(
                "static/messages/pet/error",
                "static/messages/pet/response",
                "static/messages/pet/validation"
        );
        return msgSrc;
    }

    @Bean
    public static MessageSource getCommunityMessageSource() {
        ReloadableResourceBundleMessageSource msgSrc = new ReloadableResourceBundleMessageSource();
        msgSrc.setDefaultEncoding("UTF-8");
        msgSrc.setBasenames(
                "static/messages/community/error",
                "static/messages/community/response",
                "static/messages/community/validation",
                "static/messages/community/notification",
                "static/messages/position/validation"
        );
        return msgSrc;
    }

    @Bean
    public static MessageSource getMapMessageSource() {
        ReloadableResourceBundleMessageSource msgSrc = new ReloadableResourceBundleMessageSource();
        msgSrc.setBasenames(
                "static/messages/map/error",
                "static/messages/map/response",
                "static/messages/map/validation",
                "static/messages/position/validation"
        );
        return msgSrc;
    }

    @Bean
    public static MessageSource getShopMessageSource() {
        ReloadableResourceBundleMessageSource msgSrc = new ReloadableResourceBundleMessageSource();
        msgSrc.setBasenames(
                "static/messages/shop/error",
                "static/messages/shop/response",
                "static/messages/shop/validation"
        );
        return msgSrc;
    }
}
