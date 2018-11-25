package com.forgerock.edu.contactlist.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author vrg
 */
public class LocalDateTimeXmlAdapter extends XmlAdapter<String, LocalDateTime> {
    
    private final static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    @Override
    public String marshal(LocalDateTime dateTime) throws Exception {
        return DATETIME_FORMATTER.format(dateTime);
    }

    @Override
    public LocalDateTime unmarshal(String dateTimeAsString) throws Exception {
        return LocalDateTime.parse(dateTimeAsString, DATETIME_FORMATTER);
    }
}
