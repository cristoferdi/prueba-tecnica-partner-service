package com.telco.backend.web;

import com.telco.backend.domain.SaleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaleStatusParamParserTest {

    private final SaleStatusParamParser parser = new SaleStatusParamParser();

    @Test
    void parsesValidStatus() {
        assertThat(parser.parse("PENDIENTE")).isEqualTo(SaleStatus.PENDIENTE);
        assertThat(parser.parse("APROBADA")).isEqualTo(SaleStatus.APROBADA);
        assertThat(parser.parse("RECHAZADA")).isEqualTo(SaleStatus.RECHAZADA);
    }

    @Test
    void parsesNullAndEmptyAsNull() {
        assertThat(parser.parse(null)).isNull();
        assertThat(parser.parse("")).isNull();
    }

    @Test
    void throwsBadRequestOnInvalidStatus() {
        assertThatThrownBy(() -> parser.parse("INVALIDO"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode.value")
                .isEqualTo(400);
    }
}
