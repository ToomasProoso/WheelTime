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
    console.log('Booking URL:', url); // Debug log
    console.log('Request body:', request); // Debug log
    let response;
    try {
        if (workshop.toLowerCase() === 'manchester') {
            response = await axios.post(url, request, {
                headers: {
                    'Content-Type': 'application/json',
                    'time': request.time
                }
            });
        } else if (workshop.toLowerCase() === 'london') {
            const xmlBody = `<tireChangeBookingRequest><contactInformation>${request.contactInformation}</contactInformation><time>${request.time}</time></tireChangeBookingRequest>`;
            response = await axios.put(url, xmlBody, {
                headers: {
                    'Content-Type': 'text/xml',
                    'time': request.time
                }
            });
        }
        console.log('Response:', response); // Debug log
        return response.data;
    } catch (error) {
        console.error('API call error:', error); // Debug log
        throw error;
    }
}
