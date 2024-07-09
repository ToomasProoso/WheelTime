// src/store/index.js
import Vue from 'vue';
import Vuex from 'vuex';
import { getAvailableSlots, bookSlot } from '../services/api';

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
        slots: [],
        error: null,
    },
    mutations: {
        setSlots(state, slots) {
            state.slots = slots;
        },
        setError(state, error) {
            state.error = error;
        },
    },
    actions: {
        fetchSlots({ commit }, { workshopId, vehicleType, startDate, endDate }) {
            return getAvailableSlots(workshopId, vehicleType, startDate, endDate).then(response => {
                commit('setSlots', response.data);
            }).catch(error => {
                commit('setError', error.response.data);
            });
        },
        bookSlot({ dispatch, commit }, { workshopId, slotId, filters }) {
            return bookSlot(workshopId, slotId).then(() => {
                dispatch('fetchSlots', filters);
            }).catch(error => {
                commit('setError', error.response.data);
            });
        },
    },
    getters: {
        slots: state => state.slots,
        error: state => state.error,
    },
});
