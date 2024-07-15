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
    const url = `${API_BASE_URL}/appointments/${id}/booking?workshop=${workshop.toLowerCase()}`;
    try {
        let response;
        const localTime = new Date(request.time).toLocaleString('en-US', { timeZone: 'Europe/Moscow', hour12: false });
        if (workshop.toLowerCase() === 'manchester') {
            response = await axios.post(url, { ...request, time: localTime }, {
                headers: {
                    'Content-Type': 'application/json',
                    'time': localTime
                }
            });
        } else if (workshop.toLowerCase() === 'london') {
            const xmlBody = `<tireChangeBookingRequest><contactInformation>${request.contactInformation}</contactInformation><time>${localTime}</time></tireChangeBookingRequest>`;
            response = await axios.put(url, xmlBody, {
                headers: {
                    'Content-Type': 'text/xml',
                    'time': localTime
                }
            });
        }
        return response;
    } catch (error) {
        console.error('API call error:', error);
        throw error;
    }
}
