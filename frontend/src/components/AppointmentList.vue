<template>
  <div>
    <h1>Rehvivahetuse ajad</h1>
    <select v-model="selectedWorkshop">
      <option value="manchester">Manchester</option>
      <option value="london">London</option>
    </select>
    <label for="fromDate">Alates kuupäev:</label>
    <input type="date" v-model="fromDate" :min="minFromDate" id="fromDate" required>
    <label for="untilDate" v-if="selectedWorkshop === 'london'">Kuni kuupäev:</label>
    <input type="date" v-model="untilDate" :min="fromDate" id="untilDate" v-if="selectedWorkshop === 'london'">
    <label for="vehicleType">Sõiduki tüüp:</label>
    <select v-model="vehicleType" id="vehicleType">
      <option v-for="type in vehicleTypeOptions" :key="type" :value="type">{{ type }}</option>
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
      vehicleType: 'Sõiduauto',
      selectedAppointment: null
    };
  },
  computed: {
    minFromDate() {
      const today = new Date();
      return today.toISOString().split('T')[0];
    },
    vehicleTypeOptions() {
      return ['Sõiduauto', 'Veoauto'];
    },
    filteredAppointments() {
      const now = new Date();
      return this.appointments.filter(appointment => {
        const appointmentDate = new Date(appointment.time);
        const fromDate = new Date(this.fromDate);
        const untilDate = this.selectedWorkshop === 'london' ? new Date(this.untilDate) : fromDate;
        return appointmentDate >= now && appointmentDate >= fromDate && (!this.untilDate || appointmentDate <= untilDate);
      });
    }
  },
  watch: {
    selectedWorkshop() {
      this.resetData();
      this.fetchAppointments();
    },
    fromDate() {
      if (this.untilDate && new Date(this.untilDate) < new Date(this.fromDate)) {
        this.untilDate = '';
      }
      this.fetchAppointments();
    },
    untilDate() {
      this.fetchAppointments();
    },
    vehicleType() {
      this.fetchAppointments();
    }
  },
  methods: {
    async fetchAppointments() {
      console.log(`Fetching appointments for workshop: ${this.selectedWorkshop}`);
      try {
        this.appointments = await getAvailableAppointments(this.selectedWorkshop, this.fromDate, this.selectedWorkshop === 'london' ? this.untilDate : null, this.vehicleType);
        console.log('Received appointments:', this.appointments);
      } catch (error) {
        console.error('Error fetching appointments:', error);
      }
    },
    selectAppointment(appointment) {
      this.selectedAppointment = appointment;
      this.$emit('appointment-selected', { appointment, workshop: this.selectedWorkshop });
    },
    resetData() {
      this.appointments = [];
      this.fromDate = '';
      this.untilDate = '';
      this.vehicleType = 'Sõiduauto';
      this.selectedAppointment = null;
    }
  },
  created() {
    this.fetchAppointments();
  }
};
</script>
