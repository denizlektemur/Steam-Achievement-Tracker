import { useNavigate } from 'react-router-dom'
import './GameCard.css'

export default function GameCard({ userGame, showProgress }) {
    const navigate = useNavigate()
    const imageUrl = `https://cdn.cloudflare.steamstatic.com/steam/apps/${userGame.appId}/library_600x900.jpg`
    const fallbackUrl = `https://cdn.cloudflare.steamstatic.com/steam/apps/${userGame.appId}/header.jpg`
    const placeholder = 'https://placehold.co/300x450/2a475e/66c0f4?text=No+Image'

    const percentage = userGame.totalAchievements > 0
        ? Math.round((userGame.unlockedAchievements / userGame.totalAchievements) * 100)
        : null

    const greyHeight = percentage !== null ? `${100 - percentage}%` : '0%'

    return (
        <div className="game-card" onClick={() => navigate(`/game/${userGame.gameId}`)}>
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
        </div>
    )
}