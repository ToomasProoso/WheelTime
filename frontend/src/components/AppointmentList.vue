<template>
  <div>
    <h1>Rehvivahetuse ajad</h1>
    <select :value="selectedWorkshop" @change="updateSelectedWorkshop">
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
    <ul v-if="filteredAppointments.length">
      <li v-for="appointment in paginatedAppointments" :key="appointment.id" @click="selectAppointment(appointment)">
        {{ formatDate(appointment.time) }} - {{ appointment.vehicleType || 'Pole määratud' }} - {{ appointment.workshop }} - {{ appointment.address }}
      </li>
    </ul>
    <div v-else>
      <p>Saadaolevaid aegu ei ole.</p>
    </div>
    <div class="pagination" v-if="totalPages > 1">
      <button @click="prevPage" :disabled="currentPage === 1">Eelmine</button>
      <span>Lehekülg {{ currentPage }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage === totalPages">Järgmine</button>
    </div>
  </div>
</template>

<script>
import { getAvailableAppointments } from '@/services/api';

export default {
  props: {
    selectedWorkshop: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      appointments: [],
      fromDate: '',
      untilDate: '',
      vehicleType: 'Sõiduauto',
      selectedAppointment: null,
      currentPage: 1,
      pageSize: 20
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
    },
    paginatedAppointments() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredAppointments.slice(start, end);
    },
    totalPages() {
      return Math.ceil(this.filteredAppointments.length / this.pageSize);
    }
  },
  watch: {
    selectedWorkshop() {
      this.resetData();
      this.fetchAppointments();
      this.$emit('clear-message');
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
    },
    filteredAppointments() {
      this.currentPage = 1; // Reset to first page if filteredAppointments change
    }
  },
  methods: {
    async fetchAppointments() {
      try {
        this.appointments = await getAvailableAppointments(this.selectedWorkshop, this.fromDate, this.selectedWorkshop === 'london' ? this.untilDate : null, this.vehicleType);
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
    },
    updateSelectedWorkshop(event) {
      this.$emit('update:selectedWorkshop', event.target.value);
      this.$emit('clear-message');
    },
    formatDate(dateString) {
      const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
      return new Date(dateString).toLocaleString('et-EE', options);
    },
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
      }
    },
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
      }
    }
  },
  created() {
    this.fetchAppointments();
  }
};
</script>
