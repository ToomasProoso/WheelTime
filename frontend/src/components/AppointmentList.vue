<template>
  <div>
    <h1>Rehvivahetuse ajad</h1>
    <select v-model="selectedWorkshop">
      <option value="manchester">Manchester</option>
      <option value="london">London</option>
    </select>
    <label for="fromDate">Alates kuupäev:</label>
    <input type="date" v-model="fromDate" id="fromDate" required>
    <label for="untilDate" v-if="selectedWorkshop === 'london'">Kuni kuupäev:</label>
    <input type="date" v-model="untilDate" id="untilDate" v-if="selectedWorkshop === 'london'">
    <label for="vehicleType">Sõiduki tüüp:</label>
    <select v-model="vehicleType" id="vehicleType">
      <option value="">Kõik</option>
      <option value="Sõiduauto">Sõiduauto</option>
      <option value="Veoauto">Veoauto</option>
    </select>
    <button @click="fetchAppointments">Otsi aegu</button>
    <ul>
      <li v-for="appointment in filteredAppointments" :key="appointment.id" @click="selectAppointment(appointment)">
        {{ appointment.time }} - {{ appointment.vehicleType || 'Pole määratud' }} - {{ appointment.workshop }} - {{ appointment.address }}
      </li>
    </ul>
  </div>
</template>

<script>
import { getAvailableAppointments } from '@/services/api';

export default {
  data() {
    return {
      appointments: [],
      selectedWorkshop: 'manchester',
      fromDate: '',
      untilDate: '',
      vehicleType: '',
      selectedAppointment: null
    };
  },
  computed: {
    filteredAppointments() {
      const now = new Date();
      return this.appointments.filter(appointment => {
        const appointmentDate = new Date(appointment.time);
        const fromDate = new Date(this.fromDate);
        const untilDate = new Date(this.untilDate);
        return appointmentDate >= now && appointmentDate >= fromDate && (!this.untilDate || appointmentDate <= untilDate);
      });
    }
  },
  methods: {
    async fetchAppointments() {
      console.log(`Fetching appointments for workshop: ${this.selectedWorkshop}`);
      try {
        this.appointments = await getAvailableAppointments(this.selectedWorkshop, this.fromDate, this.selectedWorkshop === 'london' ? this.untilDate : this.fromDate, this.vehicleType);
        console.log('Received appointments:', this.appointments);
      } catch (error) {
        console.error('Error fetching appointments:', error);
      }
    },
    selectAppointment(appointment) {
      this.selectedAppointment = appointment;
      this.$emit('appointment-selected', appointment);
    }
  }
};
</script>
