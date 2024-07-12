import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export async function getAvailableAppointments(workshop, fromDate, untilDate, vehicleType) {
    const params = {
        workshop: workshop,
        from: fromDate,
    };
    if (workshop === 'london') {
        params.until = untilDate;
    }
    if (vehicleType) {
        params.vehicleType = vehicleType;
    }
    const response = await axios.get(`${API_BASE_URL}/appointments`, { params });
    return response.data;
}

export async function bookAppointment(workshop, id, request) {
    const url = `${API_BASE_URL}/appointments/${id}/booking`;
    const response = await axios.post(url, request);
    return response.data;
}
