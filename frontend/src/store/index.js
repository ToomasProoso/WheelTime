import { createStore } from 'vuex';
import { getAvailableAppointments, bookAppointment } from '../services/api';

const store = createStore({
    state: {
        appointments: [],
        selectedWorkshop: 'manchester',
        bookingMessage: ''
    },
    mutations: {
        setAppointments(state, appointments) {
            state.appointments = appointments;
        },
        setBookingMessage(state, message) {
            state.bookingMessage = message;
        }
    },
    actions: {
        async fetchAppointments({ commit, state }) {
            try {
                const response = await getAvailableAppointments(state.selectedWorkshop);
                commit('setAppointments', response.data);
            } catch (error) {
                console.error('Error fetching appointments:', error);
            }
        },
        async bookAppointment({ commit, state }, { id, vehicleType, time }) {
            try {
                await bookAppointment(state.selectedWorkshop, id, { vehicleType, time });
                commit('setBookingMessage', 'Broneering õnnestus!');
            } catch (error) {
                commit('setBookingMessage', 'Broneering ebaõnnestus: ' + error.response.data.message);
            }
        }
    }
});

export default store;
