<template>
  <div>
    <h1>Broneeri rehvivahetuse aeg</h1>
    <form @submit.prevent="bookAppointment">
      <label for="id">Aja ID:</label>
      <input type="text" v-model="id" id="id" required readonly>
      <label for="vehicleType">Sõiduki tüüp:</label>
      <input type="text" v-model="vehicleType" id="vehicleType" required>
      <label for="time">Aeg:</label>
      <input type="text" v-model="time" id="time" required readonly>
      <button type="submit">Broneeri</button>
    </form>
    <p>{{ message }}</p>
  </div>
</template>

<script>
import {bookAppointment} from '@/services/api';

export default {
  props: ['selectedWorkshop', 'selectedAppointment'],
  data() {
    return {
      id: '',
      vehicleType: '',
      time: '',
      message: ''
    };
  },
  methods: {
    async bookAppointment() {
      try {
        await bookAppointment(this.selectedWorkshop, this.id, {
          vehicleType: this.vehicleType,
          time: this.time
        });
        this.message = 'Broneering õnnestus!';
      } catch (error) {
        this.message = 'Broneering ebaõnnestus: ' + error.response.data.message;
      }
    }
  },
  watch: {
    selectedAppointment: {
      immediate: true,
      handler(appointment) {
        if (appointment) {
          this.id = appointment.id || appointment.uuid;
          this.vehicleType = appointment.vehicleType || '';
          this.time = appointment.time;
        }
      }
    }
  }
};
</script>
