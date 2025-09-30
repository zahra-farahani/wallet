package com.snapppay.wallet.util;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

public class MessageSourceUtil {
    public static String getMessageIfExist(MessageSource messageSource, String key) {
        try {
            return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException noSuchMessageException) {
            //todo :: log
            return "";
        }
    }

}
