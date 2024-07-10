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
import {getAvailableAppointments} from '@/services/api';

export default {
  data() {
    return {
      appointments: [],
      selectedWorkshop: 'manchester'
    };
  },
  methods: {
    async fetchAppointments() {
      console.log(`Fetching appointments for workshop: ${this.selectedWorkshop}`);
      try {
        const response = await getAvailableAppointments(this.selectedWorkshop);
        this.appointments = response.data;
      } catch (error) {
        console.error('Error fetching appointments:', error);
      }
    }
  }
};
</script>
