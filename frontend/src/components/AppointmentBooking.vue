<template>
  <div>
    <h1>Broneeri rehvivahetuse aeg</h1>
    <form @submit.prevent="bookAppointment">
      <label for="id">Aja ID:</label>
      <input type="text" v-model="id" id="id" required>
      <label for="vehicleType">Sõiduki tüüp:</label>
      <input type="text" v-model="vehicleType" id="vehicleType" required>
      <label for="time">Aeg:</label>
      <input type="text" v-model="time" id="time" required>
      <button type="submit">Broneeri</button>
    </form>
    <p>{{ message }}</p>
  </div>
</template>

<script>
import { bookAppointment } from '@/services/api';

export default {
  data() {
    return {
      id: '',
      vehicleType: '',
      time: '',
      workshop: 'manchester', // Või "london"
      message: ''
    };
  },
  methods: {
    async bookAppointment() {
      try {
        const response = await bookAppointment(this.workshop, this.id, {
          vehicleType: this.vehicleType,
          time: this.time
        });
        this.message = 'Broneering õnnestus!';
      } catch (error) {
        this.message = 'Broneering ebaõnnestus: ' + error.response.data.message;
      }
    }
  }
};
</script>
