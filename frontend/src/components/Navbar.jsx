import { Link } from 'react-router-dom'
import { syncGames } from '../api/api'
import { useState } from 'react'
import './Navbar.css'

export default function Navbar() {
    const [syncing, setSyncing] = useState(false)

    const handleSync = async () => {
        setSyncing(true)
        try {
            const res = await syncGames()
            alert(`Sync complete — ${res.data.newGamesAdded} new games added`)
        } catch (e) {
            alert('Sync failed')
        } finally {
            setSyncing(false)
        }
    }

    return (
        <nav className="navbar">
            <Link to="/" className="navbar-brand">🎮 Steam Tracker</Link>
            <button className="sync-btn" onClick={handleSync} disabled={syncing}>
                {syncing ? 'Syncing...' : 'Sync Library'}
            </button>
        </nav>
    )
}