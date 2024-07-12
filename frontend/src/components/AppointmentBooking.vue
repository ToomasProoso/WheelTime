<template>
  <div>
    <h1>Broneeri rehvivahetuse aeg</h1>
    <form @submit.prevent="bookAppointment">
      <label for="id">Aeg:</label>
      <input type="text" v-model="time" id="time" required readonly>
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
      message: ''
    };
  },
  methods: {
    async bookAppointment() {
      try {
        const requestBody = {
          contactInformation: 'string', // replace this with actual contact information if needed
          workshop: this.selectedWorkshop
        };
        await bookAppointment(this.selectedWorkshop, this.id, requestBody);
        this.message = 'Broneering õnnestus!';
      } catch (error) {
        this.message = 'Broneering ebaõnnestus: ' + (error.response && error.response.data ? error.response.data.message : error.message);
      }
    }
  },
  watch: {
    selectedAppointment: {
      immediate: true,
      handler(appointment) {
        if (appointment) {
          this.id = appointment.id || appointment.uuid;
          this.time = appointment.time;
        }
      }
    }
  }
};
</script>
