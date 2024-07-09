<template>
  <div>
    <h1>Rehvivahetuse ajad</h1>
    <select v-model="selectedWorkshop">
      <option value="manchester">Manchester</option>
      <option value="london">London</option>
    </select>
    <button @click="fetchAppointments">Otsi aegu</button>
    <ul>
      <li v-for="appointment in appointments" :key="appointment.id">
        {{ appointment.time }} - {{ appointment.vehicleType }}
      </li>
    </ul>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      appointments: [],
      selectedWorkshop: 'manchester'
    };
  },
  methods: {
    async fetchAppointments() {
      const response = await axios.get(`/api/appointments?workshop=${this.selectedWorkshop}`);
      this.appointments = response.data;
    }
  }
};
</script>
