<template>
  <div id="app">
    <AppointmentList />
    <AppointmentBooking />
  </div>
</template>

<script>
import AppointmentList from './components/AppointmentList.vue';
import AppointmentBooking from './components/AppointmentBooking.vue';

export default {
  name: 'App',
  components: {
    AppointmentList,
    AppointmentBooking
  }
};
</script>

<style>
/* jäta meelde lisa stiilid  */
</style>
