package org.example.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;

// Guarda el vencimiento de la tarjeta como texto "2028-05" (columna CHAR(7))
@Converter
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth vencimiento) {
        return vencimiento == null ? null : vencimiento.toString();
    }

    @Override
    public YearMonth convertToEntityAttribute(String texto) {
        return texto == null ? null : YearMonth.parse(texto);
    }
}
