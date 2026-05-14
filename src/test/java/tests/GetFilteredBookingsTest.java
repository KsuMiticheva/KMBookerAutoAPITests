package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import core.models.Bookingdates;
import core.models.SingleBooking;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetFilteredBookingsTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private SingleBooking firstBooking;
    private SingleBooking secondBooking;
    private SingleBooking thirdBooking;
    private SingleBooking fourthBooking;
    private Bookingdates dates;
    private int firstBookingId;
    private int secondBookingId;
    private int thirdBookingId;
    private int fourthBookingId;

    @BeforeAll
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        createTestBookings();
    }

    @Step("Create test bookings to use in the test")
    public void createTestBookings() {
        dates = new Bookingdates("2033-11-11", "2033-12-12");
        firstBooking = new SingleBooking("Name1", "Lalala", 500, true, dates, "twin bed");
        secondBooking = new SingleBooking("Name2", "Doe", 777, true, dates, "twin bed");
        thirdBooking = new SingleBooking("Name2", "Smith", 900, false, dates, "non-smoking room");
        fourthBooking = new SingleBooking("Name2", "Lalala", 999, false, dates, "non-smoking room");

        Response firstResponse = step("Send POST request to create 1st booking", () -> {
            String requestBody = objectMapper.writeValueAsString(firstBooking);
            return apiClient.createBooking(requestBody);
        });

        firstBookingId = firstResponse.jsonPath().getInt("bookingid");

        Response secondResponse = step("Send POST request to create 2nd booking", () -> {
            String requestBody = objectMapper.writeValueAsString(secondBooking);
            return apiClient.createBooking(requestBody);
        });

        secondBookingId = secondResponse.jsonPath().getInt("bookingid");

        Response thirdResponse = step("Send POST request to create 3rd booking", () -> {
            String requestBody = objectMapper.writeValueAsString(thirdBooking);
            return apiClient.createBooking(requestBody);
        });

        thirdBookingId = thirdResponse.jsonPath().getInt("bookingid");

        Response fourthResponse = step("Send POST request to create 4th booking", () -> {
            String requestBody = objectMapper.writeValueAsString(fourthBooking);
            return apiClient.createBooking(requestBody);
        });

        fourthBookingId = fourthResponse.jsonPath().getInt("bookingid");
    }

        @Test
        @Feature("Booking")
        @Severity(SeverityLevel.NORMAL)
        @Owner("ksenia miticheva")
        public void testGetFilteredBookingsByFirstname() {

        List<Booking> fnameBookings = step("Send GET request to collect list of bookings filtered by first name", () -> {
            Map<String, String> params = new HashMap<>();
            params.put("firstname", "Name2");
            Response fnameResponse = apiClient.getBookingByQuery(params);
            assertThat(fnameResponse.statusCode()).isEqualTo(200);
            assertThat(fnameResponse).isNotNull();
            List<Booking> bookings = objectMapper.readValue(fnameResponse.body().asString(), new TypeReference<List<Booking>>() {
            });
            return bookings;
        });

        step("Validate bookings in the list", () -> {
            for (Booking booking : fnameBookings) {
                Response onebooking = apiClient.getBookingById(booking.getBookingid());
                assertEquals(onebooking.jsonPath().getString("firstname"), "Name2");
            }
        });
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.NORMAL)
    @Owner("ksenia miticheva")
    public void testGetFilteredBookingsByTwoNames() {

        List<Booking> bothNamesBookings = step("Send GET request to collect list of bookings filtered by first and last names", () -> {
            Map<String, String> params = new HashMap<>();
            params.put("firstname", "Name2");
            params.put("lastname", "Lalala");
            Response bothNamesResponse = apiClient.getBookingByQuery(params);
            assertThat(bothNamesResponse.statusCode()).isEqualTo(200);
            assertThat(bothNamesResponse).isNotNull();
            List<Booking> bookings = objectMapper.readValue(bothNamesResponse.body().asString(), new TypeReference<List<Booking>>() {
            });
            return bookings;
        });

        step("Validate bookings in the list", () -> {
            for (Booking booking : bothNamesBookings) {
                Response onebooking = apiClient.getBookingById(booking.getBookingid());
                assertEquals(onebooking.jsonPath().getString("firstname"), "Name2");
                assertEquals(onebooking.jsonPath().getString("lastname"), "Lalala");
            }
        });
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.NORMAL)
    @Owner("ksenia miticheva")
    public void testGetFilteredBookingsbyDates() {

        List<Booking> dateBookings = step("Send GET request to collect list of bookings filtered by booking dates", () -> {
            Map<String, String> params = new HashMap<>();
            params.put("checkin", "2033-11-11");
            params.put("checkout", "2033-12-12");
            Response datesResponse = apiClient.getBookingByQuery(params);
            assertThat(datesResponse.statusCode()).isEqualTo(200);
            assertThat(datesResponse).isNotNull();
            List<Booking> bookings = objectMapper.readValue(datesResponse.body().asString(), new TypeReference<List<Booking>>() {
            });
            return bookings;
        });

        step("Validate bookings in the list", () -> {
            for (Booking booking : dateBookings) {
                Response onebooking = apiClient.getBookingById(booking.getBookingid());
                assertEquals(onebooking.jsonPath().getString("bookingdates.checkin"), "2033-11-11");
                assertEquals(onebooking.jsonPath().getString("bookingdates.checkout"), "2033-12-12");
            }
        });
    }

    @AfterAll
    @Step("Remove created bookings after test to clean the DB")
    public void tearDown() {
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(firstBookingId);
        assertThat(apiClient.getBookingById(firstBookingId).getStatusCode()).isEqualTo(404);
        apiClient.deleteBooking(secondBookingId);
        assertThat(apiClient.getBookingById(secondBookingId).getStatusCode()).isEqualTo(404);
        apiClient.deleteBooking(thirdBookingId);
        assertThat(apiClient.getBookingById(thirdBookingId).getStatusCode()).isEqualTo(404);
        apiClient.deleteBooking(fourthBookingId);
        assertThat(apiClient.getBookingById(fourthBookingId).getStatusCode()).isEqualTo(404);
    }
}

