import axios from 'axios'

const BASE_URL = '/api'
const USER_ID = 9  // change this to your actual user ID

export const getUser = () =>
    axios.get(`${BASE_URL}/users/${USER_ID}`)

export const getUserGames = () =>
    axios.get(`${BASE_URL}/users/${USER_ID}/games`)

export const getUserGamesByStatus = (status) =>
    axios.get(`${BASE_URL}/users/${USER_ID}/games/status/${status}`)

export const updateGameStatus = (gameId, status) =>
    axios.patch(`${BASE_URL}/users/${USER_ID}/games/${gameId}/status?status=${status}`)

export const syncGames = () =>
    axios.post(`${BASE_URL}/sync/users/${USER_ID}/games`)

export const syncAchievements = (gameId) =>
    axios.post(`${BASE_URL}/sync/users/${USER_ID}/games/${gameId}/achievements`)

export const getAchievements = (gameId) =>
    axios.get(`${BASE_URL}/users/${USER_ID}/games/${gameId}/achievements`)

export const getProgress = (gameId) =>
    axios.get(`${BASE_URL}/users/${USER_ID}/games/${gameId}/achievements/progress`)

export const syncAll = (userId) =>
    axios.post(`${BASE_URL}/sync/users/${userId}/all`)