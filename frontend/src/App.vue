<template>
  <div id="app">
    <AppointmentList v-model:selectedWorkshop="selectedWorkshop" @appointment-selected="handleAppointmentSelected" ref="appointmentList" @clear-message="clearMessage" />
    <AppointmentBooking :selectedWorkshop="selectedWorkshop" :selectedAppointment="selectedAppointment" @booking-success="handleBookingSuccess" />
    <p>{{ bookingMessage }}</p>
  </div>
</template>

<script>
import AppointmentList from './components/AppointmentList.vue';
import AppointmentBooking from './components/AppointmentBooking.vue';

export default {
  components: {
    AppointmentList,
    AppointmentBooking
  },
  data() {
    return {
      selectedAppointment: null,
      selectedWorkshop: 'manchester',
      bookingMessage: ''
    };
  },
  methods: {
    handleAppointmentSelected(appointment) {
      this.selectedAppointment = appointment;
    },
    handleBookingSuccess({ time, contactInformation }) {
      this.bookingMessage = `Broneering õnnestus, ootame teid ${this.formatDate(time)}, Auto nr: ${contactInformation}`;
      this.$refs.appointmentList.fetchAppointments();
    },
    clearMessage() {
      this.bookingMessage = '';
    },
    formatDate(dateString) {
      const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', timeZone: 'Europe/Moscow', hour12: false };
      return new Date(dateString).toLocaleString('et-EE', options);
    }
  },
  watch: {
    selectedWorkshop() {
      this.clearMessage();
    }
  }
};
</script>
