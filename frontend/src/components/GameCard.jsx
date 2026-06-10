import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { updateGameStatus } from '../api/api'
import './GameCard.css'

const STATUSES = [
    { value: 'BACKLOG', label: 'Backlog' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'IGNORED', label: 'Ignored' },
]

export default function GameCard({ userGame, showProgress, onStatusChange }) {
    const navigate = useNavigate()
    const [menuOpen, setMenuOpen] = useState(false)
    const [menuPos, setMenuPos] = useState({ x: 0, y: 0 })
    const [updating, setUpdating] = useState(false)

    const imageUrl = `https://cdn.cloudflare.steamstatic.com/steam/apps/${userGame.appId}/library_600x900.jpg`
    const fallbackUrl = `https://cdn.cloudflare.steamstatic.com/steam/apps/${userGame.appId}/header.jpg`
    const placeholder = 'https://placehold.co/300x450/2a475e/66c0f4?text=No+Image'

    const percentage = userGame.totalAchievements > 0
        ? Math.round((userGame.unlockedAchievements / userGame.totalAchievements) * 100)
        : null

    const greyHeight = percentage !== null ? `${100 - percentage}%` : '0%'

    const handleRightClick = (e) => {
        e.preventDefault()
        setMenuPos({ x: e.clientX, y: e.clientY })
        setMenuOpen(true)
    }

    const handleStatusChange = async (status) => {
        setUpdating(true)
        setMenuOpen(false)
        try {
            await updateGameStatus(userGame.gameId, status)
            onStatusChange(userGame.id, status)
        } catch (e) {
            console.error('Failed to update status', e)
        } finally {
            setUpdating(false)
        }
    }

    const handleClick = () => {
        if (!menuOpen) navigate(`/game/${userGame.gameId}`)
    }

    return (
        <>
            <div
                className={`game-card ${updating ? 'updating' : ''}`}
                onClick={handleClick}
                onContextMenu={handleRightClick}
            >
                <img
                    src={imageUrl}
                    alt={userGame.title}
                    onError={e => {
                        if (e.target.src !== fallbackUrl) {
                            e.target.src = fallbackUrl
                        } else {
                            e.target.src = placeholder
                        }
                    }}
                />

                {showProgress && percentage !== null && percentage < 100 && (
                    <div className="completion-overlay" style={{ height: greyHeight }} />
                )}

                {showProgress && percentage !== null && (
                    <div className={`completion-badge ${percentage === 100 ? 'complete' : ''}`}>
                        {percentage === 100 ? '✓ 100%' : `${percentage}%`}
                    </div>
                )}

                <div className="game-card-overlay">
                    <span className="game-title">{userGame.title}</span>
                    <span className={`game-status status-${userGame.status.toLowerCase()}`}>
            {userGame.status.replace('_', ' ')}
          </span>
                </div>

                {updating && <div className="card-updating">Saving...</div>}
            </div>

            {menuOpen && (
                <ContextMenu
                    x={menuPos.x}
                    y={menuPos.y}
                    currentStatus={userGame.status}
                    onSelect={handleStatusChange}
                    onClose={() => setMenuOpen(false)}
                />
            )}
        </>
    )
}

function ContextMenu({ x, y, currentStatus, onSelect, onClose }) {
    // Adjust position so menu doesn't go off screen
    const menuWidth = 180
    const menuHeight = 180
    const adjustedX = x + menuWidth > window.innerWidth ? x - menuWidth : x
    const adjustedY = y + menuHeight > window.innerHeight ? y - menuHeight : y

    return (
        <>
            {/* invisible backdrop to catch outside clicks */}
            <div className="context-backdrop" onClick={onClose} onContextMenu={e => { e.preventDefault(); onClose() }} />
            <div
                className="context-menu"
                style={{ top: adjustedY, left: adjustedX }}
            >
                <div className="context-menu-title">Move to</div>
                {STATUSES.map(s => (
                    <button
                        key={s.value}
                        className={`context-option ${currentStatus === s.value ? 'active' : ''}`}
                        onClick={() => onSelect(s.value)}
                    >
                        <span className={`context-dot status-dot-${s.value.toLowerCase()}`} />
                        {s.label}
                        {currentStatus === s.value && <span className="context-check">✓</span>}
                    </button>
                ))}
            </div>
        </>
    )
}