import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
    getAchievements,
    getProgress,
    syncAchievements,
    getUserGame,
    getAllAchievements
} from '../api/api'
import './GameDetailPage.css'

const STATUS_COLORS = {
    BACKLOG: '#8ba7b8',
    IN_PROGRESS: '#66c0f4',
    COMPLETED: '#4caf7d',
    IGNORED: '#888'
}

export default function GameDetailPage() {
    const { gameId } = useParams()
    const navigate = useNavigate()
    const [userGame, setUserGame] = useState(null)
    const [achievements, setAchievements] = useState([])
    const [progress, setProgress] = useState({ total: 0, unlocked: 0 })
    const [syncing, setSyncing] = useState(false)
    const [loading, setLoading] = useState(true)
    const [search, setSearch] = useState('')
    const [filter, setFilter] = useState('all')

    const load = async () => {
        const [ugRes, allAchRes, unlockedRes, progRes] = await Promise.all([
            getUserGame(gameId),
            getAllAchievements(gameId),      // all achievement definitions
            getAchievements(gameId),         // user's unlocked ones
            getProgress(gameId)
        ])

        setUserGame(ugRes.data)
        setProgress(progRes.data)

        // Merge — mark each achievement as unlocked or not
        const unlockedMap = {}
        unlockedRes.data.forEach(ua => {
            unlockedMap[ua.achievement?.id] = ua.unlockedAt
        })

        const merged = allAchRes.data.map(ach => ({
            id: ach.id,
            achievement: ach,
            unlockedAt: unlockedMap[ach.id] || null
        }))

        // Sort — unlocked first
        merged.sort((a, b) => {
            if (a.unlockedAt && !b.unlockedAt) return -1
            if (!a.unlockedAt && b.unlockedAt) return 1
            return 0
        })

        setAchievements(merged)
        setLoading(false)
    }

    useEffect(() => { load() }, [gameId])

    const handleSync = async () => {
        setSyncing(true)
        await syncAchievements(gameId)
        await load()
        setSyncing(false)
    }

    const percentage = progress.total > 0
        ? Math.round((progress.unlocked / progress.total) * 100)
        : 0

    const unlockedIds = new Set(achievements.map(ua => ua.achievement?.id))

    const filteredAchievements = achievements.filter(ua => {
        const name = ua.achievement?.displayName || ua.achievement?.apiName || ''
        const matchesSearch = name.toLowerCase().includes(search.toLowerCase())
        return matchesSearch
    })

    if (loading) return <div className="loading">Loading...</div>

    const game = userGame?.game
    const headerImage = `https://cdn.cloudflare.steamstatic.com/steam/apps/${game?.appId}/header.jpg`

    return (
        <div className="detail-page">
            {/* Game Hero Banner */}
            <div className="game-hero" style={{ backgroundImage: `url(${headerImage})` }}>
                <div className="game-hero-overlay">
                    <button className="back-btn" onClick={() => navigate('/')}>← Back</button>
                    <div className="game-hero-info">
                        <h1 className="game-hero-title">{game?.title}</h1>
                        <span
                            className="game-hero-status"
                            style={{ color: STATUS_COLORS[userGame?.status] }}
                        >
              {userGame?.status?.replace('_', ' ')}
            </span>
                    </div>
                </div>
            </div>

            {/* Progress Bar */}
            <div className="progress-section">
                <div className="progress-info">
                    <span>{progress.unlocked} / {progress.total} achievements</span>
                    <span className="progress-pct">{percentage}%</span>
                </div>
                <div className="progress-bar-bg">
                    <div className="progress-bar-fill" style={{ width: `${percentage}%` }} />
                </div>
            </div>

            {/* Controls */}
            <div className="detail-controls">
                <input
                    className="search-input"
                    placeholder="Search achievements..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
                <button className="sync-btn" onClick={handleSync} disabled={syncing}>
                    {syncing ? 'Syncing...' : 'Sync Achievements'}
                </button>
            </div>

            {/* Achievement List */}
            <div className="achievement-list">
                {filteredAchievements.length === 0 ? (
                    <p className="empty">No achievements found.</p>
                ) : (
                    filteredAchievements.map(ua => (
                        <div key={ua.id} className={`achievement-item ${ua.unlockedAt ? 'unlocked' : 'locked'}`}>
                            <img
                                className="achievement-icon"
                                src={ua.achievement?.iconUrl || `https://cdn.cloudflare.steamstatic.com/steamcommunity/public/images/apps/${game?.appId}/default.jpg`}
                                alt={ua.achievement?.displayName}
                                onError={e => { e.target.style.opacity = '0.3' }}
                            />
                            <div className="achievement-info">
                <span className="achievement-name">
                  {ua.achievement?.displayName || ua.achievement?.apiName}
                </span>
                                <span className="achievement-desc">
                  {ua.achievement?.description || 'No description'}
                </span>
                            </div>
                            <span className={`achievement-unlocked ${ua.unlockedAt ? '' : 'locked-text'}`}>
                {ua.unlockedAt
                    ? new Date(ua.unlockedAt).toLocaleDateString()
                    : 'Locked'}
              </span>
                        </div>
                    ))
                )}
            </div>
        </div>
    )
}