package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Bookingdates;
import core.models.CreatedBooking;
import core.models.SingleBooking;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private SingleBooking newBooking;
    private CreatedBooking createdBooking;

    @BeforeEach
    public void setUp() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        newBooking = step("Create new booking object for use in the test", () -> {
            SingleBooking booking = new SingleBooking();
            booking.setFirstname("Jill");
            booking.setLastname("Valentine");
            booking.setTotalprice(1250);
            booking.setDepositpaid(true);
            booking.setBookingdates(new Bookingdates("2026-06-01", "2026-07-01"));
            booking.setAdditionalneeds("Non-smoking room");
            return booking;
        });
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("ksenia miticheva")
    public void testCreateBooking() throws JsonProcessingException {
        Response response = step("Send POST request to create new booking", () -> {
            String requestBody = objectMapper.writeValueAsString(newBooking);
            return apiClient.createBooking(requestBody);
        });

        step("Confirm that request was sent successfully", () ->
                assertThat(response.getStatusCode()).isEqualTo(200));

        createdBooking = step("Extract created booking from response", () -> {
            String responseBody = response.getBody().asString();
            return objectMapper.readValue(responseBody, CreatedBooking.class);
        });

        step("Check that received booking is not empty", () ->
                assertThat(createdBooking).isNotNull());

        step("Compare fields of received booking against sent booking", () -> {
            assertEquals(newBooking.getFirstname(), createdBooking.getBooking().getFirstname());
            assertEquals(newBooking.getLastname(), createdBooking.getBooking().getLastname());
            assertEquals(newBooking.getTotalprice(), createdBooking.getBooking().getTotalprice());
            assertEquals(newBooking.isDepositpaid(), createdBooking.getBooking().isDepositpaid());
            assertEquals(newBooking.getBookingdates().getCheckin(), createdBooking.getBooking().getBookingdates().getCheckin());
            assertEquals(newBooking.getBookingdates().getCheckout(), createdBooking.getBooking().getBookingdates().getCheckout());
            assertEquals(newBooking.getAdditionalneeds(), createdBooking.getBooking().getAdditionalneeds());
        });
    }

    @AfterEach
    @Step("Remove created booking after test to clean the DB")
    public void tearDown() {
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBooking.getBookingid());

        assertThat(apiClient.getBookingById(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
    }
}
