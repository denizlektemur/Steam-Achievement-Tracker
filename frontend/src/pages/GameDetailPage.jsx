import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getAchievements, getProgress, syncAchievements, updateGameStatus } from '../api/api.js'
import './GameDetailPage.css'

const STATUSES = ['BACKLOG', 'IN_PROGRESS', 'COMPLETED', 'IGNORED']

export default function GameDetailPage() {
    const { gameId } = useParams()
    const navigate = useNavigate()
    const [achievements, setAchievements] = useState([])
    const [progress, setProgress] = useState({ total: 0, unlocked: 0 })
    const [syncing, setSyncing] = useState(false)
    const [loading, setLoading] = useState(true)

    const load = async () => {
        const [achRes, progRes] = await Promise.all([
            getAchievements(gameId),
            getProgress(gameId)
        ])
        setAchievements(achRes.data)
        setProgress(progRes.data)
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

    if (loading) return <div className="loading">Loading achievements...</div>

    return (
        <div className="detail-page">
            <button className="back-btn" onClick={() => navigate('/')}>← Back to Library</button>

            <div className="detail-header">
                <div className="progress-section">
                    <h2>Achievements</h2>
                    <p className="progress-text">{progress.unlocked} / {progress.total} unlocked</p>
                    <div className="progress-bar-bg">
                        <div className="progress-bar-fill" style={{ width: `${percentage}%` }} />
                    </div>
                    <p className="progress-pct">{percentage}%</p>
                </div>
                <button className="sync-btn" onClick={handleSync} disabled={syncing}>
                    {syncing ? 'Syncing...' : 'Sync Achievements'}
                </button>
            </div>

            <div className="achievement-list">
                {achievements.length === 0 ? (
                    <p className="empty">No achievements synced yet. Hit Sync Achievements above.</p>
                ) : (
                    achievements.map(ua => (
                        <div key={ua.id} className="achievement-item">
                            <div className="achievement-info">
                <span className="achievement-name">
                  {ua.achievement?.displayName || ua.achievement?.apiName}
                </span>
                                <span className="achievement-desc">
                  {ua.achievement?.description}
                </span>
                            </div>
                            <span className="achievement-unlocked">
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