package com.snapppay.wallet.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

@Slf4j
public class MessageSourceUtil {
    public static String getMessageIfExist(MessageSource messageSource, String key) {
        try {
            return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException noSuchMessageException) {
            log.warn("No message found for key '{}'", key);
            return "";
        }
    }
}
