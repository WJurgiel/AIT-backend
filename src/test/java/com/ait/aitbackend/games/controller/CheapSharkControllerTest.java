package com.ait.aitbackend.games.controller;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDetailsDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.dto.cheapshark.DealsPageResponse;
import com.ait.aitbackend.games.service.CheapSharkFilterService;
import com.ait.aitbackend.games.service.CheapSharkService;
import com.ait.aitbackend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheapSharkController.class)
class CheapSharkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheapSharkService cheapSharkService;

    @MockitoBean
    private CheapSharkFilterService filterService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    /**
     * Sprawdza, czy endpoint z listą promocji poprawnie filtruje, paginuje i zwraca oferty od zewnętrznego API na podstawie przekazanego parametru platformId.
     * @throws Exception
     */
    @Test
    void shouldReturnDealsPayload() throws Exception {
        CheapSharkDealDto deal = new CheapSharkDealDto(
                "THEWITCHER3WILDHUNT",
                "The Witcher 3: Wild Hunt",
                "/game/the-witcher-3-wild-hunt/",
                "abc123",
                "1",
                "112330",
                "7.99",
                "39.99",
                "1",
                "80.020005",
                "93",
                "Overwhelmingly Positive",
                "96",
                "234755",
                "292030",
                1431907200L,
                1766082419L,
                "9.9",
                "thumb"
        );

        when(cheapSharkService.getDeals(1)).thenReturn(List.of(deal));
        when(filterService.filter(anyList(), any(), any(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new DealsPageResponse(List.of(deal), 0, 20, 1, 1, true));

        mockMvc.perform(get("/api/cheapshark/deals")
                        .param("platformId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].dealID").value("abc123"));
    }

    /**
     * Upewnia się, że w przypadku dostarczenia w URL zarówno ID platformy, jak i sklepu, kontroler słusznie priorytetyzuje ID platformy.
     * @throws Exception
     */
    @Test
    void shouldPreferPlatformIdOverStoreIdWhenBothProvided() throws Exception {
        when(cheapSharkService.getDeals(7)).thenReturn(List.of());
        when(filterService.filter(anyList(), any(), any(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new DealsPageResponse(List.of(), 0, 20, 0, 0, true));

        mockMvc.perform(get("/api/cheapshark/deals")
                        .param("platformId", "7")
                        .param("storeID", "1"))
                .andExpect(status().isOk());
    }

    /**
     * Weryfikuje, czy zapytanie o oferty z pominiętym opcjonalnym identyfikatorem sklepu przetwarza się pomyślnie i zwraca status 200 OK.
     * @throws Exception
     */
    @Test
    void shouldReturn200WhenStoreIdIsMissing() throws Exception {
        when(cheapSharkService.getDeals(null)).thenReturn(List.of());
        when(filterService.filter(anyList(), any(), any(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new DealsPageResponse(List.of(), 0, 20, 0, 0, true));

        mockMvc.perform(get("/api/cheapshark/deals"))
                .andExpect(status().isOk());
    }

    /**
     * Zabezpiecza przed wysypywaniem się serwera – weryfikuje czy błąd 404 rzucony przez zewnętrzne API propaguje się z powrotem jako HTTP 404, a nie błąd serwera 500
     * @throws Exception
     */
    @Test
    void shouldReturnNotFoundFromExternalApiInsteadOf500() throws Exception {
        when(cheapSharkService.getDealById("missing-deal"))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        mockMvc.perform(get("/api/cheapshark/game/details").param("id", "missing-deal"))
                .andExpect(status().isNotFound());
    }

    /**
     * Testuje endpoint szczegółów oferty; upewnia się, że wywołanie poprawnego ID zwraca złożony obiekt zawierający zmapowane informacje o najtańszej cenie.
     * @throws Exception
     */
    @Test
    void shouldReturnDealDetailsDto() throws Exception {
        CheapSharkDealDetailsDto dto = new CheapSharkDealDetailsDto(
                new CheapSharkDealDetailsDto.GameInfoDto(
                        "1",
                        "112330",
                        "The Witcher 3: Wild Hunt",
                        "292030",
                        "7.99",
                        "39.99",
                        "Overwhelmingly Positive",
                        "96",
                        "234755",
                        "93",
                        "/game/the-witcher-3-wild-hunt/",
                        1431907200L,
                        "N/A",
                        null,
                        "thumb"
                ),
                List.of(),
                new CheapSharkDealDetailsDto.CheapestPriceDto("3.99", 1766082419L)
        );

        when(cheapSharkService.getDealById("deal-123")).thenReturn(dto);

        mockMvc.perform(get("/api/cheapshark/game/details").param("id", "deal-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("The Witcher 3: Wild Hunt"));
    }

    /**
     * Sprawdza, czy endpoint przekierowania prawidłowo przetwarza dealID i rzuca statusem HTTP 302 Found, przenosząc z odpowiednim nagłówkiem Location do sklepu.
     * @throws Exception
     */
    @Test
    void shouldRedirectToCheapSharkStoreUrl() throws Exception {
        String redirectUrl = "https://www.cheapshark.com/redirect?dealID=x77a6faCQSCDjyCF%2Fe6U0ed%2B202eYPAdrpMjRjoJvYc%3D";
        when(cheapSharkService.buildRedirectUrl("x77a6faCQSCDjyCF%2Fe6U0ed%2B202eYPAdrpMjRjoJvYc%3D"))
                .thenReturn(redirectUrl);

        mockMvc.perform(get("/api/cheapshark/redirect")
                        .param("dealID", "x77a6faCQSCDjyCF%2Fe6U0ed%2B202eYPAdrpMjRjoJvYc%3D"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", redirectUrl));
    }

    /**
     * Upewnia się, że zapytanie do endpointu przekierowania całkowicie ignorującego wymagany parametr zapytania skutkuje odpowiedzią 400 Bad Request.
     * @throws Exception
     */
    @Test
    void shouldReturn400WhenRedirectParamIsMissing() throws Exception {
        mockMvc.perform(get("/api/cheapshark/redirect"))
                .andExpect(status().isBadRequest());
    }
}

