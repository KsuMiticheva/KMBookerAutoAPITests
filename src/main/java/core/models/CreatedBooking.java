package core.models;

public class CreatedBooking {
    private int bookingid;
    private SingleBooking booking;

    public int getBookingid() {
        return bookingid;
    }

    public void setBookingid(int bookingid) {
        this.bookingid = bookingid;
    }

    public SingleBooking getBooking() {
        return booking;
    }

    public void setBooking(SingleBooking booking) {
        this.booking = booking;
    }
}
