// src/services/api.js
import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export const getAvailableSlots = (workshopId, vehicleType, startDate, endDate) => {
    return axios.get(`${API_URL}/workshops/${workshopId}/slots`, {
        params: { vehicleType, startDate, endDate }
    });
};

export const bookSlot = (workshopId, slotId) => {
    return axios.post(`${API_URL}/workshops/${workshopId}/book`, { slotId });
};
