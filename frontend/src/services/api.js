import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export function getAvailableAppointments(workshop) {
    return axios.get(`${API_URL}/appointments`, {
        params: { workshop }
    });
}

export function bookAppointment(workshop, id, data) {
    return axios.post(`${API_URL}/appointments/${id}/booking`, data, {
        params: { workshop }
    });
}