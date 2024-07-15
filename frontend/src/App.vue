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
      this.bookingMessage = `Broneering õnnestus! Aeg: ${time}, Auto nr.: ${contactInformation}`;
      this.$refs.appointmentList.fetchAppointments();
    },
    clearMessage() {
      this.bookingMessage = '';
    }
  },
  watch: {
    selectedWorkshop() {
      this.clearMessage();
    }
  }
};
</script>
