<template>
  <div>
    <h1>Broneeri rehvivahetuse aeg</h1>
    <form @submit.prevent="bookAppointment">
      <label for="time">Aeg:</label>
      <input type="text" v-model="time" id="time" required readonly>

      <label for="contactInformation">Sisesta auto nr:</label>
      <input type="text" v-model="contactInformation" id="contactInformation" required>

      <button type="submit">Broneeri</button>
    </form>
    <p>{{ message }}</p>
  </div>
</template>

<script>
import { bookAppointment } from '@/services/api';

export default {
  props: ['selectedWorkshop', 'selectedAppointment'],
  data() {
    return {
      id: '',
      time: '',
      contactInformation: '',
      message: '',
      localSelectedWorkshop: this.selectedWorkshop
    };
  },
  methods: {
    async bookAppointment() {
      try {
        const requestBody = {
          contactInformation: this.contactInformation,
          time: this.time
        };
        const response = await bookAppointment(this.localSelectedWorkshop, this.id, requestBody);
        if (response.status === 200) {
          this.$emit('booking-success', {time: this.time, contactInformation: this.contactInformation});
          this.time = '';
          this.contactInformation = '';
        } else {
          this.message = `Broneering ebaõnnestus: ${response.data.message || 'Unknown error'}`;
        }
      } catch (error) {
        this.message = 'Broneering ebaõnnestus: ' + (error.response && error.response.data ? error.response.data.message : error.message);
      }
    }
  },
  watch: {
    selectedAppointment: {
      immediate: true,
      handler(appointmentData) {
        if (appointmentData) {
          this.id = appointmentData.appointment.id || appointmentData.appointment.uuid;
          this.time = new Date(appointmentData.appointment.time).toLocaleString('en-US', { timeZone: 'Europe/Moscow', hour12: false });
          this.localSelectedWorkshop = appointmentData.workshop;
        }
      }
    },
    selectedWorkshop(newWorkshop) {
      this.localSelectedWorkshop = newWorkshop;
      this.$emit('clear-message');
      this.message = '';
    }
  }
};
</script>
