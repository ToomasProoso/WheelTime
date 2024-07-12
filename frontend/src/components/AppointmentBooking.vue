<template>
  <div>
    <h1>Broneeri rehvivahetuse aeg</h1>
    <form @submit.prevent="bookAppointment">
      <label for="time">Aeg:</label>
      <input type="text" v-model="time" id="time" required readonly>

      <label for="contactInformation">Nimi või auto nr:</label>
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
      contactInformation: '', // Dünaamiline välja
      message: '',
      localSelectedWorkshop: this.selectedWorkshop
    };
  },
  methods: {
    async bookAppointment() {
      console.log('Booking appointment for workshop:', this.localSelectedWorkshop); // Debug log
      try {
        const requestBody = {
          contactInformation: this.contactInformation,
          time: this.time
        };
        const response = await bookAppointment(this.localSelectedWorkshop, this.id, requestBody);
        console.log('Booking response:', response); // Debug log
        this.message = response.status === 'success' ? 'Broneering õnnestus!' : `Broneering ebaõnnestus: ${response.message}`;
      } catch (error) {
        console.error('Booking error:', error); // Debug log
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
          this.time = appointmentData.appointment.time;
          this.localSelectedWorkshop = appointmentData.workshop;
          console.log('Selected appointment:', appointmentData); // Debug log
          console.log('Selected workshop:', this.localSelectedWorkshop); // Debug log
        }
      }
    },
    selectedWorkshop(newWorkshop) {
      this.localSelectedWorkshop = newWorkshop;
    }
  }
};
</script>
