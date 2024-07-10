import axios from 'axios';
import xml2js from 'xml2js';

const API_URL = 'http://localhost:8080/api';

export async function getAvailableAppointments(workshop, from, until) {
    let url = `${API_URL}/appointments`;
    let params = { workshop, from };
    if (workshop === 'london' && until) {
        params.until = until;
    }
    const response = await axios.get(url, { params });

    if (workshop === 'london') {
        try {
            const parser = new xml2js.Parser({ explicitArray: false, mergeAttrs: true });
            const result = await parser.parseStringPromise(response.data);
            return result.tireChangeTimesResponse.availableTime.map(item => ({
                id: item.uuid,
                time: item.time
            }));
        } catch (error) {
            console.error('XML parsing error:', error);
            throw error;
        }
    }

    return response.data;
}

export function bookAppointment(workshop, id, data) {
    return axios.post(`${API_URL}/appointments/${id}/booking`, data, {
        params: { workshop }
    });
}
