<template>
  <div id="app">
    <AppointmentList @appointment-selected="handleAppointmentSelected" />
    <AppointmentBooking :selectedWorkshop="selectedWorkshop" :selectedAppointment="selectedAppointment" />
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
      selectedWorkshop: 'manchester'
    };
  },
  methods: {
    handleAppointmentSelected(appointment) {
      this.selectedAppointment = appointment;
    }
  }
};
</script>
