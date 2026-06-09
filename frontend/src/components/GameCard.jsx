import { useNavigate } from 'react-router-dom'
import './GameCard.css'

export default function GameCard({ userGame }) {
    const navigate = useNavigate()
    const { game } = userGame
    const imageUrl = `https://cdn.cloudflare.steamstatic.com/steam/apps/${game.appId}/library_600x900.jpg`
    const fallbackUrl = `https://cdn.cloudflare.steamstatic.com/steam/apps/${game.appId}/header.jpg`
    const placeholder = 'https://placehold.co/300x450/2a475e/66c0f4?text=No+Image'

    return (
        <div className="game-card" onClick={() => navigate(`/game/${game.id}`)}>
            <img
                src={imageUrl}
                alt={game.title}
                onError={e => {
                    if (e.target.src !== fallbackUrl) {
                        e.target.src = fallbackUrl
                    } else {
                        e.target.src = placeholder
                    }
                }}
            />
            <div className="game-card-overlay">
                <span className="game-title">{game.title}</span>
                <span className={`game-status status-${userGame.status.toLowerCase()}`}>
          {userGame.status.replace('_', ' ')}
        </span>
            </div>
        </div>
    )
}